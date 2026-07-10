package com.accounting.system.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(max = 50, message = "昵称最长50位")
    private String nickname;

    @Size(max = 100, message = "邮箱最长100位")
    private String email;

    @Size(max = 20, message = "手机号最长20位")
    private String phone;
}
