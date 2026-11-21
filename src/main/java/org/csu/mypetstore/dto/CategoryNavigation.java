package org.csu.mypetstore.dto;

import java.util.List;

/**
 * 首页分类导航返回对象：一个一级分类及其按序排列的二级分类列表。
 * 仅包含页面导航所需字段，不含排序值与时间。
 */
public record CategoryNavigation(
        Long id,
        String code,
        String name,
        List<CategoryNavigation> children
) {
}
