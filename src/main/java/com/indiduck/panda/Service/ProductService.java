package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.*;
import com.indiduck.panda.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    FileRepository fileRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    ProductOptionService productOptionService;

    @Autowired
    FileService fileService;

    @Autowired
    ShopRepository shopRepository;

    public Product createNewProduct(String user, List<String> thumb, String title, String descriptoin,
                                    List<String> images, List<ProductOption> options, int type, String notice,String noticeValue,String pandam) {

        Product newProduct = Product.newProDuct(title, descriptoin, type, notice,noticeValue,pandam);
        productRepository.save(newProduct);

        images.forEach(e -> {
            File filebyFilepath1 = fileService.getFilebyFilepath(e);
            newProduct.setImage(filebyFilepath1);
        });
        thumb.forEach(e -> {
            File fileByFilename = fileRepository.myqueryfind(e);
            newProduct.setThumbImage(fileService.getFilebyFilepath(e));
        });

        options.forEach(e ->
                {
                    ProductOption productOption = productOptionService.saveOption(e);
                    newProduct.setProductOptions(productOption);
                }

        );
        Optional<User> byEmail = userRepository.findByEmail(user);
        Optional<Shop> byUserUsername = shopRepository.findByUserId(byEmail.get().getId());
        newProduct.setShop(byUserUsername.get());


        return newProduct;

    }

    //더미데이터
    public void addtempProudct() {
        //더미데이터 만들기기
        Optional<Product> byId = productRepository.findById(13l);
//        Product product = byId.get();
//            Product copy = Product.copyPro(product,999);
//        Product save = productRepository.save(copy);
//        System.out.println(" 카피요청받음 "+save.getId());
//        Optional<Product> byId1 = productRepository.findById(save.getId());
//        System.out.println("생성된거 = " + byId1.get().getProductName());
//        for(int i =0; i<=10; i++)
//        {
//            System.out.println(" 로직싫애");
//            Product newProduct = Product.newProDuct("더미데이터제목+"+"i", "더미밸류"+"i", 2, "{\"a\":\"신발\",\"b\":\"색상\",\"c\":\"치수\",\"d\":\"제조자\",\"e\":\"제조국\",\"f\":\"취급시주의사항\",\"g\":\"품질보증기준\",\"h\":\"A/S책임자와 전화번호\",\"i\":\"\",\"j\":\"\",\"k\":\"\",\"l\":\"\",\"m\":\"\",\"n\":\"\",\"o\":\"\"}");
//            productRepository.save(newProduct);
//            File filebyFilepath = fileService.getFilebyFilepath("shop2@gmail.com/ab3307ea-2fd7-4b46-9d05-c295c0c929e511d91d21-17de-499b-b4b5-8b0cde94f4cb디테일2.png");
//            newProduct.setImage(filebyFilepath);
//            File filebyFilepath1 = fileService.getFilebyFilepath("shop2@gmail.com/5c2fd420-7977-4a08-8ade-d0b5251adcaa54df513d-eb60-4fe6-8afa-987b633b1a0d썸1.png");
//            newProduct.setThumbImage(filebyFilepath1);
//            newProduct.setProductOptions(byId.get().getProductOptions().get(0));
//            newProduct.setShop(byId.get().getShop());
//        }




        //        for(int i = 0; i>=200; i ++)
//        {
//            System.out.println("카피번호 = " + i);
//            Product copy = Product.copyPro(product,i);
//            productRepository.save(copy);
//
//        }
    }

    public void addFileProduct(Long file, Long product, String type) {
        Optional<File> byId1 = fileRepository.findById(file);
        Optional<Product> byId = productRepository.findById(product);
//        System.out.println(type);
        if (type.equals("thumb")) {
            byId.get().setThumbImage(byId1.get());

        } else {

            byId.get().setImage(byId1.get());

        }

    }

    public void editStatus(long productId, String type) {
        Optional<Product> byId = productRepository.findById(productId);
        if (type.equals("판매중지")) {
            byId.get().stopSale();

        } else if (type.equals("상품삭제")) {
            byId.get().delProduct();
            byId.get().stopSale();


        } else if (type.equals("판매재개")) {
            byId.get().restartSale();
        }

    }

    public void editText(Long product, String type, String text) {
        Optional<Product> byId = productRepository.findById(product);
        if (type.equals("name")) {
            byId.get().setName(text);
        } else if(type.equals("desc")) {
            byId.get().setDesc(text);

        }else if(type.equals("pandaMessage"))
        {
            byId.get().setPandaMessage(text);
        }
    }


    public void editLow(Long product, int type, String no,String nV) {
        Optional<Product> byId = productRepository.findById(product);
        byId.get().setNotice(no);
        byId.get().setNoticeValue(nV);
//        byId.get().setLowvalue(law);
        byId.get().changeType(type);

    }
}
