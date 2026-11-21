package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.csu.mypetstore.entity.Product;
import org.csu.mypetstore.mapper.ProductMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductService 真实数据库集成测试：dev 配置，只读教学演示数据。
 * 复用 ProductMapper 独立读取全表，在 Java 中计算预期的过滤、排序与限量，
 * 核对 getNewProducts / getFeaturedProducts 的结果，不写库、不重置数据库。
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    /** 独立读取全表作为基准，不经过 Service，也不使用 JdbcTemplate */
    private List<Product> loadAll() {
        return productMapper.selectList(Wrappers.emptyWrapper());
    }

    private static List<Product> onSale(List<Product> all) {
        return all.stream().filter(p -> "ON_SALE".equals(p.getStatus())).toList();
    }

    @Test
    @DisplayName("新品：ON_SALE 过滤 + created_at、id 倒序，最多 3 项与独立计算一致")
    void newProductsMatchIndependentExpectation() {
        List<Product> all = loadAll();
        List<Product> onSale = onSale(all);
        // 限量断言有意义的前提：在售商品多于 3 个
        assertThat(onSale).hasSizeGreaterThan(3);

        List<Long> expectedIds = onSale.stream()
                .sorted(Comparator.comparing(Product::getCreatedAt, Comparator.reverseOrder())
                        .thenComparing(Product::getId, Comparator.reverseOrder()))
                .limit(3)
                .map(Product::getId)
                .toList();

        List<Product> actual = productService.getNewProducts();

        assertThat(actual).hasSize(3);
        assertThat(actual).extracting(Product::getId).containsExactlyElementsOf(expectedIds);
        assertThat(actual).allMatch(p -> "ON_SALE".equals(p.getStatus()));
    }

    @Test
    @DisplayName("新品可含上架零库存商品，且绝不含 DRAFT")
    void newProductsRespectStatusAndAllowZeroStock() {
        List<Product> onSale = onSale(loadAll());
        // 演示数据存在上架零库存商品（如 CT-BED-01、BIRD-BATH-26）
        assertThat(onSale).anyMatch(p -> p.getStock() == 0);

        List<Product> actual = productService.getNewProducts();
        assertThat(actual).noneMatch(p -> "DRAFT".equals(p.getStatus()));
    }

    @Test
    @DisplayName("精选：ON_SALE 过滤 + sort_order、id 升序，最多 4 项与独立计算一致")
    void featuredProductsMatchIndependentExpectation() {
        List<Product> all = loadAll();
        List<Product> onSale = onSale(all);
        assertThat(onSale).hasSizeGreaterThan(4);

        List<Long> expectedIds = onSale.stream()
                .sorted(Comparator.comparing(Product::getSortOrder).thenComparing(Product::getId))
                .limit(4)
                .map(Product::getId)
                .toList();

        List<Product> actual = productService.getFeaturedProducts();

        assertThat(actual).hasSize(4);
        assertThat(actual).extracting(Product::getId).containsExactlyElementsOf(expectedIds);
        assertThat(actual).allMatch(p -> "ON_SALE".equals(p.getStatus()));
    }

    @Test
    @DisplayName("精选结果确定可复现，且允许与新品栏目出现相同商品")
    void featuredProductsDeterministicAndMayOverlapNew() {
        List<Long> first = productService.getFeaturedProducts().stream().map(Product::getId).toList();
        List<Long> second = productService.getFeaturedProducts().stream().map(Product::getId).toList();
        assertThat(second).containsExactlyElementsOf(first);

        // 演示数据中 sort_order=0 的金毛/英短同时也是较晚录入的商品，两栏目存在交叉；
        // 此处只核对“不做去重”这一契约：若有相同 id 属预期，不作排除断言
        List<Long> newIds = productService.getNewProducts().stream().map(Product::getId).toList();
        assertThat(newIds).hasSize(3);
        assertThat(first).hasSize(4);
    }
}
