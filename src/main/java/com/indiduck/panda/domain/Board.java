package com.indiduck.panda.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Board {
    @Id
    @GeneratedValue
    long id;

    String title;
    @Lob
    String content;
    // 작성자
    String creater;
    boolean isdelete;
    LocalDateTime createdAt;
    LocalDateTime editAt;
    LocalDateTime deleteAt;
    //00 상품문의사항 //01 공지사항
    int categoryNumber;

    //판매자가 확인했는지
    boolean isCheck;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
    //덧글리스트
    @OneToMany(mappedBy = "board")
    List<Comment> commentList = new ArrayList<>();

    //생성메서드
    public static Board newBoard(String name,String title,String content,int caten)
    {
        Board bo= new Board();
        bo.title=title;
        bo.content=content;
        bo.creater=name;
        bo.isdelete=false;
        bo.isCheck=false;
        bo.categoryNumber=caten;
        bo.createdAt=LocalDateTime.now();

        return bo;
    }

    //비즈니스 메서드
    public void setProduct(Product pro)
    {
        this.product=pro;
        pro.getBoards().add(this);
    }

    public void setComment(Comment co)
    {
        this.commentList.add(co);
    }




}
