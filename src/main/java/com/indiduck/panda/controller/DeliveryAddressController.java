package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.DeliverAddressRepository;

import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.DeliverAddressService;

import com.indiduck.panda.domain.DeliverAddress;
import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class DeliveryAddressController {

    @Autowired
    DeliverAddressRepository deliverAddressRepository;
    @Autowired
    DeliverAddressService deliverAddressService;
    @Autowired
    UserRepository userRepository;

   //주소 추가하기
    @RequestMapping(value = "/api/addaddress", method = RequestMethod.POST)
    public ResponseEntity<?> addAddress(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody AddAddressDAO addaddressDAO) throws Exception {


//        System.out.println(addaddressDAO);
        DeliverAddress deliverAddress = deliverAddressService.newAddress(authentication.getName(), addaddressDAO.receiver, addaddressDAO.addressName, addaddressDAO.phonenumb
                , addaddressDAO.phonenummiddle, addaddressDAO.phonenumlast, addaddressDAO.subphonenum, addaddressDAO.subphonenummiddle, addaddressDAO.subphonenumlast,
                addaddressDAO.zonecode, addaddressDAO.fulladdress, addaddressDAO.addressdetail);


        return ResponseEntity.ok(new AddressResultDto(true,"주소생성성공"));
    }


    //주소 조회하기
    @RequestMapping(value = "/api/myaddress", method = RequestMethod.GET)
    public ResponseEntity<?> addressList(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication) throws Exception {

        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Optional<List<DeliverAddress>> deliverAddressByUserName = deliverAddressRepository.findDeliverAddressByUserName(byEmail.get());
        System.out.println("deliverAddressByUserName = " + deliverAddressByUserName);
        if(deliverAddressByUserName.isEmpty())
        {
            return ResponseEntity.ok(new AddressListDto(true,null,byEmail.get().getUserRName(),byEmail.get().getRecentAddress(),
                    byEmail.get().getUserPhoneNumber()));

        }
        else
        {
            List<DeliverAddress> deliverAddresses = deliverAddressByUserName.get();
            return ResponseEntity.ok(new AddressListDto(true,deliverAddresses,byEmail.get().getUserRName(),byEmail.get().getRecentAddress()
                    ,byEmail.get().getUserPhoneNumber()));
        }

    }

    //주소 삭제하기
    @RequestMapping(value = "/api/deleteaddr", method = RequestMethod.POST)
    public ResponseEntity<?> removeList(@CurrentSecurityContext(expression = "authentication")
                                                 Authentication authentication,@RequestBody DeleteAddrDAO deleteAddrDAO) throws Exception {

        String name = authentication.getName();
//        System.out.println(deleteAddrDAO);
        Optional<DeliverAddress> byId = deliverAddressRepository.findById(deleteAddrDAO.deleteid);
        Optional<User> byEmail = userRepository.findByEmail(name);
        if(byId.get().getUserName().getEmail()==name)
        {
            byId.get().delete(byEmail.get());
            deliverAddressRepository.delete(byId.get());
            return ResponseEntity.ok(new AddressResultDto(true,"성공적으로 삭제헀습니다"));

        }
        return ResponseEntity.ok(new AddressResultDto(false,"삭제에 실패했습니다"));







    }
    @Data
    static class AddAddressDAO {
        String receiver;
        String addressName;
        String phonenumb;
        String phonenummiddle;
        String phonenumlast;
        String subphonenum;
        String subphonenummiddle;
        String subphonenumlast;
        String zonecode;
        String fulladdress;
        String addressdetail;

    }

    @Data
    static class AddressResultDto {
        boolean success;
        String message;


        public AddressResultDto(boolean b,String me) {
            success = b;
            message = me;
        }
    }

    @Data
    private class AddressListDto {
        List<ListDeliverAddress> list=new ArrayList<>();
        String name;
        Long recent;
        String phoneNumb;
        public AddressListDto(boolean b, List<DeliverAddress> address,String name, Long recent,String phone) {
            for (DeliverAddress deliverAddress : address) {
                list.add(new ListDeliverAddress(deliverAddress));
            }
            this.name =name;
            this.phoneNumb=phone;
            if(recent==null)
            {
                this.recent=null;
            }else{
                this.recent=recent;
            }
        }

    }
    @Data
    static class ListDeliverAddress {
        Long id;
        String receiver;
        String addressName;
        String mainPhoneNumber;
        String subPhoneNumber;

        String zonecode;
        String fulladdress;
        String addressdetail;
        public ListDeliverAddress(DeliverAddress da)
        {
            id=da.getId();
            receiver=da.getReceiver();
            addressName=da.getAddressName();
            mainPhoneNumber=da.getMainPhoneNumber();
            subPhoneNumber=da.getSubPhoneNumber();
            zonecode=da.getZipcode();
            fulladdress=da.getFulladdress();
            addressdetail=da.getAddressdetail();

        }

    }
    @Data
    static class DeleteAddrDAO {
        Long deleteid;
    }
}
