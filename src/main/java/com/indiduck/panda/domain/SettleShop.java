package com.indiduck.panda.domain;


import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter

public class SettleShop {
    @Id
    @GeneratedValue
    private Long id;

    //정산등록된날자
    LocalDateTime enrollSettle;
    //실제 정산된 날자
    LocalDateTime depositDate;
    //정산될 금액
    int depoist;
    //정산된 userOrder
    @OneToMany(mappedBy = "settleShop")
    private List<UserOrder> userOrder= new ArrayList<>();

    //정산될 샵
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;
    //입금했는지여부
    boolean isDeposit;
    //생성메서드
    public static SettleShop createSettleShop(List<UserOrder> us,Shop shop)
    {
        SettleShop settle= new SettleShop();
        settle.enrollSettle=LocalDateTime.now();
        int depoistmoney=0;
        settle.userOrder=us;
        for (UserOrder u : us) {
            u.setEnrollRefundShop(true,settle);
            u.confirmOrder();
            depoistmoney+=u.getShopMoney();
            u.setExpectCalculate();
        }
        settle.shop=shop;
        settle.depoist=depoistmoney;
        settle.isDeposit=false;
        return settle;

    }
    public void setDepositMoney(int money)
    {
        this.depoist=money;
    }

    public void depoist()
    {
        this.isDeposit=true;
        this.depositDate=LocalDateTime.now();
        for (UserOrder order : userOrder) {
            order.settlefinishShop();
        }
    }

}
