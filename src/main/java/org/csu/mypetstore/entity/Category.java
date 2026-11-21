package org.csu.mypetstore.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 对应 category 表：固定两级商品分类。
 * 字段命名遵循下划线转驼峰，依赖 MyBatis-Plus 默认的 mapUnderscoreToCamelCase 完成列名映射。
 */
@Getter
@Setter
@TableName("category")
public class Category {

    /** 内部主键，对应 id BIGINT AUTO_INCREMENT */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 全局唯一编码，对应 code VARCHAR(32)，创建后不修改 */
    private String code;

    /** 父分类 ID，对应 parent_id BIGINT；一级为空，二级指向一级，可空 */
    private Long parentId;

    /**
     * 数据库生成列，对应 parent_key BIGINT GENERATED ALWAYS AS (COALESCE(parent_id, 0)) STORED。
     * 仅用于读取（配合 uk_category_sibling_name 校验同级重名），
     * 插入与更新策略均为 NEVER，任何写操作不携带该列。
     */
    @TableField(value = "parent_key",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER)
    private Long parentKey;

    /** 分类名称，对应 name VARCHAR(50) */
    private String name;

    /** 排序值，对应 sort_order INT，>=0 */
    private Integer sortOrder;

    /** 创建时间，对应 created_at DATETIME */
    private LocalDateTime createdAt;

    /** 更新时间，对应 updated_at DATETIME */
    private LocalDateTime updatedAt;
}
