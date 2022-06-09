package com.indiduck.panda.controller;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.indiduck.panda.Repository.*;
import com.indiduck.panda.Service.OrderDetailService;
import com.indiduck.panda.config.ApiKey;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dto.ResultDto;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.Data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
@CrossOrigin
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderDetailController {
    @Autowired
    OrderDetailService orderDetailService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderDetailRepository orderDetailRepository;
    @Autowired
    ShopRepository shopRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    private ApiKey apiKey;

    @Autowired
    UserOrderRepository userOrderRepository;

    //상품 주문
    @RequestMapping(value = "/api/addcart", method = RequestMethod.POST)
    public ResponseEntity<?> createShop(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody CreateOrderDetailDAO createProductDAO) throws Exception {

        //장바구니에 담을때 재고 수량 체크

        List<detailOptionDAO> cart = createProductDAO.cart;
        for (detailOptionDAO detailOptionDAO : cart) {
            try {
                Optional<ProductOption> byId = productOptionRepository.findById(detailOptionDAO.optionId);
                int i = byId.get().getOptionStock();
                int k = detailOptionDAO.optionCount;
                int j= i-k;
                String message= byId.get().getOptionName()+"의 수량을 "+ byId.get().getOptionStock()+"개 보다 적게 선택해주세요";
                if(j<0)
                {
                    System.out.println(" --재고부족진입"+message);
                    return ResponseEntity.ok(new ResultDto(false,message));
                }

                if(createProductDAO.selectpanda==null)
                {
                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
                            , detailOptionDAO.optionId, detailOptionDAO.optionCount);
                }else
                {


                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
                            , detailOptionDAO.optionId, detailOptionDAO.optionCount, createProductDAO.selectpanda);
                }

            }catch (Exception e)

            {
                System.out.println("오더디테일 컨트롤러 에러발생"+e);

                return ResponseEntity.ok(new ResultDto(false,"상품을 담는중 오류가 발생했습니다" +
                        "계속 발생할시 1:1 문의바랍니다"));
            }
        }


        return ResponseEntity.ok(new ResultDto(true,"상품담기 성공"));
    }


    //카트 업데이트
    @RequestMapping(value = "/api/updatecart", method = RequestMethod.POST)
    public ResponseEntity<?> updatecart(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody CreateOrderDetailDAO createProductDAO) throws Exception {

        log.info((authentication.getName()+"의 카트업데이트"));
        System.out.println("createProductDAO = " + createProductDAO.selectpanda);
        for (detailOptionDAO detailOptionDAO : createProductDAO.cart) {
            System.out.println("detailOptionDAO = " + detailOptionDAO);

            //새상품
            if(detailOptionDAO.detailedId==null)
            {
                try {
                if(createProductDAO.selectpanda==null)
                {
                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
                            , detailOptionDAO.optionId, detailOptionDAO.optionCount);
                }else
                {


                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
                            , detailOptionDAO.optionId, detailOptionDAO.optionCount, createProductDAO.selectpanda);
                }

            }catch (Exception e)

            {
                System.out.println("오더디테일 컨트롤러 에러발생"+e);

                return ResponseEntity.ok(new ResultDto(false,"상품을 담는중 오류가 발생했습니다" +
                        "계속 발생할시 1:1 문의바랍니다"));
            }



                //기존상품
            }else
            {
                Optional<OrderDetail> byId = orderDetailRepository.findById(detailOptionDAO.detailedId);
                try {
                    if(createProductDAO.selectpanda==null)
                    {
                        orderDetailService.updateOrderDetail(byId.get(),detailOptionDAO.optionCount);
                    }else
                    {

                        orderDetailService.updateOrderDetail(byId.get(),detailOptionDAO.optionCount, createProductDAO.selectpanda);

                    }

                }catch (Exception e)

                {
                    System.out.println("오더디테일 컨트롤러 에러발생"+e);

                    return ResponseEntity.ok(new ResultDto(false,"상품을 담는중 오류가 발생했습니다" +
                            "계속 발생할시 1:1 문의바랍니다"));
                }

            }
        }
