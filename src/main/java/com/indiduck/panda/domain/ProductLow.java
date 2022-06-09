package com.indiduck.panda.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class ProductLow {


    @Id
    @GeneratedValue
    private Long id;

    private String LowName;
    @Lob
    private String LowValue;


    @ManyToOne
    private Product product;


}
