package org.csu.mypetstore.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 对应 store_user 表：教学账号（登录身份与角色）。
 * 字段命名遵循下划线转驼峰，依赖 MyBatis-Plus 默认的 mapUnderscoreToCamelCase 完成列名映射。
 */
@Getter
@Setter
@TableName("store_user")
public class User {

    /** 内部主键，对应 id BIGINT AUTO_INCREMENT */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名，对应 username VARCHAR(50) */
    private String username;

    /** BCrypt 散列，对应 password_hash VARCHAR(255) */
    private String passwordHash;

    /** 显示名称，对应 display_name VARCHAR(50) */
    private String displayName;

    /** 角色 CUSTOMER/ADMIN，对应 role VARCHAR(16) */
    private String role;

    /** 状态 ENABLED/DISABLED，对应 status VARCHAR(16) */
    private String status;

    /** 邮箱，对应 email VARCHAR(100)，可空 */
    private String email;

    /** 电话，对应 phone VARCHAR(30)，可空 */
    private String phone;

    /** 创建时间，对应 created_at DATETIME */
    private LocalDateTime createdAt;

    /** 更新时间，对应 updated_at DATETIME */
    private LocalDateTime updatedAt;
}
