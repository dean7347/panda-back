package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.ShopRepository;
import com.indiduck.panda.Repository.UserOrderRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.ShopService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import com.indiduck.panda.domain.dto.ResultDto;
import com.indiduck.panda.jwt.JwtTokenProvider;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private JwtTokenProvider jwtTokenUtil;
    @Autowired
    ShopRepository shopRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserOrderRepository userOrderRepository;

    //샵 신청 확인하기
    @RequestMapping(value = "/api/admin/confirmregshop", method = RequestMethod.POST)
    public ResponseEntity<?> confirmregshop(@CurrentSecurityContext(expression = "authentication")
                                                  Authentication authentication, @RequestBody RegShopID regID) throws Exception {
        Shop shop = shopService.regShopResult(regID.getRegid(), regID.result);
        if (shop == null) {
            return ResponseEntity.ok(new ResultDto(false, "판다신청에 실패했습니다"));
        }
        return ResponseEntity.ok(new ResultDto(true, "판다신청에 성공했습니다"));
    }

    //샵 생성메소드
    @RequestMapping(value = "/api/createShop", method = RequestMethod.POST)
    public ResponseEntity<?> createShop(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody CreateShopDAO createShopDAO) throws Exception{
        String name = authentication.getName();

//        System.out.println("createShopDAO = " + createShopDAO);

        try{ Shop newShop = shopService.createNewShop(
                name,
                createShopDAO.shopName,
                createShopDAO.representative,
                createShopDAO.crn,createShopDAO.telnum,
                createShopDAO.freepee,createShopDAO.nofree,createShopDAO.priPhone,
                createShopDAO.csPhone,createShopDAO.csTime,createShopDAO.toPanda,
                createShopDAO.reship,createShopDAO.returnpee,createShopDAO.tradepee,
                createShopDAO.returnaddress,createShopDAO.candate,createShopDAO.noreturn,

                createShopDAO.tagree,createShopDAO.iagree,createShopDAO.comaddress,createShopDAO.avdtime,  createShopDAO.descriptionProduct);
            if (newShop!=null){
                log.info(authentication.getName()+"샵등록성공");
                return ResponseEntity.ok(new shopControllerResultDto(true,"샵등록 성공"));
            }
        }catch (Exception e){
            log.error(authentication.getName()+e+"상점 등록 실패");
            return ResponseEntity.ok(new shopControllerResultDto(false,"샵등록 실패 "));
        }

        log.error(authentication.getName()+"샵 등록 실패");
        return ResponseEntity.ok(new shopControllerResultDto(false,"샵등록 실패 "));

    }

    //샵수정메소드
    @RequestMapping(value = "/api/editShop", method = RequestMethod.POST)
    public ResponseEntity<?> editShop(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody EditShopDAO editShopDAO) throws Exception{
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Shop shop = byEmail.get().getShop();
        Shop result = shopService.editShop(editShopDAO.target, editShopDAO.values, shop);
        if(result==null)
        {
            log.error(authentication.getName() + "의 상점정보 변경 실패");
            return ResponseEntity.ok(new shopControllerResultDto(false,"변경실패 "));

        }
        

        log.info(authentication.getName()+"의 상점정보 변경 성공");
        return ResponseEntity.ok(new shopControllerResultDto(true,"변경성공 "));

    }


    @GetMapping("/api/haveshop")
    @ResponseBody
    public ResponseEntity<?> haveShop(@CurrentSecurityContext(expression = "authentication")Authentication authentication)
    {

        String usernameFromToken = authentication.getName();
        if(usernameFromToken !=null)
        {
            Optional<User> byEmail = userRepository.findByEmail(usernameFromToken);
//            System.out.println("byEmail = " + byEmail.get().getEmail());

            Optional<Shop> byUser = shopRepository.findByUserId(byEmail.get().getId());
//            System.out.println("byUser = " + byUser);

            if(byUser.isEmpty())
            {
                return ResponseEntity.status(HttpStatus.OK).body(new haveShopDto("null",false,false,false));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new haveShopDto(byUser.get().getShopName(),true,
                    byUser.get().isApprove(),true));
        }
        return ResponseEntity.ok(new TFMessageDto(false,"상점을찾을수 없습니다"));

    }


    //샵 대시보드 메인
    @RequestMapping(value = "/api/shop/dashboard", method = RequestMethod.GET)
    public ResponseEntity<?> dashboard(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, Pageable pageable) throws Exception{
        try{
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Page<UserOrder> allByShop = userOrderRepository.findAllByShop(byEmail.get().getShop(), pageable);

            long totalElements = allByShop.getTotalElements();
            int totalPages = allByShop.getTotalPages();



            List<userOrderShopDto> uosd= new ArrayList<>();
            for (UserOrder userOrder : allByShop) {
                uosd.add(new userOrderShopDto(userOrder));

            }

            return ResponseEntity.ok(new uorsDto(true,totalElements,totalPages,uosd));

        }catch (Exception e)
        {
            return ResponseEntity.ok(new uorsDto(false,0,0,null));
        }

    }

    //샵 대시보드 메인
    @RequestMapping(value = "/api/shop/dashboard/orderStatus", method = RequestMethod.GET)
    public ResponseEntity<?> dashboardnochecked(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication, Pageable pageable,@RequestParam String orderstatus) throws Exception{


        try{
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Page<UserOrder> allByShop = userOrderRepository.findAllByShopAndOrderStatus(byEmail.get().getShop(),OrderStatus.valueOf(orderstatus) , pageable);

            long totalElements = allByShop.getTotalElements();
            int totalPages = allByShop.getTotalPages();



            List<userOrderShopDto> uosd= new ArrayList<>();
            for (UserOrder userOrder : allByShop) {
                uosd.add(new userOrderShopDto(userOrder));

            }

            return ResponseEntity.ok(new uorsDto(true,totalElements,totalPages,uosd));

        }catch (Exception e)
        {
            return ResponseEntity.ok(new uorsDto(false,0,0,null));
        }

    }

    //샵대시보드 정산
    @RequestMapping(value = "/api/shop/sellerdashboard", method = RequestMethod.POST)
    public ResponseEntity<?> dashboardSettle(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication, @RequestBody ConfirmDto confirmDto) throws Exception{


        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Shop shop = byEmail.get().getShop();
//        shopRepository


        return ResponseEntity.ok(new shopControllerResultDto(false,"상태변경에 실패했습니다"));


    }


    //주문확인
