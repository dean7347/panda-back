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
        log.info(authentication.getName()+"의 탈퇴 요청");
        //상점여부
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


        //판다 샵이없을경우
        //주문상태가 모두 완료되어야 한다
        if (ispanda == true) {
//            System.out.println("판다가 있는회원입니다");
            Optional<List<PandaToProduct>> byPandaAndIsDel = pandaToProductRepository.findByPandaAndIsDel(panda, true);
            if(byPandaAndIsDel.isEmpty())
            {
                return ResponseEntity.ok(new TFMessageDto(false, "모든 영상을 삭제한 후 탈퇴 가능합니다 "));

            }
            //모든 영상이 삭제되어야 한다

        }
        //샵이 있을경우
        //모든 상품이 삭제되어야 하고 모든 상품이 배송 완료, 주문 취소 상태여야한다
        if (isshop == true) {
            //모든 상품이 삭제되었는가?
            Optional<Product> byShopAndDeleted = productRepository.findByShopAndDeleted(shop, false);
            if(!byShopAndDeleted.isEmpty())
            {
//                System.out.println("byShopAndDeleted = " + byShopAndDeleted);
                return ResponseEntity.ok(new TFMessageDto(false, "모든 상품을 삭제후 탈퇴 가능합니다"));

            }
            //준비중인 상품이 있는가? 배송중인 상품이 있는가?
            Optional<List<UserOrder>> payorder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.결제완료);
            Optional<List<UserOrder>> readyOrder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.준비중);
            Optional<List<UserOrder>> shipOrder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.발송중);
            if(payorder.isEmpty() && readyOrder.isEmpty() && shipOrder.isEmpty())
            {
                return ResponseEntity.ok(new TFMessageDto(false, "결제완료, 준비중인, 발송중인 상태의 상품이 있습니다. 모든 처리가 완료 된 후 탈퇴가 가능합니다"));
            }
//            System.out.println("샵이 있는 회원입니다");

        }


        //구매확정 되지 않은 상품이 있는가
        Optional<List<UserOrder>> payorder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.결제완료);
        Optional<List<UserOrder>> readyOrder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.준비중);
        Optional<List<UserOrder>> shipOrder = userOrderRepository.findByUserIdAndOrderStatus(user, OrderStatus.발송중);
        if(payorder.isEmpty() && readyOrder.isEmpty() && shipOrder.isEmpty())
        {
            return ResponseEntity.ok(new TFMessageDto(false, "주문중인 상품이 있습니다 모두 구매확정 후 탈퇴가 가능합니다 "));
        }


        //지금버전에선 이걸로 삭제 시그널을주고
        //유저의 주소록을 다 삭제한다

        //판다를 삭제하고
        //샵을 삭제하고
        //유저를 삭제한다
        //배치시에 실행할 로직
        userService.deleteTempSet(user);
