package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.DeliverAddress;
import com.indiduck.panda.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface DeliverAddressRepository extends JpaRepository<DeliverAddress,Long> {

    Optional<List<DeliverAddress>>findDeliverAddressByUserName(User username);
    long deleteByUserName(User user);
}
