package com.indiduck.panda.Service;


import com.indiduck.panda.Repository.SettlePandaRepository;
import com.indiduck.panda.Repository.SettleShopRepository;
import com.indiduck.panda.domain.SettlePanda;
import com.indiduck.panda.domain.SettleShop;
import com.indiduck.panda.domain.UserOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SettleService {
    @Autowired
    private final SettlePandaRepository settlePandaRepository;
    @Autowired
    private final SettleShopRepository settleShopRepository;

    public String doingSettle(long id, String type) {

        if(type.equals("shop"))
        {
            Optional<SettleShop> byId = settleShopRepository.findById(id);
            SettleShop settleShop = byId.get();
            settleShop.depoist();
            for (UserOrder userOrder : settleShop.getUserOrder()) {
                userOrder.setDepoistCompleted();
            }
            return "shop";
        }else if(type.equals("panda"))
        {
            Optional<SettlePanda> byId = settlePandaRepository.findById(id);
            SettlePanda settlePanda = byId.get();
            settlePanda.deposit();
            return "panda";
        }
        return null;
    }
}
