package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = "shop-type:list";

        // 1.从redis查询shop-type list
        if (stringRedisTemplate.opsForList().size(key) > 0) {
            List<ShopType> resList = new ArrayList<>();
            // 2.存在，直接返回
            List<String> stringList = stringRedisTemplate.opsForList().range(key, 0, -1);;
            for (String str : stringList) {
                ShopType shopType = JSONUtil.toBean(str, ShopType.class);
                resList.add(shopType);
            }
            return Result.ok(resList);
        }
        // 3.不存在查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();

        // 4. 不存在，返回错误
        if (typeList == null || typeList.size() == 0) {
            return Result.fail("商铺类型信息不存在!");
        }

        // 5. 存在，写入redis
        List<String> stringList = new ArrayList<>();
        for (ShopType shopType : typeList) {
            stringList.add(JSONUtil.toJsonStr(shopType));
        }
        stringRedisTemplate.opsForList().rightPushAll(key, stringList);
        // 6.返回
        return Result.ok(typeList);
    }
}
