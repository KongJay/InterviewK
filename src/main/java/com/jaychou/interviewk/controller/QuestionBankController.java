package com.jaychou.interviewk.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaychou.interviewk.annotation.AuthCheck;
import com.jaychou.interviewk.common.BaseResponse;
import com.jaychou.interviewk.common.DeleteRequest;
import com.jaychou.interviewk.common.ErrorCode;
import com.jaychou.interviewk.common.ResultUtils;
import com.jaychou.interviewk.constant.UserConstant;
import com.jaychou.interviewk.exception.BusinessException;
import com.jaychou.interviewk.exception.ThrowUtils;
import com.jaychou.interviewk.model.dto.question.QuestionQueryRequest;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankAddRequest;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankEditRequest;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankQueryRequest;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankUpdateRequest;
import com.jaychou.interviewk.model.entity.Question;
import com.jaychou.interviewk.model.entity.QuestionBank;
import com.jaychou.interviewk.model.entity.User;
import com.jaychou.interviewk.model.vo.QuestionBankVO;
import com.jaychou.interviewk.model.vo.QuestionVO;
import com.jaychou.interviewk.service.QuestionBankService;
import com.jaychou.interviewk.service.QuestionService;
import com.jaychou.interviewk.service.UserService;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * ClassName: QuestionBankController
 * Package: com.jaychou.interviewk.controller
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:20
 * @Version: v1.0
 */
@RestController
@RequestMapping("/QuestionBank")
@Slf4j
public class QuestionBankController {

    @Resource
    private QuestionBankService QuestionBankService;

    @Resource
    private UserService userService;
    @Resource
    private QuestionService questionService;

    // region 增删改查

    /**
     * 创建题库
     *
     * @param QuestionBankAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionBank(@RequestBody QuestionBankAddRequest QuestionBankAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank QuestionBank = new QuestionBank();
        BeanUtils.copyProperties(QuestionBankAddRequest, QuestionBank);
        // 数据校验
        QuestionBankService.validQuestionBank(QuestionBank, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        QuestionBank.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = QuestionBankService.save(QuestionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionBankId = QuestionBank.getId();
        return ResultUtils.success(newQuestionBankId);
    }

    /**
     * 删除题库
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionBank(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionBank oldQuestionBank = QuestionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionBank.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = QuestionBankService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题库（仅管理员可用）
     *
     * @param QuestionBankUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionBank(@RequestBody QuestionBankUpdateRequest QuestionBankUpdateRequest) {
        if (QuestionBankUpdateRequest == null || QuestionBankUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank QuestionBank = new QuestionBank();
        BeanUtils.copyProperties(QuestionBankUpdateRequest, QuestionBank);
        // 数据校验
        QuestionBankService.validQuestionBank(QuestionBank, false);
        // 判断是否存在
        long id = QuestionBankUpdateRequest.getId();
        QuestionBank oldQuestionBank = QuestionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = QuestionBankService.updateById(QuestionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题库（封装类）
     *
     * @param questionBankQueryRequest
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionBankVO> getQuestionBankVOById(QuestionBankQueryRequest questionBankQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = questionBankQueryRequest.getId();
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        //生成 key
        String key = "bank_detail_" + id;
        //如果是热key
        if (JdHotKeyStore.isHotKey(key)) {
            Object cachedQuestionBankVo = JdHotKeyStore.get(key);
            if (cachedQuestionBankVo != null) {
                return ResultUtils.success((QuestionBankVO) cachedQuestionBankVo);
            }
        }
        QuestionBank QuestionBank = QuestionBankService.getById(id);
        ThrowUtils.throwIf(QuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        QuestionBankVO questionBankVO = QuestionBankService.getQuestionBankVO(QuestionBank, request);
        //是否要关联查询题库下的题目列表
        boolean needQueryQuestionList = questionBankQueryRequest.isNeedQueryQuestionList();
        if (needQueryQuestionList) {
            QuestionQueryRequest questionQueryRequest = new QuestionQueryRequest();
            questionQueryRequest.setPageSize(questionQueryRequest.getPageSize());
            questionQueryRequest.setCurrent(questionQueryRequest.getCurrent());
            questionQueryRequest.setQuestionBankId(id);
            Page<Question> questionPage = questionService.listQuestionByPage(questionQueryRequest);
            Page<QuestionVO> questionVOPage = questionService.getQuestionVOPage(questionPage, request);
            questionBankVO.setQuestionPage(questionVOPage);
        }
        //设置本地缓存
        JdHotKeyStore.smartSet(key, questionBankVO);
        // 获取封装类
        return ResultUtils.success(questionBankVO);
    }


    /**
     * 分页获取题库列表（仅管理员可用）
     *
     * @param QuestionBankQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionBank>> listQuestionBankByPage(@RequestBody QuestionBankQueryRequest QuestionBankQueryRequest) {
        long current = QuestionBankQueryRequest.getCurrent();
        long size = QuestionBankQueryRequest.getPageSize();
        // 查询数据库
        Page<QuestionBank> QuestionBankPage = QuestionBankService.page(new Page<>(current, size),
                QuestionBankService.getQueryWrapper(QuestionBankQueryRequest));
        return ResultUtils.success(QuestionBankPage);
    }

    /**
     * 分页获取题库列表（封装类）
     *
     * @param QuestionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    @SentinelResource(value = "listQuestionBankVOByPage",
            blockHandler = "handleBlockException",
            fallback = "handleFallback")
    public BaseResponse<Page<QuestionBankVO>> listQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest QuestionBankQueryRequest,
                                                                       HttpServletRequest request) {
        long current = QuestionBankQueryRequest.getCurrent();
        long size = QuestionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> QuestionBankPage = QuestionBankService.page(new Page<>(current, size),
                QuestionBankService.getQueryWrapper(QuestionBankQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionBankService.getQuestionBankVOPage(QuestionBankPage, request));
    }

    /**
     * listQuestionBankVOByPage 降级操作：直接返回本地数据
     */
    public BaseResponse<Page<QuestionBankVO>> handleFallback(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                             HttpServletRequest request, Throwable ex) {
        // 可以返回本地数据或空数据
        if(ex instanceof DegradeException){
            return handleFallback(questionBankQueryRequest,request,ex);
        }
        return ResultUtils.success(null);
    }

