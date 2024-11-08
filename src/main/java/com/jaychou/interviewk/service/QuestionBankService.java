package com.jaychou.interviewk.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankQueryRequest;
import com.jaychou.interviewk.model.entity.QuestionBank;
import com.jaychou.interviewk.model.vo.QuestionBankVO;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: QuestionBankService
 * Package: com.jaychou.interviewk.service
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:12
 * @Version: v1.0
 */
public interface QuestionBankService extends IService<QuestionBank> {

    /**
     * 校验数据
     *
     * @param question_bank
     * @param add 对创建的数据进行校验
     */
    void validQuestionBank(QuestionBank question_bank, boolean add);

    /**
     * 获取查询条件
     *
     * @param question_bankQueryRequest
     * @return
     */
    QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest question_bankQueryRequest);

    /**
     * 获取题库封装
     *
     * @param question_bank
     * @param request
     * @return
     */
    QuestionBankVO getQuestionBankVO(QuestionBank question_bank, HttpServletRequest request);

    /**
     * 分页获取题库封装
     *
     * @param question_bankPage
     * @param request
     * @return
     */
    Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> question_bankPage, HttpServletRequest request);
}
