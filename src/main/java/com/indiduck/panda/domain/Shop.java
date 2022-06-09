package com.indiduck.panda.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter @Setter
public class Shop {

    @Id
    @GeneratedValue
    private Long id;
    //shopName
    private String shopName;
    //representative
    private String representative;
    //crn
    private String CRN;

    //telnum
    private String number;

    //freepee
    private int freePrice;

    //nofree 택배비용
    private int nofree;
    //priPhone
    private String priPhone;
 //csPhone
    private String csPhone;

    //csTime
    @Lob
    private String csTime;

    //toPanda
    @Lob
    private String toPanda;

    //reship
    private String reship;

    //returnpee
    private int returnpee;

    //tradepee
    private int tradepee;
    //comaddress
    private String comaddress;

    //returnaddress
    private String returnaddress;

    //candate
    @Lob
    private String candate;

    //noreturn
    @Lob
    private String noreturn;

    //Termsagree
    private boolean Termsagree;

    //Infoagree
    private boolean Infoagree;

    //승인받았는지에 대한 내역
    private boolean isApprove;
    //샵이 운영중인지에 대한 내역
    private boolean isOpen;
    //평균배송기간
    private String AVDtime;

    //취급상품
    @Lob
    private String descriptionProduct;




    //예상된 획득 수수료
    int expectedFees;
    //정산 가능 수수료
    int possibleFees;


    @OneToMany(mappedBy = "shop")
    private List<Product> products;

    @OneToMany(mappedBy = "shop")
    private List<OrderDetail> details;

    @OneToMany(mappedBy = "shop")
    private List<UserOrder> userOrders;

    @OneToOne(optional = true,mappedBy = "shop")
    private User user;
    @OneToMany(mappedBy = "shop")
    private List<SettleShop> settleShop;

    @OneToMany(mappedBy = "shop")
    private List<RefundRequest> refundRequestlist;

    //== 연관관계메서드 ==//
    public void setUser(User user){
        this.user = user;
        user.setShop(this);
    }

    //==생성메서드 ==//
    public static Shop createShop(User user,String shopName, String representative,String crn, String telnum, int freepee, int nofree,
                                  String priPhone, String csPhone, String csTime, String toPanda, String reship,
                                  int returnpee, int tradepee, String returnaddress, String candate,
                                  String noreturn, boolean Termsagree, boolean Infoagree,String comaddress,String avdtime,String descriptionProduct){
        Shop shop = new Shop();
        shop.setUser(user);
        shop.shopName=shopName;
        shop.representative=representative;
        shop.CRN=crn;
        shop.number=telnum;
        shop.freePrice=freepee;
        shop.nofree=nofree;
        shop.priPhone=priPhone;
        shop.csPhone=csPhone;
        shop.csTime=csTime;
        shop.toPanda=toPanda;
        shop.reship=reship;
        shop.returnpee=returnpee;
        shop.tradepee=tradepee;
        shop.returnaddress=returnaddress;
        shop.candate=candate;
        shop.noreturn=noreturn;
        shop.Termsagree=Termsagree;
        shop.Infoagree=Infoagree;
        shop.isApprove=false;
        shop.isOpen=false;
        shop.comaddress=comaddress;
        shop.AVDtime=avdtime;
        shop.descriptionProduct=descriptionProduct;

        return shop;
    }

    //==비즈니스 로직==//

    public void confirmShop()
    {
        this.isApprove=true;
        this.isOpen=true;
    }
    public void SettleShopLogic(){

        for (UserOrder userOrder : userOrders) {

        }

    }


}
