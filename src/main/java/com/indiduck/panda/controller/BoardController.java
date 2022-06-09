package com.indiduck.panda.controller;


import com.indiduck.panda.Repository.BoardRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.BoardService;
import com.indiduck.panda.domain.*;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@Slf4j
public class BoardController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final BoardService boardService;
    @Autowired
    private final BoardRepository boardRepository;
    @Autowired
    private final ProductRepository productRepository;


    @RequestMapping(value = "/api/createreview", method = RequestMethod.POST)
    public ResponseEntity<?> createreivew(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication,@RequestBody Qnaboard qnaboard) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        String username = byEmail.get().getUsername();
//        username=username.replaceAll("(?<=.{5}).","*");

        Board board = boardService.qnaNewBoard(username, qnaboard.title, qnaboard.contents, 01, qnaboard.productId);
        if(board != null)
        {
            log.info(authentication.getName()+"의 게시글  후기작성 성공");
            return ResponseEntity.ok(new TFMessageDto(true,"success"));

        }

        log.warn(authentication.getName()+"의 게시글 작성 실패");
        return ResponseEntity.ok(new TFMessageDto(false,"게시글 작성에 실패했습니다"));
    }
    @RequestMapping(value = "/api/createqna", method = RequestMethod.POST)
    public ResponseEntity<?> createqna(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,@RequestBody Qnaboard qnaboard) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        String username = byEmail.get().getUsername();
//        username=username.replaceAll("(?<=.{5}).","*");

        Board board = boardService.qnaNewBoard(username, qnaboard.title, qnaboard.contents, 00, qnaboard.productId);
        if(board != null)
        {
            log.info(authentication.getName()+"의 게시글 작성 성공");
            return ResponseEntity.ok(new TFMessageDto(true,"success"));

        }

        log.warn(authentication.getName()+"의 게시글 작성 실패");
        return ResponseEntity.ok(new TFMessageDto(false,"게시글 작성에 실패했습니다"));
    }

    @RequestMapping(value = "/api/createqnareply", method = RequestMethod.POST)
    public ResponseEntity<?> createqnaReply(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication,@RequestBody Qnaboard qnaboard) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        String username = byEmail.get().getUsername();
//        username=username.replaceAll("(?<=.{5}).","*");

        Board board = boardService.qnaNewBoard(username, qnaboard.title, qnaboard.contents, 00, qnaboard.productId);
        if(board != null)
        {
            log.info(authentication.getName()+"의 답변작성 성공");
            return ResponseEntity.ok(new TFMessageDto(true,"success"));

        }

        log.error(authentication.getName()+"의 게시글 작성 실패");
        return ResponseEntity.ok(new TFMessageDto(false,"게시글 작성에 실패했습니다"));
    }

    //qna 게시글 가져오기
    @RequestMapping(value = "/api/getqna", method = RequestMethod.GET)
    public ResponseEntity<?> getqna(@CurrentSecurityContext(expression = "authentication")
                                                Authentication authentication,Pageable pageable,@RequestParam("pid") long pid) throws Exception {
//        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
//        String username = byEmail.get().getUsername();
        Optional<Product> byId = productRepository.findById(pid);
        Page<Board> byProduct = boardRepository.findByProductAndCategoryNumber(pageable, byId.get(),(int)00);
//        System.out.println("byProduct = " + byProduct);
//        System.out.println("byProduct = " + pageable);

//        return ResponseEntity.ok(byProduct);

        return ResponseEntity.ok(new qnaBoardDto(true,byProduct));
    }

    //상품후기 게시글 가져오기
    @RequestMapping(value = "/api/getreview", method = RequestMethod.GET)
    public ResponseEntity<?> getreview(@CurrentSecurityContext(expression = "authentication")
                                            Authentication authentication,Pageable pageable,@RequestParam("pid") long pid) throws Exception {
//        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
//        String username = byEmail.get().getUsername();
        Optional<Product> byId = productRepository.findById(pid);
        Page<Board> byProduct = boardRepository.findByProductAndCategoryNumber(pageable, byId.get(),(int)01);
//        System.out.println("byProduct = " + byProduct);
//        System.out.println("byProduct = " + pageable);

//        return ResponseEntity.ok(byProduct);

        return ResponseEntity.ok(new qnaBoardDto(true,byProduct));
    }

    @Data
    private static class getQnaProduct{
        Long productId;

    }

    @Data
    private static class Qnaboard{
        Long productId;
        String title;
        String contents;
    }
    @Data
    static class qnaBoardDto{
        boolean success;
        int totalE;
        int totalPage;

        List<BoardList> boardLists = new ArrayList<>();

        public qnaBoardDto(boolean tf, Page<Board> byProduct) {
            success=tf;
            this.totalE = (int) byProduct.getTotalElements();
            this.totalPage = byProduct.getTotalPages();
            for (Board  co: byProduct.getContent()) {
                String creater = co.getCreater();
                String username=creater.replaceAll("(?<=.{5}).","*");

                boardLists.add(new BoardList(co.getId(),co.getTitle(),co.getContent(),co.getCreatedAt(),username,co.getCommentList()));
            }
        }
    }

    @Data
    private static class BoardList{
        long boardId;
        String title;
        String content;
        LocalDateTime createdAt;
        String user;
        List<CommentListDto> comments=new ArrayList<CommentListDto>();
        public BoardList(long boardId, String title, String content,LocalDateTime ca,String user,List<Comment> cl) {
            this.createdAt=ca;
            this.user =user;
            this.boardId = boardId;
            this.title = title;
            this.content = content;
            for (Comment comment : cl) {
                String username= comment.getCreater();
                 username=username.replaceAll("(?<=.{5}).","*");

                comments.add(new CommentListDto(comment.getId(),username,comment.getContent(),comment.getCreatedAt()));
            }
        }
    }

    @Data
    private static class CommentListDto{
        Long coId;
        String username;
        String contents;
        LocalDateTime createAt;

        public CommentListDto(Long coId, String username, String contents, LocalDateTime createAt) {
            this.coId = coId;
            this.username = username;
            this.contents = contents;
            this.createAt = createAt;
        }
    }




}
