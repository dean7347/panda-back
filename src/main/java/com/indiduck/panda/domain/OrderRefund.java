package com.indiduck.panda.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class OrderRefund {

    @Id
    @GeneratedValue
    private Long id;

    private Long orderDetailNumber;
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderDetail orderDetails;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}
