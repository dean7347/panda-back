package com.indiduck.panda.Service;


import com.indiduck.panda.Repository.OrderDetailRepository;
import com.indiduck.panda.Repository.RefundRequestRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.config.ApiKey;
import com.indiduck.panda.controller.RefundRequestController;
import com.indiduck.panda.domain.*;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class RefundRequestService {
    @Autowired
    private final RefundRequestRepository refundRequestRepository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private ApiKey apiKey;

    public RefundRequest newRefundRequest(UserOrder uo, List<OrderDetail> orderDetails, String message, User user) {
//        System.out.println("orderDetails서비스꺼 = " + orderDetails);
        RefundRequest rr = RefundRequest.newRefundRequest(uo, message, user);

        refundRequestRepository.save(rr);
        uo.setRefund(rr);


        return rr;
    }


    //판매자캔슬
    public boolean allCancelForSeller(String mind, UserOrder userOrder) {
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        IamportClient iamportClient = new IamportClient(test_api_key, test_api_secret);
        CancelData cancel_data = new CancelData(mind, true); //imp_uid를 통한 전액취소

        try {
            IamportResponse<Payment> payment_response = iamportClient.cancelPaymentByImpUid(cancel_data);
            String receiptUrl = payment_response.getResponse().getReceiptUrl();
            userOrder.setReceiptUrl(receiptUrl);

        } catch (IamportResponseException e) {
            log.error("올캔슬 폴 셀러"+e.getMessage());
            switch (e.getHttpStatusCode()) {
                case 401:
                    //TODO
                    log.error("올캔슬 포셀러e = " + e);
                    return false;
                case 500:
                    //TODO
                    log.error("올켄슬 포 셀러e = " + e);
                    return false;

            }
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("올켄슬 포 셀러e = " + e);

            e.printStackTrace();
            return false;

        } catch (Exception e) {
            log.error("올켄슬 포 셀러e = " + e);

            return false;
        }
        return true;
    }

    //시스템에의한 강제캔슬
    public boolean allCancelForSystem(String mind, UserOrder userOrder) {
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        IamportClient iamportClient = new IamportClient(test_api_key, test_api_secret);
        CancelData cancel_data = new CancelData(mind, true); //imp_uid를 통한 전액취소
        userOrder.cancelOrder();
        userOrder.cancelMoneyJob();
        try {
            IamportResponse<Payment> payment_response = iamportClient.cancelPaymentByImpUid(cancel_data);
            String receiptUrl = payment_response.getResponse().getReceiptUrl();
            userOrder.setReceiptUrl(receiptUrl);

        } catch (IamportResponseException e) {
            log.error("올캔슬 폴 셀러1"+e.getMessage());
            switch (e.getHttpStatusCode()) {
                case 401:
                    //TODO
                    log.error("올캔슬 포셀러2e = " + e);
                    return false;
                case 500:
                    //TODO
                    log.error("올켄슬 포 셀러3e = " + e);
                    return false;

            }
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("올켄슬 포 셀러4e = " + e);

            e.printStackTrace();
            return false;

        } catch (Exception e) {
            log.error("취소로직 오류로 실패  = "+userOrder.getId()+"//" + e);

            return false;
        }
        log.info("구매취소 로직 성공적ㅇ 실행");
        return true;
    }


    public boolean rejectTrade(RefundRequestController.ConfirmRefundRequest confirmRefundRequest)
    {
        long userOrderId = confirmRefundRequest.getUserOrderId();
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId);
        UserOrder userOrder = byId.get();
        userOrder.rejectRefund();
        RefundRequest refundRequest = refundRequestRepository.findById(confirmRefundRequest.getRefundId()).get();
        refundRequest.rejectTrade();
        return true;


    }

    public boolean confirmTrade(RefundRequestController.ConfirmRefundRequest confirmRefundRequest)
    {
        long userOrderId = confirmRefundRequest.getUserOrderId();
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId);
        UserOrder userOrder = byId.get();

        List<RefundRequestController.RefundArray> refundArray = confirmRefundRequest.getRefundArray();
        for (RefundRequestController.RefundArray array : refundArray) {
//            System.out.println("오디아이디array = " + array);
            Optional<OrderDetail> odid = orderDetailRepository.findById(array.getOdid());
            if(odid.get().getProductCount()-array.getRefundConfrimOrder() <0)
            {
                log.error("confirmTrade 에서 0원보다 작은 요청이 감지되었습니다");
                return false;
            }
            odid.get().tradeConfirm(array.getRefundConfrimOrder());
        }
        userOrder.confirmUserOrderTrade();
        RefundRequest refundRequest = refundRequestRepository.findById(confirmRefundRequest.getRefundId()).get();
        refundRequest.confrimTrade();
        return true;


    }

    public boolean confrimRefund(RefundRequestController.ConfirmRefundRequest confirmRefundRequest) {
        List<RefundRequestController.RefundArray> refundArray = confirmRefundRequest.getRefundArray();
        String test_api_key = apiKey.getRESTAPIKEY();
        String test_api_secret = apiKey.getRESTAPISECRET();
        IamportClient iamportClient = new IamportClient(test_api_key, test_api_secret);
//        System.out.println("confirmRefundRequest = " + confirmRefundRequest);
        long comMoney=0;
        long refundMoney = confirmRefundRequest.getRefundMoney();

        for (RefundRequestController.RefundArray array : refundArray) {
//            System.out.println("오디아이디array = " + array);
            Optional<OrderDetail> odid = orderDetailRepository.findById(array.getOdid());
            comMoney+= Math.floor(odid.get().getIndividualPrice())*array.getRefundConfrimOrder();
            if(odid.get().getProductCount()-array.getRefundConfrimOrder() <0)
            {
                log.error(confirmRefundRequest.getUserOrderId()+"갯수초과환불");

                return false;
            }

        }

        if(refundMoney!=comMoney)
        {
//            System.out.println("검증안된금액");

            log.error(confirmRefundRequest.getUserOrderId()+"의 요청 실패 원인은 검증안된 금액"+refundMoney
                    +" //// "+comMoney);
            return false;
        }
        for (RefundRequestController.RefundArray array : refundArray) {
//            System.out.println("오디아이디array = " + array);
            Optional<OrderDetail> odid = orderDetailRepository.findById(array.getOdid());
            odid.get().partialRefund(array.getRefundConfrimOrder());
        }
        long userOrderId = confirmRefundRequest.getUserOrderId();
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId);
        UserOrder userOrder = byId.get();
        //가격검증

