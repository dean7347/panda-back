package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.Panda;
import com.indiduck.panda.domain.PandaToProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface PandaToProductRepository extends JpaRepository<PandaToProduct,Long> {


    List<PandaToProduct> findByProductIdAndIsDel(Long productId,boolean tf);
    Optional<List<PandaToProduct>> findByPandaAndIsDel(Panda panda,boolean tf);
}