//        List<detailOptionDAO> cart = createProductDAO.cart;
//        for (detailOptionDAO detailOptionDAO : cart) {
//            try {
//                if(createProductDAO.selectpanda==null)
//                {
//                    System.out.println("컨트롤러");
//                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
//                            , detailOptionDAO.optionId, detailOptionDAO.optionCount);
//                }else
//                {
//
//                    System.out.println("컨트롤러");
//
//                    orderDetailService.newOrderDetail(authentication.getName(), createProductDAO.productid
//                            , detailOptionDAO.optionId, detailOptionDAO.optionCount, createProductDAO.selectpanda);
//                }
//
//            }catch (Exception e)
//
//            {
//                System.out.println("오더디테일 컨트롤러 에러발생"+e);
//
//                return ResponseEntity.ok(new ResultDto(false,"상품을 담는중 오류가 발생했습니다" +
//                        "계속 발생할시 1:1 문의바랍니다"));
//            }
//        }


        return ResponseEntity.ok(new ResultDto(true,"상품담기 성공"));
    }

    //상품카트
    @RequestMapping(value = "/api/mycart", method = RequestMethod.GET)
    public ResponseEntity<?> viewMyCart(@CurrentSecurityContext(expression = "authentication")
                                             Authentication authentication) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        //기존것