//        System.out.println("comMoney = " + comMoney);


        //환불
        String test_already_cancelled_imp_uid = byId.get().getMid();
        CancelData cancel_data = new CancelData(test_already_cancelled_imp_uid, true, BigDecimal.valueOf(refundMoney)); //imp_uid를 통한 500원 부분취소


        try {
            IamportResponse<Payment> payment_response = iamportClient.cancelPaymentByImpUid(cancel_data);
//            System.out.println("payment_response = " + payment_response.getResponse());
            Payment response = payment_response.getResponse();// 이미 취소된 거래는 response가 null이다
//            System.out.println("페이먼트메시지" + payment_response.getMessage());
            if (response == null) {
//                System.out.println("환불할 수 없는 상품입니다");
//                System.out.println(" 유저오더 아이디 ");
//                환불로직
                log.error(confirmRefundRequest.getUserOrderId()+"환불불가 상품");

                return false;


            } else {
                userOrder.setReceiptUrl(response.getReceiptUrl());

            }
        } catch (IamportResponseException e) {
            log.error(e.getMessage()+"컨펌리펀트");

            switch (e.getHttpStatusCode()) {
                case 401:
                    //TODO
                    log.error(e+"컨펌리펀트");

                    break;
                case 500:
                    log.error(e+"컨펌리펀트");

                    //TODO
                    break;
            }
        } catch (IOException e) {

            log.error(e+"컨펌리펀트");

            e.printStackTrace();
        } catch (Exception e) {
            log.error(e+"컨펌리펀트");

            return false;
        }
        //유저오더에서 금액 빼고, 상태수정하기( 배송일까지)
        //FOR문돌면서 오더 디테일 정보 수정하기
        //

//        userOrder.getRefundRequest().setRefundMoney(refundMoney);
//        userOrder.confirmRefundMoney(refundMoney);

        userOrder.confirmRefundMoney(comMoney);
        RefundRequest refundRequest = refundRequestRepository.findById(confirmRefundRequest.getRefundId()).get();
        refundRequest.confirmRefund(comMoney);
//        System.out.println(" 성공");
        return true;
    }
}
