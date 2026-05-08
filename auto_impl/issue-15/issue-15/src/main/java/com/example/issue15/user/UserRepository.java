package com.example.issue15.user.repository;

import com.example.issue15.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层 - 提供用户相关的数据库操作
 * 继承 JpaRepository 以获得基本的 CRUD 操作
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 返回 Optional 类型的用户对象，避免空指针异常
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 返回 Optional 类型的用户对象
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户（用于登录验证）
     * @param username 用户名
     * @param email 邮箱地址
     * @return 返回匹配的用户对象
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, 
                                         @Param("email") String email);

    /**
     * 检查用户名是否已存在
     * @param username 用户名
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 如果存在返回 true，否则返回 false
     */
    boolean existsByEmail(String email);

    /**
     * 查找所有已激活的用户
     * @return 返回所有激活状态的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.enabled = true")
    List<User> findAllEnabledUsers();

    /**
     * 查找所有已禁用的用户
     * @return 返回所有禁用状态的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.enabled = false")
    List<User> findAllDisabledUsers();

    /**
     * 根据用户角色查找用户
     * @param role 用户角色
     * @return 返回具有指定角色的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") String role);

    /**
     * 根据用户名模糊查询用户
     * @param username 用户名（支持模糊匹配）
     * @return 返回匹配的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);

    /**
     * 获取最近注册的用户（按创建时间降序排列）
     * @param limit 限制返回数量
     * @return 返回最近注册的用户列表
     */
    @Query(value = "SELECT * FROM users ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<User> findRecentUsers(@Param("limit") int limit);

    /**
     * 统计指定角色的用户数量
     * @param role 用户角色
     * @return 返回用户数量
     */
    long countByRole(String role);

    /**
     * 统计激活用户的数量
     * @return 返回激活用户数量
     */
    long countByEnabledTrue();

    /**
     * 统计禁用用户的数量
     * @return 返回禁用用户数量
     */
    long countByEnabledFalse();

    /**
     * 根据用户ID列表批量查找用户
     * @param ids 用户ID列表
     * @return 返回匹配的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(@Param("ids") List<Long> ids);

    /**
     * 删除指定用户名（用于用户注销）
     * @param username 用户名
     */
    void deleteByUsername(String username);
}
