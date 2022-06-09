package com.indiduck.panda.Service;

import com.indiduck.panda.Repository.BoardRepository;
import com.indiduck.panda.Repository.CommentRepository;
import com.indiduck.panda.domain.Board;
import com.indiduck.panda.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    @Autowired
    private final BoardRepository boardRepository;
    @Autowired
    private final CommentRepository commentRepository;
    public Comment newComment(String name, String content, long boardId)
    {
        Optional<Board> byId = boardRepository.findById(boardId);
        Comment co = Comment.newComment(name,content,byId.get());
        commentRepository.save(co);
        return co;
    }
}
