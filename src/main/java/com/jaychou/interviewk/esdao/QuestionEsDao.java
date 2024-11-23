package com.jaychou.interviewk.esdao;

import com.jaychou.interviewk.model.dto.question.QuestionEsDTO;
import com.jaychou.interviewk.model.entity.Question;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 题目 ES操作
 * ClassName: QuestionEsDao
 * Package: com.jaychou.interviewk.esdao
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/23 - 15:53
 * @Version: v1.0
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

    /**
     * 根据用户 id 查询
     *
     * @param userId
     * @return
     */
    List<QuestionEsDTO> findByUserId(Long userId);

}
