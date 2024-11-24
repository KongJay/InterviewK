package com.jaychou.interviewk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaychou.interviewk.annotation.AuthCheck;
import com.jaychou.interviewk.common.BaseResponse;
import com.jaychou.interviewk.common.DeleteRequest;
import com.jaychou.interviewk.common.ErrorCode;
import com.jaychou.interviewk.common.ResultUtils;
import com.jaychou.interviewk.constant.UserConstant;
import com.jaychou.interviewk.exception.BusinessException;
import com.jaychou.interviewk.exception.ThrowUtils;
import com.jaychou.interviewk.model.dto.questionbankquestion.*;
import com.jaychou.interviewk.model.entity.QuestionBankQuestion;
import com.jaychou.interviewk.model.entity.User;
import com.jaychou.interviewk.model.vo.QuestionBankQuestionVO;
import com.jaychou.interviewk.service.QuestionBankQuestionService;
import com.jaychou.interviewk.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Wrapper;
import java.util.List;

/**
 * ClassName: QuestionBankQuestionController
 * Package: com.jaychou.interviewk.controller
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:20
 * @Version: v1.0
 */
@RestController
@RequestMapping("/QuestionBankQuestion")
@Slf4j
public class QuestionBankQuestionController {

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题库题目关联
     *
     * @param QuestionBankQuestionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionBankQuestion(@RequestBody QuestionBankQuestionAddRequest QuestionBankQuestionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankQuestionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion QuestionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(QuestionBankQuestionAddRequest, QuestionBankQuestion);
        // 数据校验
        questionBankQuestionService.validQuestionBankQuestion(QuestionBankQuestion, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        QuestionBankQuestion.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionBankQuestionService.save(QuestionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankQuestionId = QuestionBankQuestion.getId();
        return ResultUtils.success(newQuestionBankQuestionId);
    }

    /**
     * 删除题库题目关联
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionBankQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBankQuestion oldQuestionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBankQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionBankQuestionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题库题目关联（仅管理员可用）
     *
     * @param QuestionBankQuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBankQuestion(@RequestBody QuestionBankQuestionUpdateRequest QuestionBankQuestionUpdateRequest) {
        if (QuestionBankQuestionUpdateRequest == null || QuestionBankQuestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBankQuestion QuestionBankQuestion = new QuestionBankQuestion();
        BeanUtils.copyProperties(QuestionBankQuestionUpdateRequest, QuestionBankQuestion);
        // 数据校验
        questionBankQuestionService.validQuestionBankQuestion(QuestionBankQuestion, false);
        // 判断是否存在
        long id = QuestionBankQuestionUpdateRequest.getId();
        QuestionBankQuestion oldQuestionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(oldQuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionBankQuestionService.updateById(QuestionBankQuestion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库题目关联（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankQuestionVO> getQuestionBankQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QuestionBankQuestion QuestionBankQuestion = questionBankQuestionService.getById(id);
        ThrowUtils.throwIf(QuestionBankQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVO(QuestionBankQuestion, request));
    }

    /**
     * 分页获取题库题目关联列表（仅管理员可用）
     *
     * @param QuestionBankQuestionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBankQuestion>> listQuestionBankQuestionByPage(@RequestBody QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest) {
        long current = QuestionBankQuestionQueryRequest.getCurrent();
        long size = QuestionBankQuestionQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = questionBankQuestionService.page(new Page<>(current, size),
                questionBankQuestionService.getQueryWrapper(QuestionBankQuestionQueryRequest));
        return ResultUtils.success(QuestionBankQuestionPage);
    }

    /**
     * 分页获取题库题目关联列表（封装类）
     *
     * @param QuestionBankQuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listQuestionBankQuestionVOByPage(@RequestBody QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest,
                                                                                       HttpServletRequest request) {
        long current = QuestionBankQuestionQueryRequest.getCurrent();
        long size = QuestionBankQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = questionBankQuestionService.page(new Page<>(current, size),
                questionBankQuestionService.getQueryWrapper(QuestionBankQuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVOPage(QuestionBankQuestionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题库题目关联列表
     *
     * @param QuestionBankQuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankQuestionVO>> listMyQuestionBankQuestionVOByPage(@RequestBody QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest,
                                                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        QuestionBankQuestionQueryRequest.setUserId(loginUser.getId());
        long current = QuestionBankQuestionQueryRequest.getCurrent();
        long size = QuestionBankQuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBankQuestion> QuestionBankQuestionPage = questionBankQuestionService.page(new Page<>(current, size),
                questionBankQuestionService.getQueryWrapper(QuestionBankQuestionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionBankQuestionService.getQuestionBankQuestionVOPage(QuestionBankQuestionPage, request));
    }

    /**
     * 移除题库题目关联
     *
     * @param questionBankQuestionRemoveRequest
     * @return
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> removeQuestionBankQuestion(@RequestBody QuestionBankQuestionRemoveRequest questionBankQuestionRemoveRequest) {
        ThrowUtils.throwIf(questionBankQuestionRemoveRequest == null, ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestionRemoveRequest.getQuestionBankId();
        Long questionId = questionBankQuestionRemoveRequest.getQuestionId();
        LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .eq(QuestionBankQuestion::getQuestionId, questionId);
        boolean result = questionBankQuestionService.remove(lambdaQueryWrapper);
        return ResultUtils.success(result);
    }

    /**
     * 批量添加题目到题库
     * @param questionBankQuestionBatchAddRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/add/batch")
    public BaseResponse<Boolean> batchAddQuestionToBank(@RequestBody QuestionBankQuestionBatchAddRequest questionBankQuestionBatchAddRequest,HttpServletRequest request){
        ThrowUtils.throwIf(questionBankQuestionBatchAddRequest == null,ErrorCode.PARAMS_ERROR);
        User user = userService.getLoginUser(request);
        Long questionBankId = questionBankQuestionBatchAddRequest.getQuestionBankId();
        List<Long> questionId = questionBankQuestionBatchAddRequest.getQuestionIdList();
       questionBankQuestionService.batchAddQuestionToBank(questionId,questionBankId,user);
        return ResultUtils.success(true);
    }
    /**
     * 批量从题库删除题目
     * @param questionBankQuestionBatchRemoveRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/remove/batch")
    public BaseResponse<Boolean> batchRemoveQuestionFromBank(@RequestBody QuestionBankQuestionBatchRemoveRequest questionBankQuestionBatchRemoveRequest,HttpServletRequest request){
        ThrowUtils.throwIf(questionBankQuestionBatchRemoveRequest == null,ErrorCode.PARAMS_ERROR);
        Long questionBankId = questionBankQuestionBatchRemoveRequest.getQuestionBankId();
        List<Long> questionId = questionBankQuestionBatchRemoveRequest.getQuestionIdList();
        questionBankQuestionService.batchRemoveQuestionFromBank(questionId,questionBankId);
        return ResultUtils.success(true);
    }
    // endregion
}
