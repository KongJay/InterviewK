package com.jaychou.interviewk.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jaychou.interviewk.common.ErrorCode;
import com.jaychou.interviewk.constant.CommonConstant;
import com.jaychou.interviewk.exception.BusinessException;
import com.jaychou.interviewk.exception.ThrowUtils;
import com.jaychou.interviewk.mapper.QuestionBankQuestionMapper;
import com.jaychou.interviewk.model.dto.questionbankquestion.QuestionBankQuestionQueryRequest;
import com.jaychou.interviewk.model.entity.Question;
import com.jaychou.interviewk.model.entity.QuestionBank;
import com.jaychou.interviewk.model.entity.QuestionBankQuestion;
import com.jaychou.interviewk.model.entity.User;
import com.jaychou.interviewk.model.vo.QuestionBankQuestionVO;
import com.jaychou.interviewk.model.vo.UserVO;
import com.jaychou.interviewk.service.QuestionBankQuestionService;
import com.jaychou.interviewk.service.QuestionBankService;
import com.jaychou.interviewk.service.QuestionService;
import com.jaychou.interviewk.service.UserService;
import com.jaychou.interviewk.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: QuestionBankQuestionImpl
 * Package: com.jaychou.interviewk.service.impl
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:13
 * @Version: v1.0
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Autowired
    private UserService userService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param QuestionBankQuestion
     * @param add                  对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion QuestionBankQuestion, boolean add) {
        ThrowUtils.throwIf(QuestionBankQuestion == null, ErrorCode.PARAMS_ERROR);
        //题目和题库必须存在
        Long questionBankId = QuestionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        Long questionId = QuestionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR, "题库不存在");
        }

      /*  // todo 从对象中取值
        String title = QuestionBankQuestion.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }*/
    }

    /**
     * 获取查询条件
     *
     * @param QuestionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest QuestionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (QuestionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = QuestionBankQuestionQueryRequest.getId();
        Long notId = QuestionBankQuestionQueryRequest.getNotId();
        String sortField = QuestionBankQuestionQueryRequest.getSortField();
        String sortOrder = QuestionBankQuestionQueryRequest.getSortOrder();
        Long questionBankId = QuestionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = QuestionBankQuestionQueryRequest.getQuestionId();
        Long userId = QuestionBankQuestionQueryRequest.getUserId();
        int current = QuestionBankQuestionQueryRequest.getCurrent();
        int pageSize = QuestionBankQuestionQueryRequest.getPageSize();
        // todo 补充需要的查询条件
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 获取题库题目关联封装
     *
     * @param QuestionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion QuestionBankQuestion, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankQuestionVO questionBankQuestion_VO = QuestionBankQuestionVO.objToVo(QuestionBankQuestion);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = QuestionBankQuestion.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionBankQuestion_VO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long QuestionBankQuestionId = QuestionBankQuestion.getId();
        User loginUser = userService.getLoginUserPermitNull(request);

        // endregion

        return questionBankQuestion_VO;
    }

    /**
     * 分页获取题库题目关联封装
     *
     * @param QuestionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> QuestionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> QuestionBankQuestionList = QuestionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> QuestionBankQuestionVOPage = new Page<>(QuestionBankQuestionPage.getCurrent(), QuestionBankQuestionPage.getSize(), QuestionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(QuestionBankQuestionList)) {
            return QuestionBankQuestionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankQuestionVO> questionBankQuestion_VOList = QuestionBankQuestionList.stream().map(QuestionBankQuestion -> {
            return QuestionBankQuestionVO.objToVo(QuestionBankQuestion);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = QuestionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> QuestionBankQuestionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> QuestionBankQuestionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);

        // 填充信息
        questionBankQuestion_VOList.forEach(questionBankQuestion_VO -> {
            Long userId = questionBankQuestion_VO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionBankQuestion_VO.setUser(userService.getUserVO(user));

        });
        // endregion

        QuestionBankQuestionVOPage.setRecords(questionBankQuestion_VOList);
        return QuestionBankQuestionVOPage;
    }

    /**
     * 批量添加题目到题库
     *
     * @param questionIdList
     * @param questionBankId
     * @param loginUser
     */
    @Override
    public void batchAddQuestionToBank(List<Long> questionIdList, long questionBankId, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表不能为空");
        ThrowUtils.throwIf(questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库ID非法");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        //获取题目ID列表
        LambdaQueryWrapper<Question> in = Wrappers.lambdaQuery(Question.class)
                .select(Question::getId)
                .in(Question::getId, questionIdList);

        List<Long> idList = questionService.listObjs(in, o -> (Long) o);

        //检查哪些题目还不存在于题库中，这样可以避免重复加入
        LambdaQueryWrapper<QuestionBankQuestion> questionBankQuestionLambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                .eq(QuestionBankQuestion::getQuestionBankId, questionBankId)
                .in(QuestionBankQuestion::getQuestionId, idList);
        List<QuestionBankQuestion> existQuestionList = this.list(questionBankQuestionLambdaQueryWrapper);
        Set<Long> existQuestionIdList = existQuestionList.stream()
                .map(QuestionBankQuestion::getId)
                .collect(Collectors.toSet());
        idList = idList.stream()
                .filter(questionId -> {
                    return !existQuestionIdList.contains(questionId);
                })
                .collect(Collectors.toList());
        ThrowUtils.throwIf(CollUtil.isEmpty(idList), ErrorCode.PARAMS_ERROR, "所有题目都已存在于题库中");
        //检查题库是否存在
        QuestionBank questionBank = questionBankService.getById(questionBankId);
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");
        // 自定义线程池(IO密集型)
        ThreadPoolExecutor customExecutor = new ThreadPoolExecutor(
                4,                         // 核心线程数
                10,                        // 最大线程数
                60L,                       // 线程空闲存活时间
                TimeUnit.SECONDS,           // 存活时间单位
                new LinkedBlockingQueue<>(1000),  // 阻塞队列容量
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用线程处理任务
        );
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        //执行插入
        //分批处理，避免长事务
        int batchSize = 1000;
        int totalQuestionListSize = idList.size();
        for (int i = 0; i < totalQuestionListSize; i+=batchSize) {
            List<Long> subList = idList.subList(i, Math.min(i + batchSize, totalQuestionListSize));
            List<QuestionBankQuestion> questionBankQuestions = subList.stream()
                    .map(questionId -> {
                        QuestionBankQuestion questionBankQuestion = new QuestionBankQuestion();
                        questionBankQuestion.setQuestionBankId(questionBankId);
                        questionBankQuestion.setQuestionId(questionId);
                        questionBankQuestion.setUserId(loginUser.getId());
                        return questionBankQuestion;
                    }).collect(Collectors.toList());
            //使用事务处理每批数据
            QuestionBankQuestionService questionBankQuestionService = (QuestionBankQuestionServiceImpl) AopContext.currentProxy();
            //异步处理每批数据，将任务添加到异步任务列表
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                questionBankQuestionService.batchAddQuestionsToBankInner(questionBankQuestions);
            }, customExecutor).exceptionally(ex ->{
                log.error("批处理任务执行失败",ex);
                return null;
            });
            futures.add(future);
        }
        //等待所有批次完成操作
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        //执行完之后关闭线程池
        customExecutor.shutdown();
    }

    /**
     * 批量添加题目到题目（事务，仅供内部调用）
     *
     * @param questionBankQuestions
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddQuestionsToBankInner(List<QuestionBankQuestion> questionBankQuestions) {
        try {
            boolean result = this.saveBatch(questionBankQuestions);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        } catch (DataIntegrityViolationException e) {
            log.error("数据库唯一键冲突或违反其他完整性约束, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目已存在于该题库，无法重复添加");
        } catch (DataAccessException e) {
            log.error("数据库连接问题、事务问题等导致操作失败, 错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作失败");
        } catch (Exception e) {
            // 捕获其他异常，做通用处理
            log.error("添加题目到题库时发生未知错误，错误信息: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "向题库添加题目失败");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchRemoveQuestionFromBank(List<Long> questionIdList, long questionBankId) {
        //参数校验
        ThrowUtils.throwIf(CollUtil.isEmpty(questionIdList), ErrorCode.PARAMS_ERROR, "题目列表不能为空");
        ThrowUtils.throwIf(questionBankId <= 0, ErrorCode.PARAMS_ERROR, "题库ID非法");
        //执行插入
        for (long q : questionIdList) {
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .eq(QuestionBankQuestion::getQuestionId, q)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            boolean result = this.remove(lambdaQueryWrapper);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "批量移除题目失败");
        }

    }
}
