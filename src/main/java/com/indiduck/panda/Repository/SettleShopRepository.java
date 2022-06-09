package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.SettleShop;
import com.indiduck.panda.domain.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettleShopRepository extends JpaRepository<SettleShop,Long> {
    Page<SettleShop> findByIsDeposit(boolean tf, Pageable pageable);
}
