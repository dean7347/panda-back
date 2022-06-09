package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
public class PandaToProduct {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Panda panda;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private String link;
    boolean isDel;
    LocalDateTime createAt;
    LocalDateTime editAt;
    LocalDateTime delAt;

    //==생성메서드==//
    public static PandaToProduct newPandaToProduct(String link)
    {
        PandaToProduct ptp = new PandaToProduct();
        ptp.link=link;
        ptp.createAt=LocalDateTime.now();
        ptp.isDel=false;
        return ptp;
    }

    //연관관계메서드
    public void setPanda(Panda panda)
    {
        this.panda=panda;
        panda.getPandaToproducts().add(this);

    }

    public void setProduct(Product product)
    {
        this.product=product;
        product.getPandas().add(this);
    }
    public void changeURL(String url)
    {
        this.link=url;
        this.editAt=LocalDateTime.now();
    }
    public void delURl()
    {
        this.delAt=LocalDateTime.now();
        this.isDel=true;
        this.product.getPandas().remove(this);
    }

}


