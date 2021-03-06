package com.indiduck.panda.controller;

import com.indiduck.panda.Repository.*;
import com.indiduck.panda.Service.*;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import com.indiduck.panda.domain.dto.UserDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final JwtUserDetailsService userService;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final UserOrderService userOrderService;

    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final PandaToProductRepository pandaToProductRepository;
    @Autowired
    private final VerifyService verifyService;

    @GetMapping("/api/userresign")
    public ResponseEntity<?> userResign(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,HttpServletRequest req) {
        log.info(authentication.getName()+"??? ?????? ??????");
        //????????????
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        User user = byEmail.get();
        Shop shop = byEmail.get().getShop();
        Panda panda = byEmail.get().getPanda();
        boolean isshop = false;
        boolean ispanda = false;
        if (shop != null) {
            isshop = true;
        }
        if (panda != null) {
            ispanda = true;
        }


        //?????? ??????????????????
        //??????????????? ?????? ??????????????? ??????
        if (ispanda == true) {
//            System.out.println("????????? ?????????????????????");
            Optional<List<PandaToProduct>> byPandaAndIsDel = pandaToProductRepository.findByPandaAndIsDel(panda, true);
            if(byPandaAndIsDel.isEmpty())
            {
                return ResponseEntity.ok(new TFMessageDto(false, "?????? ????????? ????????? ??? ?????? ??????????????? "));

            }
            //?????? ????????? ??????????????? ??????

        }
        //?????? ????????????
        //?????? ????????? ??????????????? ?????? ?????? ????????? ?????? ??????, ?????? ?????? ??????????????????
        if (isshop == true) {
            //?????? ????????? ???????????????????
            Optional<Product> byShopAndDeleted = productRepository.findByShopAndDeleted(shop, false);
            if(!byShopAndDeleted.isEmpty())
            {
//                System.out.println("byShopAndDeleted = " + byShopAndDeleted);
                return ResponseEntity.ok(new TFMessageDto(false, "?????? ????????? ????????? ?????? ???????????????"));

            }
            //???????????? ????????? ?????????? ???????????? ????????? ??????????
            Optional<List<UserOrder>> payorder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.????????????);
            Optional<List<UserOrder>> readyOrder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.?????????);
            Optional<List<UserOrder>> shipOrder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.?????????);
            if(payorder.isEmpty() && readyOrder.isEmpty() && shipOrder.isEmpty())
            {
                return ResponseEntity.ok(new TFMessageDto(false, "????????????, ????????????, ???????????? ????????? ????????? ????????????. ?????? ????????? ?????? ??? ??? ????????? ???????????????"));
            }
//            System.out.println("?????? ?????? ???????????????");

        }


        //???????????? ?????? ?????? ????????? ?????????
        Optional<List<UserOrder>> payorder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.????????????);
        Optional<List<UserOrder>> readyOrder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.?????????);
        Optional<List<UserOrder>> shipOrder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.?????????);
        if(payorder.isEmpty() && readyOrder.isEmpty() && shipOrder.isEmpty())
        {
            return ResponseEntity.ok(new TFMessageDto(false, "???????????? ????????? ???????????? ?????? ???????????? ??? ????????? ??????????????? "));
        }


        //?????????????????? ????????? ?????? ??????????????????
        //????????? ???????????? ??? ????????????

        //????????? ????????????
        //?????? ????????????
        //????????? ????????????
        //???????????? ????????? ??????
        userService.deleteTempSet(user);
//        user.setLeaveAt();
        log.info(user + "??? ????????????");

        String atCookie="";
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("accessToken"))
                atCookie = cookie.getValue();
        }
        String atToken = atCookie;
