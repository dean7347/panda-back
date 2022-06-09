package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserOrderRepository extends JpaRepository<UserOrder,Long> {

    Page<UserOrder> findAllByShop(Shop shop, Pageable pageable);
    Page<UserOrder> findAllByShopAndOrderStatus(Shop shop, OrderStatus orderStatus,Pageable pageable);
//    Page<UserOrder> findAllByShopAndOrderStatus(Shop shop, OrderStatus orderStatus);
    Page<UserOrder> findAllByUserId(User user,Pageable pageable);
    List<UserOrder> findByUserId(User user);
    Optional<UserOrder> findById(Long id);
    Optional<UserOrder> findByShopAndId(Shop shop,Long id);

    //정산일자 기준
    Optional<List<UserOrder>>findByShopAndDepositCompletedNotNullAndDepositCompletedBetween(Shop shop,LocalDateTime start,LocalDateTime end);

    // 정산예정일기준
    Optional<List<UserOrder>>findByShopAndExpectCalculateNotNullAndExpectCalculateBetween(Shop shop,LocalDateTime start,LocalDateTime end);

    //판매일자 기준
    Optional<List<UserOrder>>findByShopAndCreatedAtNotNullAndCreatedAtBetween(Shop shop,LocalDateTime start,LocalDateTime end);

    //구매확정일자 기준
    Optional<List<UserOrder>>findByShopAndFinishAtNotNullAndFinishAtBetween(Shop shop,LocalDateTime start,LocalDateTime end);


    //오더스테이터스 기준
    Optional<List<UserOrder>>findByShopAndOrderStatus(Shop shop,OrderStatus od);
    Optional<List<UserOrder>>findByShopAndOrderStatusAndCreatedAtBetween(Shop shop,OrderStatus od,LocalDateTime st,LocalDateTime end);
    Optional<List<UserOrder>>findByShopAndOrderStatusAndFinishAtBetween(Shop shop,OrderStatus od,LocalDateTime st,LocalDateTime end);
    Optional<List<UserOrder>>findByShopAndCreatedAtBetween(Shop shop,LocalDateTime st,LocalDateTime end);
    Optional<List<UserOrder>>findByShopAndPaymentStatusAndEnrollSettleShopAndOrderStatus(Shop shop ,PaymentStatus paymentStatus,boolean enloal,OrderStatus orderStatus);

    //페이징
    Page<UserOrder> findByShopAndOrderStatus(Pageable page,Shop shop,OrderStatus od);

    //회원탈퇴 확인
    Optional<List<UserOrder>> findByUserIdAndOrderStatus(User user,OrderStatus orderStatus);
    //n일 이전에 배송중인 상품을 찾느낟
//    Page<UserOrder> findOrderStatusAndShippedAtBefore(OrderStatus od,LocalDateTime ld);


    //정산서에 포함된거 찾지
    Optional<List<UserOrder>> findBySettleShop(SettleShop settleShop);

}
