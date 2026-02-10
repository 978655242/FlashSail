package com.flashsell.infrastructure.board.convertor;

import com.flashsell.domain.board.entity.Board;
import com.flashsell.infrastructure.board.dataobject.BoardDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 看板转换器
 * 负责 BoardDO 和领域实体之间的转换
 */
@Component
public class BoardConvertor {

    /**
     * 将看板数据对象转换为领域实体
     *
     * @param boardDO 看板数据对象
     * @return 看板领域实体
     */
    public Board toEntity(BoardDO boardDO) {
        if (boardDO == null) {
            return null;
        }

        return Board.builder()
                .id(boardDO.getId())
                .userId(boardDO.getUserId())
                .name(boardDO.getName())
                .productIds(new ArrayList<>())
                .createdAt(boardDO.getCreatedAt())
                .deletedAt(boardDO.getDeletedAt())
                .build();
    }

    /**
     * 将看板数据对象转换为领域实体（包含产品ID列表）
     *
     * @param boardDO 看板数据对象
     * @param productIds 产品ID列表
     * @return 看板领域实体
     */
    public Board toEntity(BoardDO boardDO, List<Long> productIds) {
        if (boardDO == null) {
            return null;
        }

        return Board.builder()
                .id(boardDO.getId())
                .userId(boardDO.getUserId())
                .name(boardDO.getName())
                .productIds(productIds != null ? new ArrayList<>(productIds) : new ArrayList<>())
                .createdAt(boardDO.getCreatedAt())
                .deletedAt(boardDO.getDeletedAt())
                .build();
    }

    /**
     * 将看板领域实体转换为数据对象
     *
     * @param board 看板领域实体
     * @return 看板数据对象
     */
    public BoardDO toDataObject(Board board) {
        if (board == null) {
            return null;
        }

        return BoardDO.builder()
                .id(board.getId())
                .userId(board.getUserId())
                .name(board.getName())
                .createdAt(board.getCreatedAt())
                .deletedAt(board.getDeletedAt())
                .build();
    }

    /**
     * 将看板数据对象列表转换为领域实体列表
     *
     * @param boardDOList 看板数据对象列表
     * @return 看板领域实体列表
     */
    public List<Board> toEntityList(List<BoardDO> boardDOList) {
        if (boardDOList == null) {
            return List.of();
        }
        return boardDOList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
