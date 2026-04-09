package com.gk.study.service;


import com.gk.study.entity.Thing;

import java.util.List;

public interface ThingService {
    List<Thing> getThingList(String keyword, String sort, String c, String tag);

    /**
     * 客服/AI 检索：标题模糊匹配 + 标签名模糊匹配（含常见同义扩展，如「衣服」↔「服装」）。
     */
    List<Thing> searchThingsByTitleOrTag(String keyword);
    void createThing(Thing thing);
    void deleteThing(String id);

    void updateThing(Thing thing);

    Thing getThingById(String id);

    /**
     * 按主键查询商品，不增加浏览量，供 AI 工具等内部使用。
     */
    Thing selectThingById(String id);

    void addWishCount(String thingId);

    void addCollectCount(String thingId);
}
