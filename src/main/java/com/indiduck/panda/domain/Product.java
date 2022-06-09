package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 상품 엔티티 설계
 */

@Entity
@EqualsAndHashCode(of = "id")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<File> images = new ArrayList<>();

    private String productName;
    private int productPrice;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String productDesc;
    private LocalDateTime productRegAt;
    private LocalDateTime productEditAt;
    private LocalDateTime productDeleteAt;

    //판매중지인지
    private boolean sales = true;
    //삭제인지
    private boolean deleted = false;

    private int productHits;

    //전자상거래등에서의상품등의정보제공에관한고시
    private int type;

    //고시값
    @Lob
    private String notice;
    @Lob
    private String noticeValue;
    @Lob
    private String pandaMessage;


//    @Lob
//    private String lowvalue;

    //엮인 게시글
    @OneToMany(mappedBy = "product")
    private List<Board> boards = new ArrayList<>();


    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "products")
    private List<ProductCategory> categorys = new ArrayList<>();

    @OneToMany(mappedBy = "products")
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<PandaToProduct> pandas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;

    //연관관계 메서드
    public void setName(String text) {
        this.productName = text;
    }

    public void setDesc(String text) {
        this.productDesc = text;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
        shop.getProducts().add(this);
    }

    public void setProductOptions(ProductOption po) {
        this.productOptions.add(po);
        po.setProduct(this);
    }

    public void setImage(File file) {
        this.images.add(file);
        file.setProduct(this);

    }

    public void setThumbImage(File file) {
        this.images.add(file);
        file.setProduct(this);
        file.setIsthumb(true);

    }

    //==생성==//
    public static Product newProDuct(String name, String desc, int type, String notice,String noticeValue,String panam) {
        Product pro = new Product();
        pro.productName = name;
        pro.productDesc = desc;
        pro.type = type;
        pro.notice=notice;
        pro.noticeValue=noticeValue;
        pro.productRegAt = LocalDateTime.now();
        pro.pandaMessage=panam;


        return pro;
    }

    public static Product copyPro(Product pro, int num) {
        Product copy = new Product();
        copy.images = pro.images;
        copy.productName = pro.getProductName() + num;
        copy.productPrice = pro.getProductPrice();
        copy.productDesc = pro.getProductDesc();
        copy.sales = pro.sales;
        copy.deleted = pro.deleted;
        copy.type = pro.type;

        copy.productOptions = pro.productOptions;
        copy.shop = pro.shop;

        return copy;
    }

    //==비즈니스==//
    public void delFile(File file) {
        images.remove(file);
    }


    public void changeType(int type) {
        this.type = type;
    }

    public void stopSale() {
        this.sales = false;
    }

    public void delProduct() {
        this.deleted = true;
        this.productDeleteAt = LocalDateTime.now();

    }

    public void restartSale() {
        this.sales = true;
    }


    //==조회==//


}
