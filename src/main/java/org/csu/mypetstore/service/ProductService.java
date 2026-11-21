package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.csu.mypetstore.entity.Product;
import org.csu.mypetstore.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品查询服务。本期提供首页两个商品栏目：「新品」按录入时间读取，「精选」按人工排序读取。
 */
@Service
public class ProductService {

    private final ProductMapper productMapper;

    // 构造器注入，便于测试且保证依赖不可变
    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /** 首页新品栏最多展示条数 */
    private static final int NEW_PRODUCTS_LIMIT = 3;

    /** 首页精选栏最多展示条数 */
    private static final int FEATURED_PRODUCTS_LIMIT = 4;

    /**
     * 首页新品：仅查询 ON_SALE 商品，按 created_at、id 倒序取最多 3 条。
     * created_at 表示最近录入时间，不代表本周销量或首次上架时间；
     * 上架但零库存（stock=0）的商品仍可展示，故不加库存过滤。
     */
    public List<Product> getNewProducts() {
        return productMapper.selectList(
                Wrappers.<Product>lambdaQuery()
                        .eq(Product::getStatus, "ON_SALE")
                        .orderByDesc(Product::getCreatedAt)
                        .orderByDesc(Product::getId)
                        .last("LIMIT " + NEW_PRODUCTS_LIMIT)
        );
    }

    /**
     * 首页精选：仅查询 ON_SALE 商品，按 sort_order、id 升序取最多 4 条。
     * sort_order 为人工设置的展示顺序，结果确定、所有游客看到相同列表；
     * 不与新品栏目去重，允许出现相同商品。无推荐算法、销量统计或登录依赖。
     */
    public List<Product> getFeaturedProducts() {
        return productMapper.selectList(
                Wrappers.<Product>lambdaQuery()
                        .eq(Product::getStatus, "ON_SALE")
                        .orderByAsc(Product::getSortOrder)
                        .orderByAsc(Product::getId)
                        .last("LIMIT " + FEATURED_PRODUCTS_LIMIT)
        );
    }
}
