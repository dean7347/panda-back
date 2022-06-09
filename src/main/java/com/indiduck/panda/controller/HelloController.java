package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.FileRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.config.Wrapper;
import com.indiduck.panda.domain.File;
import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.ProductOption;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class HelloController {

    @Autowired
    FileRepository fileRepository;
    @Autowired
    ProductRepository productRepository;

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public void createShop(@RequestParam("data")String data, @RequestBody String test) throws Exception{
//        @RequestParam(value = "file",defaultValue = nu) MultipartFile files,
//        System.out.println("files = " + files);
//        System.out.println("data = " + data);
//        System.out.println("test = " + test);

    }
//    @RequestMapping(value = "/**/{[path:[^\\.]*}")
//    public String redirect() {
//        // Forward to home page so that route is preserved.
//        return "index";
//    }
//    @RequestMapping( method = {RequestMethod.OPTIONS, RequestMethod.GET}, path = {"/login"} )
//    public String forwardAngularPathsP() {
//        System.out.println("겟호출");
//        return "index";
//    }
//    @RequestMapping( method = {RequestMethod.OPTIONS, RequestMethod.POST}, path = {"/login"} )
//    public String forwardAngularPathsG() {
//        System.out.println("포스트호출");
//
//        return "forward:/index.html";
//    }
//    @RequestMapping(value = "/test2", method = RequestMethod.GET)
//    public Page<ProductDto> viewAll(@CurrentSecurityContext(expression = "authentication")
//                                             Authentication authentication, Pageable pageable) throws Exception {
//
//        Page<Product> result = productRepository.findAll(pageable);
//        Page<ProductDto> tomap = result.map(e -> new ProductDto(e));
//
//
//        return tomap;
//    }
//
//    @Data
//    static class ProductDto {
//        String proname;
//        String shopname;
//        List<FileDtopro> images=new ArrayList<>();
//
//        public ProductDto(Product pro) {
//            proname=pro.getProductName();
//            shopname=pro.getShop().getShopName();
////            images= pro.getImages();
//            List<File> getImages = pro.getImages();
//            for (File getImage : getImages) {
//                if(getImage.isIsthumb()){
//                    images.add(new FileDtopro(getImage));
//                }
//            }
//        }
//    }
//    @Data
//    static class FileDtopro {
//        String filepath;
//        public FileDtopro(File file){
//                filepath= file.getFilepath();
//        }
//    }
//

//    //v1
//    @RequestMapping(value = "/test2", method = RequestMethod.GET)
//    public List<ProductDto> viewAll(@CurrentSecurityContext(expression = "authentication")
//                                             Authentication authentication, Pageable pageable) throws Exception {
//
//        List<Product> pro =productRepository.findAll();
//        List<ProductDto> result = pro.stream()
//                .map(o -> new ProductDto(o))
//                .collect(Collectors.toList());
//
//        return  result;
//    }
//
//    @Data
//    static class ProductDto {
//        String productName;
//        List<FileDtop> images;
//        public ProductDto(Product pro)
//        {
//            productName= pro.getProductName();
//            images = pro.getImages().stream()
//                    .map( e  -> new FileDtop(e))
//                    .collect(Collectors.toList());
//        }
//    }
//
//    @Data
//    private static class FileDtop {
//        String filePath;
//        public FileDtop(File f){
//           filePath= f.getFilepath();
//
//        }
//    }
//    ///v1 end

//    @RequestMapping(value = "/test2", method = RequestMethod.POST)
//    public void createShop(@RequestBody TestDAO testDAO) throws Exception {
//
//        System.out.println("testDAO = " + testDAO);
////        testDAO.toString()
////        System.out.println("array = " + array);
////        @RequestBody String test
////        System.out.println("test = " + test);
//
//
//    }
//@RequestMapping(value = "/test2", method = RequestMethod.POST)
//    public void createNewProduct(List<String> thumb) {
//        System.out.println("thumb = " + thumb);
//        thumb.forEach( e->{
//            File byFilePath = fileRepository.findByFilePath(e);
//            System.out.println("byFilePath = " + byFilePath);
//        });
//
//
//    }

//    @RequestMapping(value = "/test2", method = RequestMethod.POST)
//    public ResponseEntity<?> createShop(@CurrentSecurityContext(expression = "authentication")
//                                                Authentication authentication, @RequestBody CreateProductDAO createProductDAO) throws Exception {
//
//
//        System.out.println("createProductDAO = " + createProductDAO);
//        return ResponseEntity.ok("생성 완료");
//    }
    //상품 수정
    //상품 삭제
    //상품 조회


//    //== 상품 DAO == //
//    @Data
//    static class CreateProductDAO {
//        //        private List<Map<Long,String>> tumb;
//        private String title;
//        private String description;
//        private List<String> images;
//        private List<ProductOption> Options;
//
//
//
//    }
//
//    @Data
//    static class TestDAO {
//        private List<Long> array;
//        private String test;
//
//
//    }



}
