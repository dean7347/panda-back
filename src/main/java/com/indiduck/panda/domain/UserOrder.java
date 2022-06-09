package com.indiduck.panda.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class UserOrder {
    @Id
    @GeneratedValue
    private long id;

    private long orderDetailId;

    //주문자
    @ManyToOne(fetch = FetchType.LAZY)
    private User userId;
    //상점
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;

    //택배비가 포함된 금액을 말합니다
    int fullprice = 0;

    //택배비가 포함되지 않은 판다할인 적용 결제금액을 말합니다.
    int amount = 0;
    //판다금액과 택배비가 없는 가격
    int PureAmount = 0;
    //무료배송 금액(이상 무료)
    int freeprice = 0;
    //택배비
    int shipPrice = 0;

    //연장가능 횟수 1회연장당 7일
    int canExtend = 3;

    //판매일자
    private LocalDateTime createdAt;
    private LocalDateTime checkedAt;
    //배송일자 + 10일뒤에 구매확정
    private LocalDateTime shippedAt;
    private LocalDateTime refundAt;
    private LocalDateTime arriveAt;
    //구매확정일자
    private LocalDateTime finishAt;
    //입금예정일
    private LocalDateTime expectCalculate;
    //입금 완료일
    private LocalDateTime depositCompleted;
    //구매확정 기준일
    private LocalDateTime standardfinishAt;

    private String mid;

    private String reveiverName;
    private String receiverPhone;
    private String receiverZipCode;
    private String receiverAddress;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "userOrder")
    private List<OrderDetail> detail = new ArrayList<>();

    @OneToMany(mappedBy = "userOrder")
    private List<RefundRequest> refundRequest = new ArrayList<>();
//    @ManyToOne
//    @OneToOne
//    private SettleShop settleShop;
//    @OneToOne
//    private SettlePanda settlePanda;

    //택배 송장 등록시 작성되는칼럼
    //택배사
    private String courierCom;
    //운송장 번호
    private String waybillNumber;
    //배송메모
    private String memo;
    //결제정보 저장을 위한칼람
    //uid가 중복되서 저장될 수 있으므로 주의할것
    //특히 전체 취소시 결제 금액만을 취소해줘야 한다
    // mid와 겹치는 칼럼 일단 공백
    private String uid;
    //영수증 주소
    private String receiptUrl;
    //환불 사유
    private String refundMessage;

    //샵이 가져갈 돈
    int shopMoney = 0;

    //쇼핑호스트 판다가 가져갈 돈
    int hostMoney = 0;

    //이 주문에서 판다에게 지급된 돈
    int pandaMoney = 0;

    //버림했기때문에 남은돈이 발생한다
    int balance = 0;
    //환불된 금액을 표기한다
    int finalRefundMoney = 0;
    //지급 상태 샵에대해서
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    //지급 상태 판다에 대해서
    @Enumerated(EnumType.STRING)
    private PaymentStatus pandaPaymentStatus;

    //지급목록 등록여부
    boolean enrollSettlePanda;
    boolean enrollSettleShop;

    @ManyToOne(fetch = FetchType.LAZY)
    private SettleShop settleShop;

    //취소로 인해 환불됐어야 ㅎ ㅏㄹ 돈
    int cancelMoney = 0;


    //==생성메서드==//
    public static UserOrder newUserOrder(User user, Shop shop, String mid,
                                         String name, String phoneNumber, String zipCode, String Address, String receipt, String memo) {
//        System.out.println("생성메서드 네임"+name);
        UserOrder uo = new UserOrder();
        uo.setUser(user);
        uo.setShop(shop);
        uo.freeprice = shop.getFreePrice();
        uo.shipPrice = shop.getNofree();
        uo.createdAt = LocalDateTime.now();
        uo.mid = mid;
        uo.reveiverName = name;
        uo.receiverPhone = phoneNumber;
        uo.receiverZipCode = zipCode;
        uo.receiverAddress = Address;
        uo.orderStatus = OrderStatus.결제완료;
        uo.receiptUrl = receipt;
        uo.memo = memo;
        uo.canExtend = 3;
        uo.enrollSettlePanda = false;
        uo.enrollSettleShop = false;

        return uo;
    }

    //==연관관계 메서드 ==//


    public void setUser(User user) {
        this.userId = user;
        user.getUserOrders().add(this);
    }

    public void setShop(Shop shop) {
        this.shop = shop;
        shop.getUserOrders().add(this);
    }

    public void setDetail(OrderDetail orderDetail) {
        this.detail.add(orderDetail);
        orderDetail.setUserOrder(this);
        if (orderDetail.getPanda() != null) {
            this.amount += orderDetail.getIndividualPrice()*orderDetail.getProductCount();
            this.PureAmount += orderDetail.getTotalPrice();

        } else {
            this.amount += orderDetail.getIndividualPrice()*orderDetail.getProductCount();
            this.PureAmount += orderDetail.getTotalPrice();
        }

    }

    //==비지니스 메서드 ==//

    //정산메서드. 오더 디테일이 바뀌면 실행해주자
    //구매확정시 -> 주문취소가 아닌것들 가격 계산해서
    // 상품가격75%+택배비를 샵머니에 넣고
    // 호스트 머니에는 상품가격의 25%에서 판다가격을 뺀 가격을 넣는다

