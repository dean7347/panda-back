package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.SettleShopRepository;
import com.indiduck.panda.Repository.ShopRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.controller.ShopController;
import com.indiduck.panda.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final SettleShopRepository settleShopRepository;


    public Shop regShopResult(long id, String result){
        Optional<Shop> byId = shopRepository.findById(id);
        Shop shop = byId.get();

        if(result.equals("confirm"))
        {
            shop.confirmShop();
            return shop;
        }else
        {
            shop.getUser().deleteShop();
            shopRepository.delete(shop);
            return null;
        }


    }

    public Shop createNewShop(String username, String shopName, String representative, String crn,
                              String telnum, int freepee, int nofree,
                              String priPhone, String csPhone, String csTime,
                              String toPanda, String reship,
                              int returnpee, int tradepee, String returnaddress, String candate,
                              String noreturn, boolean Termsagree, boolean Infoagree, String comAddress, String avdtime,String descriptionProduct) {

        //TODO: 처리할것 도메인에서 쓰로우 런타임으로 우류처리
        Optional<User> byEmail = userRepository.findByEmail(username);
        //유저가 이미 샵을 가지고 있다면

        if (byEmail.get().getShop() == null) {
            Shop shop = Shop.createShop(byEmail.get(), shopName, representative,
                    crn, telnum, freepee, nofree,
                    priPhone, csPhone, csTime, toPanda, reship,
                    returnpee, tradepee, returnaddress, candate,
                    noreturn, Termsagree, Infoagree, comAddress, avdtime,descriptionProduct);
            Shop save = shopRepository.save(shop);
            return save;
        }
        return null;
    }

    //샵에서 계산서를 생성하는 로직
    public SettleShop SettleLogic(Shop shop) {
        Optional<List<UserOrder>> byShopAndPaymentStatusAndEnrollSettle = userOrderRepository.findByShopAndPaymentStatusAndEnrollSettleShopAndOrderStatus(shop, PaymentStatus.지급예정, false,OrderStatus.구매확정);
        List<UserOrder> userOrders = byShopAndPaymentStatusAndEnrollSettle.get();
        SettleShop settleShop = SettleShop.createSettleShop(userOrders, shop);
        settleShopRepository.save(settleShop);
        return settleShop;


    }

    public Shop editShop(String target, String value, Shop shop) {
        switch (target) {
            case "shopName":
                shop.setShopName(value);
                break;
            default:
                return null;
            case "avdtime":
                shop.setAVDtime(value);
                break;
            case "crn":
                shop.setCRN(value);
                break;
            case "canDate":
                shop.setCandate(value);
                break;
            case "noreturn":
                shop.setNoreturn(value);
                break;
            case "comAddr":
                shop.setComaddress(value);
                break;
            case "csphone":
                shop.setCsPhone(value);
                break;
            case "csTime":
                shop.setCsTime(value);
                break;
            case "freePrice":
                shop.setFreePrice(Integer.parseInt(value));
                break;
            case "noFree":
                shop.setNofree(Integer.parseInt(value));
                break;
            case "number":
                shop.setNumber(value);
                break;
            case "priPhone":
                shop.setPriPhone(value);
                break;
            case "representative":
                shop.setRepresentative(value);
                break;
            case "reship":
                shop.setReship(value);
                break;
            case "returnAddress":
                shop.setReturnaddress(value);
                break;
            case "returnPee":
                shop.setReturnpee(Integer.parseInt(value));
                break;
            case "toPanda":
                shop.setToPanda(value);
                break;
            case "tradeFee":
                shop.setTradepee(Integer.parseInt(value));
                break;
        }
        return shop;


    }
}



//    //샵이 있는지 조회
//    public Shop haveShop(String userName)
//    {
//        Optional<User> byEmail = userRepository.findByEmail(userName);
//        System.out.println("byEmail = " + byEmail.get().getEmail());
//        Optional<Shop> byUserUsername = shopRepository.findShopByUser(byEmail.get());
//        System.out.println("shopWithUserByusername = " + byUserUsername);
//        return byUserUsername.get();
//    }

