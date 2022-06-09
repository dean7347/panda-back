package com.indiduck.panda.Service;


import com.indiduck.panda.Repository.BoardRepository;
import com.indiduck.panda.Repository.ProductRepository;
import com.indiduck.panda.domain.Board;
import com.indiduck.panda.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final BoardRepository boardRepository;
    public Board qnaNewBoard(String name,String title,String content,int caten,long ProId)
    {
        try {
            Optional<Product> byId = productRepository.findById(ProId);
            Board bo = Board.newBoard(name, title, content, caten);
            bo.setProduct(byId.get());
            boardRepository.save(bo);
            return bo;

        }catch (Exception E)
        {
            return null;
        }
    }

}
