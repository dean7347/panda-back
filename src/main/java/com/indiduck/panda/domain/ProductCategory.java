package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
public class ProductCategory {

    @Id
    @GeneratedValue
    @Column(name = "Product_Category_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = LAZY)
    private Product products;

//    @ManyToOne(fetch = LAZY)
//    private Category category;


}
