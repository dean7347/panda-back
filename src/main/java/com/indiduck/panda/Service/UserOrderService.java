package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.ShopRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.controller.UserOrderController;
import com.indiduck.panda.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserOrderService {


    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private UserOrderRepository userOrderRepository;
    @Autowired
    private final RefundRequestService refundRequestService;


    //구매자가 호출하는것
    public UserOrder cancelOrder(long userorderId) {
        Optional<UserOrder> byId = userOrderRepository.findById(userorderId);
        for (OrderDetail orderDetail : byId.get().getDetail()) {
            //주문을 취소할수 있는 상태는 결제완료상태일때만 가능하다
            if ((orderDetail.getOrderStatus() != OrderStatus.결제완료)) {
                return null;
            }
        }
        String mid = byId.get().getMid();
        boolean b = refundRequestService.allCancelForSeller(mid, byId.get());
        if (!b) {
            return null;
        }
        byId.get().cancelOrder();
        byId.get().cancelMoneyJob();
        return byId.get();


    }

    public UserOrder cancelOrderSystem(long userorderId) {
        log.info("미 확인 주문 취소 로직 실행");
        Optional<UserOrder> byId = userOrderRepository.findById(userorderId);
        System.out.println("byId = " + byId.get().getId());

        String mid = byId.get().getMid();
        boolean b = refundRequestService.allCancelForSystem(mid, byId.get());
        if (!b) {
            log.error("취소 실패로 인한 강제 취소 실행");

            return byId.get();
        }

        log.info("미 확인 주문 취소 로직 종료");

        return byId.get();


    }

    //부분취소

    public boolean extendConfirm(long ui) {
        Optional<UserOrder> byId = userOrderRepository.findById(ui);
        return byId.get().extendConfirm();

    }


    //판매자의 호출스테이터스
    public UserOrder ChangeOrder(long id, String status, String cur, String wayb) {
        Optional<UserOrder> byId = userOrderRepository.findById(id);
        UserOrder userOrder = byId.get();
        switch (status) {
            case "준비중":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                userOrder.readyOrder();
                break;
            //cur = couriercom /waybillnumber
            case "발송중":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                userOrder.sendOutOrder(cur, wayb);

                break;
            case "구매확정":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정 ||userOrder.getOrderStatus() == OrderStatus.준비중||userOrder.getOrderStatus() == OrderStatus.결제완료) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                userOrder.confirmOrder();
                break;
            case "주문취소(유저)":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정||userOrder.getOrderStatus() == OrderStatus.준비중 ) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                boolean bo = refundRequestService.allCancelForSeller(userOrder.getMid(), byId.get());
                if (bo == false) {

                    return null;
                } else {
                    userOrder.cancelOrder();

                }
                break;
            case "주문취소":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정 ) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                boolean b = refundRequestService.allCancelForSeller(userOrder.getMid(), byId.get());
                if (b == false) {

                    return null;
                } else {
                    userOrder.cancelOrder();

                }
                break;
            case "환불신청":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정
                        ||userOrder.getOrderStatus() == OrderStatus.준비중 ) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                userOrder.refundOrder(cur);
                break;
            case "상점확인중":
                if (userOrder.getOrderStatus() == OrderStatus.주문취소 ||userOrder.getOrderStatus() == OrderStatus.구매확정) {
                    return null;
                }
                log.info(id + "번 주문이" + status + "상태로 변경되었습니다");

                userOrder.checkRefund();
        }
        return userOrder;

    }


}
