package com.jaychou.interviewk.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jaychou.interviewk.model.dto.question.QuestionQueryRequest;
import com.jaychou.interviewk.model.entity.Question;
import com.jaychou.interviewk.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: QuestionService
 * Package: com.jaychou.interviewk.service
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 21:51
 * @Version: v1.0
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);
}