//    @RequestMapping(value = "/api/shop/confirm", method = RequestMethod.POST)
//    public ResponseEntity<?> dashboard(@CurrentSecurityContext(expression = "authentication")
//                                               Authentication authentication, @RequestBody ShopDashBoardDto shopDashBoardDto) throws Exception{
//
//        try{
//
//        }catch(Exception E){
//
//            return ResponseEntity.ok(new TFMessageDto(false,"오류가 발생했습니다"));
//
//        }
//
//        return ResponseEntity.ok(new shopControllerResultDto(false,"상태변경에 실패했습니다"));
//
//
//    }


    //샵 대시보드 메인 V2
    @RequestMapping(value = "/api/shopdashboardmainv2", method = RequestMethod.POST)
    public ResponseEntity<?> shopDashBoardForOrderNumber(@CurrentSecurityContext(expression = "authentication")
                                                                 Authentication authentication,@RequestBody ShopDashBoardMain shopDashBoardMain) throws Exception {


        try{
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Shop shop = byEmail.get().getShop();
            int newOrderSize =0;
            int readyOrder =0;
            int cROrder =0;
            int finOrder=0;
            LocalDateTime startDay= LocalDateTime.of(shopDashBoardMain.year,1,1
                    ,0,0,0,0);
            LocalDateTime endDay= LocalDateTime.of(shopDashBoardMain.year,12,31
                    ,23,59,59,999999999);


            Optional<List<UserOrder>> newOrder = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.결제완료);

            Optional<List<UserOrder>> ready = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.준비중);
            Optional<List<UserOrder>> ready2 = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.발송중);
            Optional<List<UserOrder>> ready3 = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.배송완료);

            Optional<List<UserOrder>> cancelReturn = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.상점확인중);
            Optional<List<UserOrder>> cancelReturn2 = userOrderRepository.findByShopAndOrderStatus(shop, OrderStatus.환불대기);

            Optional<List<UserOrder>> finish1 = userOrderRepository.findByShopAndOrderStatusAndCreatedAtBetween(shop, OrderStatus.구매확정,startDay,endDay);
            Optional<List<UserOrder>> finish2 = userOrderRepository.findByShopAndOrderStatusAndCreatedAtBetween(shop, OrderStatus.환불완료,startDay,endDay);
            Optional<List<UserOrder>> finish3 = userOrderRepository.findByShopAndOrderStatusAndCreatedAtBetween(shop, OrderStatus.교환완료,startDay,endDay);

            LocalDateTime tnow =  LocalDateTime.now();
            LocalDateTime chartEnd= LocalDateTime.of(tnow.getYear(),tnow.getMonth(),tnow.getDayOfMonth()
                    ,23,59,59,999999999);

            LocalDateTime told= tnow.minusDays(11);
            LocalDateTime chartstart= LocalDateTime.of(told.getYear(),told.getMonth(),told.getDayOfMonth()
                    ,0,0,0,0);

            Optional<List<UserOrder>> chartData = userOrderRepository.findByShopAndCreatedAtBetween(shop, chartstart, tnow);

            newOrderSize=newOrder.get().size();
            readyOrder=ready.get().size()+ready2.get().size()+ready3.get().size();
            cROrder=cancelReturn.get().size()+cancelReturn2.get().size();
            finOrder=finish1.get().size()+finish2.get().size()+finish3.get().size();
            //<-오래된 데이터   --------- 최근데이터 ->
            int[] money={0,0,0,0,0,0,0,0,0,0,0,0};
            int[] quantity={0,0,0,0,0,0,0,0,0,0,0,0};
            String[] day={tnow.minusDays(11).toString(),
                    tnow.minusDays(10).toString(),tnow.minusDays(9).toString(),tnow.minusDays(8).toString()
                    ,tnow.minusDays(7).toString(),tnow.minusDays(6).toString(),tnow.minusDays(5).toString()
                    ,tnow.minusDays(4).toString(),tnow.minusDays(3).toString(),tnow.minusDays(2).toString()
                    ,tnow.minusDays(1).toString(),tnow.toString()};
            System.out.println("chartData.get() = " + chartData.get());
            for (UserOrder userOrder : chartData.get()) {
//                tnow.getD

//                int i = chartEnd.getDayOfMonth() - userOrder.getCreatedAt().getDayOfMonth();
//                int num= 11-i;
                long between = ChronoUnit.DAYS.between(userOrder.getCreatedAt(),tnow);
                int num = 11-(int)between;
                money[num]+=userOrder.getPureAmount();
                quantity[num]++;
//                System.out.println("칼람 " + between+"주문번호"+userOrder.getId());

            }
