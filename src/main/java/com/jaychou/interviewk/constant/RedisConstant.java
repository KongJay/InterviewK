package com.jaychou.interviewk.constant;

/**
 * ClassName: RedisConstant
 * Package: com.jaychou.interviewk.constant
 * Description:
 *
 * @Author: 红模仿
 * @Create: 2024/11/22 - 19:19
 * @Version: v1.0
 */

/**
 * redis 常量
 */
public interface RedisConstant {
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user:signins";

    /**
     * 获取用户签到记录的Redis key
     * @param year
     * @param userId
     * @return 拼接好的redis key
     */
    static String getUserSignInRedisKey(int year,long userId){

        return String.format("%s:%s:%s",USER_SIGN_IN_REDIS_KEY_PREFIX,year,userId);

    }
}
