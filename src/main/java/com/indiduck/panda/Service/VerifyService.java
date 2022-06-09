package com.indiduck.panda.Service;


import com.indiduck.panda.Repository.FileRepository;
import com.indiduck.panda.Repository.PandaToProductRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.controller.PandaToProductController;
import com.indiduck.panda.domain.File;
import com.indiduck.panda.domain.PandaToProduct;
import com.indiduck.panda.domain.User;
import com.indiduck.panda.domain.UserOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VerifyService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final PandaToProductRepository pandaToProductRepository;
    @Autowired
    private final FileRepository fileRepository;

    //판다인지 검증합니다
    public boolean verifyPanda(Authentication authentication)
    {
      log.info("판다인지 검증을 시작합니다");
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        User user = byEmail.get();

        return false;
    }
    public boolean verifyAdmin(Authentication authentication)
    {
        log.info("어드민 검증을 시작합니다");
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        User user = byEmail.get();

        return false;
    }

    //주문이 해당 샵의 주문이 맞는지 확인합니다
    public boolean verifyByerOrder(String userName,long userOrderId) {
        Optional<User> byEmail = userRepository.findByEmail(userName);
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId);
        if(byEmail.get().getId()==byId.get().getShop().getUser().getId())
        {
            return true;
        }
        return false;

    }

    //판다가 올린 영상이 맞는지 검증합니다
    public boolean verifyPandaForMovie(String name, long pandaMovieID) {
        Optional<User> byEmail = userRepository.findByEmail(name);
        Optional<PandaToProduct> byId1 = pandaToProductRepository.findById(pandaMovieID);
        if(byEmail.get().getId()==byId1.get().getPanda().getUser().getId())
        {
            return true;
        }
        return false;
    }

    public boolean orderForShop(String name, long userOrderId) {
        Optional<User> byEmail = userRepository.findByEmail(name);
        Optional<UserOrder> byId = userOrderRepository.findById(userOrderId);
        if(byEmail.get().getId()==byId.get().getShop().getUser().getId())
        {
            return true;
        }
        return false;
    }

    //유저가 한 주문이 맞는지 확인합니다
    public boolean userOrderVerifyForuser(String name, long userOrderId) {
        User user = userRepository.findByEmail(name).get();
        UserOrder userOrder = userOrderRepository.findById(userOrderId).get();
        if(user.getId()==userOrder.getUserId().getId())
        {
            return true;
        }
        return false;
    }


    public boolean userOrderForShopOrUser(String name, long userOrderId) {
        User user = userRepository.findByEmail(name).get();
        UserOrder userOrder = userOrderRepository.findById(userOrderId).get();
        if(user.getId()==userOrder.getUserId().getId() || user.getShop().getId()==userOrder.getShop().getId())
        {
            return true;
        }
        return false;
    }



    public boolean verifyShopForFile(String name, String filePath) {
        User user = userRepository.findByEmail(name).get();
        File myqueryfind = fileRepository.myqueryfind(filePath);
        if(user.getId()==myqueryfind.getProduct().getShop().getUser().getId())
        {
            return true;
        }
        return false;

    }
}
