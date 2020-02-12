/**
 * FileName: GmallManageTest
 * Author: WP
 * Date: 2020/2/12 11:10
 * Description:
 * History:
 **/
package com.wp.gmall.manage;

import com.wp.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageTest {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void test(){
        Jedis jedis = redisUtil.getJedis();
        System.out.println(jedis);
    }
}
