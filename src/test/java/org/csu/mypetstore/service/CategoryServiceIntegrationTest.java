package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.csu.mypetstore.dto.CategoryNavigation;
import org.csu.mypetstore.entity.Category;
import org.csu.mypetstore.mapper.CategoryMapper;
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
 * CategoryService 真实数据库集成测试：dev 配置，只读教学演示数据。
 * 复用 CategoryMapper 独立读取全部分类，在 Java 中计算预期的一级顺序与分组，
 * 核对 getHomeNavigation 的分组与排序结果，不写库、不重置数据库。
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    /** 与 Service 相同的排序口径，但在这里只用于从原始数据独立构造预期 */
    private static final Comparator<Category> BY_SORT_THEN_ID =
            Comparator.comparing(Category::getSortOrder).thenComparing(Category::getId);

    @Test
    @DisplayName("首页导航：一级顺序与二级分组和全表独立计算结果一致")
    void homeNavigationGroupsByIndependentlyComputedExpectation() {
        // 独立基准：直接读全表，不经过 Service
        List<Category> all = categoryMapper.selectList(Wrappers.emptyWrapper());
        assertThat(all).isNotEmpty();

        List<CategoryNavigation> actual = categoryService.getHomeNavigation();

        List<Category> expectedTops = all.stream()
                .filter(c -> c.getParentId() == null)
                .sorted(BY_SORT_THEN_ID)
                .toList();

        assertThat(actual).hasSize(expectedTops.size());
        for (int i = 0; i < expectedTops.size(); i++) {
            Category top = expectedTops.get(i);
            assertThat(actual.get(i).id()).isEqualTo(top.getId());

            List<Category> expectedChildren = all.stream()
                    .filter(c -> top.getId().equals(c.getParentId()))
                    .sorted(BY_SORT_THEN_ID)
                    .toList();
            assertThat(actual.get(i).children())
                    .extracting(CategoryNavigation::id)
                    .containsExactlyElementsOf(expectedChildren.stream().map(Category::getId).toList());
        }
    }

    @Test
    @DisplayName("分组不变量：一级均无父、二级归属所在一级且按 sort_order、id 升序")
    void homeNavigationRespectsTwoLevelOrderingInvariants() {
        List<Category> all = categoryMapper.selectList(Wrappers.emptyWrapper());
        List<CategoryNavigation> actual = categoryService.getHomeNavigation();

        // 演示数据固定 5 个一级分类（DOGS/CATS/FISH/BIRDS/REPTILES）
        long expectedTopCount = all.stream().filter(c -> c.getParentId() == null).count();
        assertThat(actual).hasSize((int) expectedTopCount).extracting(CategoryNavigation::code)
                .containsExactly("DOGS", "CATS", "FISH", "BIRDS", "REPTILES");

        for (CategoryNavigation top : actual) {
            assertThat(top.children()).isNotEmpty();
            List<Integer> childSortOrders = top.children().stream()
                    .map(child -> categoryMapper.selectById(child.id()).getSortOrder())
                    .toList();
            // 升序校验：相邻元素前 <= 后（sort_order 已按同 id 兜底，此处核对主键序即可）
            for (int i = 1; i < top.children().size(); i++) {
                assertThat(childSortOrders.get(i - 1)).isLessThanOrEqualTo(childSortOrders.get(i));
            }
            // 每个二级都必须真实挂在该一级下
            for (CategoryNavigation child : top.children()) {
                assertThat(categoryMapper.selectById(child.id()).getParentId()).isEqualTo(top.id());
            }
        }

        // 全表中的每个二级都恰好出现一次，孤立的二级不应出现（演示数据无孤立行）
        List<Long> secondLevelIds = all.stream()
                .filter(c -> c.getParentId() != null)
                .map(Category::getId)
                .toList();
        assertThat(actual.stream().flatMap(t -> t.children().stream()).map(CategoryNavigation::id))
                .containsExactlyInAnyOrderElementsOf(secondLevelIds);
    }
}
