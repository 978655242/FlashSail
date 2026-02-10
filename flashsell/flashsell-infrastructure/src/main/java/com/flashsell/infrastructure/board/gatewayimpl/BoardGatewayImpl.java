package com.flashsell.infrastructure.board.gatewayimpl;

import com.flashsell.domain.board.entity.Board;
import com.flashsell.domain.board.gateway.BoardGateway;
import com.flashsell.infrastructure.board.convertor.BoardConvertor;
import com.flashsell.infrastructure.board.dataobject.BoardDO;
import com.flashsell.infrastructure.board.dataobject.BoardProductDO;
import com.flashsell.infrastructure.board.mapper.BoardMapper;
import com.flashsell.infrastructure.board.mapper.BoardProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 看板网关实现
 * 实现 BoardGateway 接口，提供看板数据访问的具体实现
 */
@Repository
@RequiredArgsConstructor
public class BoardGatewayImpl implements BoardGateway {

    private final BoardMapper boardMapper;
    private final BoardProductMapper boardProductMapper;
    private final BoardConvertor boardConvertor;

    @Override
    public Optional<Board> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        BoardDO boardDO = boardMapper.selectByIdNotDeleted(id);
        return Optional.ofNullable(boardConvertor.toEntity(boardDO));
    }

    @Override
    public Optional<Board> findByIdWithProducts(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        BoardDO boardDO = boardMapper.selectByIdNotDeleted(id);
        if (boardDO == null) {
            return Optional.empty();
        }
        List<Long> productIds = boardProductMapper.selectProductIdsByBoardId(id);
        return Optional.ofNullable(boardConvertor.toEntity(boardDO, productIds));
    }

    @Override
    public List<Board> findByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<BoardDO> boardDOList = boardMapper.selectByUserId(userId);
        return boardConvertor.toEntityList(boardDOList);
    }

    @Override
    public List<Board> findByUserId(Long userId, int page, int pageSize) {
        if (userId == null || page < 1 || pageSize < 1) {
            return List.of();
        }
        int offset = (page - 1) * pageSize;
        List<BoardDO> boardDOList = boardMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        return boardConvertor.toEntityList(boardDOList);
    }

    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return boardMapper.countByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndName(Long userId, String name) {
        if (userId == null || name == null || name.trim().isEmpty()) {
            return false;
        }
        return boardMapper.countByUserIdAndName(userId, name) > 0;
    }

    @Override
    public Board save(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }

        BoardDO boardDO = boardConvertor.toDataObject(board);

        if (boardDO.getCreatedAt() == null) {
            boardDO.setCreatedAt(LocalDateTime.now());
        }
        boardMapper.insert(boardDO);

        // 返回包含生成ID的看板实体
        return boardConvertor.toEntity(boardDO);
    }

    @Override
    public Board update(Board board) {
        if (board == null || board.getId() == null) {
            throw new IllegalArgumentException("Board and Board ID cannot be null");
        }

        BoardDO boardDO = boardConvertor.toDataObject(board);
        boardMapper.updateById(boardDO);

        return boardConvertor.toEntity(boardDO);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            // 软删除看板
            boardMapper.softDeleteById(id);
            // 删除看板的所有产品关联
            boardProductMapper.deleteByBoardId(id);
        }
    }

    @Override
    public boolean addProductToBoard(Long boardId, Long productId) {
        if (boardId == null || productId == null) {
            return false;
        }

        // 检查是否已存在
        if (boardProductMapper.countByBoardIdAndProductId(boardId, productId) > 0) {
            return false;
        }

        BoardProductDO boardProductDO = BoardProductDO.builder()
                .boardId(boardId)
                .productId(productId)
                .addedAt(LocalDateTime.now())
                .build();
        boardProductMapper.insert(boardProductDO);
        return true;
    }

    @Override
    public int addProductsToBoard(Long boardId, List<Long> productIds) {
        if (boardId == null || productIds == null || productIds.isEmpty()) {
            return 0;
        }

        int addedCount = 0;
        for (Long productId : productIds) {
            if (addProductToBoard(boardId, productId)) {
                addedCount++;
            }
        }
        return addedCount;
    }

    @Override
    public boolean removeProductFromBoard(Long boardId, Long productId) {
        if (boardId == null || productId == null) {
            return false;
        }
        int deleted = boardProductMapper.deleteByBoardIdAndProductId(boardId, productId);
        return deleted > 0;
    }

    @Override
    public List<Long> getProductIdsByBoardId(Long boardId) {
        if (boardId == null) {
            return List.of();
        }
        return boardProductMapper.selectProductIdsByBoardId(boardId);
    }

    @Override
    public int countProductsByBoardId(Long boardId) {
        if (boardId == null) {
            return 0;
        }
        return boardProductMapper.countByBoardId(boardId);
    }

    @Override
    public boolean isProductInBoard(Long boardId, Long productId) {
        if (boardId == null || productId == null) {
            return false;
        }
        return boardProductMapper.countByBoardIdAndProductId(boardId, productId) > 0;
    }
}
