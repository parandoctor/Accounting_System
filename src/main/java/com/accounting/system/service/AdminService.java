package com.accounting.system.service;

import com.accounting.system.model.vo.PageVO;
import com.accounting.system.model.vo.UserVO;
import java.util.Map;

public interface AdminService {
    PageVO<UserVO> listUsers(int page, int size, String keyword);
    void toggleUserStatus(Long userId);
    void resetUserPassword(Long userId);
    Map<String, Long> getSystemStatistics();
}
