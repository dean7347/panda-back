package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.OrderDetailRepository;
import com.indiduck.panda.Repository.ShopRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.OrderDetailService;
import com.indiduck.panda.Service.RefundRequestService;
import com.indiduck.panda.Service.UserOrderService;
import com.indiduck.panda.Service.VerifyService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class UserOrderController {
    @Autowired
    private final UserOrderService userOrderService;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ShopRepository shopRepository;
    @Autowired
    private final RefundRequestService refundRequestService;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final OrderDetailService orderDetailService;
    @Autowired
    private final VerifyService verifyService;

    //유저가 최초로 환불 신청을 한다
    @RequestMapping(value = "/api/refundactionforuser", method = RequestMethod.POST)
    public ResponseEntity<?> refundactionforuser(@CurrentSecurityContext(expression = "authentication")
                                                         Authentication authentication, @RequestBody RefundReq refundReq) throws Exception {
        log.info(authentication.getName() + "의 환불 신청" + refundReq.getUserOrderId() + "");
        boolean b = verifyService.userOrderVerifyForuser(authentication.getName(), refundReq.getUserOrderId());
        if (!b) {
            return ResponseEntity.ok(new TFMessageDto(false, "상태변경 완료"));

        }
        try {
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Optional<UserOrder> byId = userOrderRepository.findById(refundReq.userOrderId);
            if (!(byId.get().getOrderStatus() == OrderStatus.발송중)) {
                return ResponseEntity.ok(new TFMessageDto(false, "환불신청 실패"));


            }
            List<RefundList> refundList = refundReq.refundList;
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (RefundList list : refundList) {
                Optional<OrderDetail> byId1 = orderDetailRepository.findById(list.optionId);
                if(byId1.get().getProductCount() < list.optionCount)
                {
                    return ResponseEntity.ok(new TFMessageDto(false, "남은 수량보다 적은 상품입니다"));

                }
                if(list.optionCount <=0)
               {
                   return ResponseEntity.ok(new TFMessageDto(false, "0개는 입력할 수 없습니다"));

               }
            }
            for (RefundList list : refundList) {
                long optionId = list.optionId;
                Optional<OrderDetail> byId1 = orderDetailRepository.findById(optionId);
                OrderDetail orderDetail = byId1.get();
                
                orderDetail.reqRefund(list.optionCount);
                orderDetails.add(orderDetail);
            }
            refundRequestService.newRefundRequest(byId.get(), orderDetails, refundReq.refundMessage, byEmail.get());
            log.info(authentication.getName() + "의" + refundReq.getUserOrderId() + "번 주문" + "환불신청작성 성공");
            return ResponseEntity.ok(new TFMessageDto(true, "상태변경 완료"));
        } catch (Exception e) {
            log.error(authentication.getName() + "의" + refundReq.getUserOrderId() + "번 주문" + "환불신청작성 실패");

            return ResponseEntity.ok(new TFMessageDto(false, "환불신청 실패"));

        }


        //리펀드 리퀘스트 생성


    }

    //부분취소
    @RequestMapping(value = "/api/partialCancel", method = RequestMethod.POST)
    public ResponseEntity<?> partialCancel(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication, @RequestBody RefundReq refundReq) throws Exception {
        log.info(authentication.getName() + "의 부분취소 요청");
        boolean b1 = verifyService.orderForShop(authentication.getName(), refundReq.userOrderId);
        if (!b1) {
            log.error(authentication.getName() + "의 부분취소 요청 실패");

            return ResponseEntity.ok(new TFMessageDto(false, "상태변경 완료 실패했습니다"));

        }


        try {
            List<RefundList> refundList = refundReq.refundList;
            List<OrderDetail> orderDetails = new ArrayList<>();
            int refundMoney = 0;
            int serverMoney =0;
            boolean result = true;
            for (RefundList list : refundList) {
                long optionId = list.optionId;
                Optional<OrderDetail> byId1 = orderDetailRepository.findById(optionId);
                //현재갯수보다 작인지 체크
                if (byId1.get().getProductCount() >= list.optionCount) {
                    refundMoney += Integer.parseInt(list.optionPrice.replace(",", ""));
                    serverMoney +=byId1.get().getIndividualPrice()*list.optionCount;
                } else {
                    result = false;
                }
                //금액이 맞는지 체크



            }
            if(refundMoney != serverMoney)
            {
                result= false;
            }
            if (result) {
                boolean b = orderDetailService.refundOrder(refundReq.getUserOrderId(), refundMoney,refundList, refundReq.refundMessage);
                if (b) {
                    log.info(authentication.getName() + "의 부분취소 요청 성공");

                    return ResponseEntity.ok(new TFMessageDto(true, "상태변경 완료"));

                } else {
                    log.error(authentication.getName() + "의 부분취소 요청성공했으나 환불신청에는 실패함" + refundReq.getUserOrderId() + "번 주문 확인 요망");

                    return ResponseEntity.ok(new TFMessageDto(false, "상태변경에 성공했으니 환불에 실패했습니다  고객센터로 문의주세요(필수사항)"));

                }

            }
//            for (RefundList list : refundList) {
//                long optionId = list.optionId;
//                Optional<OrderDetail> byId1 = orderDetailRepository.findById(optionId);
//
//
//                orderDetailService.partialCancelation(byId1.get(), list.optionCount, refundReq.refundMessage);
//
//
//            }

        } catch (Exception e) {
            log.error(authentication.getName() + "의 부분취소 실패함" + refundReq.getUserOrderId() + "번 주문 확인 요망");

            return ResponseEntity.ok(new TFMessageDto(false, "문자오류로 인해 상태변경에 실패했습니다"));

        }
        log.error(authentication.getName() + "의 부분취소 실패함" + refundReq.getUserOrderId() + "번 주문 확인 요망");


        return ResponseEntity.ok(new TFMessageDto(false, "상태변경에 실패했습니다"));


    }

    @RequestMapping(value = "/api/editstatus", method = RequestMethod.POST)
    public ResponseEntity<?> editStatus(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody ChangeAction changeAction) throws Exception {


        UserOrder userOrder = userOrderService.ChangeOrder(changeAction.userOrderId, changeAction.state, changeAction.courier, changeAction.waybill);
        if (userOrder != null) {
            log.info(authentication.getName() + "의 상태변경 요청 성공" + changeAction.userOrderId + changeAction.state + changeAction.courier + changeAction.waybill);
            return ResponseEntity.ok(new TFMessageDto(true, "상태변경 완료"));

        }
        log.error(authentication.getName() + "의 상태변경 실패");

        return ResponseEntity.ok(new TFMessageDto(false, "상태변경에 실패했습니다"));

    }

    @RequestMapping(value = "/api/extendconfirm", method = RequestMethod.POST)
    public ResponseEntity<?> extendConfirm(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication, @RequestBody ShopDashBoardForUserOrderId sdbf) throws Exception {


        boolean b = userOrderService.extendConfirm(sdbf.orderId);
        if (b) {
            log.info(authentication.getName() + "의 구매기간 연장신청");
            return ResponseEntity.ok(new TFMessageDto(true, "구매기간 연장 성공"));

        }
        log.error(authentication.getName() + "의 구매기간 연장 실패");
        return ResponseEntity.ok(new TFMessageDto(false, "3번이상 연장을 했거나 연장할 수 있는 상태가 아닙니다"));

    }


    //여러 주문들의 스테이터스를 바꿈
    @RequestMapping(value = "/api/selecteditstatus", method = RequestMethod.POST)
    public ResponseEntity<?> selectedEditStatus(@CurrentSecurityContext(expression = "authentication")
                                                        Authentication authentication, @RequestBody ChangeActions changeAction) throws Exception {

//        System.out.println("changeAction = " + changeAction.userOrderId);
        long num = 0;
        for (long l : changeAction.userOrderId) {
            boolean b = verifyService.userOrderForShopOrUser(authentication.getName(), l);
            if (!b) {
                log.error(authentication.getName() + "이 스테이터스를 바꾸려고 했으나" + l + "번주문에 이상이 생겼습니다1");
                return ResponseEntity.ok(new TFMessageDto(false, l + "번 주문이 이미 취소되었거나 확인에 실패했습니다"));

            }
            UserOrder userOrder = userOrderService.ChangeOrder(l, changeAction.state, changeAction.courier, changeAction.waybill);
            if (userOrder == null) {
                num = l;
                log.error(authentication.getName() + "이 스테이터스를 바꾸려고 했으나" + l + "번주문에 이상이 생겼습니다2");

                return ResponseEntity.ok(new TFMessageDto(false, l + "번 주문이 이미 취소되었거나 확인에 실패했습니다"));


            }
            log.info(authentication.getName() + "이 스테이터스를 바꿈");


        }
        return ResponseEntity.ok(new TFMessageDto(true, "상태변경 완료"));


    }


    @RequestMapping(value = "/api/shop/shoporderlist", method = RequestMethod.GET)
    public ResponseEntity<?> editStatus(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, Pageable pageable, @RequestParam("type") String type) throws Exception {
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        List<UserOrder> byUserId = new ArrayList<>();
        Page<UserOrder> byShopAndOrderStatus = null;

        switch (type) {
            //신규주문
            case "recent":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.결제완료);
//                System.out.println("최근주문조회");
                break;
            //준비중인주문
            case "ready":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.준비중);
                break;

            //배송중인주문
            case "shipping":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.발송중);
                break;

            //교환/반품요청
            case "change":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.상점확인중);
                break;

            //완료된 주문
            case "finish":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.구매확정);
                break;

            //교환/반품확인
            case "check":
                byShopAndOrderStatus = userOrderRepository.findByShopAndOrderStatus(pageable, byEmail.get().getShop(), OrderStatus.환불대기);
                break;
            default:
                log.error("없는 주문요청입니다");
                break;

        }
        List<ShopDashBoardDTO> dashs = new ArrayList<>();
        if (!byShopAndOrderStatus.isEmpty()) {
            for (UserOrder shopAndOrderStatus : byShopAndOrderStatus) {
                List<OrderDetail> detail = shopAndOrderStatus.getDetail();
                HashSet<String> productName = new HashSet<>();
                int originMoney = 0;
                for (OrderDetail orderDetail : detail) {
                    productName.add(orderDetail.getProducts().getProductName());
                    originMoney += orderDetail.getOriginOrderMoney();
                }

                //택배금액을 포함하여야 하는가
                int redundancy = 0;

                if (originMoney < shopAndOrderStatus.getFreeprice()) {
                    redundancy += shopAndOrderStatus.getShipPrice();
                }

                dashs.add(new ShopDashBoardDTO(shopAndOrderStatus.getId(), productName.toString(), shopAndOrderStatus.getPureAmount() + redundancy
                        , shopAndOrderStatus.getCreatedAt(), shopAndOrderStatus.getPaymentStatus(),
                        shopAndOrderStatus.getCourierCom(), shopAndOrderStatus.getWaybillNumber()));
            }
        }

