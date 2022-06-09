package com.indiduck.panda.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class RefundRequest {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    private String refundMessage;



    @ManyToOne(fetch = FetchType.LAZY)
    private UserOrder userOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus od;

    //생성일(환불신청일)
    private LocalDateTime createdAt;

    //판매자가 확인한날
    private LocalDateTime checkedAt;
    //환불 완료된날
    private LocalDateTime finishAt;
    //최종환불된 금액
    private long refundMoney;


    //==생성메서드==//
    public static RefundRequest newRefundRequest(UserOrder userOrder,String message,User user){
        RefundRequest rr = new RefundRequest();
        rr.user=user;
        rr.createdAt=LocalDateTime.now();
        rr.refundMessage=message;
        rr.shop=userOrder.getShop();
        rr.userOrder=userOrder;
        rr.refundMoney=0;

        return rr;





    }
    //비즈니스 메서드

    public void setUserOrder(UserOrder uo)
    {
        this.userOrder=uo;
    }
    public void setRefundMoney(long money)
    {
        this.refundMoney=money;
    }
    public void confirmRefund(long money)
    {
        this.refundMoney=money;
        this.od=OrderStatus.환불완료;
        this.finishAt=LocalDateTime.now();
    }
    public void rejectTrade()
    {
        this.finishAt=LocalDateTime.now();
        this.od=OrderStatus.거절;
    }
    public void confrimTrade()
    {
        this.finishAt=LocalDateTime.now();
        this.od=OrderStatus.교환완료;

    }








}
