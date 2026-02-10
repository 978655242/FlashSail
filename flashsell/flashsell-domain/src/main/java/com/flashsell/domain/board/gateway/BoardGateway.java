package com.flashsell.domain.board.gateway;

import com.flashsell.domain.board.entity.Board;

import java.util.List;
import java.util.Optional;

/**
 * 看板网关接口
 * 定义看板数据访问的抽象接口，由 infrastructure 层实现
 */
public interface BoardGateway {

    /**
     * 根据ID查询看板
     *
     * @param id 看板ID
     * @return 看板实体（可能为空）
     */
    Optional<Board> findById(Long id);

    /**
     * 根据ID查询看板（包含产品列表）
     *
     * @param id 看板ID
     * @return 看板实体（包含产品ID列表，可能为空）
     */
    Optional<Board> findByIdWithProducts(Long id);

    /**
     * 查询用户的所有看板（不包含已删除的）
     *
     * @param userId 用户ID
     * @return 看板列表
     */
    List<Board> findByUserId(Long userId);

    /**
     * 查询用户的所有看板（分页，不包含已删除的）
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param pageSize 每页数量
     * @return 看板列表
     */
    List<Board> findByUserId(Long userId, int page, int pageSize);

    /**
     * 统计用户的看板数量（不包含已删除的）
     *
     * @param userId 用户ID
     * @return 看板数量
     */
    long countByUserId(Long userId);

    /**
     * 检查用户是否拥有指定名称的看板
     *
     * @param userId 用户ID
     * @param name 看板名称
     * @return 是否存在
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * 保存看板
     *
     * @param board 看板实体
     * @return 保存后的看板实体（包含生成的ID）
     */
    Board save(Board board);

    /**
     * 更新看板
     *
     * @param board 看板实体
     * @return 更新后的看板实体
     */
    Board update(Board board);

    /**
     * 删除看板（软删除）
     *
     * @param id 看板ID
     */
    void deleteById(Long id);

    /**
     * 添加产品到看板
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 是否添加成功
     */
    boolean addProductToBoard(Long boardId, Long productId);

    /**
     * 批量添加产品到看板
     *
     * @param boardId 看板ID
     * @param productIds 产品ID列表
     * @return 成功添加的数量
     */
    int addProductsToBoard(Long boardId, List<Long> productIds);

    /**
     * 从看板移除产品
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 是否移除成功
     */
    boolean removeProductFromBoard(Long boardId, Long productId);

    /**
     * 获取看板中的产品ID列表
     *
     * @param boardId 看板ID
     * @return 产品ID列表
     */
    List<Long> getProductIdsByBoardId(Long boardId);

    /**
     * 统计看板中的产品数量
     *
     * @param boardId 看板ID
     * @return 产品数量
     */
    int countProductsByBoardId(Long boardId);

    /**
     * 检查产品是否在看板中
     *
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 是否在看板中
     */
    boolean isProductInBoard(Long boardId, Long productId);
}
