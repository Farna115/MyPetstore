package org.csu.mypetstore.mapper;

import org.csu.mypetstore.entity.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductMapper 真实数据库集成测试：dev 配置，只调用 selectById 读取教学演示数据，
 * 验证金额、库存、状态、分类外键与时间字段映射，不做任何写操作。
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class ProductMapperIntegrationTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("selectById 读取商品：字段映射与演示数据一致")
    void selectByIdMapsProductFields() {
        // 演示数据固定 ID：1 K9-BGL-03 比格犬，归属二级分类 101（犬类·宠物）
        Product product = productMapper.selectById(1L);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getSku()).isEqualTo("K9-BGL-03");
        assertThat(product.getName()).isEqualTo("比格犬");
        // category_id -> categoryId 下划线转驼峰（父分类链路见 CategoryMapperIntegrationTest）
        assertThat(product.getCategoryId()).isEqualTo(101L);
        // cover_image -> coverImage
        assertThat(product.getCoverImage()).isEqualTo("images/products/K9-BGL-03/1.jpg");
        // DECIMAL(12,2) -> BigDecimal，按数值比较不受 scale 影响
        assertThat(product.getPrice()).isNotNull()
                .isEqualByComparingTo(new BigDecimal("1280.00"));
        // INT -> Integer
        assertThat(product.getStock()).isEqualTo(10);
        assertThat(product.getStatus()).isEqualTo("ON_SALE");
        assertThat(product.getSortOrder()).isEqualTo(10);
        assertThat(product.getVersion()).isZero();
        // DATETIME -> LocalDateTime
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("下架商品可正常读取，状态为 DRAFT")
    void selectByIdMapsDraftProduct() {
        // 演示数据固定 ID：12 AQ-TANK-01 为下架教学商品
        Product draft = productMapper.selectById(12L);

        assertThat(draft).isNotNull();
        assertThat(draft.getSku()).isEqualTo("AQ-TANK-01");
        assertThat(draft.getStatus()).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("零库存上架商品映射为 0 而非 null")
    void selectByIdMapsZeroStock() {
        // 演示数据固定 ID：8 CT-BED-01 上架但售罄
        Product soldOut = productMapper.selectById(8L);

        assertThat(soldOut).isNotNull();
        assertThat(soldOut.getStatus()).isEqualTo("ON_SALE");
        assertThat(soldOut.getStock()).isNotNull().isZero();
    }

    @Test
    @DisplayName("不存在的商品 ID 返回 null")
    void selectByIdReturnsNullForMissingId() {
        assertThat(productMapper.selectById(999_999L)).isNull();
    }
}
