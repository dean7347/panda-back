package com.indiduck.panda.domain;


import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
public class Calculate {

    @Id
    @GeneratedValue
    private long id;



}
