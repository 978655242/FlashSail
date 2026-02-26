package com.flashsell.domain.product.gateway;

import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 产品网关接口
 * 定义产品数据访问的抽象接口，由 infrastructure 层实现
 */
public interface ProductGateway {

    /**
     * 根据ID查询产品
     *
     * @param id 产品ID
     * @return 产品实体（可能为空）
     */
    Optional<Product> findById(Long id);

    /**
     * 根据ID查询产品（直接返回）
     *
     * @param id 产品ID
     * @return 产品实体（可能为null）
     */
    Product findByIdDirect(Long id);

    /**
     * 统计指定时间之后创建的产品数量
     *
     * @param afterTime 时间阈值
     * @return 产品数量
     */
    int countCreatedAfter(java.time.LocalDateTime afterTime);

    /**
     * 根据ASIN查询产品
     *
     * @param asin Amazon标准识别号
     * @return 产品实体（可能为空）
     */
    Optional<Product> findByAsin(String asin);

    /**
     * 根据品类ID查询产品列表
     *
     * @param categoryId 品类ID
     * @return 产品列表
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * 保存产品（新增或更新）
     *
     * @param product 产品实体
     * @return 保存后的产品实体（包含生成的ID）
     */
    Product save(Product product);

    /**
     * 更新产品信息
     *
     * @param product 产品实体
     */
    void update(Product product);

    /**
     * 保存或更新产品（根据ASIN判断）
     *
     * @param product 产品实体
     * @return 保存后的产品实体
     */
    Product saveOrUpdate(Product product);

    /**
     * 检查ASIN是否已存在
     *
     * @param asin Amazon标准识别号
     * @return 是否存在
     */
    boolean existsByAsin(String asin);

    /**
     * 获取产品价格历史
     *
     * @param productId 产品ID
     * @return 价格历史列表（按日期升序）
     */
    List<PricePoint> findPriceHistory(Long productId);

    /**
     * 获取指定日期范围内的产品价格历史
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格历史列表（按日期升序）
     */
    List<PricePoint> findPriceHistoryByDateRange(Long productId, LocalDate startDate, LocalDate endDate);

    /**
     * 保存价格记录
     *
     * @param pricePoint 价格点实体
     * @return 保存后的价格点实体
     */
    PricePoint savePricePoint(PricePoint pricePoint);

    /**
     * 批量保存价格记录
     *
     * @param pricePoints 价格点列表
     */
    void savePricePoints(List<PricePoint> pricePoints);

    /**
     * 获取产品最新的价格记录
     *
     * @param productId 产品ID
     * @return 最新价格点（可能为空）
     */
    Optional<PricePoint> findLatestPricePoint(Long productId);

    /**
     * 根据ID列表批量查询产品
     *
     * @param ids 产品ID列表
     * @return 产品列表
     */
    List<Product> findByIds(List<Long> ids);

    /**
     * 根据标题模糊搜索产品
     *
     * @param keyword 搜索关键词
     * @return 匹配的产品列表
     */
    List<Product> findByTitleContaining(String keyword);
}
