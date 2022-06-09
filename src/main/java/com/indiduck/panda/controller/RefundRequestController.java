package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Service.RefundRequestService;
import com.indiduck.panda.Service.VerifyService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class RefundRequestController {

    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final RefundRequestService refundRequestService;
    @Autowired
    private final VerifyService verifyService;

    @RequestMapping(value = "/api/readRefundRequest", method = RequestMethod.POST)
    public ResponseEntity<?> readRefundRequest(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody UserOrderId userOrderId) throws Exception {
//        System.out.println("userOrderId = " + userOrderId);
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId.uoid);
//        System.out.println("byId = " + byId);
        RefundRequest refundRequests=null;
        if(byId.get().getRefundRequest().size() !=0)
        {
            refundRequests=byId.get().getRefundRequest().get(byId.get().getRefundRequest().size() -1);
            return ResponseEntity.ok(new RefundDTO(true,refundRequests.getRefundMessage(),refundRequests.getId()
                    ,byId.get(),refundRequests));
        }
        return null;




    }

    //교환거절절
   @RequestMapping(value = "/api/rejecttrade", method = RequestMethod.POST)
    public ResponseEntity<?> rejectTrade(@CurrentSecurityContext(expression = "authentication")
                                                  Authentication authentication, @RequestBody ConfirmRefundRequest confirmRefundRequest) throws Exception {


        log.info(authentication.getName()+"교환 승인 거절");
        boolean b1 = verifyService.orderForShop(authentication.getName(), confirmRefundRequest.userOrderId);
        if(!b1)
        {
            log.error(authentication.getName()+"교환 승인 요청 실패");

            return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));

        }


        //기준일 뒤로 미루고
        boolean b = refundRequestService.rejectTrade(confirmRefundRequest);

        if(b)
        {
            log.info(authentication.getName()+"교환 승인 거절 성공");

            return ResponseEntity.ok(new TFMessageDto(true,"성공"));

        }

       log.error(authentication.getName()+"교환 승인 요청 실패"+confirmRefundRequest.refundId+"<리펀드아이디"+confirmRefundRequest.userOrderId+"<유저오더아이디");

        return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));
    }

    //교환승인
    @RequestMapping(value = "/api/confirmtrade", method = RequestMethod.POST)
    public ResponseEntity<?> confirmTrade(@CurrentSecurityContext(expression = "authentication")
                                                       Authentication authentication, @RequestBody ConfirmRefundRequest confirmRefundRequest) throws Exception {

        
        String name = authentication.getName();
        boolean before = verifyService.verifyByerOrder(name, confirmRefundRequest.userOrderId);
        if(!before)
        {
            log.error(name+"교환 검증 실패");

            return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));

        }



        //기준일 뒤로 미루고
        boolean b = refundRequestService.confirmTrade(confirmRefundRequest);

        if(b)
        {
            log.info(name+"교환신청성공");

            return ResponseEntity.ok(new TFMessageDto(true,"성공"));

        }
        
        log.error(name+"교환요청 실패");

        return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));
    }


    //환불신청확인
    @RequestMapping(value = "/api/confirmRefundRequest", method = RequestMethod.POST)
    public ResponseEntity<?> confirmRefundRequest(@CurrentSecurityContext(expression = "authentication")
                                                       Authentication authentication, @RequestBody ConfirmRefundRequest confirmRefundRequest) throws Exception {
//        System.out.println("confirmRefundRequest = " + confirmRefundRequest);
        String name = authentication.getName();
        boolean before = verifyService.verifyByerOrder(name, confirmRefundRequest.userOrderId);
        if(!before)
        {
            return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));

        }



        //기준일 뒤로 미루고
        boolean b = refundRequestService.confrimRefund(confirmRefundRequest);

        if(b)
        {
            log.info(name+"환불신청성공");

            return ResponseEntity.ok(new TFMessageDto(true,"성공"));

        }
        log.error(name+"환불신청실패");

        return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));
    }

