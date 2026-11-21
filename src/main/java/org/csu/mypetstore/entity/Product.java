package org.csu.mypetstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对应 product 表：商品及可销售库存，一个商品对应一个 SKU。
 * 字段命名遵循下划线转驼峰，依赖 MyBatis-Plus 默认的 mapUnderscoreToCamelCase 完成列名映射。
 */
@Getter
@Setter
@TableName("product")
public class Product {

    /** 内部主键，对应 id BIGINT AUTO_INCREMENT */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 唯一销售编码，对应 sku VARCHAR(40)，创建后不修改 */
    private String sku;

    /** 归属二级分类 ID，对应 category_id BIGINT */
    private Long categoryId;

    /** 商品名称，对应 name VARCHAR(120) */
    private String name;

    /** 摘要，对应 summary VARCHAR(500)，可空 */
    private String summary;

    /** 纯文本详情，对应 description TEXT，可空，模板转义展示 */
    private String description;

    /** 封面图站内相对路径，对应 cover_image VARCHAR(255)，默认 images/logo.png */
    private String coverImage;

    /** 售价，对应 price DECIMAL(12,2)，金额用 BigDecimal 避免精度丢失 */
    private BigDecimal price;

    /** 可销售库存，对应 stock INT，下单扣减、未支付取消恢复 */
    private Integer stock;

    /** 状态 DRAFT/ON_SALE，对应 status VARCHAR(16) */
    private String status;

    /** 排序值，对应 sort_order INT，>=0 */
    private Integer sortOrder;

    /**
     * 乐观锁版本，对应 version BIGINT。
     * 所有商品写操作显式递增，防旧值覆盖；由业务层维护递增，此处仅作为普通字段映射。
     */
    private Long version;

    /** 创建时间，对应 created_at DATETIME */
    private LocalDateTime createdAt;

    /** 更新时间，对应 updated_at DATETIME */
    private LocalDateTime updatedAt;
}
