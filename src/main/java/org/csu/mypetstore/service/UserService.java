package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.csu.mypetstore.dto.UserReadResult;
import org.csu.mypetstore.entity.User;
import org.csu.mypetstore.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户只读查询服务。基于 UserMapper，返回脱敏的展示结果，不触及密码散列。
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    // 构造器注入，便于测试且保证依赖不可变
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 按登录名查询展示字段。
     * 仅 SELECT 五个展示列，不读取 password_hash；不存在返回空 Optional；
     * 数据访问异常不做捕获，交由上层处理。
     */
    public Optional<UserReadResult> findByUsername(String username) {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .select(User::getId, User::getUsername,
                                User::getDisplayName, User::getRole, User::getStatus)
                        .eq(User::getUsername, username)
        );
        if (user == null) {
            return Optional.empty();
        }
        return Optional.of(new UserReadResult(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getStatus()
        ));
    }
}
