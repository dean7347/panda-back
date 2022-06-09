package com.indiduck.panda.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;


import javax.persistence.Entity;
import javax.persistence.*;

@Getter
@Entity
@Setter
@NoArgsConstructor
public class File {
//파일 저장 클래스
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String origfilename;

    @Column(nullable = false)
    private String filename;

    @Column( nullable = false,unique = true)
    private String filepath;

    private boolean isthumb;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Builder
    public File(Long id, String origFilename, String filename, String filePath) {
        this.id = id;
        this.origfilename = origFilename;
        this.filename = filename;
        this.filepath = filePath;
    }

    //=비즈니스 메서드=//
    public  void delFile()
    {

    }


}