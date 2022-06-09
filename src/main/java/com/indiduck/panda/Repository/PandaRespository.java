package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.Panda;
import com.indiduck.panda.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface PandaRespository extends JpaRepository<Panda,Long> {


    Optional<Panda> findByUser(User user);
    Page<Panda> findByRecognize(Pageable pageable,boolean tf);
}