//    @RequestMapping(value = "/api/refundlist", method = RequestMethod.POST)
//    public ResponseEntity<?> refundList(@CurrentSecurityContext(expression = "authentication")
//                                                          Authentication authentication, @RequestBody ConfirmRefundRequest confirmRefundRequest) throws Exception {
//        System.out.println("confirmRefundRequest = " + confirmRefundRequest);
//
//        boolean b = refundRequestService.confrimRefund(confirmRefundRequest);
//
//
//        if(b)
//        {
//
//            return ResponseEntity.ok(new TFMessageDto(true,"성공"));
//
//        }
//        return ResponseEntity.ok(new TFMessageDto(false,"요청에 실패했습니다 해당 현상이 계속될경우 문의해주세요"));
//
//    }

    @Data
    private static class UserOrderId {
        long uoid;
    }

    @Data
    private static class RefundDTO
    {
        boolean success;
        long requestId;
        long userOrderId;
        String refundMessage;
        //결제한 전체금액
        long allPrice;
        //배송비
        int shipPrice;
        //배송비를 제외한 상품 금액
        long allPriceMinusShipPrice;
        //이 주문에서 전체 환불한 금액?
        long allRefundMoney;

        //환불 리스트
        List<RefundList> refundListList=new ArrayList<>();

        public RefundDTO(boolean su,String mes,long requestId, UserOrder uo,RefundRequest refundRequest) {
            this.success=su;
            this.refundMessage=mes;
            this.requestId = requestId;
            this.userOrderId = uo.getId();
            this.allPrice = uo.finalPrice();
            this.shipPrice = uo.getShipPrice();
            this.allRefundMoney=uo.getFinalRefundMoney();
            this.allPriceMinusShipPrice = uo.getAmount();
            for (OrderDetail orderDetail : uo.getDetail()) {
                if(orderDetail.getOrderStatus()== OrderStatus.환불대기)
                {
                    this.refundListList.add(new RefundList(orderDetail.getId(),orderDetail.getProducts().getProductName(),
                            orderDetail.getProductCount(),orderDetail.getReqRefund(),orderDetail.getPanda(),
                            orderDetail.getTotalPrice(),orderDetail.getIndividualPrice(),orderDetail.getOptions().getOptionName(),refundRequest.getRefundMoney(),orderDetail.getConfirmRefund()));
                }
            }


        }
    }

    @Data
    private static class RefundList {
        long orderDetailId;
        String productName;
        //주문갯수
        String optionName;
        int orderCount;
        //환불신청갯수
        int refundCount;
        //환불신청된액수
        long refundMoney;
        //환불 완료된 갯수
        int compleCount;

        //판다여부
        boolean isPanda;
        //이 옵션의 총가격
        long optionAllPrice;
        //이 옵션의 개별가격
        long optionIndividualPrice;

        public RefundList(long orderDetailId, String productName, int orderCount,
                          int refundCount, Panda isPanda, long optionAllPrice, long optionIndividualPrice,String optionName,long refundMoney,int com) {
            this.orderDetailId = orderDetailId;
            this.productName = productName;
            this.optionName=optionName;
            this.orderCount = orderCount;
            this.refundCount = refundCount;
            if(isPanda!=null)
            {
                this.isPanda = true;
            }else{
                this.isPanda=false;
            }
            this.compleCount=com;
            this.optionAllPrice = optionAllPrice;
            this.optionIndividualPrice = optionIndividualPrice;
            this.refundMoney=refundMoney;
        }
    }

    @Data
    public static class ConfirmRefundRequest {
        long  refundId;
        long userOrderId;
        long refundMoney;
        List<RefundArray> refundArray;

    }

    @Data
    public static class RefundArray {
        long expectMoney;
        long idx;
        long individualPrice;
        boolean issale;
        long key;
        long odid;
        int originOrder;
        int refundConfrimOrder;
        int refundOrder;

    }
}
