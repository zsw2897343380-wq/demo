package com.example.issue15.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息更新请求DTO
 * 用于接收前端传递的用户信息更新数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    /**
     * 用户昵称
     * 长度限制：2-50个字符
     */
    @Size(min = 2, max = 50, message = "昵称长度必须在2-50个字符之间")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$", message = "昵称只能包含中文、英文、数字和下划线")
    private String nickname;

    /**
     * 用户邮箱
     * 用于联系和找回密码
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 用户手机号
     * 中国大陆手机号格式
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 用户头像URL
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;

    /**
     * 用户性别
     * 0: 未知, 1: 男, 2: 女
     */
    @Pattern(regexp = "^[012]$", message = "性别值不正确，0:未知, 1:男, 2:女")
    private String gender;

    /**
     * 用户生日
     * 格式：yyyy-MM-dd
     */
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "生日格式不正确，应为yyyy-MM-dd")
    private String birthday;

    /**
     * 用户个人简介
     * 长度限制：最多200个字符
     */
    @Size(max = 200, message = "个人简介长度不能超过200个字符")
    private String bio;

    /**
     * 用户所在地区
     */
    @Size(max = 100, message = "地区长度不能超过100个字符")
    private String location;

    /**
     * 用户网站URL
     */
    @Size(max = 500, message = "网站URL长度不能超过500个字符")
    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$", message = "网站URL格式不正确")
    private String website;

    /**
     * 检查是否有任何字段被设置
     * 用于验证更新请求是否包含有效数据
     *
     * @return true 如果有至少一个字段被设置
     */
    public boolean hasAnyField() {
        return nickname != null || email != null || phone != null || avatar != null ||
               gender != null || birthday != null || bio != null || location != null || website != null;
    }

    /**
     * 获取非空的字段数量
     * 用于统计实际需要更新的字段数
     *
     * @return 非空字段的数量
     */
    public int getNonNullFieldCount() {
        int count = 0;
        if (nickname != null) count++;
        if (email != null) count++;
        if (phone != null) count++;
        if (avatar != null) count++;
        if (gender != null) count++;
        if (birthday != null) count++;
        if (bio != null) count++;
        if (location != null) count++;
        if (website != null) count++;
        return count;
    }
}
