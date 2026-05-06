package com.gk.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gk.study.common.CacheKeyUtils;
import com.gk.study.entity.Tag;
import com.gk.study.entity.Thing;
import com.gk.study.entity.ThingTag;
import com.gk.study.mapper.TagMapper;
import com.gk.study.mapper.ThingMapper;
import com.gk.study.mapper.ThingTagMapper;
import com.gk.study.service.ThingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ThingServiceImpl extends ServiceImpl<ThingMapper, Thing> implements ThingService {
    @Autowired
    ThingMapper mapper;

    @Autowired
    ThingTagMapper thingTagMapper;

    @Autowired
    TagMapper tagMapper;

    @Override
    @Cacheable(value = "thing:list", key = "'list:' + T(com.gk.study.common.CacheKeyUtils).buildKey(#keyword, #sort, #c, #tag)")
    public List<Thing> getThingList(String keyword, String sort, String c, String tag) {
        QueryWrapper<Thing> queryWrapper = new QueryWrapper<>();

        // 搜索
        queryWrapper.like(StringUtils.isNotBlank(keyword), "title", keyword);

        // 排序
        if (StringUtils.isNotBlank(sort)) {
            if (sort.equals("recent")) {
                queryWrapper.orderBy(true, false, "create_time");
            } else if (sort.equals("hot") || sort.equals("recommend")) {
                queryWrapper.orderBy(true, false, "pv");
            }
        }else {
            queryWrapper.orderBy(true, false, "create_time");
        }

        // 根据分类筛选
        if (StringUtils.isNotBlank(c) && !c.equals("-1")) {
            queryWrapper.eq(true, "classification_id", c);
        }

        List<Thing> things = mapper.selectList(queryWrapper);

        // tag筛选
        if (StringUtils.isNotBlank(tag)) {
            List<Thing> tThings = new ArrayList<>();
            QueryWrapper<ThingTag> thingTagQueryWrapper = new QueryWrapper<>();
            thingTagQueryWrapper.eq("tag_id", tag);
            List<ThingTag> thingTagList = thingTagMapper.selectList(thingTagQueryWrapper);
            for (Thing thing : things) {
                for (ThingTag thingTag : thingTagList) {
                    if (thing.getId().equals(thingTag.getThingId())) {
                        tThings.add(thing);
                    }
                }
            }
            things.clear();
            things.addAll(tThings);
        }

        fillThingTags(things);
        return things;
    }

    @Override
    public List<Thing> searchThingsByTitleOrTag(String keyword) {
        if (!StringUtils.isNotBlank(keyword)) {
            return new ArrayList<>();
        }
        String kw = keyword.trim();
        LinkedHashMap<Long, Thing> merged = new LinkedHashMap<>();

        for (Thing t : getThingList(kw, null, "-1", null)) {
            if (t != null && t.getId() != null) {
                merged.put(t.getId(), t);
            }
        }

        LinkedHashSet<String> tagTerms = expandTagSearchTerms(kw);
        List<Long> tagIds = findTagIdsMatchingTerms(tagTerms);
        if (!tagIds.isEmpty()) {
            QueryWrapper<ThingTag> ttq = new QueryWrapper<>();
            ttq.in("tag_id", tagIds);
            List<ThingTag> links = thingTagMapper.selectList(ttq);
            LinkedHashSet<Long> thingIds = links.stream()
                    .map(ThingTag::getThingId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!thingIds.isEmpty()) {
                QueryWrapper<Thing> tw = new QueryWrapper<>();
                tw.in("id", thingIds);
                tw.orderByDesc("create_time");
                tw.last("LIMIT 40");
                for (Thing t : mapper.selectList(tw)) {
                    if (t != null && t.getId() != null && !merged.containsKey(t.getId())) {
                        merged.put(t.getId(), t);
                    }
                }
            }
        }

        List<Thing> result = new ArrayList<>(merged.values());
        fillThingTags(result);
        return result;
    }

    private List<Long> findTagIdsMatchingTerms(LinkedHashSet<String> terms) {
        List<String> clean = terms.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (clean.isEmpty()) {
            return List.of();
        }
        QueryWrapper<Tag> q = new QueryWrapper<>();
        q.and(wrapper -> {
            for (int i = 0; i < clean.size(); i++) {
                String term = clean.get(i);
                if (i == 0) {
                    wrapper.like("title", term);
                } else {
                    wrapper.or().like("title", term);
                }
            }
        });
        return tagMapper.selectList(q).stream()
                .map(Tag::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 扩展与用户口语相关的标签检索词（例如用户说「衣服」，后台标签可能是「服装」）。
     */
    private static LinkedHashSet<String> expandTagSearchTerms(String kw) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        if (kw == null || kw.isBlank()) {
            return terms;
        }
        String t = kw.trim();
        terms.add(t);

        boolean clothingHint = t.contains("衣") || t.contains("裤") || t.contains("裙") || t.contains("袜")
                || t.contains("帽") || t.contains("鞋") || t.contains("穿") || t.contains("T恤")
                || t.contains("衬衫") || t.contains("外套") || t.contains("卫衣") || t.contains("牛仔")
                || t.contains("大衣") || t.contains("羽绒服") || t.contains("时装");
        if (clothingHint || t.contains("服") || t.contains("饰")) {
            terms.add("服装");
            terms.add("服饰");
            terms.add("衣服");
        }
        return terms;
    }

    private void fillThingTags(List<Thing> things) {
        if (things == null || things.isEmpty()) {
            return;
        }
        List<Long> thingIds = things.stream().map(Thing::getId).collect(Collectors.toList());

        // 一次 IN 查询拿所有关联
        QueryWrapper<ThingTag> q = new QueryWrapper<>();
        q.in("thing_id", thingIds);
        List<ThingTag> allLinks = thingTagMapper.selectList(q);

        // 按 thing_id 分组
        Map<Long, List<Long>> tagMap = allLinks.stream()
            .collect(Collectors.groupingBy(ThingTag::getThingId,
                Collectors.mapping(ThingTag::getTagId, Collectors.toList())));

        things.forEach(t -> t.setTags(tagMap.getOrDefault(t.getId(), List.of())));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "thing:list", allEntries = true)
    })
    public void createThing(Thing thing) {
        System.out.println(thing);
        thing.setCreateTime(java.time.LocalDateTime.now());

        if (thing.getPv() == null) {
            thing.setPv("0");
        }
        if (thing.getScore() == null) {
            thing.setScore("0");
        }
        if (thing.getWishCount() == null) {
            thing.setWishCount("0");
        }
        mapper.insert(thing);
        // 更新tag
        setThingTags(thing);
}

    @Override
    @Caching(evict = {
        @CacheEvict(value = "thing:list", allEntries = true),
        @CacheEvict(value = "thing:detail", key = "#id")
    })
    public void deleteThing(String id) {
        mapper.deleteById(id);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "thing:list", allEntries = true),
        @CacheEvict(value = "thing:detail", key = "#thing.id")
    })
    public void updateThing(Thing thing) {

        // 更新tag
        setThingTags(thing);

        mapper.updateById(thing);
    }

    @Override
    public Thing getThingById(String id) {
        // 注意：getThingById 每次都更新 PV，不适合缓存
        if (!StringUtils.isNotBlank(id)) {
            return null;
        }
        String trimmed = id.trim();
        try {
            mapper.incrementPv(Long.parseLong(trimmed));
        } catch (NumberFormatException ignored) {
            // skip invalid id
        }
        return mapper.selectById(trimmed);
    }

    @Override
    public Thing selectThingById(String id) {
        if (!StringUtils.isNotBlank(id)) {
            return null;
        }
        String trimmed = id.trim();
        try {
            Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
        return mapper.selectById(trimmed);
    }

    // 心愿数加1
    @Override
    public void addWishCount(String thingId) {
        Thing thing = mapper.selectById(thingId);
        thing.setWishCount(String.valueOf(Integer.parseInt(thing.getWishCount()) + 1));
        mapper.updateById(thing);
    }

    // 收藏数加1
    @Override
    public void addCollectCount(String thingId) {
        Thing thing = mapper.selectById(thingId);
        thing.setCollectCount(String.valueOf(Integer.parseInt(thing.getCollectCount()) + 1));
        mapper.updateById(thing);
    }

    public void setThingTags(Thing thing) {
        // 删除tag
        Map<String, Object> map = new HashMap<>();
        map.put("thing_id", thing.getId());
        thingTagMapper.deleteByMap(map);
        // 新增tag
        if (thing.getTags() != null) {
            for (Long tag : thing.getTags()) {
                ThingTag thingTag = new ThingTag();
                thingTag.setThingId(thing.getId());
                thingTag.setTagId(tag);
                thingTagMapper.insert(thingTag);
            }
        }
    }

}