//    public void settle() {
//        int pandamoney = 0;
//        for (OrderDetail orderDetail : detail) {
//            if (orderDetail.getOrderStatus() != OrderStatus.주문취소)
//                pandamoney += orderDetail.getPandaMoney();
//        }
//        this.hostMoney = (int) Math.floor((this.PureAmount  * 0.25) - pandamoney - (this.PureAmount - this.amount));
//        this.shopMoney = (int) Math.floor((this.PureAmount ) * 0.75);
//        this.pandaMoney = (int) Math.floor(pandamoney);
//        if (this.PureAmount < this.freeprice) {
//            this.shopMoney += this.shipPrice;
//        }
//
//        this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;
//
//
//    }

    public void setReceiptUrl(String url) {
        this.receiptUrl = url;
    }

    public void reCalAmount() {

    }

    //구매확정기간 연장
    public boolean extendConfirm() {
        if (this.canExtend <= 0 || this.orderStatus != OrderStatus.발송중) {

            return false;
        } else {
            this.standardfinishAt = standardfinishAt.plusDays(7);
            this.canExtend -= 1;
            return true;
        }

    }

    //택배비를 포함한 최종금액을 리턴한다
    public long finalPrice() {
        long fp = 0;
        if (this.freeprice >= this.PureAmount) {
            fp = this.PureAmount;
        } else {
            fp = this.PureAmount + this.shipPrice;
        }
        return fp;
    }

    public void batchConfirm(LocalDateTime ldt) {
        this.paymentStatus=PaymentStatus.지급예정;
        this.orderStatus = OrderStatus.구매확정;
        for (OrderDetail orderDetail : detail) {
            orderDetail.setOrderStatus(OrderStatus.구매확정);
        }
        this.finishAt = ldt;
    }

    public void settlefinishShop() {
        this.paymentStatus = PaymentStatus.지급완료;
    }

    public void batchTest(LocalDateTime ldt) {
        this.arriveAt = ldt;
    }

    public void Calculate() {
        if (this.PureAmount >= this.freeprice) {
            this.fullprice = this.amount;
        } else {
            this.fullprice = this.amount + this.shipPrice;

        }

    }


    //스테이터스의 변경과 작업
    //주문취소
    public void cancelOrder() {
        for (OrderDetail orderDetail : detail) {
            orderDetail.setOrderStatus(OrderStatus.주문취소);
        }
        this.orderStatus = OrderStatus.주문취소;

    }

    //결제완료 -> 준비중
    public void readyOrder() {
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소) {
                orderDetail.setOrderStatus(OrderStatus.준비중);
                orderDetail.setPaymentM(PaymentStatus.지급예정);
            }
        }
        this.checkedAt = LocalDateTime.now();
        this.orderStatus = OrderStatus.준비중;
        this.paymentStatus = PaymentStatus.결제완료;
