package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.*;
import com.indiduck.panda.config.ApiKey;
import com.indiduck.panda.controller.OrderDetailController;
import com.indiduck.panda.controller.UserOrderController;
import com.indiduck.panda.domain.*;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderDetailService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final ProductOptionRepository productOptionRepository;
    @Autowired
    private final PandaRespository pandaRespository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final ShopRepository shopRepository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private ApiKey apiKey;


    public OrderDetail newOrderDetail(String user,Long productid,Long optionId,int optionCount
                                           ,Long selectpanda)
    {


        Optional<User> getUser = userRepository.findByEmail(user);
        Optional<Product> getProduct = productRepository.findById(productid);
        Optional<ProductOption> getPO = productOptionRepository.findById(optionId);
        Optional<Panda> getPanda = pandaRespository.findById(selectpanda);


        //기존에 결제대기로 찾지않으면
        // 옵션을 리턴할때 유저랑 옵션으로 찾음
        // 결제대기랑 결제완료가 같이 찾아짐
        Optional<OrderDetail> byUserAAndOptions = orderDetailRepository.findByUserAndOptionsAndOrderStatus(getUser.get(), getPO.get(),OrderStatus.결제대기);


        if(!byUserAAndOptions.isEmpty()&& (byUserAAndOptions.get().getOrderStatus()==OrderStatus.결제대기))
        {


            byUserAAndOptions.get().plusCount(optionCount);
            byUserAAndOptions.get().setPanda(getPanda.get());
            return (byUserAAndOptions.get());
        }else {


            OrderDetail od = OrderDetail.newOrderDetail(getUser.get(), getProduct.get(), getPO.get(), optionCount, getPanda.get());
            orderDetailRepository.save(od);

        return od;
        }

    }
//    public  OrderDetail cancelOrderchangeOrderStatus(OrderStatus od,long odid)
//    {
//        Optional<UserOrder> byId = userOrderRepository.findById(odid);
////        Optional<OrderDetail> byId = orderDetailRepository.findById(odid);
//        if(byId.get().getOrderStatus()==OrderStatus.결제완료)
//        {
//
//            byId.get().
//            return byId.get();
//        }
//        return null;
//    }

    public OrderDetail newOrderDetail(String user, Long productid, Long optionId, int optionCount) {
        Optional<User> getUser = userRepository.findByEmail(user);
        Optional<Product> getProduct = productRepository.findById(productid);
        Optional<ProductOption> getPO = productOptionRepository.findById(optionId);


        Optional<OrderDetail> byUserAAndOptions = orderDetailRepository.findByUserAndOptionsAndOrderStatus(getUser.get(), getPO.get(),OrderStatus.결제대기);
//        int i = getPO.get().getOptionStock() - optionCount;
//        if(i <=0)
//        {
//            return  null;
//        }
        //이미 상품이 있을때
        if(!byUserAAndOptions.isEmpty() && (byUserAAndOptions.get().getOrderStatus()==OrderStatus.결제대기)) {


            byUserAAndOptions.get().plusCount(optionCount);
            return (byUserAAndOptions.get());
        }else {

            OrderDetail od = OrderDetail.newOrderDetail(getUser.get(), getProduct.get(), getPO.get(), optionCount);
            orderDetailRepository.save(od);
            return od;
        }
    }


    public OrderDetail updateOrderDetail(OrderDetail order, int optionCount) {
            order.update(optionCount);
            return order;
    }

    public OrderDetail updateOrderDetail(OrderDetail order, int optionCount,Long panda) {
        Optional<Panda> getPanda = pandaRespository.findById(panda);

        order.setPanda(getPanda.get());
        order.update(optionCount);

        return order;
    }

    public void paymentOrderDetail(Payment info)
    {

//        System.out.println(info.getAmount());
//        System.out.println(info.getCustomData());
//        System.out.println(info.getBuyerAddr());
//        System.out.println(info.getBuyerPostcode());
//        System.out.println(info.getPayMethod());
//        System.out.println(info.getPaidAt());


        String customData = info.getCustomData();
        JSONObject jsonObject=new JSONObject(customData);
        //디테일배열
        JSONArray detail =jsonObject.getJSONArray("detaildId");

        //샵

        for (Object o : detail) {
            Optional<OrderDetail> byId = orderDetailRepository.findById(Long.parseLong(o.toString()));

//            byId.get().setOrderStatus(OrderStatus.결제완료);
        }

    }

    public boolean delete(Long odId)
    {
        try {
            Optional<OrderDetail> byId = orderDetailRepository.findById(odId);
            Panda panda = byId.get().getPanda();
            if(panda != null)
            {
                byId.get().deletePanda(panda);
            }
            orderDetailRepository.deleteById(odId);
            return true;
        }catch (Exception e)
        {
            return false;

        }
    }


    public boolean newUserOrder(User user,OrderDetailController.DetailedCart myCart,String mid,
                                String name,String phoneNumber,String zipCode,String Address,String rec,String memo) {

//        System.out.println("오더서비스 겟네임"+name);
        for (OrderDetailController.DetailedShop d : myCart.getDs()) {
            Optional<Shop> byId = shopRepository.findById(d.getShopId());
            UserOrder uo= UserOrder.newUserOrder(user,byId.get(),mid,name,phoneNumber,zipCode,Address,rec,memo);
            for (OrderDetailController.DetailedProduct detailedProduct : d.getDp()) {
                for (OrderDetailController.DetailedOption detailedOption : detailedProduct.getDO()) {
                    Optional<OrderDetail> byId1 = orderDetailRepository.findById(detailedOption.getDetailedId());
                    uo.setDetail(byId1.get());
                }
            }
            uo.Calculate();
            userOrderRepository.save(uo);
        }

        return true;

    }

    public int minusOption(OrderDetail od)
    {
        int productCount = od.getProductCount();
        ProductOption options = od.getOptions();
        int i = options.minusOption(productCount);
        return i;

    }

    //판매자 측에서 취소했을때
    public void partialCancelation(OrderDetail od,int cancel,String message) {
        od.partialCancel(cancel,message);

    }

    //부분취소 환불
    public boolean refundOrder(long uo, int money, List<UserOrderController.RefundList> refundList,String message)
    {
        Optional<UserOrder> byId = userOrderRepository.findById(uo);
        UserOrder userOrder = byId.get();
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        IamportClient iamportClient = new IamportClient(test_api_key, test_api_secret);
        CancelData cancel_data = new CancelData(byId.get().getMid(), true, BigDecimal.valueOf(money)); //imp_uid를 통한 전액취소

        try {
            IamportResponse<Payment> payment_response = iamportClient.cancelPaymentByImpUid(cancel_data);
            log.info(uo+"취소데이터"+payment_response);

            String receiptUrl = payment_response.getResponse().getReceiptUrl();

            userOrder.setReceiptUrl(receiptUrl);

        } catch (IamportResponseException e) {
            log.error(e.getMessage()+"리펀드오더");
      
            switch(e.getHttpStatusCode()) {
                case 401 :
                    //TODO
                    log.error("리펀드오더"+e);
                    return false;
                case 500 :
                    //TODO
                    log.error("리펀드오더 = " + e);

                    return false;

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;

        } catch (Exception e)
        {
            log.error(" 부분환불이 불가능합니다 리펀드오더"+e);
            return false;
        }
        for (UserOrderController.RefundList list : refundList) {
            long optionId = list.getOptionId();
            Optional<OrderDetail> byId1 = orderDetailRepository.findById(optionId);

            partialCancelation(byId1.get(), list.getOptionCount(),message);


        }
        userOrder.confirmCancelMoney(money);


        return true;
    }

}
