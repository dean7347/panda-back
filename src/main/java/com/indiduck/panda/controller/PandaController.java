package com.indiduck.panda.controller;

import com.indiduck.panda.Repository.OrderDetailRepository;
import com.indiduck.panda.Repository.PandaRespository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.PandaService;

import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import com.indiduck.panda.domain.dto.ResultDto;
import com.indiduck.panda.jwt.JwtTokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.text.spi.DateFormatProvider;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@Slf4j
public class PandaController {

    @Autowired
    private JwtTokenProvider jwtTokenUtil;

    @Autowired
    PandaRespository pandaRespository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;

    @Autowired
    PandaService pandaService;


    @GetMapping("/api/ispanda")
    @ResponseBody
    public ResponseEntity<?> ispanda(@CurrentSecurityContext(expression = "authentication")
                                             Authentication authentication) {

        String usernameFromToken = authentication.getName();
//        System.out.println("usernameFromToken = " + usernameFromToken);
        if (usernameFromToken != null) {
            Optional<User> byEmail = userRepository.findByEmail(usernameFromToken);

            if (!byEmail.isEmpty()) {
                Optional<Panda> byUser = pandaRespository.findByUser(byEmail.get());

                if (byUser.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.OK).body(new isPandaDto(false, false));
                } else {
                    if (byUser.get().isRecognize()) {
                        return ResponseEntity.status(HttpStatus.OK).body(new isPandaDto(true, true));

                    }
                }
                return ResponseEntity.status(HttpStatus.OK).body(new isPandaDto(true, false));

            }
        }

