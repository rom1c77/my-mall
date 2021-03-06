package com.example.feign;

import com.example.util.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: chenq
 * @Description:
 * @Date: Created in 2021-06-23 15:10
 */
@FeignClient("mall-page-web")
@RequestMapping("/page")
public interface PageFeign {
    /**
     * 生成静态页
     */
    @GetMapping("/{id}")
    RespResult html(@PathVariable("id")String id) throws Exception;
}
