package com.jaychou.interviewk.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: UserRegisterRequst
 * Package: com.jaychou.interviewk.model.dto
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/7 - 1:45
 * @Version: v1.0
 */
@Data
public class UserRegisterRequst implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;
}
