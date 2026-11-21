package org.csu.mypetstore.mapper;

import org.csu.mypetstore.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CategoryMapper 真实数据库集成测试：dev 配置，只调用 selectById 读取教学演示数据，
 * 验证下划线列映射与生成列 parent_key 的可读性，不做任何写操作。
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional(readOnly = true)
class CategoryMapperIntegrationTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("selectById 读取二级分类：父子关系与生成列映射正确")
    void selectByIdMapsSecondLevelCategory() {
        // 演示数据固定 ID：101 DOGS_PETS（宠物）父分类为 1 DOGS（犬类）
        Category secondLevel = categoryMapper.selectById(101L);

        assertThat(secondLevel).isNotNull();
        assertThat(secondLevel.getId()).isEqualTo(101L);
        assertThat(secondLevel.getCode()).isEqualTo("DOGS_PETS");
        assertThat(secondLevel.getName()).isEqualTo("宠物");
        // parent_id -> parentId 下划线转驼峰
        assertThat(secondLevel.getParentId()).isEqualTo(1L);
        // 生成列 parent_key = COALESCE(parent_id, 0)，只读可映射
        assertThat(secondLevel.getParentKey()).isEqualTo(1L);
        assertThat(secondLevel.getSortOrder()).isEqualTo(10);
        // DATETIME -> LocalDateTime
        assertThat(secondLevel.getCreatedAt()).isNotNull();
        assertThat(secondLevel.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("selectById 读取一级分类：父分类为空且 parent_key 为 0")
    void selectByIdMapsTopLevelCategory() {
        Category top = categoryMapper.selectById(1L);

        assertThat(top).isNotNull();
        assertThat(top.getCode()).isEqualTo("DOGS");
        assertThat(top.getName()).isEqualTo("犬类");
        assertThat(top.getParentId()).isNull();
        assertThat(top.getParentKey()).isEqualTo(0L);
    }

    @Test
    @DisplayName("二级分类的父分类可通过 selectById 二次读取")
    void parentCategoryReadableById() {
        Category secondLevel = categoryMapper.selectById(101L);
        assertThat(secondLevel).isNotNull();

        Category parent = categoryMapper.selectById(secondLevel.getParentId());

        assertThat(parent).isNotNull();
        assertThat(parent.getId()).isEqualTo(1L);
        assertThat(parent.getCode()).isEqualTo("DOGS");
    }

    @Test
    @DisplayName("不存在的分类 ID 返回 null")
    void selectByIdReturnsNullForMissingId() {
        assertThat(categoryMapper.selectById(999_999L)).isNull();
    }
}
