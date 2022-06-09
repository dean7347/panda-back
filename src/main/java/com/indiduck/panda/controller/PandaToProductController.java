package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.PandaToProductRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.PandaToProductService;
import com.indiduck.panda.Service.VerifyService;
import com.indiduck.panda.domain.Panda;
import com.indiduck.panda.domain.PandaToProduct;
import com.indiduck.panda.domain.User;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@Slf4j
public class PandaToProductController {

    @Autowired
    PandaToProductService pandaToProductService;
    @Autowired
    PandaToProductRepository pandaToProductRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VerifyService verifyService;

    //판다를 신청합니다
    //사전조건: 인증된 회원이어야 한다
    @RequestMapping(value = "/api/addpropanda", method = RequestMethod.POST)
    public ResponseEntity<?> addPandaToProduct(@CurrentSecurityContext(expression = "authentication")
                                                       Authentication authentication, @RequestBody AddPandaProDAO addPandaProDAO) throws Exception {

        String name = authentication.getName();
        userRepository.findByEmail(name);
        PandaToProduct pandaToProduct = pandaToProductService.newPtP(authentication.getName(), addPandaProDAO.productId, addPandaProDAO.link);

        if (pandaToProduct == null) {
            log.error("판다신청에  실패했습니다");
            return ResponseEntity.ok(new ResultDto(false, "판다신청에 실패했습니다"));
        }
        log.info("판다신청을 완료했습니다");
        return ResponseEntity.ok(new ResultDto(true, "성공적으로 등록되었습니다"));

    }

    //상품 옵션에서 판다 아이디 가져오는 로직
    @RequestMapping(value = "/api/getpandas_by_id", method = RequestMethod.GET)
    public ResponseEntity<?> viewDetail(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,
                                        @RequestParam(name = "id") Long productid) throws Exception {

        List<PandaToProduct> byProduct = pandaToProductRepository.findByProductIdAndIsDel(productid, false);


        if (!byProduct.isEmpty()) {
            return ResponseEntity.ok(new PandasDto(true, byProduct));
        }

        return ResponseEntity.ok(new PandasDto(false, null));

    }

    @RequestMapping(value = "/api/pandadashboardmovie", method = RequestMethod.GET)
    public ResponseEntity<?> viewDetail(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication) throws Exception {

        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Panda panda = byEmail.get().getPanda();
        Optional<List<PandaToProduct>> byPanda = pandaToProductRepository.findByPandaAndIsDel(panda, false);
        if (!byPanda.isEmpty()) {
            return ResponseEntity.ok(new PandasDto(true, byPanda.get()));
        }

        return ResponseEntity.ok(new PandasDto(false, null));

    }

    @RequestMapping(value = "/api/pandamoviedel", method = RequestMethod.POST)
    public ResponseEntity<?> pandaMovieDel(@CurrentSecurityContext(expression = "authentication")
                                                   Authentication authentication, @RequestBody PandaMovieID pandaMovieID) throws Exception {
        boolean b = verifyService.verifyPandaForMovie(authentication.getName(), pandaMovieID.id);
        if(!b)
        {
            log.error(authentication.getName()+"의 영상 삭제 실패");

            return ResponseEntity.ok((new TFMessageDto(false,"영상의 주인이 아닙니다")));
        }
        pandaToProductService.delURL(pandaMovieID.id);

        log.info(authentication.getName()+"의 영상 삭제");        
        return ResponseEntity.ok(new TFMessageDto(true, "변경성공"));

    }

    @RequestMapping(value = "/api/pandamovieedit", method = RequestMethod.POST)
    public ResponseEntity<?> pandaMovieEdit(@CurrentSecurityContext(expression = "authentication")
                                                    Authentication authentication, @RequestBody PandaMovieID pandaMovieID) throws Exception {
        boolean b = verifyService.verifyPandaForMovie(authentication.getName(), pandaMovieID.id);
        if(!b)
        {
            log.error(authentication.getName()+"의 영상 수정실패");

            return ResponseEntity.ok((new TFMessageDto(false,"영상의 주인이 아닙니다")));
        }
        System.out.println("pandaMovieID = " + pandaMovieID);
        PandaToProduct pandaToProduct = pandaToProductService.editURL(pandaMovieID.id, pandaMovieID.editUrl);

        log.info(authentication.getName()+"의 영상 변경 성공");

        return ResponseEntity.ok(new TFMessageDto(true, "변경성공"));

    }

    @Data
    static class PandasDto {
        boolean success;
        List<PandasDetail> details = new ArrayList<>();

        public PandasDto(boolean t, List<PandaToProduct> ptp) {
            success = t;
            if(ptp==null)
            {
                return;
            }
            for (PandaToProduct pandaToProduct : ptp) {
                details.add(new PandasDetail(pandaToProduct.getLink(), pandaToProduct.getPanda().getPandaName(),
                        pandaToProduct.getPanda().getId(), pandaToProduct.getId()));
            }

        }

    }

    @Data
    static class AddPandaProDAO {
        String link;
        Long productId;
    }

    @Data
    static class PandasDetail {
        String link;
        String panda;
        Long pandaId;
        long pandaToProductId;

        public PandasDetail(String getLink, String getPanda, Long getPandaId, Long pdid) {
            link = getLink;
            panda = getPanda;
            pandaId = getPandaId;
            pandaToProductId = pdid;
        }
    }

    @Data
    private class ResultDto {
        boolean success;
        String message;

        public ResultDto(boolean b, String message) {
            success = b;
            message = message;
        }
    }

    @Data
    private static class PandaMovieID {
        long id;
        String editUrl;
    }
}
