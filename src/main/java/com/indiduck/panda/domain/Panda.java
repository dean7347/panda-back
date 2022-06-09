package com.indiduck.panda.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Panda {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private User user;

    private String pandaName;
    private String mainCh;
    @Lob
    private String intCategory;

    private boolean Terms;
    private boolean info;

    private boolean recognize;

    //예상된 획득 수수료
    private int expectedFees;
    //출금 가능 수수료
    private int possibleFees;

    @OneToMany(mappedBy = "panda")
    private List<PandaToProduct> pandaToproducts= new ArrayList<>();

    @OneToMany(mappedBy = "panda")
    private List<OrderDetail> orderDetailPandas= new ArrayList<>();
    @OneToMany(mappedBy = "panda")
    private List<SettlePanda> settlePandas;

    //==생성메서드==//
    public static Panda newPanda(String pandaName,String mainCh,String intCategory,boolean T,boolean I){
        Panda panda= new Panda();
        panda.pandaName=pandaName;
        panda.mainCh=mainCh;
        panda.intCategory=intCategory;
        panda.Terms=T;
        panda.info=I;
        panda.recognize=false;
        return panda;

    }

    //==연관관계메서드 ==//
    public void setUser(User user)
    {
        this.user=user;
        user.setPanda(this);
    }
    public void confrimPanda()
    {
        this.recognize=true;
    }
    public void deleteOrderdetail(OrderDetail od)
    {
        orderDetailPandas.remove(od);
    }

}