        return ResponseEntity.ok(new TFMessageDto(false, "실패했습니다"));

    }


    //판다승인하기
    @RequestMapping(value = "/api/admin/confirmregpanda", method = RequestMethod.POST)
    public ResponseEntity<?> confirmpanda(@CurrentSecurityContext(expression = "authentication")
                                              Authentication authentication, @RequestBody RegID regID) throws Exception {


        Panda panda = pandaService.regPandaResult(regID.getRegid(),regID.result);
        if (panda == null) {
            return ResponseEntity.ok(new ResultDto(false, "판다신청에 실패했습니다"));
        }
        return ResponseEntity.ok(new ResultDto(true, "판다신청에 성공했습니다"));
    }


    @RequestMapping(value = "/api/regpanda", method = RequestMethod.POST)
    public ResponseEntity<?> regPanda(@CurrentSecurityContext(expression = "authentication")
                                              Authentication authentication, @RequestBody RegPandaDAO regPandaDAO) throws Exception {


        Panda panda = pandaService.newPanda(authentication.getName(),
                regPandaDAO.pandaname,
                regPandaDAO.mainchname,
                regPandaDAO.intcategory,
                regPandaDAO.termsagree,
                regPandaDAO.infoagree);

        if (panda == null) {
            return ResponseEntity.ok(new ResultDto(false, "판다신청에 실패했습니다"));
        }
        return ResponseEntity.ok(new ResultDto(true, "판다신청에 성공했습니다"));
    }

    //자신이 판다인지 조회하고 시작날짜 끝날자, 스테이터스로 조회한다다
    @RequestMapping(value = "/api/pandadashboard", method = RequestMethod.POST)
    public ResponseEntity<?> pandaDashBoard(@CurrentSecurityContext(expression = "authentication")
                                                    Authentication authentication, @RequestBody PandaDashBoardDto pandaDashBoardDto) throws Exception {

        LocalDateTime startDay = LocalDateTime.of(pandaDashBoardDto.startYear, pandaDashBoardDto.startMonth + 1, pandaDashBoardDto.startDay
                , 0, 0, 0, 0);
        LocalDateTime endDay = LocalDateTime.of(pandaDashBoardDto.endYear, pandaDashBoardDto.endMonth + 1, pandaDashBoardDto.endDay
                , 23, 59, 59, 999999999);

//        System.out.println("startDay = " + startDay);
//        System.out.println("endDay = " + endDay);
//        System.out.println("pandaDashBoardDto = " + pandaDashBoardDto.startDay);
        try {
            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Panda panda = byEmail.get().getPanda();
            List<PandaDashboardDtoType> pandaDashboardDtoList = new ArrayList<>();
            List<OrderDetail> odList = new ArrayList<>();


            if (pandaDashBoardDto.status.equals("all")) {
//                System.out.println("전체");
                Optional<List<OrderDetail>> one = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급예정, startDay, endDay);
                Optional<List<OrderDetail>> two = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급완료, startDay, endDay);
                Optional<List<OrderDetail>> three = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급대기, startDay, endDay);
                if (!one.isEmpty()) {
                    odList.addAll(one.get());
                }
                if (!two.isEmpty()) {
                    odList.addAll(two.get());
                }
                if (!three.isEmpty()) {
                    odList.addAll(three.get());
                }

            } else if (pandaDashBoardDto.status.equals("지급완료")) {
//                System.out.println("정산완료");
                Optional<List<OrderDetail>> one = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급예정, startDay, endDay);
                if (!one.isEmpty()) {
                    odList.addAll(one.get());
                }


            } else if (pandaDashBoardDto.status.equals("지급예정")) {
                Optional<List<OrderDetail>> two = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급완료, startDay, endDay);
                Optional<List<OrderDetail>> three = orderDetailRepository.findByPandaAndPaymentStatusAndFinishedAtBetween(panda, PaymentStatus.지급대기, startDay, endDay);

                if (!two.isEmpty()) {
                    odList.addAll(two.get());
                }
                if (!three.isEmpty()) {
                    odList.addAll(three.get());
                }

            }
            int finish = 0;
            int yet = 0;

            for (OrderDetail orderDetail : odList) {
                if (orderDetail.getPaymentStatus() == PaymentStatus.지급완료) {
                    finish += orderDetail.getPandaMoney();
                } else if (orderDetail.getPaymentStatus() == PaymentStatus.지급예정 || orderDetail.getPaymentStatus() == PaymentStatus.지급대기) {
                    yet += orderDetail.getPandaMoney();
                }
                pandaDashboardDtoList.add(new PandaDashboardDtoType(orderDetail.getId(), orderDetail.getPaymentStatus().toString(), orderDetail.getPandaMoney(), orderDetail.getFinishedAt()));

            }

            return ResponseEntity.ok(new DashboardDto(true, pandaDashboardDtoList, finish, yet,odList));


        } catch (Exception E) {
            System.out.println("E = " + E);
            return ResponseEntity.ok(new DashboardDto(false, null, 0, 0,null));

        }


    }

    @RequestMapping(value = "/api/pandadashboardmain", method = RequestMethod.POST)
    public ResponseEntity<?> pandaDashBoardMain(@CurrentSecurityContext(expression = "authentication")
                                                        Authentication authentication, @RequestBody PandaDashBoardMain pandaDashBoardMain) throws Exception {


        try {
            LocalDateTime startDay = LocalDateTime.of(pandaDashBoardMain.year, 1, 1
                    , 0, 0, 0, 0);
            LocalDateTime endDay = LocalDateTime.of(pandaDashBoardMain.year, 12, 31
                    , 23, 59, 59, 999999999);

            String name = authentication.getName();
            Optional<User> byEmail = userRepository.findByEmail(name);
            Panda panda = byEmail.get().getPanda();
            Optional<List<OrderDetail>> byPandaAndPaymentStatusNotNull = orderDetailRepository.findByPandaAndPaymentStatusNotNullAndFinishedAtBetween(panda, startDay, endDay);
            int fin = 0;
            int yet = 0;
            int[] salse = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (OrderDetail orderDetail : byPandaAndPaymentStatusNotNull.get()) {
                if (orderDetail.getPaymentStatus() == PaymentStatus.지급완료) {
                    fin += orderDetail.getPandaMoney();
                } else if (orderDetail.getPaymentStatus() == PaymentStatus.지급대기 || orderDetail.getPaymentStatus() == PaymentStatus.지급예정) {
                    yet += orderDetail.getPandaMoney();
                }
                int monthValue = orderDetail.getFinishedAt().getMonthValue();
                salse[monthValue - 1]++;

            }

            return ResponseEntity.ok(new DashBoardMainDto(salse, fin, yet));

        } catch (Exception E) {
            System.out.println("E = " + E);
            return ResponseEntity.ok(new DashBoardMainDto(null, 0, 0));


        }


    }

    @RequestMapping(value = "/api/editPanda", method = RequestMethod.POST)
    public ResponseEntity<?> editPanda(@CurrentSecurityContext(expression = "authentication")
                                              Authentication authentication, @RequestBody EditPandaDAO editPandaDAO) throws Exception{
        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Panda panda = byEmail.get().getPanda();
        Panda result = pandaService.editPanda(editPandaDAO.target, editPandaDAO.values, panda);
        if(result==null)
        {
            log.error(authentication.getName()+"의 판다 정보 변경 실패");
            return ResponseEntity.ok(new ShopController.shopControllerResultDto(false,"변경실패 "));

        }


        log.info(authentication.getName()+"의 판다정보 변경 성공");
        return ResponseEntity.ok(new ShopController.shopControllerResultDto(true,"변경성공 "));

    }

    @Data
    private static class DashBoardMainDto {
        int salse[] = null;
        long finish;
        long expect;

        public DashBoardMainDto(int salse[], long finish, long expect) {
            this.salse = salse;
            this.finish = finish;
            this.expect = expect;
        }
    }


    @Data
    private  class DashboardDto {
        boolean success;
        List<PandaDashboardDtoType> pandaDashboardDtoList = new ArrayList<>();
        List<SettleListOrderDetailForPanda> pandaData = new ArrayList<>();

        int finMoney;
        int expectMoney;


        public DashboardDto(boolean success, List<PandaDashboardDtoType> pandaDashboardDtoList, int f, int e, List<OrderDetail> detailId) {
            this.success = success;
            this.pandaDashboardDtoList = pandaDashboardDtoList;
            this.finMoney = f;
            this.expectMoney = e;
            for (OrderDetail orderDetail : detailId) {
                pandaData.add(new SettleListOrderDetailForPanda(orderDetail));
            }
        }
    }


    @Data
    private static class PandaDashboardDtoType {
        Long id;
        String status;
        int money;
        LocalDateTime localDateTime;

        public PandaDashboardDtoType(Long id, String status, int money, LocalDateTime localDateTime) {
            this.id = id;
            this.status = status;
            this.money = money;
            this.localDateTime = localDateTime;
        }
    }

    @Data
    private static class RegID{
        long regid;
        String result;
    }
    @Data
    private static class PandaDashBoardDto {
        int startYear;
        int startMonth;
        int startDay;

        int endYear;
        int endMonth;
        int endDay;

        String status;


    }

    @Data
    static class isPandaDto {
        boolean ispanda;
        boolean approve;

        public isPandaDto(boolean b, boolean a) {
            ispanda = b;
            approve = a;
        }
    }

    @Data
    static class RegPandaDAO {
        String pandaname;
        String mainchname;
        String intcategory;
        boolean termsagree;
        boolean infoagree;
    }

    @Data
    private static class PandaDashBoardMain {
        int year;
    }

    @Data
    private static class EditPandaDAO {
        String target;
        String values;
    }


    @Data
    public class SettleListOrderDetailForPanda
    {
        //주문번호
        long id;
        //판매 옵션
        String productName;

        //상품 판매 갯수
        int count;
        //판다머니
        long pandaMoney;
        //정산예정일
        LocalDateTime expectDay;
        //정산일자
        LocalDateTime finishDay;
        //정산상태
        String state;

        public SettleListOrderDetailForPanda(OrderDetail od) {
            this.id = od.getId();
            this.productName = od.getOptions().getProduct().getProductName();
            this.count = od.getProductCount();
            this.pandaMoney = od.getPandaMoney();
            this.expectDay = od.getSettlePanda().getEnrollSettle();
            this.finishDay = od.getSettlePanda().getDepositDate();
            if(od.getSettlePanda().isDeposit())
            {
                this.state = "정산완료";
            }else
            {
                this.state = "정산예정";
            }
        }
    }
}
