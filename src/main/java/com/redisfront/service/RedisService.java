package com.redisfront.service;

import com.redisfront.constant.RedisModeEnum;
import com.redisfront.model.ClusterNode;
import com.redisfront.model.ConnectInfo;
import com.redisfront.service.impl.RedisServiceImpl;
import com.redisfront.util.Fn;
import redis.clients.jedis.ClusterPipeline;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RedisService {

    RedisService service = new RedisServiceImpl();

    ClusterPipeline getClusterPipeline(ConnectInfo connectInfo);

    /**
     * 获取 JedisCluster
     *
     * @param connectInfo
     * @return
     */
    JedisCluster getJedisCluster(ConnectInfo connectInfo);

    /**
     * redis ping
     *
     * @param connectInfo
     * @return
     */
    Boolean ping(ConnectInfo connectInfo);

    /**
     * 获取 redisMode
     *
     * @param connectInfo
     * @return
     */
    RedisModeEnum getRedisModeEnum(ConnectInfo connectInfo);

    /**
     * 获取集群节点
     *
     * @param connectInfo
     * @return
     */
    List<ClusterNode> getClusterNodes(ConnectInfo connectInfo);


    /**
     * 获取集群信息
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getClusterInfo(ConnectInfo connectInfo);

    /**
     * 获取info
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getInfo(ConnectInfo connectInfo);

    /**
     * 获取cpu info
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getCpuInfo(ConnectInfo connectInfo);

    /**
     * 获取memory info
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getMemoryInfo(ConnectInfo connectInfo);

    /**
     * 获取 server info
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getServerInfo(ConnectInfo connectInfo);

    /**
     * 获取单机 KeySpace
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getKeySpace(ConnectInfo connectInfo);

    /**
     * 获取 client info
     *
     * @param connectInfo
     * @return
     */
    Map<String, Object> getClientInfo(ConnectInfo connectInfo);

    /**
     * @param connectInfo
     * @return
     */
    Map<String, Object> getStatInfo(ConnectInfo connectInfo);

    Boolean isClusterMode(ConnectInfo connectInfo);

    Long getKeyCount(ConnectInfo connectInfo);

    default Map<String, Object> strToMap(String str) {
        Map<String, Object> result = new HashMap<>();
        for (String s : str.split("\r\n")) {
            if (!Fn.startWith(s, "#") && Fn.isNotEmpty(s)) {
                String[] v = s.split(":");
                if (v.length > 1) {
                    result.put(v[0], v[1]);
                } else {
                    result.put(v[0], "");
                }
            }
        }
        return result;
    }

    default JedisClientConfig getJedisClientConfig(ConnectInfo connectInfo) {
        return DefaultJedisClientConfig
                .builder()
                .database(connectInfo.database())
                .user(connectInfo.user())
                .password(connectInfo.password())
                .build();
    }


}