//        user.setLeaveAt();
        log.info(user + "의 탈퇴요청");

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

            return ResponseEntity.ok(new TFMessageDto(true, "회원 탈퇴요청이 성공적으로 입력되었습니다 7일이후 계정정보는 완전히 삭제됩니다 "));


        }
        return ResponseEntity.ok(new TFMessageDto(b, "요청에 실패했습니다"));



    }


    @GetMapping("/api/userprivateedit")
    public ResponseEntity<?> userPrivateEdit(@CurrentSecurityContext(expression = "authentication")
                                                     Authentication authentication) { // 회원 추가
        //상점여부
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

        log.info(authentication.getName() + "의 에딧요청");
        return ResponseEntity.ok(new UserEditDTO(true, isshop, ispanda, user.getRegAt(), user.getEmail(), user.getUserRName(), shop, panda));
        //판다여부
    }

    @GetMapping("/api/dashboard")
    public ResponseEntity<?> mainDashBoard(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication) { // 회원 추가
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        List<UserOrder> byUserId = userOrderRepository.findByUserId(byEmail.get());
        Optional<List<OrderDetail>> byUserAndOrderStatus = orderDetailRepository.findByUserAndOrderStatus(byEmail.get(), OrderStatus.결제대기);
//        Optional<List<OrderDetail>> orderDetailByUser = orderDetailRepository.findOrderDetailByUser(byEmail.get());
        Optional<List<OrderDetail>> cartNum = orderDetailRepository.findByUserAndOrderStatusAndOptions_Sales(byEmail.get(), OrderStatus.결제대기,true);

        int ready = 0;
        int finish = 0;
        int cancel = 0;
        int cart = cartNum.get().size();
        for (UserOrder userOrder : byUserId) {
            if ((userOrder.getOrderStatus() == (OrderStatus.발송중)) || (userOrder.getOrderStatus() == (OrderStatus.준비중)) || (userOrder.getOrderStatus() == (OrderStatus.결제완료))) {
                ready++;
            }

            if ((userOrder.getOrderStatus() == (OrderStatus.배송완료)) || (userOrder.getOrderStatus() == (OrderStatus.구매확정))) {
                finish++;
            }
            if ((userOrder.getOrderStatus() == (OrderStatus.환불대기)) || (userOrder.getOrderStatus() == (OrderStatus.환불완료)) || (userOrder.getOrderStatus() == (OrderStatus.주문취소))) {
                cancel++;
            }
//            if ((userOrder.getOrderStatus() == (OrderStatus.결제대기))) {
//                cart++;
//            }
        }
        if (byUserAndOrderStatus.get().isEmpty()) {
            cart = 0;
        } else {
            byUserAndOrderStatus.get().size();
        }
        //유저의 내역이 비었을경우
        if (byUserId.isEmpty()) {
            ready = 0;
            finish = 0;
            cancel = 0;
        }
        return ResponseEntity.ok(new dashBoardDto(true, ready, finish, cancel, cart));
    }

    //주문취소
    @PostMapping("/api/userordercancel")
    public ResponseEntity<?> cancelOrder(@CurrentSecurityContext(expression = "authentication")
                                                 Authentication authentication, @RequestBody SituationDto situationDto) {

        boolean b = verifyService.userOrderForShopOrUser(authentication.getName(), situationDto.detailId);
        if(!b)
        {
            log.error(authentication.getName()+"의 userOrdercancel요청  조건부 실패");
            return ResponseEntity.ok(new TFMessageDto(false, "취소할 수 없는주문입니다"));

        }
        UserOrder userOrder = userOrderService.cancelOrder(situationDto.detailId);
//        System.out.println("situationDto = " + situationDto);
        if (userOrder != null) {

            log.info(authentication.getName()+"의 요청 성공");

            return ResponseEntity.ok(new TFMessageDto(true, "변경성공"));

        }
        log.error(authentication.getName()+"의 userOrdercancel요청 실패");

        return ResponseEntity.ok(new TFMessageDto(false, "취소할 수 없는주문입니다"));


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

    //디테일 내려주는곳
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
        System.out.println("내려주는데이타 = " + proname);


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

            UserOrder sta = userOrderService.ChangeOrder(l, "준비중", "", "");
            if (sta == null) {
                return ResponseEntity.ok(new TFMessageDto(false, userOrder.getId() + "번 주문이 이미 취소되었거나 확인에 실패했습니다"));

            }

            for (OrderDetail orderDetail : userOrder.getDetail()) {
                proname.add(orderDetail.getProducts().getProductName());
            }
            System.out.println("내려주는데이타 = " + proname);


            recentSituationDtoV2 rsd = new recentSituationDtoV2(true, byEmail.get().getUserRName(), userOrder.getId(), userOrder.getAmount(), userOrder.getShipPrice()
                    , userOrder.getFullprice(), userOrder.getReveiverName(), userOrder.getReceiverAddress(), userOrder.getReceiverPhone(), detail
                    , proname.toString(), detail.get(0).getPaymentAt(), detail.get(0).getShop().getShopName(), detail.get(0).getShop().getCsPhone(),
                    userOrder.getOrderStatus(), userOrder.getPureAmount(), userOrder.getFreeprice(), userOrder.getReceiverZipCode(), userOrder.getMemo(), userOrder.getUserId().getUserPhoneNumber()
            ,userOrder.getCourierCom(),userOrder.getWaybillNumber(),userOrder.getReceiptUrl());
            printdatas.add(rsd);


        }

        return ResponseEntity.ok(new DetailListDTO(true, printdatas));
    }


    //주문 상태 변경신청
