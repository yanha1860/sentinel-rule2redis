package com.alibaba.csp.sentinel.dashboard.client;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.fastjson.JSON;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

/**
 * 
 * 规则保存到redis，在sentinel客户端会监听reids的channel，根据监听到的数据刷新规则。 在保存、修改、删除规则的接口不需要调用http去同步规则给sentinel客户端了，客户端自己监听reids去刷新规则。
 * 但是在管理平台查询展示规则时还时去客户端获取，其实去redis获取也可以
 * 
 * @author yanghaipeng
 *
 */
@Component
public class RedisSenderClient {

    private static String FLOW_RULE_KEY = "sentinel:rules.flow.key_%s";
    private static String FLOW_RULE_CHANNEL = "sentinel:rules.flow.channel_%s";

    private static String API_DEFINITION_KEY = "sentinel:api_definition.key_%s";
    private static String API_DEFINITION_CHANNEL = "sentinel:api_definition.channel_%s";

    @Value("${spring.redis-common.database}")
    private int database;

    @Value("${spring.redis-common.host}")
    private String host;

    @Value("${spring.redis-common.port}")
    private int port;

    @Value("${spring.redis-common.password}")
    private String password;

    private RedisClient client;

    @PostConstruct
    public void initClient() {
        client = RedisClient.create(RedisURI.Builder.redis(host, port).withPassword(password).withDatabase(database).build());
    }

    public void pubGatewayFlowRule(String app, String ip, int port, List<GatewayFlowRuleEntity> rules) {
        String flowRulesJson = JSON.toJSONString(rules.stream().map(r -> r.toGatewayFlowRule()).collect(Collectors.toList()));

        StatefulRedisPubSubConnection<String, String> keyConnection = client.connectPubSub();
        RedisPubSubCommands<String, String> keyCommands = keyConnection.sync();
        keyCommands.set(String.format(FLOW_RULE_KEY, app), flowRulesJson);

        StatefulRedisPubSubConnection<String, String> channelConnection = client.connectPubSub();
        RedisPubSubCommands<String, String> channelCommands = channelConnection.sync();
        channelCommands.publish(String.format(FLOW_RULE_CHANNEL, app), flowRulesJson);
    }

    public List<GatewayFlowRuleEntity> fetchGatewayFlowRules(String app, String ip, int port) {
        RedisCommands<String, String> subCommands = client.connect().sync();
        String value = subCommands.get(String.format(FLOW_RULE_KEY, app));

        List<GatewayFlowRuleEntity> gatewayFlowRules = JSON.parseArray(value, GatewayFlowRuleEntity.class);

        return gatewayFlowRules;
    }

    public void pubApiDefinition(String app, String ip, int port, List<ApiDefinitionEntity> apis) {
        String apisJson = JSON.toJSONString(apis.stream().map(r -> r.toApiDefinition()).collect(Collectors.toList()));

        StatefulRedisPubSubConnection<String, String> keyConnection = client.connectPubSub();
        RedisPubSubCommands<String, String> keyCommands = keyConnection.sync();
        keyCommands.set(String.format(API_DEFINITION_KEY, app), apisJson);

        StatefulRedisPubSubConnection<String, String> channelConnection = client.connectPubSub();
        RedisPubSubCommands<String, String> channelCommands = channelConnection.sync();
        channelCommands.publish(String.format(API_DEFINITION_CHANNEL, app), apisJson);
    }

    @PreDestroy
    public void shutdown() {
        client.shutdown();
    }

}
