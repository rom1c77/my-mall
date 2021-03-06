package com.example.filter;

import com.alibaba.fastjson.JSON;
import com.example.hot.HotQueue;
import com.example.permission.AuthorizationIntterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: chenq
 * @Description:
 * @Date: Created in 2021-07-21 16:51
 */
@Configuration
public class ApiFilter implements GlobalFilter, Ordered {
    @Autowired
    private HotQueue hotQueue;

    @Autowired
    private AuthorizationIntterceptor authorizationIntterceptor;

    /***
     * 执行拦截处理      http://localhost:9001/mall/seckill/order?id&num
     *                 JWT
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        //uri
        String uri = request.getURI().getPath();

        //布隆过滤器-uri
//        if(!authorizationIntterceptor.isValid(uri)){
//            return endProcess(exchange, 401, "uri don't exits");
//        }

       //不在权限表的可以直接直接访问 不需要拦截
        if (!authorizationIntterceptor.isIntercept(exchange)) {
            return chain.filter(exchange);
        }

        //令牌校验
        Map<String, Object> resultMap = authorizationIntterceptor.tokenIntercept(exchange);
        if (resultMap == null || !authorizationIntterceptor.rolePermission(exchange, resultMap)) {
            //令牌校验失败 或者没有权限
            return endProcess(exchange, 401, "Access denied");
        }

        //秒杀过滤
        if (uri.equals("/seckill/order")) {
            return seckillFilter(chain, exchange, request, resultMap.get("username").toString());
        }

        //NOT_HOT 直接由后端服务处理
        return chain.filter(exchange);
    }

    /***
     * 秒杀过滤
     * @param exchange
     * @param request
     * @param username
     * @return
     */
    private Mono<Void> seckillFilter(GatewayFilterChain chain, ServerWebExchange exchange,
                                     ServerHttpRequest request, String username) {
        //商品ID
        String id = request.getQueryParams().getFirst("id");
        //数量
        Integer num = Integer.valueOf(request.getQueryParams().getFirst("num"));

        //排队结果
        int result = hotQueue.hot2Queue(username, id, num);

        //如果状态为QUEUE_ING、HAS_QUEUE 则直接返回message
        if (result == HotQueue.QUEUE_ING || result == HotQueue.HAS_QUEUQ) {
            return endProcess(exchange, result, "hot");
        }
        return chain.filter(exchange);
    }


    /****
     * 结束程序
     * @param exchange
     * @param code
     * @param message
     */
    public Mono<Void> endProcess(ServerWebExchange exchange, Integer code, String message) {
        //响应状态码200
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("message", message);
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        response.setStatusCode(HttpStatus.OK);
        //response.getHeaders().add("message", JSON.toJSONString(resultMap));

        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(JSON.toJSONString(resultMap).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }


    @Override
    public int getOrder() {
        return 10001;
    }

}

