package com.flashsell.adapter.web;

import com.flashsell.app.service.BoardAppService;
import com.flashsell.client.dto.ApiResponse;
import com.flashsell.client.dto.req.AddToBoardReq;
import com.flashsell.client.dto.req.CreateBoardReq;
import com.flashsell.client.dto.res.BoardDetailRes;
import com.flashsell.client.dto.res.BoardRes;
import com.flashsell.client.dto.res.BoardsRes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.flashsell.adapter.web.SecurityUtils.getCurrentUserId;

/**
 * 看板控制器
 * 处理看板相关的 API 请求
 *
 * Requirements: 4.2, 4.3, 4.5
 */
@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardAppService boardAppService;

    /**
     * 创建看板
     *
     * @param userDetails 当前登录用户
     * @param req 创建看板请求
     * @return 看板响应
     */
    @PostMapping
    public ApiResponse<BoardRes> createBoard(
                        @Valid @RequestBody CreateBoardReq req) {
        Long userId = getCurrentUserId();
        log.info("创建看板: userId={}, name={}", userId, req.getName());

        try {
            BoardRes result = boardAppService.createBoard(userId, req);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("创建看板失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("创建看板失败（超过限制）: {}", e.getMessage());
            return ApiResponse.error(403, e.getMessage());
        }
    }

    /**
     * 获取看板列表
     *
     * @param userDetails 当前登录用户
     * @return 看板列表响应
     */
    @GetMapping
    public ApiResponse<BoardsRes> getBoards() {
        Long userId = getCurrentUserId();
        log.info("获取看板列表: userId={}", userId);

        BoardsRes result = boardAppService.getBoards(userId);
        return ApiResponse.success(result);
    }

    /**
     * 获取看板详情（包含产品列表）
     *
     * @param userDetails 当前登录用户
     * @param boardId 看板ID
     * @return 看板详情响应
     */
    @GetMapping("/{boardId}")
    public ApiResponse<BoardDetailRes> getBoardDetail(
                        @PathVariable Long boardId) {
        Long userId = getCurrentUserId();
        log.info("获取看板详情: userId={}, boardId={}", userId, boardId);

        try {
            BoardDetailRes result = boardAppService.getBoardDetail(userId, boardId);
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("获取看板详情失败: {}", e.getMessage());
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 添加产品到看板
     *
     * @param userDetails 当前登录用户
     * @param boardId 看板ID
     * @param req 添加产品请求
     * @return 成功添加的产品数量
     */
    @PutMapping("/{boardId}/products")
    public ApiResponse<Integer> addProductsToBoard(
                        @PathVariable Long boardId,
            @Valid @RequestBody AddToBoardReq req) {
        Long userId = getCurrentUserId();
        log.info("添加产品到看板: userId={}, boardId={}, productIds={}", userId, boardId, req.getProductIds());

        try {
            int addedCount = boardAppService.addProductsToBoard(userId, boardId, req);
            return ApiResponse.success(addedCount);
        } catch (IllegalArgumentException e) {
            log.warn("添加产品到看板失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 从看板移除产品
     *
     * @param userDetails 当前登录用户
     * @param boardId 看板ID
     * @param productId 产品ID
     * @return 操作结果
     */
    @DeleteMapping("/{boardId}/products/{productId}")
    public ApiResponse<Void> removeProductFromBoard(
                        @PathVariable Long boardId,
            @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        log.info("从看板移除产品: userId={}, boardId={}, productId={}", userId, boardId, productId);

        try {
            boolean removed = boardAppService.removeProductFromBoard(userId, boardId, productId);
            if (removed) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(404, "产品不在看板中");
            }
        } catch (IllegalArgumentException e) {
            log.warn("从看板移除产品失败: {}", e.getMessage());
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 删除看板
     *
     * @param userDetails 当前登录用户
     * @param boardId 看板ID
     * @return 操作结果
     */
    @DeleteMapping("/{boardId}")
    public ApiResponse<Void> deleteBoard(
                        @PathVariable Long boardId) {
        Long userId = getCurrentUserId();
        log.info("删除看板: userId={}, boardId={}", userId, boardId);

        try {
            boardAppService.deleteBoard(userId, boardId);
            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            log.warn("删除看板失败: {}", e.getMessage());
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 更新看板名称
     *
     * @param userDetails 当前登录用户
     * @param boardId 看板ID
     * @param req 更新请求（复用创建请求）
     * @return 更新后的看板响应
     */
    @PutMapping("/{boardId}")
    public ApiResponse<BoardRes> updateBoard(
                        @PathVariable Long boardId,
            @Valid @RequestBody CreateBoardReq req) {
        Long userId = getCurrentUserId();
        log.info("更新看板: userId={}, boardId={}, newName={}", userId, boardId, req.getName());

        try {
            BoardRes result = boardAppService.updateBoardName(userId, boardId, req.getName());
            return ApiResponse.success(result);
        } catch (IllegalArgumentException e) {
            log.warn("更新看板失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取看板数量
     *
     * @param userDetails 当前登录用户
     * @return 看板数量
     */
    @GetMapping("/count")
    public ApiResponse<Long> getBoardCount() {
        Long userId = getCurrentUserId();
        log.debug("获取看板数量: userId={}", userId);

        long count = boardAppService.getBoardCount(userId);
        return ApiResponse.success(count);
    }
}