//            System.out.println("날짜  = " + day[0]+"//돈 : "+money[0]+"//수량 : "+quantity[0]);
//            System.out.println("날짜   = " + day[1]+"//돈 : "+money[1]+"//수량 : "+quantity[1]);
//            System.out.println("날짜   = " + day[2]+"//돈 : "+money[2]+"//수량 : "+quantity[2]);
//            System.out.println("날짜   = " + day[3]+"//돈 : "+money[3]+"//수량 : "+quantity[3]);
//            System.out.println("날짜   = " + day[4]+"//돈 : "+money[4]+"//수량 : "+quantity[4]);
//            System.out.println("날짜   = " + day[5]+"//돈 : "+money[5]+"//수량 : "+quantity[5]);
//            System.out.println("날짜   = " + day[6]+"//돈 : "+money[6]+"//수량 : "+quantity[6]);
//            System.out.println("날짜   = " + day[7]+"//돈 : "+money[7]+"//수량 : "+quantity[7]);
//            System.out.println("날짜   = " + day[8]+"//돈 : "+money[8]+"//수량 : "+quantity[8]);
//            System.out.println("날짜   = " + day[9]+"//돈 : "+money[9]+"//수량 : "+quantity[9]);
//            System.out.println("날짜   = " + day[10]+"//돈 : "+money[10]+"//수량 : "+quantity[10]);
//            System.out.println("날짜   = " + day[11]+"//돈 : "+money[11]+"//수량 : "+quantity[11]);



            return ResponseEntity.ok(new ShopDashBoardMainV2Dto(true,newOrderSize,readyOrder,finOrder,cROrder,money,quantity,day));

        } catch (Exception e)
        {
            System.out.println("E = " + e);

            return ResponseEntity.ok(new ShopDashBoardMainV2Dto(false,0,0,0,0,null,null,null));



        }


    }



    //==DAO DTO ==//
    @Data
    private static class ShopDashBoardMain
    {
        int year;
    }
    @Data
    private static class ShopDashBoardMainV2Dto
    {
        boolean success;
        //신규주문
        int newOrder;
        //배송준비
        int readyOrder;
        //취소 반품
        int cancelReturn;
        //완료주문
        int completeBuy;

        //정산금액
        int[] money;
        //주문건수
        int[] quantity;
        //기준일자
        String[] day;

        public ShopDashBoardMainV2Dto(boolean tf,int newOrder, int readyOrder, int completeBuy, int cancelReturn, int[] money, int[] quantity, String[] d) {
            this.success=tf;
            this.newOrder = newOrder;
            this.readyOrder = readyOrder;
            this.completeBuy = completeBuy;
            this.cancelReturn = cancelReturn;
            this.money = money;
            this.quantity = quantity;
            this.day=d;
        }
    }

    @Data
    private static class ShopDashBoardDto {
        LocalDateTime startDay;
        LocalDateTime endDay;
        String status;


    }

    @Data
    static class userOrderShopDto {

        private Long userOrderId;
        private String orderuser;
        private String orderuserPhone;
        private String receiverName;
        private String receiverZipCode;
        private String receiverAddress;
        private String receiverPhone;
        private  int amount;
        private int pureamount;
        private int shipprice;
        private int fullprice;

        private OrderStatus orderStatus;
        private List<OrderItems> orders=new ArrayList<>();


        public userOrderShopDto(UserOrder uo){
            userOrderId=uo.getId();
            orderuser=uo.getUserId().getUserRName();
            orderuserPhone=uo.getUserId().getUserPhoneNumber();
            receiverName=uo.getReveiverName();
            receiverZipCode=uo.getReceiverZipCode();
            receiverAddress=uo.getReceiverAddress();
            receiverPhone=uo.getReceiverPhone();
            orderStatus=uo.getOrderStatus();
            amount=uo.getAmount();
            pureamount=uo.getPureAmount();
            if(uo.getPureAmount()>=uo.getFreeprice())
            {
                shipprice=0;



            }else
            {
                shipprice=uo.getShipPrice();
            }
            fullprice=uo.getFullprice();

            HashSet<Long> pro=new HashSet<>();
            //pro에 프로덕트 목록을 만든다
            for (OrderDetail od : uo.getDetail()) {
                pro.add(od.getProducts().getId());
            }

            for (Long aLong : pro) {
                orders.add(new OrderItems(aLong));
            }

            for (OrderDetail orderDetail : uo.getDetail()) {
                for (OrderItems order : orders) {
                    if(orderDetail.getProducts().getId()==order.productId)
                    {
                        order.productName=orderDetail.getProducts().getProductName();
                        order.options.add(new ProductOptions(orderDetail));
                    }
                }
            }




        }

        @Data
        private static class OrderItems {
            Long productId;
            String productName;
            List<ProductOptions> options=new ArrayList<>();
            public OrderItems(Long productId)
            {
                this.productId=productId;


            }
        }
        @Data
        private static class ProductOptions {
            Long optionId;
            String optionName;
            int optionCount;
            boolean ispanda;
            public ProductOptions(OrderDetail od)
            {
                optionId=od.getOptions().getId();
                optionName=od.getOptions().getOptionName();
                optionCount=od.getProductCount();
                if(od.getPanda() !=null){
                    ispanda=true;
            }else
                {
                    ispanda=false;
                }
            }
        }
    }
        @Data
        static class CreateShopDAO {
        private String shopName;
        private String representative;
        private String crn;
        private String telnum;
        private int freepee;
        private int nofree;
        private String priPhone;
        private String csPhone;
        private String csTime;
        private String toPanda;
        private String reship;
        private int returnpee;
        private int tradepee;
        private String returnaddress;
        private String candate;
        private String noreturn;
        private boolean tagree;
        private boolean iagree;
        private String comaddress;
        private String avdtime;
        private String descriptionProduct;

    }

    @Data
    static class haveShopDto {

        private String shopName;
        private boolean success;
        private boolean isShop;
        private boolean isApprove;
        public haveShopDto(String name,Boolean isShop,Boolean ap,Boolean su){

            this.shopName=name;
            this.isShop=isShop;
            this.isApprove =ap;
            this.success=su;

        }
    }
    @Data
    static class shopControllerResultDto {
        boolean success;
        String  message;

        public shopControllerResultDto(boolean b, String mes) {
            success = b;
            message = mes;
        }
    }

    @Data
    static class uorsDto {
        boolean success;
        long totalElements;
        int totalPages;
        List<userOrderShopDto> uosd;

        public uorsDto(boolean b,long te,int tp,  List<userOrderShopDto> getuosd) {
            success = b;
            totalElements=te;
            totalPages=tp;
            uosd=getuosd;

        }
    }

    @Data
    private class uorsCheck {
        boolean success;
        int num;
        public uorsCheck(boolean b, int numb) {
            success=b;
            num=numb;
        }
    }

    @Data
    private static  class OrderStatusDto {
        String orderstatus;
    }

    @Data
    private static class ConfirmDto {
        Long userorderId;
    }

    @Data
    private static class EditShopDAO {
        String target;
        String values;
    }
    @Data
    private static class RegShopID{
        long regid;
        String result;

    }
}