    /**
     * listQuestionBankVOByPage 流控操作
     * 限流：提示“系统压力过大，请耐心等待”
     */
    public BaseResponse<Page<QuestionBankVO>> handleBlockException(@RequestBody QuestionBankQueryRequest questionBankQueryRequest,
                                                                   HttpServletRequest request, BlockException ex) {
        // 限流操作
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
    }

    /**
     * 分页获取当前登录用户创建的题库列表
     *
     * @param QuestionBankQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionBankVO>> listMyQuestionBankVOByPage(@RequestBody QuestionBankQueryRequest QuestionBankQueryRequest,
                                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(QuestionBankQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        QuestionBankQueryRequest.setUserId(loginUser.getId());
        long current = QuestionBankQueryRequest.getCurrent();
        long size = QuestionBankQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<QuestionBank> QuestionBankPage = QuestionBankService.page(new Page<>(current, size),
                QuestionBankService.getQueryWrapper(QuestionBankQueryRequest));
        // 获取封装类
        return ResultUtils.success(QuestionBankService.getQuestionBankVOPage(QuestionBankPage, request));
    }

    /**
     * 编辑题库（给用户使用）
     *
     * @param QuestionBankEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editQuestionBank(@RequestBody QuestionBankEditRequest QuestionBankEditRequest, HttpServletRequest request) {
        if (QuestionBankEditRequest == null || QuestionBankEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        QuestionBank QuestionBank = new QuestionBank();
        BeanUtils.copyProperties(QuestionBankEditRequest, QuestionBank);
        // 数据校验
        QuestionBankService.validQuestionBank(QuestionBank, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = QuestionBankEditRequest.getId();
        QuestionBank oldQuestionBank = QuestionBankService.getById(id);
        ThrowUtils.throwIf(oldQuestionBank == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestionBank.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = QuestionBankService.updateById(QuestionBank);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    //
    // endregion
}