//        this.settle();
    }

    //준비중 -> 발송중
    public void sendOutOrder(String com, String num) {
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소) {
                orderDetail.setOrderStatus(OrderStatus.발송중);
            }
        }
        this.orderStatus = OrderStatus.발송중;
        this.courierCom = com;
        this.waybillNumber = num;
        this.shippedAt = LocalDateTime.now();
        this.standardfinishAt = LocalDateTime.now();

    }

    // xxx -> 구매확정
    public void confirmOrder() {
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소) {
                orderDetail.setOrderStatus(OrderStatus.구매확정);
                orderDetail.setPaymentM(PaymentStatus.지급예정);
            }
        }
        this.finishAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.지급예정;
        this.orderStatus = OrderStatus.구매확정;

        int pandamoney = 0;
        int isfreemoney = 0;
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소)
                pandamoney += orderDetail.getPandaMoney();
            isfreemoney += orderDetail.getOriginOrderMoney();
        }
        this.hostMoney = (int) Math.floor((this.PureAmount) * 0.25) - pandamoney - (this.PureAmount - this.amount);
        this.shopMoney = (int) Math.floor((this.PureAmount) * 0.75);
        this.pandaMoney = (int) Math.floor(pandamoney);
//        if(this.PureAmount <this.freeprice)
//        {
//            this.shopMoney+=this.shipPrice;
//        }
        //유료배송의경우
        if (isfreemoney < this.freeprice) {
            this.shopMoney += this.shipPrice;

            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        } else {
            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        }

    }
    // xxx -> 결제취소 ?

    //    // xxx -> 환불신청 개별환불과 전체환불 구분할것것
    public void refundOrder(String message) {
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소 && orderDetail.getOrderStatus() != OrderStatus.구매확정) {
                orderDetail.setOrderStatus(OrderStatus.환불대기);
            }
        }
        this.refundMessage = message;
        this.orderStatus = OrderStatus.환불대기;
        this.refundAt = LocalDateTime.now();
    }

    //환불신청을확인함
    public void checkRefund() {
        this.orderStatus = OrderStatus.상점확인중;
    }

    public void setRefund(RefundRequest refundRequests) {

        this.orderStatus = OrderStatus.환불대기;
        this.refundRequest.add(refundRequests);
        refundRequests.setUserOrder(this);


    }

    //교환환불 거절
    public void rejectRefund() {
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소 && orderDetail.getOrderStatus() != OrderStatus.구매확정) {
                orderDetail.setOrderStatus(OrderStatus.발송중);
            }
        }
        this.refundMessage = "환불/교환 거절";
        this.orderStatus = OrderStatus.발송중;
        this.paymentStatus = PaymentStatus.지급예정;

        this.standardfinishAt = LocalDateTime.now().minusDays(7);
        this.refundAt = LocalDateTime.now();
    }

    public void setEnrollRefundShop(boolean tf, SettleShop shop) {
        this.enrollSettleShop = tf;
        this.settleShop = shop;
    }
    //취소이후
    public void confirmCancelMoney(int money) {
        this.finalRefundMoney += (int) money;
        int pandamoney = 0;
        int isfreemoney = 0;
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소)
                pandamoney += orderDetail.getPandaMoney();
            isfreemoney += orderDetail.getOriginOrderMoney();
        }
        this.hostMoney = (int) Math.floor((this.PureAmount) * 0.25) - pandamoney - (this.PureAmount - this.amount);
        this.shopMoney = (int) Math.floor((this.PureAmount) * 0.75);
        this.pandaMoney = (int) Math.floor(pandamoney);
