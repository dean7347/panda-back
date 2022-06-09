package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.PandaRespository;
import com.indiduck.panda.Repository.PandaToProductRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.domain.PandaToProduct;
import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PandaToProductService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PandaRespository pandaRespository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    PandaToProductRepository pandaToProductRepository;

    public PandaToProduct newPtP(String user,Long productId,String Link){

        Optional<User> byEmail = userRepository.findByEmail(user);
        Optional<Product> byId = productRepository.findById(productId);

        if(!byEmail.isEmpty() && !byId.isEmpty())
        {
            PandaToProduct ptp = PandaToProduct.newPandaToProduct(Link);
            ptp.setPanda(byEmail.get().getPanda());
            ptp.setProduct(byId.get());
            pandaToProductRepository.save(ptp);
            return ptp;

        }
        return null;

    }


    public PandaToProduct editURL(Long id, String URl) {
        Optional<PandaToProduct> byId = pandaToProductRepository.findById(id);
        byId.get().changeURL(URl);
        return byId.get();
    }

    public PandaToProduct delURL(Long id)
    {
        Optional<PandaToProduct> byId = pandaToProductRepository.findById(id);
        byId.get().delURl();

        return byId.get();
    }
}
