package com.flashsell.app.service;

import com.flashsell.app.assembler.BoardAssembler;
import com.flashsell.client.dto.req.AddToBoardReq;
import com.flashsell.client.dto.req.CreateBoardReq;
import com.flashsell.client.dto.res.BoardDetailRes;
import com.flashsell.client.dto.res.BoardRes;
import com.flashsell.client.dto.res.BoardsRes;
import com.flashsell.domain.board.entity.Board;
import com.flashsell.domain.board.gateway.BoardGateway;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;
import com.flashsell.domain.user.entity.SubscriptionLevel;
import com.flashsell.domain.user.entity.User;
import com.flashsell.domain.user.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 看板应用服务
 * 提供看板相关的业务编排
 * 
 * Requirements: 4.2, 4.3, 4.5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardAppService {

    private final BoardGateway boardGateway;
    private final UserGateway userGateway;
    private final ProductGateway productGateway;
    private final CategoryGateway categoryGateway;
    private final BoardAssembler boardAssembler;

    /**
     * 创建看板
     *
     * @param userId 用户ID
     * @param req 创建看板请求
     * @return 看板响应
     * @throws IllegalArgumentException 如果用户不存在或看板名称已存在
     * @throws IllegalStateException 如果超过看板数量限制
     */
    @Transactional
    public BoardRes createBoard(Long userId, CreateBoardReq req) {
        log.debug("创建看板: userId={}, name={}", userId, req.getName());

        // 1. 获取用户信息
        User user = userGateway.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 2. 检查看板数量限制
        long currentBoardCount = boardGateway.countByUserId(userId);
        int maxBoards = user.getEffectiveSubscriptionLevel().getMaxBoards();
        
        if (!user.canCreateBoard((int) currentBoardCount)) {
            log.warn("超过看板数量限制: userId={}, current={}, max={}", userId, currentBoardCount, maxBoards);
            throw new IllegalStateException(
                    String.format("已达到看板数量上限（%d/%d），请升级订阅以创建更多看板", currentBoardCount, maxBoards));
        }

        // 3. 检查看板名称是否已存在
        if (boardGateway.existsByUserIdAndName(userId, req.getName())) {
            log.warn("看板名称已存在: userId={}, name={}", userId, req.getName());
            throw new IllegalArgumentException("看板名称已存在");
        }

        // 4. 创建看板
        Board board = Board.create(userId, req.getName());
        Board savedBoard = boardGateway.save(board);
        log.info("看板创建成功: userId={}, boardId={}, name={}", userId, savedBoard.getId(), savedBoard.getName());

        return boardAssembler.toBoardRes(savedBoard, 0);
    }

    /**
     * 获取用户的看板列表
     *
     * @param userId 用户ID
     * @return 看板列表响应
     */
    public BoardsRes getBoards(Long userId) {
        log.debug("获取看板列表: userId={}", userId);

        // 1. 获取用户信息以获取最大看板数
        User user = userGateway.findById(userId).orElse(null);
        int maxBoards = user != null 
                ? user.getEffectiveSubscriptionLevel().getMaxBoards() 
                : SubscriptionLevel.FREE.getMaxBoards();

        // 2. 获取看板列表
        List<Board> boards = boardGateway.findByUserId(userId);

        // 3. 批量获取每个看板的产品数量
        Map<Long, Integer> productCountMap = new HashMap<>();
        for (Board board : boards) {
            int count = boardGateway.countProductsByBoardId(board.getId());
            productCountMap.put(board.getId(), count);
        }

        // 4. 组装响应
        List<BoardRes> boardResList = boardAssembler.toBoardResList(boards, productCountMap);
        return boardAssembler.toBoardsRes(boardResList, maxBoards, boards.size());
    }

    /**
     * 获取看板详情（包含产品列表）
     *
     * @param userId 用户ID
     * @param boardId 看板ID
     * @return 看板详情响应
     * @throws IllegalArgumentException 如果看板不存在或不属于该用户
     */
    public BoardDetailRes getBoardDetail(Long userId, Long boardId) {
        log.debug("获取看板详情: userId={}, boardId={}", userId, boardId);

        // 1. 获取看板（包含产品ID列表）
        Board board = boardGateway.findByIdWithProducts(boardId)
                .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

        // 2. 验证看板所有权
        if (!board.belongsToUser(userId)) {
            log.warn("看板不属于该用户: userId={}, boardId={}", userId, boardId);
            throw new IllegalArgumentException("看板不存在");
        }

        // 3. 批量获取产品信息
        List<Product> products = getProductsByIds(board.getProductIds());

        // 4. 批量获取品类信息
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Category> categoryMap = getCategoryMap(categoryIds);

        // 5. 组装响应
        return boardAssembler.toBoardDetailRes(board, products, categoryMap);
    }

    /**
     * 添加产品到看板
     *
     * @param userId 用户ID
     * @param boardId 看板ID
     * @param req 添加产品请求
     * @return 成功添加的产品数量
     * @throws IllegalArgumentException 如果看板不存在或不属于该用户
     */
    @Transactional
    public int addProductsToBoard(Long userId, Long boardId, AddToBoardReq req) {
        log.debug("添加产品到看板: userId={}, boardId={}, productIds={}", userId, boardId, req.getProductIds());

        // 1. 获取看板
        Board board = boardGateway.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

        // 2. 验证看板所有权
        if (!board.belongsToUser(userId)) {
            log.warn("看板不属于该用户: userId={}, boardId={}", userId, boardId);
            throw new IllegalArgumentException("看板不存在");
        }

        // 3. 验证产品是否存在
        List<Long> validProductIds = req.getProductIds().stream()
                .filter(productId -> productGateway.findById(productId).isPresent())
                .collect(Collectors.toList());

        if (validProductIds.isEmpty()) {
            log.warn("没有有效的产品ID: productIds={}", req.getProductIds());
            return 0;
        }

        // 4. 添加产品到看板
        int addedCount = boardGateway.addProductsToBoard(boardId, validProductIds);
        log.info("产品添加到看板成功: userId={}, boardId={}, addedCount={}", userId, boardId, addedCount);

        return addedCount;
    }

    /**
     * 从看板移除产品
     *
     * @param userId 用户ID
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 是否移除成功
     * @throws IllegalArgumentException 如果看板不存在或不属于该用户
     */
    @Transactional
    public boolean removeProductFromBoard(Long userId, Long boardId, Long productId) {
        log.debug("从看板移除产品: userId={}, boardId={}, productId={}", userId, boardId, productId);

        // 1. 获取看板
        Board board = boardGateway.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

        // 2. 验证看板所有权
        if (!board.belongsToUser(userId)) {
            log.warn("看板不属于该用户: userId={}, boardId={}", userId, boardId);
            throw new IllegalArgumentException("看板不存在");
        }

        // 3. 移除产品
        boolean removed = boardGateway.removeProductFromBoard(boardId, productId);
        if (removed) {
            log.info("产品从看板移除成功: userId={}, boardId={}, productId={}", userId, boardId, productId);
        } else {
            log.debug("产品不在看板中: boardId={}, productId={}", boardId, productId);
        }

        return removed;
    }

    /**
     * 删除看板
     *
     * @param userId 用户ID
     * @param boardId 看板ID
     * @return 是否删除成功
     * @throws IllegalArgumentException 如果看板不存在或不属于该用户
     */
    @Transactional
    public boolean deleteBoard(Long userId, Long boardId) {
        log.debug("删除看板: userId={}, boardId={}", userId, boardId);

        // 1. 获取看板
        Board board = boardGateway.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

        // 2. 验证看板所有权
        if (!board.belongsToUser(userId)) {
            log.warn("看板不属于该用户: userId={}, boardId={}", userId, boardId);
            throw new IllegalArgumentException("看板不存在");
        }

        // 3. 删除看板（软删除）
        boardGateway.deleteById(boardId);
        log.info("看板删除成功: userId={}, boardId={}", userId, boardId);

        return true;
    }

    /**
     * 更新看板名称
     *
     * @param userId 用户ID
     * @param boardId 看板ID
     * @param newName 新名称
     * @return 更新后的看板响应
     * @throws IllegalArgumentException 如果看板不存在、不属于该用户或名称已存在
     */
    @Transactional
    public BoardRes updateBoardName(Long userId, Long boardId, String newName) {
        log.debug("更新看板名称: userId={}, boardId={}, newName={}", userId, boardId, newName);

        // 1. 获取看板
        Board board = boardGateway.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("看板不存在"));

        // 2. 验证看板所有权
        if (!board.belongsToUser(userId)) {
            log.warn("看板不属于该用户: userId={}, boardId={}", userId, boardId);
            throw new IllegalArgumentException("看板不存在");
        }

        // 3. 检查新名称是否已存在（排除当前看板）
        if (!board.getName().equals(newName) && boardGateway.existsByUserIdAndName(userId, newName)) {
            log.warn("看板名称已存在: userId={}, name={}", userId, newName);
            throw new IllegalArgumentException("看板名称已存在");
        }

        // 4. 更新名称
        board.setName(newName);
        Board updatedBoard = boardGateway.update(board);
        log.info("看板名称更新成功: userId={}, boardId={}, newName={}", userId, boardId, newName);

        int productCount = boardGateway.countProductsByBoardId(boardId);
        return boardAssembler.toBoardRes(updatedBoard, productCount);
    }

    /**
     * 获取用户的看板数量
     *
     * @param userId 用户ID
     * @return 看板数量
     */
    public long getBoardCount(Long userId) {
        return boardGateway.countByUserId(userId);
    }

    // ==================== 私有方法 ====================

    /**
     * 批量获取产品
     */
    private List<Product> getProductsByIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productIds.stream()
                .map(productGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 批量获取品类映射
     */
    private Map<Long, Category> getCategoryMap(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryIds.stream()
                .map(categoryGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Category::getId, c -> c));
    }
}
