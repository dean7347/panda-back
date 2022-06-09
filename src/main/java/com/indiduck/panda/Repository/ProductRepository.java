package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Transactional(readOnly = true)
public interface ProductRepository extends JpaRepository<Product,Long> {


    @Query("select p from Product p")
    Page<Product> findFreeView(Pageable pageable);
//        Page<Product> findAllBy(Pageable pageable);

    Page<Product> findByDeletedAndSalesAndNameContaining(Pageable pageable,boolean del,boolean sa,String productName);
    Page<Product> findByShop(Pageable pageable, Shop shop);
    Page<Product> findByShopAndDeleted(Pageable pageable, Shop shop,boolean tf);
    Page<Product> findAllByDeletedAndSales(Pageable pageable,boolean tf,boolean tf2);

    //회원탈퇴로직
    Optional<Product> findByShopAndDeleted(Shop shop, boolean delete);





}
