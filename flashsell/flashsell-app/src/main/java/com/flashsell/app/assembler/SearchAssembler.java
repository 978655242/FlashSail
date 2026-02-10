package com.flashsell.app.assembler;

import com.flashsell.client.dto.res.ProductItemRes;
import com.flashsell.client.dto.res.SearchRes;
import com.flashsell.domain.category.entity.Category;
import com.flashsell.domain.category.gateway.CategoryGateway;
import com.flashsell.domain.data.entity.DataFreshness;
import com.flashsell.domain.product.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 搜索结果转换器
 * 负责将领域对象转换为 DTO
 * 
 * Requirements: 2.1, 2.2
 */
@Component
@RequiredArgsConstructor
public class SearchAssembler {

    private final CategoryGateway categoryGateway;

    /**
     * 将产品列表转换为搜索响应
     */
    public SearchRes toSearchRes(List<Product> products, 
                                  int total, 
                                  int page, 
                                  int pageSize,
                                  String aiSummary,
                                  DataFreshness dataFreshness) {
        // 获取所有品类用于名称映射
        Map<Long, String> categoryNameMap = getCategoryNameMap(products);

        List<ProductItemRes> productItems = products.stream()
                .map(p -> toProductItemRes(p, categoryNameMap))
                .collect(Collectors.toList());

        return SearchRes.builder()
                .products(productItems)
                .total((long) total)
                .page(page)
                .pageSize(pageSize)
                .hasMore(page * pageSize < total)
                .aiSummary(aiSummary)
                .dataFreshness(dataFreshness != null ? dataFreshness.getStatusString() : "UNKNOWN")
                .freshnessMessage(dataFreshness != null ? dataFreshness.getMessage() : null)
                .build();
    }

    /**
     * 将产品转换为列表项 DTO
     */
    public ProductItemRes toProductItemRes(Product product, Map<Long, String> categoryNameMap) {
        String categoryName = null;
        if (product.getCategoryId() != null && categoryNameMap != null) {
            categoryName = categoryNameMap.get(product.getCategoryId());
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
                .categoryName(categoryName)
                .build();
    }

    /**
     * 获取品类名称映射
     */
    private Map<Long, String> getCategoryNameMap(List<Product> products) {
        // 收集所有品类ID
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 查询品类信息
        return categoryIds.stream()
                .map(categoryGateway::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }
}
