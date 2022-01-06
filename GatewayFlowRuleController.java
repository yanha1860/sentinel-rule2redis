/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.controller.gateway;


import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.client.RedisSenderClient;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayParamFlowItemEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.GatewayParamFlowItemVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.rule.UpdateFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemGatewayFlowRuleStore;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;
import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;
import static com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity.*;

/**
 * Gateway flow rule Controller for manage gateway flow rules.
 *
 * @author cdfive,yanghaipeng
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "/gateway/flow")
public class GatewayFlowRuleController {

    private final Logger logger = LoggerFactory.getLogger(GatewayFlowRuleController.class);

    private boolean publishRules(String app, String ip, Integer port) {
        List<GatewayFlowRuleEntity> rules = repository.findAllByMachine(MachineInfo.of(app, ip, port));

        // return sentinelApiClient.modifyGatewayFlowRules(app, ip, port, rules);

        redisSenderClient.pubGatewayFlowRule(app, ip, port, rules);

        return true;
    }
}