//        if(this.PureAmount <this.freeprice)
//        {
//            this.shopMoney+=this.shipPrice;
//        }
        //유료배송의경우
        if (isfreemoney < this.freeprice) {
            this.shopMoney += this.shipPrice;

            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        } else {
            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        }

        this.orderStatus = OrderStatus.준비중;
    }

    //환불이후

    public void confirmRefundMoney(long money) {
        this.finalRefundMoney += (int) money;
        int pandamoney = 0;
        int isfreemoney = 0;
        for (OrderDetail orderDetail : detail) {
            if (orderDetail.getOrderStatus() != OrderStatus.주문취소)
                pandamoney += orderDetail.getPandaMoney();
            isfreemoney += orderDetail.getOriginOrderMoney();
        }
        this.hostMoney = (int) Math.floor((this.PureAmount) * 0.25) - pandamoney - (this.PureAmount - this.amount);
        this.shopMoney = (int) Math.floor((this.PureAmount) * 0.75);
        this.pandaMoney = (int) Math.floor(pandamoney);
//        if(this.PureAmount <this.freeprice)
//        {
//            this.shopMoney+=this.shipPrice;
//        }
        //유료배송의경우
        if (isfreemoney < this.freeprice) {
            this.shopMoney += this.shipPrice;

            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        } else {
            this.balance = this.fullprice - hostMoney - shopMoney - pandaMoney;

        }

        this.orderStatus = OrderStatus.발송중;
        this.paymentStatus = PaymentStatus.지급예정;
        this.standardfinishAt = LocalDateTime.now().minusDays(7);
    }

    // 회원 탈퇴/ 기록 삭제시시 바꿔줄것
    public void resignUser() {
        this.receiverAddress = "조회 불가능한 회원입니다";
        this.receiverPhone = "조회 불가능한 회원입니다";
        this.receiverZipCode = "조회 불가능한 회원입니다";
        this.reveiverName = "조회 불가능한 회원입니다";

    }

    //교환후
    public void confirmUserOrderTrade() {
        this.orderStatus = OrderStatus.발송중;
        this.paymentStatus = PaymentStatus.지급예정;

        this.standardfinishAt = LocalDateTime.now().minusDays(7);
    }

    //환불  취소시 전체 금액에서 빼주는 로직
    public void refundAndCancelMinusPrice(int refundPuerAmount, int realminusPrice) {

        //퓨어어마운트 순수금액
        //풀프라이스 택배비 포함금액
        //어마운트 실제 결제된금액
        this.PureAmount -= refundPuerAmount;
        this.fullprice -= realminusPrice;
        this.amount -= realminusPrice;


    }
    public void setDepoistCompleted()
    {
        this.depositCompleted=LocalDateTime.now();
    }

    public void setExpectCalculate() {
        LocalDateTime now= LocalDateTime.now();
        //월요일 1 화요일 2 수요일 3 목요일 4 금요일 5 토요일 6 일요일 7
        //오늘이 일요일이면 1 더하고 7+1
        //오늘이 토요일이면 2더함 6+2
        //8빼기 오늘
        int value = now.getDayOfWeek().getValue();
        LocalDateTime localDateTime = now.plusDays(8 - value);
        this.expectCalculate=LocalDateTime.of(localDateTime.getYear(),localDateTime.getMonth(),localDateTime.getDayOfMonth(),0,0);
    }

    public int shipPriceCalculation()
    {
        int originMoney=0;
        for (OrderDetail orderDetail : detail) {
            originMoney+=orderDetail.getOriginOrderMoney();
        }
        if(this.shop.getFreePrice()>originMoney)
        {
            return this.shop.getNofree();
        }
        return 0;
    }

    public void cancelMoneyJob() {
        this.finalRefundMoney=this.fullprice;
        this.fullprice=0;
        this.PureAmount=0;
        this.amount=0;
    }


//    @ManyToOne(fetch = FetchType.LAZY)
//    private OrderDetail orderDetails;
}
