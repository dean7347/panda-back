package com.indiduck.panda.domain;


import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
public class Comment {

    @Id
    @GeneratedValue
    private long id;

    //삭제여부
    boolean isdelete;

    //덧글을 판매자가 확인했는지
    boolean isCheck;
    //작성자
    private String creater;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime deleteAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;


    //생성메서드
    public static Comment newComment(String cre,String content,Board bo)
    {
        Comment co = new Comment();
        co.creater=cre;
        co.content=content;
        co.createdAt=LocalDateTime.now();
        co.isCheck=false;
        co.isdelete=false;
        co.board=bo;
        bo.setComment(co);

        return co;

    }

    //비즈니스 메서드

}
