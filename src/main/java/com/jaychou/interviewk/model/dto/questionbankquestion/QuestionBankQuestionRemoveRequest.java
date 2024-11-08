package com.jaychou.interviewk.model.dto.questionbankquestion;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: QuestionBankQuestionRemoveRequest
 * Package: com.jaychou.interviewk.model.dto.questionbankquestion
 * Description:移除题库题目关联请求
 *
 * @Author: 红模仿
 * @Create: 2024/11/9 - 3:53
 * @Version: v1.0
 */
@Data
public class QuestionBankQuestionRemoveRequest implements Serializable {
    /**
     * 题库id
     */
    private Long questionBankId;
    /**
     * 题目id
     */
    private Long questionId;
    private static final long serialVersionUID = 1L;
}
