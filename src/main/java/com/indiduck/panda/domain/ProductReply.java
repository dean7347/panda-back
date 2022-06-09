package com.indiduck.panda.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
public class ProductReply {
    //여기 왜만들다 말았는지 모르겠음.. -> 코멘트로 대체된 클래스

    @Id
    @GeneratedValue
    private long id;

    private String content;


    private LocalDateTime reportingDate;
    private Long replyOriginNumber;
    private int replyOrder;
    private int reply_depth;
    private String creater;

    @ManyToOne(fetch = FetchType.LAZY)
    private Board board;

//    //연관관계 삭제 -> 쿼리로 해결하면댐
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User writerName;

//    //생성메서드
//    public static ProductReply newReply(String content,String user,Board board)
//    {
//        ProductReply pr= new ProductReply();
//        pr.content=content;
//        pr.creater=user;
//        pr.board=board;
//        board.
//
//    }


}