//        Optional<List<OrderDetail>> byUserAndOrderStatus = orderDetailRepository.findByUserAndOrderStatus(byEmail.get(), OrderStatus.결제대기);
                Optional<List<OrderDetail>> byUserAndOrderStatus = orderDetailRepository.findByUserAndOrderStatusAndOptions_Sales(byEmail.get(), OrderStatus.결제대기,true);

        List<OrderDetail> orderDetails = byUserAndOrderStatus.get();

        DetailedCart myCart = new DetailedCart(orderDetails);




        return ResponseEntity.ok(new thisResultDto(true,myCart));
    }

    //결제페이지
    @RequestMapping(value = "/api/payment", method = RequestMethod.POST)
    public ResponseEntity<?> payMyCart(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,@RequestBody paymentDAO paymentDAO) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        List<OrderDetail> byUserAndOrderStatus = new ArrayList<>();
        for (Long id : paymentDAO.shopId) {
            Optional<Shop> byId = shopRepository.findById(id);
            if(byId!=null)
            {
//                  기존에 판매중옵션보기
//                Optional<List<OrderDetail>> byUserAndOrderStatusAndShop = orderDetailRepository.findByUserAndOrderStatusAndShop(byEmail.get(), OrderStatus.결제대기, byId.get());
                  Optional<List<OrderDetail>> byUserAndOrderStatusAndShop = orderDetailRepository.findByUserAndOrderStatusAndShopAndOptions_Sales(byEmail.get(), OrderStatus.결제대기, byId.get(),true);

                byUserAndOrderStatus.addAll(byUserAndOrderStatusAndShop.get());

            }

        }

        DetailedCart myCart = new DetailedCart(byUserAndOrderStatus);





        return ResponseEntity.ok(new thisResultDto(true,myCart));
    }

    //핸드폰 본인인증 왜 여기있지?????
    //결제완료
    @RequestMapping(value = "/api/authentication", method = RequestMethod.POST)
    public ResponseEntity<?> selfAuthentication(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication,@RequestBody String muid) throws Exception {
        log.info(authentication.getName() + "의 인증요청");
        IamportClient client;
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = "imp_974774403374";
        client = new IamportClient(test_api_key, test_api_secret);

//        System.out.println("muid는 = " + muid);


        try {
            IamportResponse<Certification> certification_response = client.certificationByImpUid(test_imp_uid);
            System.out.println("certification_response = " + certification_response.getResponse().getName());
            System.out.println("certification_response = " + certification_response.getResponse().getPhone());
            System.out.println("certification_response = " + certification_response.getResponse().getBirth());
            System.out.println("certification_response = " + certification_response.getResponse().getBirth().getMonth());
            System.out.println("certification_response = " + certification_response.getResponse().getBirth().getYear());
            System.out.println("certification_response = " + certification_response.getResponse().isCertified());
            System.out.println("certification_response = " + certification_response.getResponse().getUniqueKey());
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            LocalDate now = LocalDate.now();
            Date birth = certification_response.getResponse().getBirth();

            String format1 = format.format(birth);
            LocalDate parsedBirthDate = LocalDate.parse(format1, DateTimeFormatter.ofPattern("yyyyMMdd"));

            int americanAge = now.minusYears(parsedBirthDate.getYear()).getYear(); // (1)
            if (parsedBirthDate.plusYears(americanAge).isAfter(now)) {
                americanAge = americanAge -1;
            }
            System.out.println("americanAge = " + americanAge);

        } catch (IamportResponseException e) {
            System.out.println(e.getMessage());

            switch(e.getHttpStatusCode()) {
                case 401 :
                    //TODO
                    break;
                case 500 :
                    //TODO
                    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }




        return ResponseEntity.ok(new tfResultDto(false));



    }
    //결제 이전 ㅅ ㅏ전검증
    @RequestMapping(value = "/api/payment/after", method = RequestMethod.POST)
    public ResponseEntity<?> afterConfirm(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication,@RequestBody AfterConfirm afterConfirm) throws Exception {

        for (long l : afterConfirm.dataList) {
            Optional<OrderDetail> byId = orderDetailRepository.findById(l);
            int optionStock = byId.get().getOptions().getOptionStock();
            int productCount = byId.get().getProductCount();
            int res= optionStock-productCount;
            boolean salesOp = byId.get().getOptions().isSales();
            //판매중지/삭제상품인경우
            if(!byId.get().getProducts().isSales()||byId.get().getProducts().isDeleted())
            {
                String message= byId.get().getProducts().getProductName()+"은/는 구매 불가능한 상품입니다.";
                return ResponseEntity.ok(new ResultDto(false,message));

            }
            //삭제된 옵션인경우
            if(!salesOp)
            {
                String message= byId.get().getOptions().getOptionName()+"은/는 구매 불가능한 옵션입니다.";
                return ResponseEntity.ok(new ResultDto(false,message));
            }
            //수량이 부족한경우

            if((res<0))
            {
                String message= byId.get().getOptions().getOptionName()+"은/는"+byId.get().getOptions().getOptionStock()+"개 이하로 선택해주세요.";
                return ResponseEntity.ok(new ResultDto(false,message));
            }
        }



        return ResponseEntity.ok(new ResultDto(true,"검증성공"));

    }
        //결제완료
    @RequestMapping(value = "/api/payment/complete", method = RequestMethod.POST)
    public ResponseEntity<?> finishpayment(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication,@RequestBody finishPaymentDAO finishpaymentDAO) throws Exception {


        Payment tokentoInfo = getTokentoInfo(finishpaymentDAO.impuid);
        String customData = tokentoInfo.getCustomData();
        JSONObject jsonObject=new JSONObject(customData);
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        JSONArray detail =jsonObject.getJSONArray("detaildId");
//        TODO:오류일으키기 메모없어도 오류가 뜬다
//        JSONObject memo = jsonObject.getJSONObject("memo");

        Object memo = jsonObject.get("memo");
        HashSet<Shop> shopId =new HashSet<>();
        HashSet<OrderDetail> orders= new HashSet<>();
        HashSet<Verification> Veri=new HashSet<>();

        List<OrderDetail> orderDetails =new ArrayList<>();
        for (Object o : detail) {

            Optional<OrderDetail> byId = orderDetailRepository.findById(Long.parseLong(o.toString()));
            Long id = byId.get().getOptions().getId();
            Optional<ProductOption> opId = productOptionRepository.findById(id);
            int i = opId.get().getOptionStock();
            int k = byId.get().getProductCount();
            int j =i-k;

            if(j<0)
            {
                String message= opId.get().getOptionName()+"의 수량을 "+ opId.get().getOptionStock()+"개 보다 적게 선택해주세요";
                System.out.println(" --재고부족진입"+message);
                return ResponseEntity.ok(new ResultDto(false,message));
            }
            orderDetails.add(byId.get());
            shopId.add(byId.get().getShop());
            orders.add(byId.get());
        }

        for (Shop shop : shopId) {
            Veri.add(new Verification(shop));

        }


        for (Verification verification : Veri) {
            for (OrderDetail order : orders) {
                if(order.getShop().getId()== verification.shopId)
                {

                        verification.amount+=Math.floor(order.getIndividualPrice()*order.getProductCount());
                        verification.pure+=Math.floor(order.getTotalPrice());



                }
            }
            if(verification.free > verification.pure)
            {

                verification.amount= verification.amount+ verification.shipprice;
            }
        }

        DetailedCart myCart = new DetailedCart(orderDetails);

        int allamount = 0;
        for (Verification verification : Veri) {
            allamount+=verification.amount;
            System.out.println("verification = " + verification);
        }

        BigDecimal amount = tokentoInfo.getAmount();


        if(allamount==amount.intValue())
        {
            System.out.println("검증성공");
            System.out.println("검증네임"+tokentoInfo.getName()+tokentoInfo.getBuyerName());

            System.out.println(myCart);
            boolean b = orderDetailService.newUserOrder(byEmail.get(), myCart, tokentoInfo.getImpUid(),
                    tokentoInfo.getBuyerName(),tokentoInfo.getBuyerTel(),tokentoInfo.getBuyerPostcode(),tokentoInfo.getBuyerAddr(),tokentoInfo.getReceiptUrl(),memo.toString());
            //결제성공시
            if(b){
                for (OrderDetail orderDetail : orderDetails) {

                    int i = orderDetailService.minusOption(orderDetail);

                    if( i<0)
                    {
                        int productCount = orderDetail.getProductCount();
                        ProductOption options = orderDetail.getOptions();
                        int j = options.minusOption(productCount);
                        System.out.println("남은재고 = " + j);
                        String message= options.getOptionName()+"의 수량을 "+ options+"개 보다 적게 선택해주세요";

                        return ResponseEntity.ok(new ResultDto(false,message));

                    }
                }

                return ResponseEntity.ok(new tfResultDto(b));

            }else
            {
                log.error(authentication.getName()+"검증실패 이슈 발생");
                log.error("결제해야할 금액 = " + allamount);
                log.error("실제 결제된 금액 " + amount.intValue());
                paymentAlreadyCancelled(finishpaymentDAO.impuid);
                paymentAlreadyCancelled(finishpaymentDAO.impuid);
                return ResponseEntity.ok(new tfResultDto(false));
            }

        }else
        {

            log.error("검증실패 이슈 발생");
            log.error("결제해야할 금액 = " + allamount);
            log.error("실제 결제된 금액 " + amount.intValue());
            paymentAlreadyCancelled(finishpaymentDAO.impuid);
            return ResponseEntity.ok(new tfResultDto(false));

        }

    }

    @Data
    public class Verification {
        private Long shopId;
        private int amount;
        private int pure;
        private int free;
        private int shipprice;
        public Verification(Shop shopId)
        {
            this.shopId=shopId.getId();
            this.free=shopId.getFreePrice();
            this.shipprice=shopId.getNofree();
        }


    }

    public void paymentAlreadyCancelled(String merchuid)
    {
        CancelData cancel_data = new CancelData(merchuid, true); //imp_uid를 통한 전액취소
        IamportClient client;
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = merchuid;


        client = new IamportClient(test_api_key, test_api_secret);
        try {
            IamportResponse<Payment> payment_response = client.cancelPaymentByImpUid(cancel_data);

        } catch (IamportResponseException e) {
            System.out.println(e.getMessage());

            switch(e.getHttpStatusCode()) {
                case 401 :
                    //TODO
                    break;
                case 500 :
                    //TODO
                    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public Payment getTokentoInfo(String merchuid) {
        IamportClient client;
//        String test_api_key = "imp_apikey";
//        String test_api_secret = "ekKoeW8RyKuT0zgaZsUtXXTLQ4AhPFW3ZGseDA6bkA5lamv9OqDMnxyeB9wqOsuO9W3Mx9YSJ4dTqJ3f";
//        String test_imp_uid = "imp_448280090638";
        //restapi키

        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        //결제내역에서
        String test_imp_uid = merchuid;


        client = new IamportClient(test_api_key, test_api_secret);
        try {
            IamportResponse<com.siot.IamportRestClient.response.Payment> payment_response = client.paymentByImpUid(test_imp_uid);
            System.out.println("결제정보");

            return payment_response.getResponse();
        } catch (IamportResponseException e) {
            System.out.println(e.getMessage());

            switch(e.getHttpStatusCode()) {
                case 401 :
                    //TODO
                    break;
                case 500 :
                    //TODO
                    break;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


//        return NewrestTemplate.postForObject("https://api.iamport.kr/payments/"+imp, newentity, String.class);

        return null;
    }
    @Data
    public class GetTokenVO {
        private String access_token;
        private long now;
        private long expired_at;



    }

    //옵션삭제
    @RequestMapping(value = "/api/cart/removeoption", method = RequestMethod.POST)
    public ResponseEntity<?> removeoption(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody removeDetailDAO removeDetailDAO) throws Exception {

        try {
            log.info(authentication.getName() + "의 옵션삭제 시도");
            boolean delete = orderDetailService.delete(removeDetailDAO.orderDetailId);
            if(delete) {
                return ResponseEntity.ok(new ResultDto(true, "성공적으로 삭제했습니다"));
            }else
            {
                log.error(authentication.getName() + "의 옵션삭제 시도 실패");

                return ResponseEntity.ok(new ResultDto(false,"삭제실패"));

            }
        }catch (Exception e)
        {
            log.error(authentication.getName() + "의 옵션삭제 시도 실패");

            return ResponseEntity.ok(new ResultDto(false,"삭제실패"));
        }
        
    }



    ///////////
    @Data
    public static class DetailedCart{

        HashSet<DetailedShop> ds = new HashSet<>();
        Long orderId;
        public DetailedCart(List<OrderDetail> orderDetails){

            for (OrderDetail orderDetail : orderDetails) {
                if (ds.contains(new DetailedShop(orderDetail)))
                {
                    for (DetailedShop d : ds) {
                        if(d.shopId==orderDetail.getShop().getId())
                        {
                            if(d.dp.contains(new DetailedProduct(orderDetail)))
                            {
                                for (DetailedProduct detailedProduct : d.dp) {
                                    if(detailedProduct.productId==orderDetail.getProducts().getId())
                                    {
                                        detailedProduct.dO.add(new DetailedOption(orderDetail));
                                    }
                                }
                            }
                            else
                            {
                                d.dp.add(new DetailedProduct(orderDetail));

                            }
                        }
                    }
                }
                else
                {
                    ds.add(new DetailedShop(orderDetail));
                }




            }
        }
    }

    @Data
    public static class DetailedShop {
        Long shopId;
        String shopName;
        int freePrice;
        int shipPrice;
        HashSet<DetailedProduct> dp = new HashSet<>();



        public DetailedShop(OrderDetail od) {
            shopId = od.getShop().getId();
            shopName = od.getShop().getShopName();
            freePrice=od.getShop().getFreePrice();


            shipPrice=od.getShop().getNofree();
            dp.add(new DetailedProduct(od));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetailedShop that = (DetailedShop) o;
            return Objects.equals(shopId, that.shopId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(shopId);
        }
        


    }







    @Data
    public static class DetailedProduct{
        Long productId;
        String productName;
        String thumbNail;
        HashSet<DetailedOption> dO = new HashSet<DetailedOption>();

        public DetailedProduct(OrderDetail od){
            if(od.getOptions().isSales())
            {

            }


            productId =od.getProducts().getId();
            productName=od.getProducts().getProductName();
            for (File image : od.getProducts().getImages()) {
                if(image.isIsthumb()==true)
                {
                    thumbNail=image.getFilepath();
                    break;
                }
            }
            dO.add(new DetailedOption(od));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DetailedProduct that = (DetailedProduct) o;
            return Objects.equals(productId, that.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(productId);
        }
    }

    @Data
    public static class DetailedOption{
        Long optionId;
        int optionCount;
        int originPrice;
        String optionName;
        Long detailedId;
        String pandaName;
        boolean discount=false;
        public DetailedOption(OrderDetail detail){
            detailedId=detail.getId();
            optionId=detail.getOptions().getId();
            optionCount=detail.getProductCount();
            originPrice=detail.getOptions().getOptionPrice();
            optionName=detail.getOptions().getOptionName();
            if(detail.getPanda() !=null)
            {
                pandaName=detail.getPanda().getPandaName();
                discount=true;
            }
        }

    }


    ///




        @Data
        static class CreateOrderDetailDAO {
            private Long productid;
            private List<detailOptionDAO> cart;
            private Long selectpanda;
        }

        @Data
        static class detailOptionDAO {
            private Long optionId;
            private int optionCount;
            private String originPrice;
            private String optionPrice;
            private Long detailedId;

        }

        @Data
        static class thisResultDto {
            boolean success;
            DetailedCart dtos;

            public thisResultDto(boolean b, DetailedCart mycart) {
                success = b;
                dtos = mycart;
            }
        }

    @Data
    static class tfResultDto {
        boolean success;


        public tfResultDto(boolean b) {
            success = b;

        }
    }
        @Data
    static class removeDetailDAO {
        private Long orderDetailId;
    }

    @Data
    static class paymentDAO {
        private ArrayList<Long> shopId;
    }
    @Data
    static class finishPaymentDAO {
        String impuid;
        String merchantuid;
        String paymethod;
        String paid_amount;
        String stat;
    }

    @Data
    static class  AfterConfirm {
        long[] dataList;
    }
}
