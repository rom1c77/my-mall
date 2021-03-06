package com.example.mall.controller;

import com.example.goods.model.Sku;
import com.example.mall.service.SkuService;
import com.example.model.Cart;
import com.example.util.RespResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: chenq
 * @Description:
 * @Date: Created in 2021-06-17 11:30
 */
@RestController
@RequestMapping("/sku")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 库存递减
     * @param id
     * @return
     */
    @PostMapping("/dcount")
    public RespResult decount(@RequestBody List<Cart> cartList){
        skuService.decrease(cartList);
        return RespResult.ok();
    }

    @GetMapping("/{id}")
    public  RespResult<Sku> findProductById(@PathVariable("id")String id){
        Sku sku=skuService.getById(id);
        return RespResult.ok(sku);
    }

    /**
     * 根据推广分类查询推广产品列表
     * cacheNames: 是Redis 命名空间
     */
    @GetMapping("/aditems/type")
    public List<Sku> typeItems(@RequestParam("id") Integer id) {
        List<Sku> skuList = skuService.typeSkuItems(id);
        return skuList;
    }

    @DeleteMapping("/aditems/type")
    public RespResult delTypeItems(@RequestParam("id") Integer id) {
        skuService.delTypeSkuItems(id);
        return RespResult.ok();
    }

    @PutMapping("/aditems/type")
    public RespResult<List<Sku>> updateTypeItems(@RequestParam("id") Integer id) {
        List<Sku> skuList = skuService.updateTypeSkuItems(id);
        return RespResult.ok(skuList);
    }
}
