package cn.edu.ncu;

import cn.edu.ncu.common.RedisPoolUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class JedisTests {

    @Test
    public void test(){
        Jedis jedis = RedisPoolUtil.getConn();

        assert jedis.get(UUID.randomUUID().toString().getBytes()) == null;
        assert jedis.rpop(UUID.randomUUID().toString().getBytes()) == null;
    }

    @Test
    public void testMaxConnection(){
        for (int i = 0; i < 301; ++i){
            Jedis jedis = RedisPoolUtil.getConn();
            System.out.println(i);
            jedis.close();
        }
    }
}
