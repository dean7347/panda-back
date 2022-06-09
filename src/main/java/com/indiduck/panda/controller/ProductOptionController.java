package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.Service.ProductOptionService;
import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.ProductOption;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class ProductOptionController {

    @Autowired
    private final ProductOptionService productOptionService;

    @RequestMapping(value = "/api/editoption", method = RequestMethod.POST)
    public ResponseEntity<?> viewSearch(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody OptionDao optiondao) throws Exception {
        try {
            log.info(authentication.getName() + "의 옵션 수정 시도");      
            productOptionService.delOption(optiondao.optionId);
        }catch (Exception e)
        {
            log.error(authentication.getName() + "의 옵션 수정 시도 실패");

            return ResponseEntity.ok(new OptionSuccessDto(false));


        }
        return ResponseEntity.ok(new OptionSuccessDto(true));

    }


    @RequestMapping(value = "/api/addoption", method = RequestMethod.POST)
    public ResponseEntity<?> addOption(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody OptionAddDao optionAddDao) throws Exception {

        try {
            ProductOption productOption = productOptionService.addOption(optionAddDao.OptionName, optionAddDao.getOptionCount(), optionAddDao.getOptionPrice(),optionAddDao.productId);
        }catch (Exception e)
        {
            return ResponseEntity.ok(new OptionSuccessDto(false));

        }
        return ResponseEntity.ok(new OptionSuccessDto(true));

    }


    @RequestMapping(value = "/api/editproductoption", method = RequestMethod.POST)
    public ResponseEntity<?> editOption(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication, @RequestBody OptionEditDao optionEditDao) throws Exception {


        try {
            log.info(authentication.getName() + "의 에딧 프로덕트 옵션 시도");
            productOptionService.editOption(optionEditDao.OptionName, optionEditDao.optionCount, optionEditDao.optionPrice, optionEditDao.OptionId);
        }catch (Exception e)
        {
            log.error(authentication.getName() + "의 에딧 프로덕트 옵션 시도 실패");

            return ResponseEntity.ok(new OptionSuccessDto(false));

        }
        log.info(authentication.getName() + "의 에딧 프로덕트 옵션 성공");

        return ResponseEntity.ok(new OptionSuccessDto(true));

    }
    @Data
    private static class OptionDao {
        long optionId;
    }

    @Data
    private class OptionSuccessDto {
        boolean success;
        public OptionSuccessDto(boolean b) {
            success=b;
        }
    }
    @Data
    private static class OptionAddDao {
        Long productId;
        String OptionName;
        int optionCount;
        int optionPrice;
    }
    @Data
    private static class OptionEditDao {
        Long OptionId;
        String OptionName;
        int optionCount;
        int optionPrice;
    }
}
