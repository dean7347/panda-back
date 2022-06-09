package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.*;
import com.indiduck.panda.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PandaService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PandaRespository pandaRespository;
    @Autowired
    private final UserOrderRepository userOrderRepository;
    @Autowired
    private final SettlePandaRepository settlePandaRepository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;

//    public Panda confirmSettlePanda(long id)
//    {
//        return
//    }
    public Panda regPandaResult(long id, String result)
    {
        Optional<Panda> byId = pandaRespository.findById(id);
        Panda panda = byId.get();

        if(result.equals("confirm"))
        {
            panda.confrimPanda();
            return panda;
        }else
        {
            panda.getUser().deletePanda();
            pandaRespository.delete(panda);
            return null;
        }
    }

    public Panda newPanda(String user, String pandaName, String mainCh,
                          String intCategory, boolean terms, boolean info) {

        Optional<User> byEmail = userRepository.findByEmail(user);

        if (!byEmail.isEmpty()) {
            if (byEmail.get().getPanda() == null) {
                Panda panda = Panda.newPanda(pandaName, mainCh, intCategory, terms, info);
                panda.setUser(byEmail.get());
                pandaRespository.save(panda);
                return panda;

            }
        }

        return null;

    }


    public SettlePanda SettleLogic(Panda panda) {
//        Optional<List<UserOrder>> byShopAndPaymentStatusAndEnrollSettle = userOrderRepository.findByPandaAndPaymentStatusAndEnrollSettlePanda(panda, PaymentStatus.지급대기, false);
        Optional<List<OrderDetail>> byPandaAndPaymentStatusAndEnrollSettle = orderDetailRepository.findByPandaAndOrderStatusAndPaymentStatusAndEnrollSettle(panda,OrderStatus.구매확정, PaymentStatus.지급예정, false);
        List<OrderDetail> orderDetails = byPandaAndPaymentStatusAndEnrollSettle.get();
        SettlePanda settlePanda = SettlePanda.createSettlePanda(orderDetails, panda);
        settlePandaRepository.save(settlePanda);
        return settlePanda;


    }
    public Panda editPanda(String target,String value,Panda panda)
    {
        switch (target)
        {
            case"pandaName":panda.setPandaName(value);
                break;
            case"intCategory":panda.setIntCategory(value);
                break;
            case"mainCh":panda.setMainCh(value);
                break;
            default:return null;
        }
        return panda;

    }
}