//        if (errors.hasErrors()) {
//            return response.invalidFields(Helper.refineErrors(errors));
//        }
        boolean b = userService.logoutV2(atToken);
        if (b) {

            return ResponseEntity.ok(new TFMessageDto(true, "?????? ??????????????? ??????????????? ????????????????????? 7????????? ??????????????? ????????? ??????????????? "));


        }
        return ResponseEntity.ok(new TFMessageDto(b, "????????? ??????????????????"));



    }


    @GetMapping("/api/userprivateedit")
    public ResponseEntity<?> userPrivateEdit(@CurrentSecurityContext(expression = "authentication")
                                                     Authentication authentication) { // ?????? ??????
        //????????????
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        User user = byEmail.get();
        Shop shop = byEmail.get().getShop();
        Panda panda = byEmail.get().getPanda();
//        System.out.println("panda = " + panda);
//        System.out.println("shop = " + shop);
        boolean isshop = false;
        boolean ispanda = false;
        if (shop != null) {
            isshop = true;
        }
        if (panda != null) {
            ispanda = true;
        }

        log.info(authentication.getName() + "??? ????????????");
        return ResponseEntity.ok(new UserEditDTO(true, isshop, ispanda, user.getRegAt(), user.getEmail(), user.getUserRName(), shop, panda));
        //????????????
    }

    @GetMapping("/api/dashboard")
    public ResponseEntity<?> mainDashBoard(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication) { // ?????? ??????
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        List<UserOrder> byUserId = userOrderRepository.findByUserId(byEmail.get());
        Optional<List<OrderDetail>> byUserAndOrderStatus = orderDetailRepository.findByUserAndOrderStatus(byEmail.get(), OrderStatus.????????????);
//        Optional<List<OrderDetail>> orderDetailByUser = orderDetailRepository.findOrderDetailByUser(byEmail.get());
        Optional<List<OrderDetail>> cartNum = orderDetailRepository.findByUserAndOrderStatusAndOptions_Sales(byEmail.get(), OrderStatus.????????????,true);

        int ready = 0;
        int finish = 0;
        int cancel = 0;
        int cart = cartNum.get().size();
        for (UserOrder userOrder : byUserId) {
            if ((userOrder.getOrderStatus() == (OrderStatus.?????????)) || (userOrder.getOrderStatus() == (OrderStatus.?????????)) || (userOrder.getOrderStatus() == (OrderStatus.????????????))) {
                ready++;
            }

            if ((userOrder.getOrderStatus() == (OrderStatus.????????????)) || (userOrder.getOrderStatus() == (OrderStatus.????????????))) {
                finish++;
            }
            if ((userOrder.getOrderStatus() == (OrderStatus.????????????)) || (userOrder.getOrderStatus() == (OrderStatus.????????????)) || (userOrder.getOrderStatus() == (OrderStatus.????????????))) {
                cancel++;
            }
//            if ((userOrder.getOrderStatus() == (OrderStatus.????????????))) {
//                cart++;
//            }
        }
        if (byUserAndOrderStatus.get().isEmpty()) {
            cart = 0;
        } else {
            byUserAndOrderStatus.get().size();
        }
        //????????? ????????? ???????????????
        if (byUserId.isEmpty()) {
            ready = 0;
            finish = 0;
            cancel = 0;
        }
        return ResponseEntity.ok(new dashBoardDto(true, ready, finish, cancel, cart));
    }

    //????????????
    @PostMapping("/api/userordercancel")
    public ResponseEntity<?> cancelOrder(@CurrentSecurityContext(expression = "authentication")
                                                 Authentication authentication, @RequestBody SituationDto situationDto) {

        boolean b = verifyService.userOrderForShopOrUser(authentication.getName(), situationDto.detailId);
        if(!b)
        {
            log.error(authentication.getName()+"??? userOrdercancel??????  ????????? ??????");
            return ResponseEntity.ok(new TFMessageDto(false, "????????? ??? ?????????????????????"));

        }
        UserOrder userOrder = userOrderService.cancelOrder(situationDto.detailId);
//        System.out.println("situationDto = " + situationDto);
        if (userOrder != null) {

            log.info(authentication.getName()+"??? ?????? ??????");

            return ResponseEntity.ok(new TFMessageDto(true, "????????????"));

        }
        log.error(authentication.getName()+"??? userOrdercancel?????? ??????");

        return ResponseEntity.ok(new TFMessageDto(false, "????????? ??? ?????????????????????"));


    }


    @GetMapping("/api/recentsituation")
    public ResponseEntity<?> recentSituation(@CurrentSecurityContext(expression = "authentication")
                                                     Authentication authentication, @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String name = authentication.getName();
//        System.out.println("name = " + name);
        Authentication authentication1 = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("authentication1.getName() = " + authentication1.getName());
        Optional<User> byEmail = userRepository.findByEmail(name);
        Page<UserOrder> allByUserId = userOrderRepository.findAllByUserId(byEmail.get(), pageable);
//        System.out.println("allByUserId = " + allByUserId.get());
        List<recentSituation> pageList = new ArrayList<>();
//        System.out.println("allByUserId = " + allByUserId.get());
//        for (UserOrder userOrder : allByUserId) {
//            System.out.println("userOrder = " + userOrder.getReveiverName());
//        }
        HashSet<String> proname = new HashSet<>();

        for (UserOrder userOrder : allByUserId) {
            proname = new HashSet<>();
            List<OrderDetail> detail = userOrder.getDetail();
            for (OrderDetail orderDetail : detail) {
                String productName = orderDetail.getProducts().getProductName();
                proname.add(productName);

            }
            pageList.add(new recentSituation(userOrder.getId(), proname.toString(), userOrder.getFullprice(),
                    userOrder.getCreatedAt(), userOrder.getOrderStatus().toString()));
        }
        return ResponseEntity.ok(new pageDto(true, allByUserId.getTotalPages(), allByUserId.getTotalElements(), pageList));
    }

    //????????? ???????????????
    @PostMapping("/api/situationdetail")
    public ResponseEntity<?> situationDetail(@CurrentSecurityContext(expression = "authentication")
                                                     Authentication authentication, @RequestBody SituationDto situationDto) {
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);

        Optional<UserOrder> byId = userOrderRepository.findById(situationDto.detailId);
        UserOrder userOrder = byId.get();
        List<OrderDetail> detail = userOrder.getDetail();
        List<DetailOrderList> dol = new ArrayList<>();


        recentSituationDto rsd = new recentSituationDto(true, userOrder.getId(), userOrder.getAmount(), userOrder.getShipPrice()
                , userOrder.getFullprice(), userOrder.getReveiverName(), userOrder.getReceiverAddress(), userOrder.getReceiverPhone(), detail);


        return ResponseEntity.ok(rsd);
    }


    @PostMapping("/api/situationdetailv2")
    public ResponseEntity<?> situationDetailV2(@CurrentSecurityContext(expression = "authentication")
                                                       Authentication authentication, @RequestBody SituationDto situationDto) {
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);

        Optional<UserOrder> byId = userOrderRepository.findById(situationDto.detailId);
        UserOrder userOrder = byId.get();
        List<OrderDetail> detail = userOrder.getDetail();
        List<DetailOrderList> dol = new ArrayList<>();
        HashSet<String> proname = new HashSet<>();


        for (OrderDetail orderDetail : userOrder.getDetail()) {
            proname.add(orderDetail.getProducts().getProductName());
        }
        System.out.println("????????????????????? = " + proname);


        recentSituationDtoV2 rsd = new recentSituationDtoV2(true, byEmail.get().getUserRName(), userOrder.getId(), userOrder.getAmount(), userOrder.getShipPrice()
                , userOrder.getFullprice(), userOrder.getReveiverName(), userOrder.getReceiverAddress(), userOrder.getReceiverPhone(), detail
                , proname.toString(), detail.get(0).getPaymentAt(), detail.get(0).getShop().getShopName(), detail.get(0).getShop().getCsPhone(),
                userOrder.getOrderStatus(), userOrder.getPureAmount(), userOrder.getFreeprice(), userOrder.getReceiverZipCode(), userOrder.getMemo(), userOrder.getUserId().getUserPhoneNumber()
                ,userOrder.getCourierCom(),userOrder.getWaybillNumber(),userOrder.getReceiptUrl());


        return ResponseEntity.ok(rsd);
    }


    @PostMapping("/api/situationListdetail")
    public ResponseEntity<?> situationListDetailV1(@CurrentSecurityContext(expression = "authentication")
                                                           Authentication authentication, @RequestBody SituationListDto situationDto) {
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        List<recentSituationDtoV2> printdatas = new ArrayList<>();
        for (long l : situationDto.detailId) {
            Optional<UserOrder> byId = userOrderRepository.findById(l);
            UserOrder userOrder = byId.get();
            List<OrderDetail> detail = userOrder.getDetail();
            List<DetailOrderList> dol = new ArrayList<>();
            HashSet<String> proname = new HashSet<>();

            UserOrder sta = userOrderService.ChangeOrder(l, "?????????", "", "");
            if (sta == null) {
                return ResponseEntity.ok(new TFMessageDto(false, userOrder.getId() + "??? ????????? ?????? ?????????????????? ????????? ??????????????????"));

            }

            for (OrderDetail orderDetail : userOrder.getDetail()) {
                proname.add(orderDetail.getProducts().getProductName());
            }
            System.out.println("????????????????????? = " + proname);


            recentSituationDtoV2 rsd = new recentSituationDtoV2(true, byEmail.get().getUserRName(), userOrder.getId(), userOrder.getAmount(), userOrder.getShipPrice()
                    , userOrder.getFullprice(), userOrder.getReveiverName(), userOrder.getReceiverAddress(), userOrder.getReceiverPhone(), detail
                    , proname.toString(), detail.get(0).getPaymentAt(), detail.get(0).getShop().getShopName(), detail.get(0).getShop().getCsPhone(),
                    userOrder.getOrderStatus(), userOrder.getPureAmount(), userOrder.getFreeprice(), userOrder.getReceiverZipCode(), userOrder.getMemo(), userOrder.getUserId().getUserPhoneNumber()
            ,userOrder.getCourierCom(),userOrder.getWaybillNumber(),userOrder.getReceiptUrl());
            printdatas.add(rsd);


        }

        return ResponseEntity.ok(new DetailListDTO(true, printdatas));
    }


    //?????? ?????? ????????????
