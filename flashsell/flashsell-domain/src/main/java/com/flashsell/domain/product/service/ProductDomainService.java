package com.flashsell.domain.product.service;

import com.flashsell.domain.product.entity.PricePoint;
import com.flashsell.domain.product.entity.Product;
import com.flashsell.domain.product.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 产品领域服务
 * 处理产品相关的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProductDomainService {

    private final ProductGateway productGateway;

    /**
     * 根据ID获取产品
     *
     * @param productId 产品ID
     * @return 产品实体（可能为空）
     */
    public Optional<Product> findById(Long productId) {
        return productGateway.findById(productId);
    }

    /**
     * 根据ASIN获取产品
     *
     * @param asin Amazon标准识别号
     * @return 产品实体（可能为空）
     */
    public Optional<Product> findByAsin(String asin) {
        return productGateway.findByAsin(asin);
    }

    /**
     * 根据品类ID获取产品列表
     *
     * @param categoryId 品类ID
     * @return 产品列表
     */
    public List<Product> findByCategoryId(Long categoryId) {
        return productGateway.findByCategoryId(categoryId);
    }

    /**
     * 创建新产品
     *
     * @param product 产品实体
     * @return 创建的产品实体
     * @throws IllegalArgumentException 如果ASIN已存在
     */
    public Product createProduct(Product product) {
        if (product.getAsin() != null && productGateway.existsByAsin(product.getAsin())) {
            throw new IllegalArgumentException("产品ASIN已存在: " + product.getAsin());
        }

        product.setCreatedAt(LocalDateTime.now());
        product.setLastUpdated(LocalDateTime.now());
        return productGateway.save(product);
    }

    /**
     * 更新产品信息
     *
     * @param product 产品实体
     * @throws IllegalArgumentException 如果产品不存在
     */
    public void updateProduct(Product product) {
        if (product.getId() == null || productGateway.findById(product.getId()).isEmpty()) {
            throw new IllegalArgumentException("产品不存在");
        }

        product.setLastUpdated(LocalDateTime.now());
        productGateway.update(product);
    }

    /**
     * 保存或更新产品（根据ASIN判断）
     *
     * @param product 产品实体
     * @return 保存后的产品实体
     */
    public Product saveOrUpdateProduct(Product product) {
        product.setLastUpdated(LocalDateTime.now());
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        return productGateway.saveOrUpdate(product);
    }

    /**
     * 更新产品数据（价格、排名、评论等）
     *
     * @param productId 产品ID
     * @param price 新价格
     * @param bsrRank 新BSR排名
     * @param reviewCount 新评论数
     * @param rating 新评分
     * @return 更新后的产品实体
     * @throws IllegalArgumentException 如果产品不存在
     */
    public Product updateProductData(Long productId, BigDecimal price, Integer bsrRank, 
                                     Integer reviewCount, Double rating) {
        Product product = productGateway.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在"));

        product.updateData(price, bsrRank, reviewCount, rating);
        productGateway.update(product);

        // 记录价格历史
        recordPriceHistory(productId, price);

        return product;
    }

    /**
     * 设置产品AI分析结果
     *
     * @param productId 产品ID
     * @param recommendation AI推荐理由
     * @param competitionScore 竞争评分
     * @return 更新后的产品实体
     * @throws IllegalArgumentException 如果产品不存在
     */
    public Product setAiAnalysis(Long productId, String recommendation, BigDecimal competitionScore) {
        Product product = productGateway.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在"));

        product.setAiAnalysis(recommendation, competitionScore);
        product.setLastUpdated(LocalDateTime.now());
        productGateway.update(product);

        return product;
    }

    /**
     * 获取产品价格历史
     *
     * @param productId 产品ID
     * @return 价格历史列表
     * @throws IllegalArgumentException 如果产品不存在
     */
    public List<PricePoint> getPriceHistory(Long productId) {
        if (productGateway.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("产品不存在");
        }
        return productGateway.findPriceHistory(productId);
    }

    /**
     * 获取指定日期范围内的产品价格历史
     *
     * @param productId 产品ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 价格历史列表
     * @throws IllegalArgumentException 如果产品不存在
     */
    public List<PricePoint> getPriceHistoryByDateRange(Long productId, LocalDate startDate, LocalDate endDate) {
        if (productGateway.findById(productId).isEmpty()) {
            throw new IllegalArgumentException("产品不存在");
        }
        return productGateway.findPriceHistoryByDateRange(productId, startDate, endDate);
    }

    /**
     * 记录价格历史
     *
     * @param productId 产品ID
     * @param price 价格
     */
    public void recordPriceHistory(Long productId, BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        
        // 检查今天是否已有记录
        Optional<PricePoint> latestPoint = productGateway.findLatestPricePoint(productId);
        if (latestPoint.isPresent() && latestPoint.get().getRecordedDate().equals(today)) {
            // 今天已有记录，不重复记录
            return;
        }

        PricePoint pricePoint = PricePoint.builder()
                .productId(productId)
                .price(price)
                .recordedDate(today)
                .createdAt(LocalDateTime.now())
                .build();

        productGateway.savePricePoint(pricePoint);
    }

    /**
     * 检查产品数据是否需要刷新
     *
     * @param productId 产品ID
     * @return 是否需要刷新
     */
    public boolean needsDataRefresh(Long productId) {
        Optional<Product> productOpt = productGateway.findById(productId);
        if (productOpt.isEmpty()) {
            return true;
        }
        return productOpt.get().isDataStale();
    }

    /**
     * 检查产品详情是否完整
     *
     * @param productId 产品ID
     * @return 是否完整
     * @throws IllegalArgumentException 如果产品不存在
     */
    public boolean isProductDetailComplete(Long productId) {
        Product product = productGateway.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("产品不存在"));
        return product.isDetailComplete();
    }
}
