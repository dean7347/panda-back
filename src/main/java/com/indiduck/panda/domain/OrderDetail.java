package com.indiduck.panda.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class OrderDetail {

    @Id
    @GeneratedValue
    private long id;
    //상품갯수
    private int productCount;
    //이 상품이 담길때의 가격
    private int IndividualPrice;
    //이상품의 현재 총 가격(할인 전)
    private int totalPrice;
    //오더 디테일 생성 당시의  개당 옵션 가격
    private int createAtPrice;
    private LocalDateTime paymentAt;
    private LocalDateTime createdAt;
    private LocalDateTime checkedAt;
    private LocalDateTime shippingAt;
    private LocalDateTime finishedAt;

    //지급된 날자
    private LocalDateTime paidAt;

    //환불신청에 관한칼럼
    //환불신청 갯수
    private int reqRefund = 0;
    //최종 환불완료된 갯수
    private int confirmRefund = 0;
    //최종 교환된 갯수
    private int confirmTrade = 0;

    //판매자가 취소한 갯수수
    private int reqCancel = 0;
    //취소이유
    private String cancelReson;
    private LocalDateTime PartialCancelDate;


    @ManyToOne(fetch = FetchType.LAZY)
    private UserOrder userOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product products;

    //    @ManyToOne(fetch = FetchType.LAZY)
//    private UserOrder userOrder;
    @ManyToOne(fetch = FetchType.LAZY)
    private ProductOption options;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Panda panda;

    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    private RefundRequest refundRequest;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    boolean enrollSettle;

    @ManyToOne(fetch = FetchType.LAZY)
    private SettlePanda settlePanda;
    //    @ManyToOne(fetch = FetchType.LAZY)
//    private PartialCancellation partialCancellation;
    //10%
    private int pandaMoney = 0;


    //==생성메서드==//
    public static OrderDetail newOrderDetail(User user, Product product, ProductOption productOption, int optionCount, Panda panda) {
        OrderDetail od = new OrderDetail();
        od.setUser(user);
        od.setProduct(product);
        od.setProductOption(productOption);
        od.IndividualPrice = (int) Math.floor(productOption.getOptionPrice() * 0.95);
        od.totalPrice = productOption.getOptionPrice() * optionCount;
        od.productCount = optionCount;
        od.setPanda(panda);
        od.orderStatus = OrderStatus.결제대기;
        od.createdAt = LocalDateTime.now();
        od.setShop(product.getShop());
        od.paymentStatus = PaymentStatus.지급예정;
        int mo = (int) Math.floor(od.totalPrice );
        od.pandaMoney = (int) Math.floor(mo * 0.10);
        od.reqRefund = 0;
        od.confirmRefund = 0;
        od.enrollSettle = false;
        od.createAtPrice= productOption.getOptionPrice();

        return od;

    }

    //판다가 없을때
    public static OrderDetail newOrderDetail(User user, Product product, ProductOption productOption, int optionCount) {
        OrderDetail od = new OrderDetail();
        od.setUser(user);
        od.setProduct(product);
        od.setProductOption(productOption);
        od.IndividualPrice = productOption.getOptionPrice();
        od.totalPrice = productOption.getOptionPrice() * optionCount;
        od.productCount = optionCount;
        od.orderStatus = OrderStatus.결제대기;
        od.createdAt = LocalDateTime.now();
        od.setShop(product.getShop());
        od.pandaMoney = 0;
        od.reqRefund = 0;
        od.confirmRefund = 0;
        od.enrollSettle = false;
        od.createAtPrice= productOption.getOptionPrice();


        return od;

    }

    //==연관관계매서드==//
    public void setUser(User user) {
        this.user = user;
        user.getOrders().add(this);

    }

    private void setProduct(Product product) {
        this.products = product;
        product.getOrderDetails().add(this);
    }

    private void setProductOption(ProductOption productOption) {
        this.options = productOption;
        productOption.getOrderDetail().add(this);

    }

    public void deletePanda(Panda panda) {
        panda.deleteOrderdetail(this);

    }

    //판다수정
    public void setPanda(Panda panda) {
        //판다가 기존에 있으면
        if (panda != null) {
            panda.deleteOrderdetail(this);
        }

        this.panda = panda;
        panda.getOrderDetailPandas().add(this);
        this.IndividualPrice=(int) Math.floor(this.options.getOptionPrice()*0.95);
        this.pandaMoney =(int) Math.floor(this.totalPrice * 0.1);
    }

    public void setShop(Shop shop) {
        this.shop = shop;
        shop.getDetails().add(this);
    }

    public void setUserOrder(UserOrder userOrder) {
        this.userOrder = userOrder;
        this.orderStatus = OrderStatus.결제완료;
        this.paymentAt = LocalDateTime.now();
    }

    public void setRefundRequest(RefundRequest refundRequest) {
        this.refundRequest = refundRequest;
    }

    public void setPaymentM(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    //=비즈니스 매서드=//
    //숫자변경
    public void plusCount(int count) {
        this.productCount += count;
        this.totalPrice = this.getIndividualPrice() * this.productCount;
        if (panda != null) {
            int mo = (int) Math.floor(this.totalPrice );
            this.pandaMoney = (int) Math.floor(mo * 0.10);
        }
    }

    public void refundCount() {
        this.totalPrice = this.getIndividualPrice() * (this.productCount - this.confirmRefund);
        if (panda != null) {
            int mo = (int) Math.floor(this.totalPrice );
            this.pandaMoney = (int) Math.floor(mo * 0.10);
        }
    }

    //가격이 변경될경우
    public void update(int count) {
        this.productCount = count;
        this.totalPrice = this.getOptions().getOptionPrice() * this.productCount;
        if (panda != null) {
//            int mo = (int) Math.floor(this.totalPrice * 0.95);
            this.pandaMoney = (int) Math.floor(totalPrice*0.1);
        }


    }

    public void setOrderStatus(OrderStatus status) {
        //주문취소//준비중//발송중//구매확정
        if (status == OrderStatus.주문취소) {
            this.orderStatus=orderStatus.주문취소;

        }
        if (status == OrderStatus.준비중) {
            this.orderStatus=orderStatus.준비중;

            this.checkedAt = LocalDateTime.now();

        }
        if (status == OrderStatus.발송중) {
            this.orderStatus=orderStatus.발송중;

            this.shippingAt = LocalDateTime.now();
        }
        if (status == OrderStatus.구매확정) {
            this.finishedAt = LocalDateTime.now();
            this.orderStatus=orderStatus.구매확정;
            if (this.orderStatus != OrderStatus.주문취소) {
                this.paymentStatus = PaymentStatus.지급예정;
            }

        }
    }

    public void setOrderready(OrderStatus status) {
        this.orderStatus = status;
        this.checkedAt = LocalDateTime.now();
    }


    //주문전에만 작동해야한다
    public void editOption() {

        //판다가 없으면
        if(this.panda ==null)
        {
            this.IndividualPrice = this.getOptions().getOptionPrice();
            this.pandaMoney=0;
        }
        else
        {
            this.IndividualPrice =(int) Math.floor(this.getOptions().getOptionPrice()*0.95);
            this.pandaMoney=(int) Math.floor(this.getOptions().getOptionPrice()*0.1)*this.productCount;

        }

        this.createAtPrice=this.getOptions().getOptionPrice();
        this.totalPrice = this.createAtPrice* this.productCount;


    }

    public void reqRefund(int num) {
        this.reqRefund = num;
        this.orderStatus = OrderStatus.환불대기;
    }

    //    public void confrimRefund(int number)
//    {
//        this.confirmRefund=number;
//        if(panda!=null)
//        {
//            int mo = (int) Math.floor(this.IndividualPrice*(this.productCount-this.confirmRefund));
//            this.pandaMoney= (int) Math.floor(mo*0.12);
//        }
//        this.orderStatus=OrderStatus.환불완료;
//    }
    public void setEnrollRefundPanda(boolean tf, SettlePanda panda) {
        this.paymentStatus = PaymentStatus.지급대기;
        this.enrollSettle = tf;
        this.settlePanda = panda;
    }




    public void finishSettler() {
        this.paymentStatus = PaymentStatus.지급완료;
    }

    
    //부분취소시 uo에서 가격을 빼주는 로직
    public void partialCancel(int count, String message) {
        this.reqCancel += count;
        this.productCount -= count;
        this.cancelReson = message;
        this.PartialCancelDate = LocalDateTime.now();

        this.userOrder.refundAndCancelMinusPrice(this.createAtPrice*count,count*this.getIndividualPrice());
//        this.userOrder.confirmRefundMoney(this.getIndividualPrice() * count);
        this.totalPrice =this.createAtPrice * productCount;
//        System.out.println("productCount = " + productCount);

    }

    public void tradeConfirm(int count) {
        this.confirmTrade += count;
    }


    public void partialRefund(int count) {

        this.confirmRefund += count;
        this.productCount -= count;
        this.totalPrice = this.createAtPrice * productCount;
        this.pandaMoney=(int) Math.floor(this.totalPrice*0.1);
        //이것으로 환불된 퓨어 어마운트 금액
        this.userOrder.refundAndCancelMinusPrice(this.createAtPrice*count,count*this.getIndividualPrice());

        this.orderStatus = OrderStatus.환불완료;

    }


    public int getOriginOrderMoney() {

        return this.createAtPrice*(this.confirmRefund+this.reqCancel+productCount);
    }
}
