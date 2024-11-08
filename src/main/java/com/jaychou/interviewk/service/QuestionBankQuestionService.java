package com.jaychou.interviewk.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jaychou.interviewk.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.jaychou.interviewk.model.entity.QuestionBankQuestion;
import com.jaychou.interviewk.model.vo.QuestionBankQuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: QuestionBankQuestion
 * Package: com.jaychou.interviewk.service
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:12
 * @Version: v1.0
 */
public interface QuestionBankQuestionService extends IService<QuestionBankQuestion> {

    /**
     * 校验数据
     *
     * @param QuestionBankQuestion
     * @param add 对创建的数据进行校验
     */
    void validQuestionBankQuestion(QuestionBankQuestion QuestionBankQuestion, boolean add);

    /**
     * 获取查询条件
     *
     * @param QuestionBankQuestionQueryRequest
     * @return
     */
    QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest);

    /**
     * 获取题库题目关联封装
     *
     * @param QuestionBankQuestion
     * @param request
     * @return
     */
    QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion QuestionBankQuestion, HttpServletRequest request);

    /**
     * 分页获取题库题目关联封装
     *
     * @param QuestionBankQuestionPage
     * @param request
     * @return
     */
    Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> QuestionBankQuestionPage, HttpServletRequest request);
}
