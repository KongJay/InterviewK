package com.jaychou.interviewk.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jaychou.interviewk.common.ErrorCode;
import com.jaychou.interviewk.constant.CommonConstant;
import com.jaychou.interviewk.exception.ThrowUtils;
import com.jaychou.interviewk.mapper.QuestionBankMapper;
import com.jaychou.interviewk.model.dto.questionbank.QuestionBankQueryRequest;
import com.jaychou.interviewk.model.entity.QuestionBank;
import com.jaychou.interviewk.model.entity.User;
import com.jaychou.interviewk.model.vo.QuestionBankVO;
import com.jaychou.interviewk.model.vo.UserVO;
import com.jaychou.interviewk.service.QuestionBankService;
import com.jaychou.interviewk.service.UserService;
import com.jaychou.interviewk.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName: QuestionBankImpl
 * Package: com.jaychou.interviewk.service.impl
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/8 - 23:13
 * @Version: v1.0
 */
@Service
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param QuestionBank
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBank(QuestionBank QuestionBank, boolean add) {
        ThrowUtils.throwIf(QuestionBank == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = QuestionBank.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }



    /**
     * 获取查询条件
     *
     * @param QuestionBankQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest QuestionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (QuestionBankQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = QuestionBankQueryRequest.getId();
        Long notId = QuestionBankQueryRequest.getNotId();
        String searchText = QuestionBankQueryRequest.getSearchText();
        String title = QuestionBankQueryRequest.getTitle();
        String picture = QuestionBankQueryRequest.getPicture();
        String description = QuestionBankQueryRequest.getDescription();
        Long userId = QuestionBankQueryRequest.getUserId();
        String sortField = QuestionBankQueryRequest.getSortField();
        String sortOrder = QuestionBankQueryRequest.getSortOrder();


        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description),"description",description);
        // JSON 数组查询
     /*   if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }*/
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(picture), "picture", picture);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 获取题库封装
     *
     * @param QuestionBank
     * @param request
     * @return
     */
    @Override
    public QuestionBankVO getQuestionBankVO(QuestionBank QuestionBank, HttpServletRequest request) {
        // 对象转封装类
        QuestionBankVO QuestionBankVO = com.jaychou.interviewk.model.vo.QuestionBankVO.objToVo(QuestionBank);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = QuestionBank.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        QuestionBankVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long QuestionBankId = QuestionBank.getId();
        User loginUser = userService.getLoginUserPermitNull(request);

        // endregion

        return QuestionBankVO;
    }

    /**
     * 分页获取题库封装
     *
     * @param QuestionBankPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> QuestionBankPage, HttpServletRequest request) {
        List<QuestionBank> QuestionBankList = QuestionBankPage.getRecords();
        Page<QuestionBankVO> QuestionBankVOPage = new Page<>(QuestionBankPage.getCurrent(), QuestionBankPage.getSize(), QuestionBankPage.getTotal());
        if (CollUtil.isEmpty(QuestionBankList)) {
            return QuestionBankVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionBankVO> QuestionBankVOList = QuestionBankList.stream().map(QuestionBank -> {
            return QuestionBankVO.objToVo(QuestionBank);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = QuestionBankList.stream().map(QuestionBank::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> QuestionBankIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> QuestionBankIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);

        // 填充信息
        QuestionBankVOList.forEach(QuestionBankVO -> {
            Long userId = QuestionBankVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            QuestionBankVO.setUser(userService.getUserVO(user));

        });
        // endregion

        QuestionBankVOPage.setRecords(QuestionBankVOList);
        return QuestionBankVOPage;
    }

}
