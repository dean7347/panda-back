package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface FileRepository extends JpaRepository<File, Long> {

    @Query("select f from File f where f.filepath =:filePath")
    File myqueryfind(@Param("filePath")String filePath);
}
