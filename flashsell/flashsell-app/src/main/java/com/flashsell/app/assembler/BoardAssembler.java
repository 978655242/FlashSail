package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.BoardDetailRes;
import com.flashsell.client.dto.res.BoardRes;
import com.flashsell.client.dto.res.BoardsRes;
import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.domain.board.entity.Board;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 看板 DTO 转换器
 * 负责领域实体和 DTO 之间的转换
 */
@Component
public class BoardAssembler {

    /**
     * 将看板实体转换为看板响应
     *
     * @param board 看板实体
     * @param productCount 产品数量
     * @return 看板响应
     */
    public BoardRes toBoardRes(Board board, int productCount) {
        if (board == null) {
            return null;
        }

        return BoardRes.builder()
                .id(board.getId())
                .name(board.getName())
                .productCount(productCount)
                .createdAt(board.getCreatedAt())
                .build();
    }

    /**
     * 将看板实体转换为看板响应（使用实体内的产品数量）
     *
     * @param board 看板实体
     * @return 看板响应
     */
    public BoardRes toBoardRes(Board board) {
        if (board == null) {
            return null;
        }

        return BoardRes.builder()
                .id(board.getId())
                .name(board.getName())
                .productCount(board.getProductCount())
                .createdAt(board.getCreatedAt())
                .build();
    }

    /**
     * 将看板实体列表转换为看板响应列表
     *
     * @param boards 看板实体列表
     * @param productCountMap 产品数量映射（boardId -> count）
     * @return 看板响应列表
     */
    public List<BoardRes> toBoardResList(List<Board> boards, Map<Long, Integer> productCountMap) {
        if (boards == null) {
            return List.of();
        }

        return boards.stream()
                .map(board -> toBoardRes(board, productCountMap.getOrDefault(board.getId(), 0)))
                .collect(Collectors.toList());
    }

    /**
     * 构建看板列表响应
     *
     * @param boardResList 看板响应列表
     * @param maxBoards 最大看板数
     * @param currentCount 当前看板数量
     * @return 看板列表响应
     */
    public BoardsRes toBoardsRes(List<BoardRes> boardResList, int maxBoards, int currentCount) {
        return BoardsRes.builder()
                .boards(boardResList)
                .maxBoards(maxBoards)
                .currentCount(currentCount)
                .build();
    }

    /**
     * 将看板实体转换为看板详情响应（包含产品列表）
     *
     * @param board 看板实体
     * @param products 产品列表
     * @param categoryMap 品类映射
     * @return 看板详情响应
     */
    public BoardDetailRes toBoardDetailRes(Board board, List<Product> products, Map<Long, Category> categoryMap) {
        if (board == null) {
            return null;
        }

        List<ProductItemRes> productItems = products.stream()
                .map(product -> toProductItemRes(product, categoryMap.get(product.getCategoryId())))
                .collect(Collectors.toList());

        return BoardDetailRes.builder()
                .id(board.getId())
                .name(board.getName())
                .productCount(productItems.size())
                .createdAt(board.getCreatedAt())
                .products(productItems)
                .build();
    }

    /**
     * 将产品实体转换为产品项响应
     *
     * @param product 产品实体
     * @param category 品类实体
     * @return 产品项响应
     */
    private ProductItemRes toProductItemRes(Product product, Category category) {
        if (product == null) {
            return null;
        }

        return ProductItemRes.builder()
                .id(product.getId())
                .title(product.getTitle())
                .image(product.getImageUrl())
                .price(product.getCurrentPrice())
                .bsrRank(product.getBsrRank())
                .reviewCount(product.getReviewCount())
                .rating(product.getRating())
                .categoryId(product.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .build();
    }
}
