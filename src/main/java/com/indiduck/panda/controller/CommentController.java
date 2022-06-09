package com.indiduck.panda.controller;

import com.indiduck.panda.Repository.UserRepository;
import com.indiduck.panda.Service.CommentService;
import com.indiduck.panda.domain.Board;
import com.indiduck.panda.domain.Comment;
import com.indiduck.panda.domain.User;
import com.indiduck.panda.domain.dao.TFMessageDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RequiredArgsConstructor
@RestController
@Slf4j
public class CommentController {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final CommentService commentService;
    @RequestMapping(value = "/api/createcomment", method = RequestMethod.POST)
    public ResponseEntity<?> createqna(@CurrentSecurityContext(expression = "authentication")
                                               Authentication authentication, @RequestBody CommentCreateor commentCreateor) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(authentication.getName());
        String username = byEmail.get().getUsername();

        Comment comment = commentService.newComment(username, commentCreateor.getContents(), commentCreateor.boardId);
        if(comment != null)
        {
            log.info(authentication.getName() + "의 코멘트 등록 성공");
            return ResponseEntity.ok(new TFMessageDto(true,"success"));

        }
        log.error(authentication.getName() + "의 코멘트 등록 실패");


        return ResponseEntity.ok(new TFMessageDto(false,"게시글 작성에 실패했습니다"));
    }

    @Data
    private static class CommentCreateor{
        long boardId;
        String contents;

    }
}
