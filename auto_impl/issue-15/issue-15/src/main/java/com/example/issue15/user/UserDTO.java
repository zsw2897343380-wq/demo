package com.example.issue15.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 用于用户注册、登录、信息更新等API交互
 */
public class UserDTO {

    /**
     * 用户ID（仅在响应中使用）
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码（仅在请求中使用）
     */
    @NotBlank(message = "密码不能为空", groups = {RegisterGroup.class, LoginGroup.class})
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间", groups = {RegisterGroup.class})
    private String password;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空", groups = {RegisterGroup.class, UpdateInfoGroup.class})
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别（0-未知，1-男，2-女）
     */
    private Integer gender;

    /**
     * 用户状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * JWT Token（仅在登录响应中使用）
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

    public UserDTO() {
    }

    /**
     * 注册用构造方法
     */
    public UserDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * 登录用构造方法
     */
    public UserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ==================== Getter/Setter 方法 ====================

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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

    // ==================== 验证分组接口 ====================

    /**
     * 注册验证分组
     */
    public interface RegisterGroup {
    }

    /**
     * 登录验证分组
     */
    public interface LoginGroup {
    }

    /**
     * 更新信息验证分组
     */
    public interface UpdateInfoGroup {
    }

    // ==================== toString 方法 ====================

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", lastLoginTime=" + lastLoginTime +
                '}';
    }

    // ==================== Builder 模式 ====================

    /**
     * 使用Builder模式构建UserDTO
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final UserDTO userDTO = new UserDTO();

        public Builder id(Long id) {
            userDTO.setId(id);
            return this;
        }

        public Builder username(String username) {
            userDTO.setUsername(username);
            return this;
        }

        public Builder password(String password) {
            userDTO.setPassword(password);
            return this;
        }

        public Builder email(String email) {
            userDTO.setEmail(email);
            return this;
        }

        public Builder phone(String phone) {
            userDTO.setPhone(phone);
            return this;
        }

        public Builder nickname(String nickname) {
            userDTO.setNickname(nickname);
            return this;
        }

        public Builder avatar(String avatar) {
            userDTO.setAvatar(avatar);
            return this;
        }

        public Builder gender(Integer gender) {
            userDTO.setGender(gender);
            return this;
        }

        public Builder status(Integer status) {
            userDTO.setStatus(status);
            return this;
        }

        public Builder token(String token) {
            userDTO.setToken(token);
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            userDTO.setCreateTime(createTime);
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            userDTO.setUpdateTime(updateTime);
            return this;
        }

        public Builder lastLoginTime(LocalDateTime lastLoginTime) {
            userDTO.setLastLoginTime(lastLoginTime);
            return this;
        }

        public UserDTO build() {
            return userDTO;
        }
    }
}