//    @PostMapping("/api/changeuserorderstate")
//    public ResponseEntity<?> changeStateUserOrder(@CurrentSecurityContext(expression = "authentication")
//                                                          Authentication authentication, @RequestBody ChageDao chageDao) {
//
//        userOrderService.ChangeOrder(chageDao.userOrderId, chageDao.state, chageDao.shipCompany, chageDao.shipNumber);
//        return ResponseEntity.ok(new TFMessageDto(true, "상태변경에 성공했습니다"));
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
        //주문번호
        long num;
        //상품이름
        String productName;
        //결제가격
        int price;
        //주문일자
        LocalDateTime orderAt;
        //주문 상태
        String status;
        //프로덕트 아이디

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
        //준비중,배송중
        int readyProduct;
        //배송완료상품
        int finishProduct;
        //취소,반품상품
        int cancelProduct;
        //장바구니 갯수
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
        //주문번호
        int crn;
        //상품명
        String name;
        //가격
        int price;
        //주문일자
        LocalDateTime orderAt;
        //상태태
        OrderStatus status;
    }

    @Data
    private static class SituationListDto {
        //주문번호
        long[] detailId;
    }

    @Data
    private static class SituationDto {
        //주문번호
        long detailId;
    }

    @Data
    private class recentSituationDtoV2 {
        boolean success;
        //주문번호
        Long detailId;
        //결제금액
        int price;
        //배송비
        int shipprice;
        //순수한 가격
        int pureamount;
        //총금액
        int allamount;
        //무료배송금액
        int freeprice;
        //처음에 주문했던 가격
        int originPrice;
        //받는사람
        String receiver;
        //주소
        String address;
        //우편번호
        String addressNum;
        //받는사람전화번호
        String receiverPhone;
        //상품DTO
        HashSet<DetailOrderList> products = new HashSet<>();
        List<DetailOrderList> orderDetails = new ArrayList<>();
        //추가된것
        String proName;
        LocalDateTime orderAt;
        String shopName;
        String shopPhone;
        OrderStatus status;
        //구매자 전화번호
        String buyerPhone;
        //배송메모
        String shipmemo;
        String buyerName;
        //택배사
        String courier;
        //운송장번호
        String wayBillNumber;
        //영수증
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
        //주문번호
        Long detailId;
        //결제금액
        int price;
        //배송비
        int shipprice;
        //총금액
        int allamount;
        //받는사람
        String receiver;
        //주소
        String address;
        //받는사람전화번호
        String receiverPhone;
        //상품DTO
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
        //평균배송기간
        String avdtime;
        //CRN
        String crn;

        //반품/교환 사유에 따른 요청 가능 기간
        String canDate;
        //반품 교환 불가능 사유
        String noreturn;
        //회사주소
        String comAddr;
        //cs전화번호
        String csPhne;
        //cs시간
        String csTime;
        //무료배송비용 (새로 담기는 주문부터 적용됩니다)
        int freePrice;

        //이즈 어프로브?
        boolean isApprove;
        //이즈 오픈?
        boolean isOpen;
        //유료 배송비용
        int NOFREE;

        //통판업번호
        String number;
        //비상연락번호
        String priPhone;

        //대표자
        String representative;

        //반품지정 택배사
        String reship;
        //반송주소
        String returnAddress;
        //반품비용
        int returnpee;
        //샵 이름

        //투판다
        String topanda;
        //교환비용
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
        //승인여부
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
        //환불 완료된 갯수?
        int completeRefund;
        //취소 완료된 갯수
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
