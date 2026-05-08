package com.example.issue15.profile.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户资料数据传输对象
 * 用于用户注册、登录和资料管理的API交互
 */
public class ProfileDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名 - 用于登录
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码 - 用于注册和登录
     * 注意：密码不会在响应中返回
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    /**
     * 电子邮件地址
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 用户昵称
     */
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户手机号
     */
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    /**
     * 用户状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * JWT Token - 用于登录响应
     */
    private String token;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    // ==================== 构造方法 ====================

    public ProfileDTO() {
    }

    /**
     * 用于用户注册的构造方法
     */
    public ProfileDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * 用于用户登录的构造方法
     */
    public ProfileDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ==================== Getter 和 Setter 方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    // ==================== 业务方法 ====================

    /**
     * 清除敏感信息（密码）
     * 在返回响应前调用，确保密码不会泄露
     */
    public void clearSensitiveInfo() {
        this.password = null;
    }

    /**
     * 检查是否为有效的注册信息
     */
    public boolean isValidForRegistration() {
        return username != null && !username.trim().isEmpty()
                && password != null && password.length() >= 6
                && email != null && email.contains("@");
    }

    /**
     * 检查是否为有效的登录信息
     */
    public boolean isValidForLogin() {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }

    // ==================== toString 方法 ====================

    @Override
    public String toString() {
        return "ProfileDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", lastLoginTime=" + lastLoginTime +
                '}';
    }

    // ==================== Builder 模式 ====================

    /**
     * ProfileDTO Builder 类
     * 用于构建复杂的 ProfileDTO 对象
     */
    public static class Builder {
        private ProfileDTO profileDTO;

        public Builder() {
            profileDTO = new ProfileDTO();
        }

        public Builder id(Long id) {
            profileDTO.setId(id);
            return this;
        }

        public Builder username(String username) {
            profileDTO.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            profileDTO.setPassword(password);
            return this;
        }

        public Builder email(String email) {
            profileDTO.setEmail(email);
            return this;
        }

        public Builder nickname(String nickname) {
            profileDTO.setNickname(nickname);
            return this;
        }

        public Builder avatar(String avatar) {
            profileDTO.setAvatar(avatar);
            return this;
        }

        public Builder phone(String phone) {
            profileDTO.setPhone(phone);
            return this;
        }

        public Builder status(Integer status) {
            profileDTO.setStatus(status);
            return this;
        }

        public Builder token(String token) {
            profileDTO.setToken(token);
            return this;
        }

        public ProfileDTO build() {
            return profileDTO;
        }
    }

    /**
     * 创建 Builder 实例的静态方法
     */
    public static Builder builder() {
        return new Builder();
    }
}
