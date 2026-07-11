package com.accounting.system.service;

import com.accounting.system.model.dto.PasswordChangeDTO;
import com.accounting.system.model.dto.UserUpdateDTO;
import com.accounting.system.model.vo.UserVO;

/**
 * ============================================================
 * 用户服务接口
 * ============================================================
 *
 * 【用户操作】
 * - getProfile：获取个人信息
 * - updateProfile：更新昵称/邮箱/手机号（部分更新，只改有值的字段）
 * - changePassword：修改密码（需验证旧密码）
 *
 * 【安全设计】
 * 所有操作通过userId参数绑定当前登录用户，
 * 不受前端传入的ID影响，防止水平越权
 */
public interface UserService {
    UserVO getProfile(Long userId);
    UserVO updateProfile(Long userId, UserUpdateDTO dto);
    void changePassword(Long userId, PasswordChangeDTO dto);
}
