package com.indiduck.panda.domain;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class ProductType {
    @Id
    @GeneratedValue
    private Long id;
    private String typeName;
    private int typeStock;
}
