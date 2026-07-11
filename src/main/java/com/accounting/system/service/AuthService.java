package com.accounting.system.service;

import com.accounting.system.model.dto.LoginDTO;
import com.accounting.system.model.dto.RegisterDTO;
import com.accounting.system.model.vo.LoginVO;

/**
 * ============================================================
 * 认证服务接口
 * ============================================================
 *
 * 【接口-实现分离的意义】
 * 面向接口编程：Controller依赖接口而非具体实现，便于：
 * 1. 单元测试：可以Mock接口，无需启动整个Spring容器
 * 2. 扩展性：将来可以切换实现（如改用Redis存储Token）
 * 3. AOP代理：Spring事务、缓存等通过接口代理实现
 */
public interface AuthService {
    /** 用户注册：校验唯一性 → BCrypt加密密码 → 入库 */
    void register(RegisterDTO dto);
    /** 用户登录：校验密码 → 生成JWT → 返回Token + 用户信息 */
    LoginVO login(LoginDTO dto);
}
