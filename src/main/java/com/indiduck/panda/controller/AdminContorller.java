package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.*;
import com.indiduck.panda.Service.SettleService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RequiredArgsConstructor
@RestController
public class AdminContorller {

    @Autowired
    private final SettlePandaRepository settlePandaRepository;
    @Autowired
    private final SettleShopRepository settleShopRepository;
    @Autowired
    private final ShopRepository shopRepository;
    @Autowired
    private final PandaRespository pandaRepoSitory;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final SettleService settleService;


    //샵 정산완료

    //샵,판다 정산 완료
    @RequestMapping(value = "/api/admin/pandaSettleConfirm", method = RequestMethod.POST)
    public ResponseEntity<?> pandaSettleConfirm(@CurrentSecurityContext(expression = "authentication")
                                                 Authentication authentication,  @RequestBody GetID getid) throws Exception {

        String s = settleService.doingSettle(getid.id, getid.type);
        if(s==null)
        {
            return ResponseEntity.ok(new TFMessageDto(false,"정산실패"));
        }
        return ResponseEntity.ok(new TFMessageDto(true,"정산입력성공"));

    }

    //정산해야될 판다 리스트
    @RequestMapping(value = "/api/admin/pandaSettleList", method = RequestMethod.GET)
    public ResponseEntity<?> settlePanda(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication , Pageable pageable) throws Exception {
//        System.out.println("pageable = " + pageable);
        Page<SettlePanda> page = settlePandaRepository.findByIsDeposit(false,pageable);
        System.out.println("settlePandaByDepoistIs = " +page);


//        return ResponseEntity.ok(new TFMessageDto(true,"zz"));

        return ResponseEntity.ok(new PandaListDTO(true,page));
    }


    //완료된 판다 정산목록보기
    @RequestMapping(value = "/api/admin/completepandaSettleList", method = RequestMethod.GET)
    public ResponseEntity<?> settlePandaCompleteList(@CurrentSecurityContext(expression = "authentication")
                                                             Authentication authentication, Pageable pageable) throws Exception {
        Page<SettlePanda> page = settlePandaRepository.findByIsDeposit(true,pageable);
        System.out.println("settlePandaByDepoistIs = " +page);


        return ResponseEntity.ok(new PandaListDTO(true,page));
    }


    //정산해야될 샵 리스트
    @RequestMapping(value = "/api/admin/shopSettleList", method = RequestMethod.GET)
    public ResponseEntity<?> settleShop(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication, Pageable pageable) throws Exception {

        Page<SettleShop> byIsDeposit = settleShopRepository.findByIsDeposit(false,pageable);

        return ResponseEntity.ok(new ShopListDTO(true,byIsDeposit));

    }



    //완료된 샵 정산목록 보기
    @RequestMapping(value = "/api/admin/completeshopSettleList", method = RequestMethod.GET)
    public ResponseEntity<?> settleShopCompleteList(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, Pageable pageable) throws Exception {


        Page<SettleShop> byIsDeposit = settleShopRepository.findByIsDeposit(true,pageable);

        //액셀 시트


        return ResponseEntity.ok(new ShopListDTO(true,byIsDeposit));

    }

    //신청중인 상점보기
    @RequestMapping(value = "/api/admin/applyShopList", method = RequestMethod.GET)
    public ResponseEntity<?> applyShopList(@CurrentSecurityContext(expression = "authentication")
                                                            Authentication authentication, Pageable pageable) throws Exception {


        Page<Shop> byIsApproveAndIsOpen = shopRepository.findByIsApproveAndIsOpen(pageable, false, false);


        return ResponseEntity.ok(new ApproveShopDTO(true,byIsApproveAndIsOpen));

    }
    //신청중인 판다보기
    @RequestMapping(value = "/api/admin/applyPandaList", method = RequestMethod.GET)
    public ResponseEntity<?> applyPandaList(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication, Pageable pageable) throws Exception {


        Page<Panda> byRecognize = pandaRepoSitory.findByRecognize(pageable, false);

        return ResponseEntity.ok(new ApprovePandaDTO(true,byRecognize));

    }


    @Data
    private static class GetID
    {
        long id;
        String type;
    }
    @Data
    private class ApprovePandaDTO
    {
        boolean success;
        long totalElement;
        int totalPage;
        List<APLISTPANDA> aplist=new ArrayList<>();

        public ApprovePandaDTO(boolean success,Page<Panda> aplists) {
            this.success = success;
            this.totalElement = aplists.getTotalElements();
            this.totalPage = aplists.getTotalPages();
            for (Panda aplist : aplists) {
                this.aplist.add(new APLISTPANDA(aplist));
            }
        }

    }
    @Data
    private class APLISTPANDA{
        long id;
        String Category;
        String MainCH;
        String pandaName;
        public APLISTPANDA(Panda panda) {
            this.id = panda.getId();
            this.Category = panda.getIntCategory();
            this.MainCH = panda.getMainCh();
            this.pandaName = panda.getPandaName();
        }


    }

    @Data
    private class ApproveShopDTO
    {
        boolean success;
        long totalElement;
        int totalPage;
        List<APLIST> aplist=new ArrayList<>();

