package org.csu.mypetstore.dto;

/**
 * 用户展示结果：仅包含页面需要的五个字段，刻意不含 passwordHash，避免密码散列外泄。
 */
public record UserReadResult(
        Long id,
        String username,
        String displayName,
        String role,
        String status
) {
}
