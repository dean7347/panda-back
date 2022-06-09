package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ProductOption {

    @Id @GeneratedValue
    private Long id;

    private String optionName;
    private int optionStock;
    private int optionPrice;
    private boolean sales;

    @ManyToOne
    private Product product;

    @OneToMany(mappedBy = "options")
    private List<OrderDetail> orderDetail=new ArrayList<>();

    //생성메서드
    public ProductOption newProductOption(String name,int stock,int price){
        ProductOption newOption =new ProductOption();
        newOption.optionName=name;
        newOption.optionStock=stock;
        newOption.optionPrice=price;
        newOption.sales=true;
        return newOption;
    }

    //비즈니스 메서드
    public void optionDel()
    {
        this.sales=false;


    }
    public void edit(String name, int count, int price)
    {
        this.optionName=name;
        this.optionStock=count;
        this.optionPrice=price;
    }

    //구매 완료된것을 빼준다
    public int minusOption(int count)
    {
        this.optionStock-=count;
        return optionStock;
    }



}
