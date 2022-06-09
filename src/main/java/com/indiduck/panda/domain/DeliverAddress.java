package com.indiduck.panda.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class DeliverAddress {

    @Id
    @GeneratedValue
    private long id;

    private String receiver;
    private String addressName;
    private String mainPhoneNumber;
    private String subPhoneNumber;
    private String zipcode;
    private String fulladdress;
    private String addressdetail;

    @ManyToOne(fetch = FetchType.LAZY)
    private User userName;

    //==생성메서드 ==//
    public static DeliverAddress newAddress(User user, String receiver,
                                            String addressName, String phonenumb, String phonenummiddle,
                                            String phonenumlast, String subphonenum, String subphonenummiddle, String subphonenumlast,
                                            String zonecode, String fulladdress, String addressdetail)
    {
        DeliverAddress da = new DeliverAddress();
        da.receiver=receiver;
        da.addressName=addressName;
        da.mainPhoneNumber=phonenumb+phonenummiddle+phonenumlast;
        da.subPhoneNumber=subphonenum+subphonenummiddle+subphonenumlast;
        da.zipcode=zonecode;
        da.fulladdress=fulladdress;
        da.addressdetail=addressdetail;
        da.setUser(user);
        return  da;

    }

    //==연관관계메서드 ==//
    private void setUser(User user)
    {
        this.userName=user;
        user.getUserAddress().add(this);
    }
    public void delete(User user)
    {
        user.getUserAddress().remove(this);
    }

}
