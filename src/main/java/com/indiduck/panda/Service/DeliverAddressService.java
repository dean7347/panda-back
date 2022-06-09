package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.DeliverAddressRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.domain.DeliverAddress;
import com.indiduck.panda.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliverAddressService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    DeliverAddressRepository deliverAddressRepository;
    public DeliverAddress newAddress(String user, String receiver,
                                     String addressName, String phonenumb, String phonenummiddle,
                                     String phonenumlast, String subphonenum, String subphonenummiddle, String subphonenumlast,
                                     String zonecode, String fulladdress, String addressdetail){

        Optional<User> getUser = userRepository.findByEmail(user);
        DeliverAddress da = DeliverAddress.newAddress(getUser.get(),receiver,addressName,phonenumb,phonenummiddle,phonenumlast,subphonenum
        ,subphonenummiddle,subphonenumlast,zonecode,fulladdress,addressdetail);
        DeliverAddress save = deliverAddressRepository.save(da);

        return save;


    }

    public void userAddressDeleteAll(User user)
    {

    }
}