//        System.out.println("dashs = " + byShopAndOrderStatus);
//        System.out.println("dashs = " + type);


//        System.out.println("dashs = " + dashs);


        return ResponseEntity.ok(new pageDto(true, byShopAndOrderStatus.getTotalPages(), byShopAndOrderStatus.getTotalElements(), dashs));

    }

    @RequestMapping(value = "/api/shopdashboard", method = RequestMethod.POST)
    public ResponseEntity<?> shopDashBoard(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication, @RequestBody ShopDashBoard shopDashBoard) throws Exception {

//        System.out.println("shopDashBoard = " + shopDashBoard);
        try {
            LocalDateTime startDay = LocalDateTime.of(shopDashBoard.startYear, shopDashBoard.startMonth + 1, shopDashBoard.startDay
                    , 0, 0, 0, 0);
            LocalDateTime endDay = LocalDateTime.of(shopDashBoard.endYear, shopDashBoard.endMonth + 1, shopDashBoard.endDay
                    , 23, 59, 59, 999999999);
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Shop shop = byEmail.get().getShop();
            List<ShopDashboardDtoType> shopDashboardDtoTypeList = new ArrayList<>();
            List<UserOrder> uoList = new ArrayList<>();
            int finish = 0;
            int yet = 0;
            //상태정의
            if (shopDashBoard.searchDateMode.equals("정산일자")) {
                Optional<List<UserOrder>> data = userOrderRepository.findByShopAndDepositCompletedNotNullAndDepositCompletedBetween(shop, startDay, endDay);
                uoList.addAll(data.get());
            } else if (shopDashBoard.searchDateMode.equals("정산예정일")) {
                Optional<List<UserOrder>> data = userOrderRepository.findByShopAndExpectCalculateNotNullAndExpectCalculateBetween(shop, startDay, endDay);
                uoList.addAll(data.get());

            } else if (shopDashBoard.searchDateMode.equals("판매일자")) {
                Optional<List<UserOrder>> data = userOrderRepository.findByShopAndCreatedAtNotNullAndCreatedAtBetween(shop, startDay, endDay);
                uoList.addAll(data.get());

            } else if (shopDashBoard.searchDateMode.equals("구매확정일자")) {
                Optional<List<UserOrder>> data = userOrderRepository.findByShopAndFinishAtNotNullAndFinishAtBetween(shop, startDay, endDay);
                uoList.addAll(data.get());

            } else {
                return ResponseEntity.ok(new DashboardDto(true, null, 0, 0,null,null,null,null));

            }

            for (UserOrder userOrder : uoList) {
                if (userOrder.getPaymentStatus() == PaymentStatus.지급대기 || userOrder.getPaymentStatus() == PaymentStatus.지급예정) {
                    yet += userOrder.getShopMoney();
                } else if (userOrder.getPaymentStatus() == PaymentStatus.지급완료) {
                    finish += userOrder.getShopMoney();
                }

                shopDashboardDtoTypeList.add(new ShopDashboardDtoType(userOrder));
            }
            System.out.println(" 나가는데이터= " + shopDashboardDtoTypeList);
            return ResponseEntity.ok(new DashboardDto(true, shopDashboardDtoTypeList, finish, yet,shopDashBoard.searchDateMode,startDay,endDay,shop.getShopName()));

        } catch (Exception e) {
            System.out.println("E = " + e);

            return ResponseEntity.ok(new DashboardDto(true, null, 0, 0,null,null,null,null));


        }


    }

    @RequestMapping(value = "/api/shopdashboardforordernumber", method = RequestMethod.POST)
    public ResponseEntity<?> shopDashBoardForOrderNumber(@CurrentSecurityContext(expression = "authentication")
                                                                 Authentication authentication, @RequestBody ShopDashBoardForUserOrderId shopDashBoardForUserOrderId) throws Exception {


        try {
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Shop shop = byEmail.get().getShop();
            Optional<UserOrder> byId = userOrderRepository.findByShopAndId(shop, shopDashBoardForUserOrderId.orderId);
            List<ShopDashboardDtoType> shopDashboardDtoTypeList = new ArrayList<>();

            shopDashboardDtoTypeList.add(new ShopDashboardDtoType(byId.get()));


            return ResponseEntity.ok(new DashboardDto(true, shopDashboardDtoTypeList, 0, 0,null,null,null,null));

        } catch (Exception e) {
            System.out.println("E = " + e);

            return ResponseEntity.ok(new DashboardDto(false, null, 0, 0,null,null,null,null));


        }


    }


    @Data
    private class pageDto {
        boolean success;
        int totalpage;
        Long totalElement;
        List<ShopDashBoardDTO> pageList = new ArrayList<>();

        public pageDto(boolean su, int totalP, Long totalE, List<ShopDashBoardDTO> pl) {
            success = su;
            totalpage = totalP;
            totalElement = totalE;
            pageList = pl;
        }
    }

    @Data
    private static class ShopDashBoardDTO {
        long id;
        String name;
        int price;
        LocalDateTime orderAt;
        String paymentStatus;
        String comp;
        String number;

        public ShopDashBoardDTO(long id, String name, int price, LocalDateTime orderAt, PaymentStatus paymentStatus, String comp, String number) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.orderAt = orderAt;
            if (paymentStatus == null) {
                this.paymentStatus = null;

            } else {
                this.paymentStatus = paymentStatus.toString();

            }
            this.comp = comp;
            this.number = number;
        }

    }

    @Data
    private static class DashboardDto {
        boolean success;
        List<ShopDashboardDtoType> shopDashboardDtoTypeList = null;
        int finMoney;
        int expectMoney;
        //조회기준
        String standard;
        LocalDateTime startDay;
        LocalDateTime endDay;
        //조회자
        String name;


        public DashboardDto(boolean success, List<ShopDashboardDtoType> spdl, int f, int e,String standard,LocalDateTime sD,LocalDateTime endD,String name) {
            this.success = success;
            this.shopDashboardDtoTypeList = spdl;
            this.finMoney = f;
            this.expectMoney = e;
            this.standard=standard;
            this.startDay=sD;
            this.endDay=endD;
            this.name=name;
        }
    }

    @Data
    public static class ShopDashboardDtoType {
        //주문번호
        Long id;


        //실제 판매가(택배비 제외)
        int realPrice;
        //정가
        int beforeSalePrice;
        //택배비
        int shipPrice;
        //샵이 받게될 정산액
        int shopPrice;
        //환불금액
        int settlePrice;
        //수수료
        int fees;
        //판매일자
        LocalDateTime salesDate;
        //구매확정일자
        LocalDateTime confirmDate;
        //정산예정일
        LocalDateTime expectDate;
        //정산일자
        LocalDateTime depositCompleted;
        //정산상태
        PaymentStatus paymentStatus;


        public ShopDashboardDtoType(UserOrder uo) {
            this.id = uo.getId();
            this.realPrice = uo.getPureAmount()+uo.shipPriceCalculation();
            //무료배송이 아닐경우
            this.shipPrice=uo.shipPriceCalculation();
            this.beforeSalePrice = uo.getPureAmount();
            this.shopPrice=uo.getShopMoney();
            this.settlePrice = uo.getFinalRefundMoney();
            this.fees = uo.getPandaMoney()+uo.getHostMoney()+uo.getBalance();
            this.salesDate = uo.getCreatedAt();
            this.confirmDate = uo.getFinishAt();
            //정산예정일
            this.expectDate = uo.getExpectCalculate();
            //정산일
            this.depositCompleted = uo.getDepositCompleted();
            this.paymentStatus = uo.getPaymentStatus();
        }
    }


    @Data
    static class ChangeAction {
        //필수항목
        long userOrderId;
        String state;
        //발송중 항목에는 해당 항목을 넣어서 보낸다 없다면 ""을 담아서 보낸다
        String courier;
        String waybill;
    }

    @Data
    static class ChangeActions {
        //필수항목
        long[] userOrderId;
        String state;
        //발송중 항목에는 해당 항목을 넣어서 보낸다 없다면 ""을 담아서 보낸다
        String courier;
        String waybill;
    }

    @Data
    private static class ShopDashBoard {
        int startYear;
        int startMonth;
        int startDay;

        int endYear;
        int endMonth;
        int endDay;

        String status;
        String searchDateMode;
    }

    @Data
    private static class ShopDashBoardForUserOrderId {
        long orderId;
    }

    private class PaymentStatusType {
        String type;
    }

    @Data
    public static class RefundReq {
        long userOrderId;
        String refundMessage;
        List<RefundList> refundList;
    }

    @Data
    public static class RefundList {
        boolean ispanda;
        long key;
        int max;
        int optionCount;
        long optionId;
        String optionName;
        String optionPrice;
        int originPrice;

    }

}