        public ApproveShopDTO(boolean success,Page<Shop> aplists) {
            this.success = success;
            this.totalElement = aplists.getTotalElements();
            this.totalPage = aplists.getTotalPages();
            for (Shop aplist : aplists) {
               this.aplist.add(new APLIST(aplist));
            }
        }

    }
    @Data
    private class APLIST{
        long id;
        String CRN;
        String NUMBER;
        String shopName;

        public APLIST(Shop shop) {
            this.id = shop.getId();
            this.CRN = shop.getCRN();
            this.NUMBER = shop.getNumber();
            this.shopName=shop.getShopName();
        }
    }

    @Data
    private class PandaListDTO
    {
        boolean success;
        long totalElement;
        int totalPage;
        List<SettlePandaDetail> settlePandaDetails=new ArrayList<>();


        public PandaListDTO(boolean success, Page<SettlePanda> settlePandaPage) {
            this.success = success;
            totalElement=settlePandaPage.getTotalElements();
            totalPage=settlePandaPage.getTotalPages();
            for (SettlePanda settlePanda : settlePandaPage.getContent()) {
                settlePandaDetails.add(new SettlePandaDetail(settlePanda.getId(),settlePanda.getEnrollSettle(),settlePanda.getDepositDate(),
                        settlePanda.getDepoist(),settlePanda.isDeposit(),
                        settlePanda.getPanda().getPandaName(),settlePanda.getOrderDetails()));
            }
        }
    }

    @Data
    private class ShopListDTO
    {
        boolean success;
        long totalElement;
        int totalPage;
        List<SettleShopDetail> settleShopDetails=new ArrayList<>();


        public ShopListDTO(boolean success, Page<SettleShop> settlePandaPage) {
            this.success = success;
            totalElement=settlePandaPage.getTotalElements();
            totalPage=settlePandaPage.getTotalPages();
            for (SettleShop settleShop : settlePandaPage.getContent()) {
                settleShopDetails.add(new SettleShopDetail(settleShop.getId(),settleShop.getEnrollSettle(),settleShop.getDepositDate(),
                        settleShop.getDepoist(),settleShop.isDeposit(),
                        settleShop.getShop().getShopName(),settleShop.getUserOrder(),settleShop));
            }
        }
    }

    @Data
    private class SettlePandaDetail
    {
        //아이디
        long id;
        //정산등록날짜
        LocalDateTime enrollSettle;
        //정산날짜
        LocalDateTime depoistDate;
        //정산된 금액
        int deposit;
        //입금 여부
        boolean isdeposit;
        //정산될 판다
        String pandaname;
        //정산될 OrderDetailId
        List<SettleListUserOrderForPanda> slofp = new ArrayList<>();
        public SettlePandaDetail(long id, LocalDateTime enrollSettle, LocalDateTime depoistDate, int deposit, boolean isdeposit, String pandaname, List<OrderDetail> detailId) {
            this.id = id;
            this.enrollSettle = enrollSettle;
            this.depoistDate = depoistDate;
            this.deposit = deposit;
            this.isdeposit = isdeposit;
            this.pandaname = pandaname;

            for (OrderDetail orderDetail : detailId) {
                slofp.add(new SettleListUserOrderForPanda(orderDetail,enrollSettle,depoistDate,isdeposit));
            }
        }


    }
    @Data
    public class SettleListUserOrderForPanda
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

        public SettleListUserOrderForPanda(OrderDetail od,LocalDateTime ed, LocalDateTime fd, boolean state) {
            this.id = od.getId();
            this.productName = od.getOptions().getProduct().getProductName();
            this.count = od.getProductCount();
            this.pandaMoney = od.getPandaMoney();
            this.expectDay = ed;
            this.finishDay = fd;
            if(state)
            {
                this.state = "정산완료";
            }else
            {
                this.state = "정산예정";
            }
        }
    }

    @Data
    private class SettleShopDetail
    {
        //아이디
        long id;
        //정산등록날짜
        LocalDateTime enrollSettle;
        //정산날짜
        LocalDateTime depoistDate;
        //정산된 금액
        int deposit;
        //입금 여부
        boolean isdeposit;
        //정산될 판다
        String shopName;
        //정산될 OrderDetailId
//        List<Long> detailIds=new ArrayList<>();

        List<UserOrderController.ShopDashboardDtoType> shopDashboardDtoTypeList = new ArrayList<>();


        public SettleShopDetail(long id, LocalDateTime enrollSettle, LocalDateTime depoistDate, int deposit, boolean isdeposit, String shopName, List<UserOrder> detailId,SettleShop ss) {
            this.id = id;
            this.enrollSettle = enrollSettle;
            this.depoistDate = depoistDate;
            this.deposit = deposit;
            this.isdeposit = isdeposit;
            this.shopName = shopName;
            List<UserOrder> userOrders = userOrderRepository.findBySettleShop(ss).get();
            System.out.println("ss = " + ss.getUserOrder());
            System.out.println("userOrders = " + userOrders);
            for (UserOrder userOrder : userOrders) {
                System.out.println("userOrder = " + userOrder);
                this.shopDashboardDtoTypeList.add(new UserOrderController.ShopDashboardDtoType(userOrder));

            }

        }


    }


}
