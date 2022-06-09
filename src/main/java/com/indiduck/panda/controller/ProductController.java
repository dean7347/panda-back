package com.indiduck.panda.controller;


import com.amazonaws.services.s3.AmazonS3Client;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.FileService;
import com.indiduck.panda.Service.ProductService;

import com.indiduck.panda.Service.S3Uploader;
import com.indiduck.panda.Service.VerifyService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import com.indiduck.panda.domain.dto.FileDao;

import com.indiduck.panda.util.MD5Generator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Lob;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class ProductController {


    @Autowired
    private final ProductService productService;
    @Autowired
    private final FileService fileService;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;
    @Autowired
    private final VerifyService verifyService;


    @RequestMapping(value = "/api/amzonefile", method = RequestMethod.POST)
    public ResponseEntity<?> createFileAmazon(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestPart("file") MultipartFile files) throws Exception{
//
        log.info(authentication.getName() + "의 파일 생성");
        try{

        String origFilename = files.getOriginalFilename();
        String etx=origFilename.substring(origFilename.lastIndexOf(".") + 1);
        String filename = new MD5Generator(origFilename).toString()+System.currentTimeMillis();
        String savePath = authentication.getName();


        String aStatic = s3Uploader.upload(authentication.getName(),files);

//            System.out.println("aStatic = " + aStatic);


        FileDao fileDao = new FileDao();
        fileDao.setOrigFilename(origFilename);
        fileDao.setFilename(filename+"."+etx);
        fileDao.setFilePath(aStatic);
        Long fileId = fileService.saveFile(fileDao);

//            return  ResponseEntity.ok(new ResultDtoM(false,aStatic));

        return  ResponseEntity.ok(new FileDto(true,fileDao.getFilePath(),fileDao.getFilename()));

        }
        catch (Exception e)
        {

            log.error(authentication.getName() + "의 파일 생성실패 :"+e);

            return  ResponseEntity.ok(new ResultDtoM(false,"익셉션"+e.toString()));

        }
//            return  ResponseEntity.ok(new ResultDtoM(false,files.getOriginalFilename()));
//
////        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("상품사진 업로드 실패");




    }



    @RequestMapping(value = "/api/deletefile", method = RequestMethod.POST)
    public ResponseEntity<?> createFileAmazon(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication, @RequestBody FileDelDao fileDelDao) throws Exception{
//
        boolean b = verifyService.verifyShopForFile(authentication.getName(), fileDelDao.filepath);
        if(!b)
        {
            log.error(authentication.getName()+"의 파일 삭제 검증 실패");
            return  ResponseEntity.ok(new ResultDto(false));

        }
        try{
            log.info(authentication.getName()+"의 파일 삭제 시도 ");
            s3Uploader.delete(fileDelDao.filepath);
            fileService.delFile(fileDelDao.filepath);
            return  ResponseEntity.ok(new ResultDto(true));

        }
        catch (Exception e)
        {
            log.error(authentication.getName()+"의 파일 삭제 시도 실패");

            return  ResponseEntity.ok(new ResultDto(false));

        }




    }

    //테스트용으로 카피를 만든다
    @RequestMapping(value = "/api/copyproduct", method = RequestMethod.POST)
    public ResponseEntity<?> createFileAmazon(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication) throws Exception{
//
        try{

            productService.addtempProudct();
            return  ResponseEntity.ok(new ResultDto(true));

        }
        catch (Exception e)
        {
            return  ResponseEntity.ok(new ResultDto(false));

        }




    }






//    //상품사진 등록
//    @RequestMapping(value = "/api/createFile2", method = RequestMethod.POST)
//    public ResponseEntity<?> createFile(@CurrentSecurityContext(expression = "authentication")
//                                                Authentication authentication, @RequestPart("file") MultipartFile files) throws Exception{
//        try {
//            String origFilename = files.getOriginalFilename();
//            String etx=origFilename.substring(origFilename.lastIndexOf(".") + 1);
//            String filename = new MD5Generator(origFilename).toString()+System.currentTimeMillis();
//            /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
//            String savePath = System.getProperty("user.dir") + "\\files"+"\\"+authentication.getName();
//            /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
//            if (!new java.io.File(savePath).exists()) {
//                try{
//                    new java.io.File(savePath).mkdirs();
//                }
//                catch(Exception e){
//                    e.getStackTrace();
//                }
//            }
//
//            String filePath = savePath + "\\" + filename;
//            files.transferTo(new java.io.File(filePath+"."+etx));
//
//            FileDao fileDao = new FileDao();
//            fileDao.setOrigFilename(origFilename);
//            fileDao.setFilename(filename+"."+etx);
//            fileDao.setFilePath(filePath+"."+etx);
//            Long fileId = fileService.saveFile(fileDao);
//
//            return  ResponseEntity.ok(new FileDto(true,fileDao.getFilePath(),fileDao.getFilename()));
//
//
//
//        } catch(Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("상품사진 업로드 실패");
//        }
//
//
//
//    }


    @RequestMapping(value = "/api/fileedit", method = RequestMethod.POST)
    public ResponseEntity<?> editFileAmazon(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication, @RequestPart("file") MultipartFile files,
                                            @RequestPart("proId") String proId,@RequestPart("type") String type
                                            ) throws Exception{

        log.info(authentication.getName() + "의 파일 수정 시도");
        try{

            String origFilename = files.getOriginalFilename();
            String etx=origFilename.substring(origFilename.lastIndexOf(".") + 1);
            String filename = new MD5Generator(origFilename).toString()+System.currentTimeMillis();
            String savePath = authentication.getName();


            String aStatic = s3Uploader.upload(authentication.getName(),files);




            FileDao fileDao = new FileDao();
            fileDao.setOrigFilename(origFilename);
            fileDao.setFilename(filename+"."+etx);
            fileDao.setFilePath(aStatic);
            Long fileId = fileService.saveFile(fileDao);
            productService.addFileProduct(fileId,Long.parseLong(proId),type);

//            return  ResponseEntity.ok(new ResultDtoM(false,aStatic));

            return  ResponseEntity.ok(new FileDto(true,fileDao.getFilePath(),fileDao.getFilename()));

        }
        catch (Exception e)
        {
            log.error(authentication.getName() + "의 파일 수정 시도 실패"+ e);

            return  ResponseEntity.ok(new ResultDtoM(false,"익셉션"+e.toString()));

        }

    }
    @RequestMapping(value = "/api/edittextproduct", method = RequestMethod.POST)
    public ResponseEntity<?> editTextProduct(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication, @RequestBody EditTextDao editTextDao) throws Exception {
        log.info(authentication.getName() + "의 에딧 텍스트 프로덕트 시도");
        productService.editText(editTextDao.proId, editTextDao.type, editTextDao.param);
        return ResponseEntity.ok(new ResultDto(true));
    }


    @RequestMapping(value = "/api/regnewproduct", method = RequestMethod.POST)
    public ResponseEntity<?> createnewProduct(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication, @RequestBody CreateProductDAO createProductDAO) throws Exception {

        List<String> notice = createProductDAO.notice;
        String s = notice.toString();

        List<String> list = Arrays.asList(s.substring(1, s.length() - 1).split(", "));


        Product product= productService.createNewProduct(
                authentication.getName(),
                createProductDAO.thumb,
                createProductDAO.title,
                createProductDAO.description,
                createProductDAO.images,
                createProductDAO.Options,
                createProductDAO.type,
                createProductDAO.notice.toString(),
                createProductDAO.noticeValue.toString(),
                createProductDAO.pandaMessage

        );

        if(product==null){
            return ResponseEntity.ok(new ResultDto(false));

        }
        log.info(authentication.getName()+"의 새상품 등록"+product.getId());
        return ResponseEntity.ok(new ResultDto(true));
    }

    @RequestMapping(value = "/api/editlaw", method = RequestMethod.POST)
    public ResponseEntity<?> editProductLaw(@CurrentSecurityContext(expression = "authentication")
                                                      Authentication authentication, @RequestBody LawDAO lawDAO) throws Exception {

        try{
            log.info(authentication.getName() + "의 에딧 로우 실행 ");
            productService.editLow(lawDAO.productId, lawDAO.type, lawDAO.notice.toString(), lawDAO.noticeValue.toString());
            return ResponseEntity.ok(new ResultDto(true));

        }catch (Exception e)
        {
            log.error(authentication.getName() + "의 에딧 로우 실행 실패"+e);

            return ResponseEntity.ok(new ResultDto(false));

        }



    }

    @RequestMapping(value = "/api/product/changeprostatus", method = RequestMethod.POST)
    public ResponseEntity<?> delProdcut(@CurrentSecurityContext(expression = "authentication")
                                                    Authentication authentication, @RequestBody EditDao psc) throws Exception {

        log.info(authentication.getName() + "의 체인지 오더 시도" + psc.proId + "를 " + psc.type + "로 변경");
        try{
            productService.editStatus(psc.proId,psc.type);
            return ResponseEntity.ok(new ResultDto(true));

        }catch (Exception e)
        {
            log.error(authentication.getName() + "의 체인지 오더 시도" + psc.proId + "를 " + psc.type + "로 변경 실패");

            return ResponseEntity.ok(new ResultDto(false));

        }



    }



//    @RequestMapping(value = "/api/editinfomation", method = RequestMethod.POST)
//    public ResponseEntity<?> infomationProduct(@CurrentSecurityContext(expression = "authentication")
//                                                      Authentication authentication, @RequestBody CreateProductDAO createProductDAO) throws Exception {
//        System.out.println("createProductDAO = " + createProductDAO);
//
////        Product product=productService.createNewProduct(
////                authentication.getName(),
////                createProductDAO.thumb,
////                createProductDAO.title,
////                createProductDAO.description,
////                createProductDAO.images,
////                createProductDAO.Options,
////                createProductDAO.type
////                createProductDAO.lowform
////
////        );
//
////        if(product==null){
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("상품생성 실패");
////        }
//        return ResponseEntity.ok(new ResultDto(true));
//    }
     //상품 수정
    //상품 삭제
    //상품 조회
     @RequestMapping(value = "/api/preview", method = RequestMethod.GET)
     public ResponseEntity<?> viewAll(@CurrentSecurityContext(expression = "authentication")
                                                             Authentication authentication,@PageableDefault(sort = "productRegAt", direction = Sort.Direction.DESC)Pageable pageable) throws Exception {

        if(!authentication.isAuthenticated())
        {
            return ResponseEntity.ok(new TFMessageDto(false,"preview 조회 실패"));


        }
         System.out.println("authentication = " + authentication);
//         System.out.println("limit+offset = " + limit + offset);
         Page<Product> result = productRepository.findAllByDeletedAndSales(pageable,false,true);
         Page<ProductDto> tomap = result.map(e -> new ProductDto(e));


         if(!tomap.isEmpty()){
             return  ResponseEntity.ok(tomap);
         }
         return ResponseEntity.ok(new TFMessageDto(false,"preview 조회 실패"));

     }


    @RequestMapping(value = "/api/myproduct", method = RequestMethod.GET)
    public ResponseEntity<?> viewMyProductAll(@CurrentSecurityContext(expression = "authentication")
                                             Authentication authentication,Pageable pageable) throws Exception {


        String name = authentication.getName();
        Optional<User> byEmail = userRepository.findByEmail(name);
        Shop shop = byEmail.get().getShop();
        Page<Product> result = productRepository.findByShopAndDeleted(pageable,shop,false);
        Page<ProductDto> tomap = result.map(e -> new ProductDto(e));


        if(!tomap.isEmpty()){
            return  ResponseEntity.ok(tomap);
        }
        return ResponseEntity.ok(new TFMessageDto(false,"마이프로덕트 조회 실패"));

    }



    @RequestMapping(value = "/api/searchpreview", method = RequestMethod.GET)
    public ResponseEntity<?> viewSearch(@CurrentSecurityContext(expression = "authentication")
                                             Authentication authentication,Pageable pageable,
                                        @RequestParam(name = "productname") String productName) throws Exception {

        Page<Product> result = productRepository.findByDeletedAndSalesAndNameContaining(pageable,false,true, productName);

        Page<ProductDto> tomap = result.map(e -> new ProductDto(e));


        if(!tomap.isEmpty()){
            return  ResponseEntity.ok(tomap);
        }
        return ResponseEntity.ok(new TFMessageDto(false,"서치 조회 실패"));
    }

    //detail
    @RequestMapping(value = "/api/product/products_by_id", method = RequestMethod.GET)
    public ResponseEntity<?> viewDetail(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,
                                        @RequestParam(name = "id") Long productid) throws Exception {

        Optional<Product> byId = productRepository.findById(productid);
        ProductDetailDto productDetailDto = new ProductDetailDto(true,byId.get());


        if(!byId.isEmpty())
        {
            return ResponseEntity.ok(productDetailDto);
        }


        return ResponseEntity.ok(new TFMessageDto(false,"뷰죠회 실패"));
    }
    @Data
    static class ProductDetailDto {
        boolean success;
        String productName;
        String productDesc;
        //상품타입과 값. 통신판매업법
        int type;
        String lowform;

        //판매중지
        boolean salse;
        //삭제된상품?
        boolean isdel;

        //상점정보
        private String shopName;
        //representative
        private String representative;
        //crn
        private String CRN;
        //telnum
        private String number;
        //freepee
        private int freePrice;
        //nofree 택배비용
        private int nofree;
        //priPhone

        //csPhone
        private String csPhone;
        //csTime
        @Lob
        private String csTime;
        //toPanda
        @Lob
        private String toPanda;
        //reship
        private String reship;
        //returnpee
        private int returnpee;
        //tradepee
        private int tradepee;
        //comaddress
        private String comaddress;
        //returnaddress
        private String returnaddress;
        //candate
        @Lob
        private String candate;
        //noreturn
        @Lob
        private String noreturn;
        private String AVDtime;


        //고시정보
        private List<String> notice=new ArrayList<>();
        private List<String> noticeV=new ArrayList<>();
        private String pandaMessage;

        //판다 메시지
        //반품교환정보
        //판매자 정보보
       List<DetailOptionDto> Poptions=new ArrayList<>();
        List<FileDtopro> detailImages=new ArrayList<>();
        List<FileDtopro> thumbs=new ArrayList<>();
        public ProductDetailDto(boolean t,Product detail) {
            success=t;
            salse=detail.isSales();
            isdel=detail.isDeleted();
            productName=detail.getProductName();
            productDesc=detail.getProductDesc();
            this.pandaMessage=detail.getPandaMessage();
            this.notice = Arrays.asList(detail.getNotice().substring(1,
                    detail.getNotice().length() - 1).split(", "));
            this.noticeV=Arrays.asList(detail.getNoticeValue().substring(1,
                    detail.getNoticeValue().length() - 1).split(", "));

            type = detail.getType();
//            lowform =detail.getLowvalue();
            Shop shop = detail.getShop();
            //상점정보
            shopName=shop.getShopName();
            representative= shop.getRepresentative();
            CRN = shop.getCRN();
            number=shop.getNumber();
            freePrice=shop.getFreePrice();
            nofree=shop.getNofree();
            csPhone=shop.getCsPhone();
            csTime=shop.getCsTime();
            toPanda=shop.getToPanda();
            reship=shop.getReship();
            returnpee=shop.getReturnpee();
            tradepee=shop.getTradepee();
            comaddress=shop.getComaddress();
            returnaddress=shop.getReturnaddress();
            candate=shop.getCandate();
            noreturn=shop.getNoreturn();
            AVDtime=shop.getAVDtime();



            List<File> getImages = detail.getImages();
            for (File getImage : getImages) {
                if(getImage.isIsthumb()){
                    thumbs.add(new FileDtopro(getImage));
                }else{
                    detailImages.add(new FileDtopro(getImage));
                }
            }
            List <ProductOption> options =detail.getProductOptions();
            for (ProductOption option : options) {
                if(option.isSales()) {
                    Poptions.add(new DetailOptionDto(option));
                }
            }

        }
    }
    @Data
    static class DetailOptionDto {
        String optionName;
        Long optionId;
        int optionStock;
        int optionPrice;
        public DetailOptionDto(ProductOption o){

                optionId = o.getId();
                optionName = o.getOptionName();
                optionStock = o.getOptionStock();
                optionPrice = o.getOptionPrice();

        }

    }

    @Data
    static class ProductDto {
        Long proId;
        String proname;
        String shopname;
        boolean isSalse;
        List<FileDtopro> images=new ArrayList<>();

        public ProductDto(Product pro) {
            proId = pro.getId();
            proname=pro.getProductName();
            shopname=pro.getShop().getShopName();
            isSalse=pro.isSales();

            List<File> getImages = pro.getImages();
            for (File getImage : getImages) {
                if(getImage.isIsthumb()){
                    images.add(new FileDtopro(getImage));
                }
            }
        }
    }
    @Data
    static class FileDtopro {
        String filepath;
        public FileDtopro(File file){
            filepath= file.getFilepath();
        }
    }



         //== 상품 DAO == //

    @Data
    static class CreateProductDAO {
        private List<String> thumb;
        private String title;
        private String description;
        private List<String> images;
        private List<ProductOption> Options;
        private int type;
        private List<String> notice;
        private List<String> noticeValue;
        private String pandaMessage;
//        private String lowform;

    }

    @Data
    static class haveShopDto {

        private String shopName;
        private boolean isShop;
        public haveShopDto(String name,Boolean isShop){

            this.shopName=name;
            this.isShop=isShop;

        }
    }

    @Data
    static class FileDto {

        private boolean success;
        private String filePath;
        private String fileName;
        public FileDto(boolean success,String filePath,String fileName){
            this.success=success;
            this.filePath=filePath;
            this.fileName=fileName;
        }
        public FileDto(boolean success){
            this.success=success;

        }
    }

    @Data
    static class ResultDto {

        private boolean success;

        public ResultDto(boolean success){
            this.success=success;

        }
    }

    @Data
    static class ResultDtoM {

        private boolean success;
        private String messgae;
        public ResultDtoM(boolean success,String mes){
            this.success=success;
            this.messgae=mes;

        }
    }
    @Data
    static class LawDAO {
        private List<String> notice;
        private List<String> noticeValue;
        private int type;
        private Long productId;

    }

    @Data
    static private class EditDao {
        private Long proId;
        private String type;
    }

    @Data
    private static class FileDelDao {
        String filepath;
    }
    @Data
    private static class EditTextDao {
        String param;
        String type;
        Long proId;
    }


}