//    @PostMapping("/api/changeuserorderstate")
//    public ResponseEntity<?> changeStateUserOrder(@CurrentSecurityContext(expression = "authentication")
//                                                          Authentication authentication, @RequestBody ChageDao chageDao) {
//
//        userOrderService.ChangeOrder(chageDao.userOrderId, chageDao.state, chageDao.shipCompany, chageDao.shipNumber);
//        return ResponseEntity.ok(new TFMessageDto(true, "??????????????? ??????????????????"));
//
//    }

    @Data
    private static class DetailListDTO {
        boolean success;
        List<recentSituationDtoV2> sld;

        public DetailListDTO(boolean success, List<recentSituationDtoV2> sld) {
            this.success = success;
            this.sld = sld;
        }
    }

    @Data
    private static class ChageDao {
        long userOrderId;
        String state;
        String shipCompany;
        String shipNumber;


    }


    @Data
    private class pageDto {
        boolean success;
        int totalpage;
        Long totalElement;
        List<recentSituation> pageList = new ArrayList<>();

        public pageDto(boolean su, int totalP, Long totalE, List<recentSituation> pl) {
            success = su;
            totalpage = totalP;
            totalElement = totalE;
            pageList = pl;
        }
    }


    @Data
    private class recentSituation {
        //????????????
        long num;
        //????????????
        String productName;
        //????????????
        int price;
        //????????????
        LocalDateTime orderAt;
        //?????? ??????
        String status;
        //???????????? ?????????

        public recentSituation(Long no, String pn, int pri, LocalDateTime dateTime, String stat) {
            num = no;
            productName = pn;
            price = pri;
            orderAt = dateTime;
            status = stat;

        }
    }

    @Data
    private class dashBoardDto {
        boolean success;
        //?????????,?????????
        int readyProduct;
        //??????????????????
        int finishProduct;
        //??????,????????????
        int cancelProduct;
        //???????????? ??????
        int cartProduct;

        public dashBoardDto(boolean result, int ready, int fin, int cancel, int cart) {
            success = result;
            readyProduct = ready;
            finishProduct = fin;
            cancelProduct = cancel;
            cartProduct = cart;

        }

    }

    @Data
    private class recentOrder {
        //????????????
        int crn;
        //?????????
        String name;
        //??????
        int price;
        //????????????
        LocalDateTime orderAt;
        //?????????
        OrderStatus status;
    }

    @Data
    private static class SituationListDto {
        //????????????
        long[] detailId;
    }

    @Data
    private static class SituationDto {
        //????????????
        long detailId;
    }

    @Data
    private class recentSituationDtoV2 {
        boolean success;
        //????????????
        Long detailId;
        //????????????
        int price;
        //?????????
        int shipprice;
        //????????? ??????
        int pureamount;
        //?????????
        int allamount;
        //??????????????????
        int freeprice;
        //????????? ???????????? ??????
        int originPrice;
        //????????????
        String receiver;
        //??????
        String address;
        //????????????
        String addressNum;
        //????????????????????????
        String receiverPhone;
        //??????DTO
        HashSet<DetailOrderList> products = new HashSet<>();
        List<DetailOrderList> orderDetails = new ArrayList<>();
        //????????????
        String proName;
        LocalDateTime orderAt;
        String shopName;
        String shopPhone;
        OrderStatus status;
        //????????? ????????????
        String buyerPhone;
        //????????????
        String shipmemo;
        String buyerName;
        //?????????
        String courier;
        //???????????????
        String wayBillNumber;
        //?????????
        String receiptUrl;


        public recentSituationDtoV2(boolean su, String buyer, Long detailId, int price, int shipprice, int allamount,
                                    String receiver, String address, String receiverPhone, List<OrderDetail> dol, String pn, LocalDateTime oa,
                                    String sn, String sp, OrderStatus os, int pa, int fp, String addressNum, String shipmemo, String bp,String co,String wn,String ru) {
            this.proName = pn;
            this.buyerName = buyer;
            this.orderAt = oa;
            this.shopName = sn;
            this.shopPhone = sp;
            this.status = os;
            this.success = su;
            this.detailId = detailId;
            this.price = price;
            this.buyerPhone = bp;
            this.shipprice = shipprice;
            this.allamount = allamount;
            this.receiver = receiver;
            this.address = address;
            this.addressNum = addressNum;
            this.shipmemo = shipmemo;
            this.receiverPhone = receiverPhone;
            this.pureamount = pa;
            this.freeprice = fp;
            this.courier =co;
            this.wayBillNumber=wn;
            this.receiptUrl=ru;
            for (OrderDetail orderDetail : dol) {
                String img = null;
                List<File> images = orderDetail.getProducts().getImages();
                for (File image : images) {
                    if (image.isIsthumb()) {
                        img = image.getFilepath();
                        break;
                    }
                }
                products.add(new DetailOrderList(orderDetail.getProducts().getProductName(), img, orderDetail.getProducts().getId()));
            }

            for (OrderDetail orderDetail : dol) {
                for (DetailOrderList product : products) {

                    if (product.productName == orderDetail.getProducts().getProductName()) {
                        if (orderDetail.getPanda() == null) {
                            product.setOptions(new OptionList(orderDetail.getOptions().getOptionName(), orderDetail.getProductCount(),
                                    orderDetail.getIndividualPrice(), orderDetail.getTotalPrice(), "null", orderDetail.getId()
                                    , orderDetail.getOrderStatus(),orderDetail.getConfirmRefund(),orderDetail.getReqCancel()));
                        } else {
                            product.setOptions(new OptionList(orderDetail.getOptions().getOptionName(), orderDetail.getProductCount(),
                                    orderDetail.getIndividualPrice(), orderDetail.getTotalPrice(), orderDetail.getPanda().getPandaName(), orderDetail.getId()
                                    , orderDetail.getOrderStatus(),orderDetail.getConfirmRefund(),orderDetail.getReqCancel()));
                        }

                    }
                    this.originPrice+=orderDetail.getOriginOrderMoney();
                }
            }


        }
    }

    @Data
    private class recentSituationDto {
        boolean success;
        //????????????
        Long detailId;
        //????????????
        int price;
        //?????????
        int shipprice;
        //?????????
        int allamount;
        //????????????
        String receiver;
        //??????
        String address;
        //????????????????????????
        String receiverPhone;
        //??????DTO
        HashSet<DetailOrderList> products = new HashSet<>();
        List<DetailOrderList> orderDetails = new ArrayList<>();

        public recentSituationDto(boolean su, Long detailId, int price, int shipprice, int allamount,
                                  String receiver, String address, String receiverPhone, List<OrderDetail> dol) {
            this.success = su;
            this.detailId = detailId;
            this.price = price;
            this.shipprice = shipprice;
            this.allamount = allamount;
            this.receiver = receiver;
            this.address = address;
            this.receiverPhone = receiverPhone;
            for (OrderDetail orderDetail : dol) {
                String img = null;
                List<File> images = orderDetail.getProducts().getImages();
                for (File image : images) {
                    if (image.isIsthumb()) {
                        img = image.getFilepath();
                    }
                }
                products.add(new DetailOrderList(orderDetail.getProducts().getProductName(), img, orderDetail.getProducts().getId()));
            }

            for (OrderDetail orderDetail : dol) {
                for (DetailOrderList product : products) {

                    if (product.productName == orderDetail.getProducts().getProductName()) {
                        if (orderDetail.getPanda() == null) {
                            product.setOptions(new OptionList(orderDetail.getOptions().getOptionName(), orderDetail.getProductCount(),
                                    orderDetail.getIndividualPrice(), orderDetail.getTotalPrice(), "null", orderDetail.getId()
                                    , orderDetail.getOrderStatus(),orderDetail.getConfirmRefund(),orderDetail.getReqCancel()));
                        } else {
                            product.setOptions(new OptionList(orderDetail.getOptions().getOptionName(), orderDetail.getProductCount(),
                                    orderDetail.getIndividualPrice(), orderDetail.getTotalPrice(), orderDetail.getPanda().getPandaName(), orderDetail.getId()
                                    , orderDetail.getOrderStatus(),orderDetail.getConfirmRefund(),orderDetail.getReqCancel()));
                        }

                    }
                }
            }


        }
    }


    @Data
    private class UserEditDTO {
        boolean success;
        boolean isShop;
        boolean isPanda;
        LocalDateTime regAt;
        String Email;
        String userName;
        IfShop ifShop;
        IfPanda ifPanda;

        public UserEditDTO(boolean su, boolean isShop, boolean isPanda, LocalDateTime regAt, String email, String userName, Shop shop, Panda panda) {
            this.success = su;
            this.isShop = isShop;
            this.isPanda = isPanda;
            this.regAt = regAt;
            Email = email;
            this.userName = userName;
            if (shop != null) {
                this.ifShop = new IfShop(shop);
                ;

            }
            if (panda != null) {
                this.ifPanda = new IfPanda(panda);

            }
        }
    }

    @Data
    private class IfShop {
        String shopName;
        //??????????????????
        String avdtime;
        //CRN
        String crn;

        //??????/?????? ????????? ?????? ?????? ?????? ??????
        String canDate;
        //?????? ?????? ????????? ??????
        String noreturn;
        //????????????
        String comAddr;
        //cs????????????
        String csPhne;
        //cs??????
        String csTime;
        //?????????????????? (?????? ????????? ???????????? ???????????????)
        int freePrice;

        //?????? ?????????????
        boolean isApprove;
        //?????? ???????
        boolean isOpen;
        //?????? ????????????
        int NOFREE;

        //???????????????
        String number;
        //??????????????????
        String priPhone;

        //?????????
        String representative;

        //???????????? ?????????
        String reship;
        //????????????
        String returnAddress;
        //????????????
        int returnpee;
        //??? ??????

        //?????????
        String topanda;
        //????????????
        int tradeFee;

        public IfShop(Shop shop) {
            this.shopName = shop.getShopName();
            this.avdtime = shop.getAVDtime();
            this.crn = shop.getCRN();
            this.canDate = shop.getCandate();
            this.noreturn = shop.getNoreturn();
            this.comAddr = shop.getComaddress();
            this.csPhne = shop.getCsPhone();
            this.csTime = shop.getCsTime();
            this.freePrice = shop.getFreePrice();
            this.isApprove = shop.isApprove();
            this.isOpen = shop.isOpen();
            this.NOFREE = shop.getNofree();
            this.number = shop.getNumber();
            this.priPhone = shop.getPriPhone();
            this.representative = shop.getRepresentative();
            this.reship = shop.getReship();
            this.returnAddress = shop.getReturnaddress();
            this.returnpee = shop.getReturnpee();
            this.topanda = shop.getToPanda();
            this.tradeFee = shop.getTradepee();
        }
    }

    @Data
    private class IfPanda {
        String pandaName;
        String intCategory;
        String mainCh;
        //????????????
        boolean confirm;

        public IfPanda(Panda panda) {
            this.pandaName = panda.getPandaName();
            this.intCategory = panda.getIntCategory();
            this.mainCh = panda.getMainCh();
            this.confirm = panda.isRecognize();
        }
    }

    @Data
    private class DetailOrderList {
        String productName;
        String imgPath;
        List<OptionList> options = new ArrayList<>();
        long proId;

        public DetailOrderList(String pn, String img, long pi) {
            productName = pn;
            imgPath = img;
            proId = pi;
        }

        public void setOptions(OptionList list) {
            options.add(list);

        }
    }

    @Data
    private class OptionList {
        String optionName;
        int optionCount;
        int optionPrice;
        int allAmount;
        String pandaName;
        boolean discount;
        long odid;
        OrderStatus orderStatus;
        //?????? ????????? ???????
        int completeRefund;
        //?????? ????????? ??????
        int completeCancel;

        public OptionList(String optionName, int optionCount, int optionPrice, int allAmount, String pandaName, long odid, OrderStatus odst,int complete,int cancel) {
            this.optionName = optionName;
            this.optionCount = optionCount;
            this.optionPrice = optionPrice;
            this.allAmount = allAmount;
            this.pandaName = pandaName;
            this.completeRefund=complete;
            this.completeCancel=cancel;
            this.odid = odid;
            this.orderStatus = odst;
            if (pandaName == "null") {
                discount = false;
            } else {
                discount = true;
            }
        }
    }
}
