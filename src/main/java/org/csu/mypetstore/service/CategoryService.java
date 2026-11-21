package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.csu.mypetstore.dto.CategoryNavigation;
import org.csu.mypetstore.entity.Category;
import org.csu.mypetstore.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类查询服务。首页导航一次读取全部 category，在内存中组装两级树，
 * 不为每个一级分类单独发起查询。
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    // 构造器注入，便于测试且保证依赖不可变
    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /** 导航树排序：sort_order 升序，同值按 id 升序，与 idx_category_parent_sort 口径一致 */
    private static final Comparator<Category> NAV_COMPARATOR =
            Comparator.comparing(Category::getSortOrder)
                    .thenComparing(Category::getId);

    /**
     * 首页分类导航：单次 SELECT 全部分类（按 id 读取后内存排序，避免逐父查询），
     * 一级 = parentId 为 null，二级挂到对应一级下；父不存在孤立的二级不展示。
     */
    public List<CategoryNavigation> getHomeNavigation() {
        List<Category> all = categoryMapper.selectList(
                Wrappers.<Category>lambdaQuery().orderByAsc(Category::getId)
        );

        // 二级按 parentId 分组；all 已按 id 升序，LinkedHashMap 保留组内稳定顺序
        Map<Long, List<Category>> childrenByParent = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId, LinkedHashMap::new, Collectors.toList()));

        return all.stream()
                .filter(c -> c.getParentId() == null)
                .sorted(NAV_COMPARATOR)
                .map(top -> new CategoryNavigation(
                        top.getId(),
                        top.getCode(),
                        top.getName(),
                        childrenByParent.getOrDefault(top.getId(), List.of()).stream()
                                .sorted(NAV_COMPARATOR)
                                .map(child -> new CategoryNavigation(
                                        child.getId(), child.getCode(), child.getName(), List.of()))
                                .toList()))
                .toList();
    }
}
