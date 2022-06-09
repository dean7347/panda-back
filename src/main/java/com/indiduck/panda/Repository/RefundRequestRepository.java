package com.indiduck.panda.Repository;


import com.indiduck.panda.domain.RefundRequest;
import com.indiduck.panda.domain.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface RefundRequestRepository  extends JpaRepository<RefundRequest,Long> {
}
