package org.csu.mypetstore.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.csu.mypetstore.dto.UserReadResult;
import org.csu.mypetstore.entity.User;
import org.csu.mypetstore.mapper.UserMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UserService 纯单元测试：Mockito 模拟 UserMapper，构造真实 UserService，
 * 不启动 Spring 容器 / Web 服务，不连接数据库。
 */
class UserServiceTest {

    private UserMapper userMapper;
    private UserService userService;

    /**
     * Lambda 条件（User::getUsername 等）在构造查询时会立即解析列名，
     * 依赖 MyBatis-Plus 的 TableInfo 缓存；这里仅为用户实体补一次最小初始化，
     * 不启动完整应用。
     */
    @BeforeAll
    static void initUserTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userService = new UserService(userMapper);
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("$2a$10$should.not.leak");
        user.setDisplayName("Alice");
        user.setRole("CUSTOMER");
        user.setStatus("ENABLED");
        return user;
    }

    @Test
    @DisplayName("查到用户时正确转换为 UserReadResult，且查询不含密码列")
    void findByUsernameReturnsMappedDtoWhenFound() {
        when(userMapper.selectOne(any())).thenReturn(buildUser());

        Optional<UserReadResult> result = userService.findByUsername("alice");

        assertTrue(result.isPresent());
        assertThat(result.get()).isEqualTo(
                new UserReadResult(1L, "alice", "Alice", "CUSTOMER", "ENABLED"));
        // record 值断言已保证不含 passwordHash 字段；再验证 SELECT 列只有五个展示字段
        ArgumentCaptor<Wrapper<User>> captor = ArgumentCaptor.forClass(Wrapper.class);
        org.mockito.Mockito.verify(userMapper).selectOne(captor.capture());
        String sqlSelect = captor.getValue().getSqlSelect();
        assertThat(sqlSelect)
                .contains("id", "username", "display_name", "role", "status")
                .doesNotContain("password_hash");
    }

    @Test
    @DisplayName("查不到用户时返回空 Optional")
    void findByUsernameReturnsEmptyWhenNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThat(userService.findByUsername("ghost")).isEmpty();
    }

    @Test
    @DisplayName("Mapper 抛出 DataAccessResourceFailureException 时原样向上传播")
    void findByUsernamePropagatesDataAccessException() {
        DataAccessResourceFailureException boom =
                new DataAccessResourceFailureException("db down");
        when(userMapper.selectOne(any())).thenThrow(boom);

        DataAccessResourceFailureException thrown =
                org.junit.jupiter.api.Assertions.assertThrows(
                        DataAccessResourceFailureException.class,
                        () -> userService.findByUsername("alice"));

        // 同一个异常实例，未被包装或吞掉
        assertSame(boom, thrown);
    }
}
