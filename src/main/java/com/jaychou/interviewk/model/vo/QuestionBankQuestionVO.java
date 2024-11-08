package com.jaychou.interviewk.model.vo;


import com.jaychou.interviewk.model.entity.QuestionBankQuestion;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题库题目关联视图
 *
 * @author <a href="https://github.com/KongJay">红模仿</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionBankQuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;
    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param questionBankQuestion_VO
     * @return
     */
    public static QuestionBankQuestion voToObj(QuestionBankQuestionVO questionBankQuestion_VO) {
        if (questionBankQuestion_VO == null) {
            return null;
        }
        QuestionBankQuestion question_bank_question = new QuestionBankQuestion();
        BeanUtils.copyProperties(questionBankQuestion_VO, question_bank_question);


        return question_bank_question;
    }

    /**
     * 对象转封装类
     *
     * @param question_bank_question
     * @return
     */
    public static QuestionBankQuestionVO objToVo(QuestionBankQuestion question_bank_question) {
        if (question_bank_question == null) {
            return null;
        }
        QuestionBankQuestionVO questionBankQuestion_VO = new QuestionBankQuestionVO();
        BeanUtils.copyProperties(question_bank_question, questionBankQuestion_VO);

        return questionBankQuestion_VO;
    }
}
