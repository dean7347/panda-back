package com.indiduck.panda.domain;


import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class SettlePanda {
    @Id
    @GeneratedValue
    private Long id;

    //정산등록된날자 -> 정산 예정일로 변경
    LocalDateTime enrollSettle;
    //실제 정산된 날자
    LocalDateTime depositDate;
    //정산된 금액
    int depoist;
    //정산된 userOrder
    @OneToMany(mappedBy = "settlePanda")
    private List<OrderDetail> orderDetails= new ArrayList<>();
    //정산될 판다
    @ManyToOne(fetch = FetchType.LAZY)
    private Panda panda;
    //입금했는지여부
    boolean isDeposit;

    public static SettlePanda createSettlePanda(List<OrderDetail> od,Panda panda)
    {
        SettlePanda settle= new SettlePanda();
        LocalDateTime now= LocalDateTime.now();
        //월요일 1 화요일 2 수요일 3 목요일 4 금요일 5 토요일 6 일요일 7
        //오늘이 일요일이면 1 더하고 7+1
        //오늘이 토요일이면 2더함 6+2
        //8빼기 오늘
        int value = now.getDayOfWeek().getValue();
        LocalDateTime localDateTime = now.plusDays(8 - value);
        settle.enrollSettle=LocalDateTime.of(localDateTime.getYear(),localDateTime.getMonth(),localDateTime.getDayOfMonth(),0,0);
//        settle.enrollSettle=LocalDateTime.now();
        int depoistmoney=0;
        for (OrderDetail orderDetail : od) {
            depoistmoney+=orderDetail.getPandaMoney();
            orderDetail.setEnrollRefundPanda(true,settle);
        }
        settle.panda=panda;
        settle.depoist=depoistmoney;
        settle.orderDetails=od;
        settle.isDeposit=false;
        return settle;

    }
    public void deposit()
    {
        this.isDeposit=true;
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.finishSettler();
        }
    }

}


