package cn.edu.ncu.common;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static cn.edu.ncu.common.Singleton.*;

public class RedisPoolUtil {
    private static JedisPool pool = null;

    private static JedisPool getPool(){
        if (pool == null){
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setBlockWhenExhausted(false);
            jedisPoolConfig.setMaxTotal(300);
            pool = new JedisPool(jedisPoolConfig,
                    getServerConf().getProperty("redis.host"),
                    Integer.valueOf(getServerConf().getProperty("redis.port")));
        }
        return pool;
    }

    public static Jedis getConn(){
        return getPool().getResource();
    }
}
