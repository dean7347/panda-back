package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.OrderDetailRepository;
import com.indiduck.panda.Repository.ProductOptionRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.domain.OrderDetail;
import com.indiduck.panda.domain.OrderStatus;
import com.indiduck.panda.domain.Product;
import com.indiduck.panda.domain.ProductOption;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class ProductOptionService {

    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    public ProductOption saveOption(ProductOption option){
        ProductOption newO=new ProductOption()
                .newProductOption(option.getOptionName(), option.getOptionStock(), option.getOptionPrice());
        productOptionRepository.save(newO);
        return newO;
    }

    public void delOption(Long optionId)
    {
        Optional<ProductOption> byId = productOptionRepository.findById(optionId);
        byId.get().optionDel();
    }

    public ProductOption addOption(String opname, int stock,int price,long productId){
        Optional<Product> byId1 = productRepository.findById(productId);

        ProductOption newO=new ProductOption()
                .newProductOption(opname, stock, price);
        ProductOption save = productOptionRepository.save(newO);
        byId1.get().setProductOptions(save);
        return save;
    }

    public ProductOption editOption(String opname, int stock,int price,long OptionId){

        Optional<ProductOption> byId = productOptionRepository.findById(OptionId);
        ProductOption productOption = byId.get();
        productOption.edit(opname,stock,price);
        Optional<List<OrderDetail>> orderDetailByOrderStatusAndOptions_id = orderDetailRepository.findOrderDetailByOrderStatusAndOptions_Id(OrderStatus.결제대기, OptionId);
        for (OrderDetail orderDetail : orderDetailByOrderStatusAndOptions_id.get()) {
            orderDetail.editOption();
        }
        return byId.get();
    }


}
