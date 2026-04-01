<template>
  <div class="content mall-feed">
    <div class="mall-container mall-layout">
      <aside class="mall-sidebar">
        <div class="mall-card filter-card">
          <h4 class="mall-card-title">商品分类</h4>
          <div class="tree-wrap">
            <a-tree
              :tree-data="contentData.cData"
              :selected-keys="contentData.selectedKeys"
              @select="onSelect"
              style="min-height: 220px"
            />
          </div>
          <h4 class="mall-card-title mall-card-title--tags">热门标签</h4>
          <div class="tag-view tag-flex-view tag-sidebar">
            <span
              class="tag"
              :class="{ 'tag-select': contentData.selectTagId === item.id }"
              v-for="item in contentData.tagData"
              :key="item.id"
              @click="clickTag(item.id)"
            >{{ item.title }}</span>
          </div>
        </div>
      </aside>

      <div class="mall-main">
        <div class="mall-card mall-banner-wrap">
          <a-carousel autoplay :dots="true" class="mall-banner-carousel">
            <div class="banner-slide banner-slide-a">
              <div class="banner-inner">
                <span class="banner-tag">选好物，来E购</span>
                <p class="banner-title">发现好物</p>
                <p class="banner-sub">精选商品 · 放心选购</p>
              </div>
            </div>
            <div class="banner-slide banner-slide-b">
              <div class="banner-inner">
                <span class="banner-tag">今日推荐</span>
                <p class="banner-title">热度精选</p>
                <p class="banner-sub">最新 · 最热 · 推荐</p>
              </div>
            </div>
            <div class="banner-slide banner-slide-c">
              <div class="banner-inner">
                <span class="banner-tag">品质生活</span>
                <p class="banner-title">甄选好货</p>
                <p class="banner-sub">分类标签 · 一键直达</p>
              </div>
            </div>
          </a-carousel>
        </div>

        <div class="mall-card kingkong-card">
          <div class="kingkong-scroll">
            <button
              type="button"
              class="kingkong-item"
              v-for="item in contentData.cData"
              :key="item.key"
              :class="{ 'kingkong-item--active': contentData.selectedKeys[0] == item.key }"
              @click="onSelect([item.key])"
            >
              <div class="kingkong-icon">{{ item.title ? item.title.substring(0, 1) : '·' }}</div>
              <span class="kingkong-label">{{ item.title }}</span>
            </button>
          </div>
        </div>

        <div class="mall-card sort-card">
          <div class="top-select-view flex-view">
            <div class="order-view">
              <span class="title"></span>
              <span
                class="tab"
                :class="contentData.selectTabIndex === index ? 'tab-select' : ''"
                v-for="(item, index) in contentData.tabData"
                :key="index"
                @click="selectTab(index)"
              >{{ item }}</span>
              <span :style="{ left: contentData.tabUnderLeft + 'px' }" class="tab-underline"></span>
            </div>
          </div>
        </div>

        <a-spin :spinning="contentData.loading" class="mall-spin">
          <div class="pc-thing-list mall-waterfall">
          <div
            v-for="item in contentData.pageData"
            :key="item.id"
            @click="handleDetail(item)"
            class="thing-item"
          >
            <div class="img-view">
              <img :src="item.cover" alt="" />
            </div>
            <div class="info-view">
              <h3 class="thing-name">{{ item.title.substring(0, 20) }}</h3>
              <div class="price-row">
                <span class="a-price-symbol">¥</span>
                <span class="a-price">{{ item.price }}</span>
              </div>
              <div class="thing-sales" v-if="item.pv !== undefined && item.pv !== null && item.pv !== ''">
                销量 {{ item.pv }}
              </div>
            </div>
          </div>
            <div v-if="contentData.pageData.length <= 0 && !contentData.loading" class="no-data">暂无数据</div>
          </div>
        </a-spin>

        <div class="mall-card page-card">
          <div class="page-view">
            <a-pagination
              v-model="contentData.page"
              size="small"
              @change="changePage"
              :hideOnSinglePage="true"
              :defaultPageSize="contentData.pageSize"
              :total="contentData.total"
              :showSizeChanger="false"
            />
          </div>
        </div>
      </div>
    </div>

    <div style="position: fixed; bottom: 10px; right: 10px"></div>
  </div>
</template>

<script setup>
import {listApi as listClassificationList} from '/@/api/classification'
import {listApi as listTagList} from '/@/api/tag'
import {listApi as listThingList} from '/@/api/thing'
import {BASE_URL} from "/@/store/constants";
import {useUserStore} from "/@/store";

const userStore = useUserStore()
const router = useRouter();

const contentData = reactive({
  selectX: 0,
  selectTagId: -1,
  cData: [],
  selectedKeys: [],
  tagData: [],
  loading: false,

  tabData: ['最新', '最热', '推荐'],
  selectTabIndex: 0,
  tabUnderLeft: 12,

  thingData: [],
  pageData: [],

  page: 1,
  total: 0,
  pageSize: 12,
})

onMounted(() => {
  initSider()
  getThingList({})
})

const initSider = () => {
  contentData.cData.push({key:'-1', title:'全部'})
  listClassificationList().then(res => {
    res.data.forEach(item=>{
      item.key = item.id
      contentData.cData.push(item)
    })
  })
  listTagList().then(res => {
    contentData.tagData = res.data
  })
}

const getSelectedKey = () => {
  if (contentData.selectedKeys.length > 0) {
    return contentData.selectedKeys[0]
  } else {
    return -1
  }
}
const onSelect = (selectedKeys) => {
  contentData.selectedKeys = selectedKeys
  console.log(contentData.selectedKeys[0])
  if (contentData.selectedKeys.length > 0) {
    getThingList({c: getSelectedKey()})
  }
  contentData.selectTagId = -1
}
const clickTag = (index) => {
  contentData.selectedKeys = []
  contentData.selectTagId = index
  getThingList({tag: contentData.selectTagId})
}

// 最新|最热|推荐
const selectTab = (index) => {
  contentData.selectTabIndex = index
  contentData.tabUnderLeft = 12 + 50 * index
  console.log(contentData.selectTabIndex)
  let sort = (index === 0 ? 'recent' : index === 1 ? 'hot' : 'recommend')
  const data = {sort: sort}
  if (contentData.selectTagId !== -1) {
    data['tag'] = contentData.selectTagId
  } else {
    data['c'] = getSelectedKey()
  }
  getThingList(data)
}
const handleDetail = (item) => {
  // 跳转新页面
  let text = router.resolve({name: 'detail', query: {id: item.id}})
  window.open(text.href, '_blank')
}
// 分页事件
const changePage = (page) => {
  contentData.page = page
  let start = (contentData.page - 1) * contentData.pageSize
  contentData.pageData = contentData.thingData.slice(start, start + contentData.pageSize)
  console.log('第' + contentData.page + '页')
}
const getThingList = (data) => {
  contentData.loading = true
  listThingList(data).then(res => {
    contentData.loading = false
    res.data.forEach((item, index) => {
      if (item.cover) {
        item.cover = BASE_URL + '/api/staticfiles/image/' +  item.cover
      }
    })
    console.log(res)
    contentData.thingData = res.data
    contentData.total = contentData.thingData.length
    changePage(1)
  }).catch(err => {
    console.log(err)
    contentData.loading = false
  })
}


</script>

<style scoped lang="less">
.mall-feed {
  box-sizing: border-box;
  min-height: calc(100vh - 56px);
  padding: 80px 24px 24px;
}

.mall-container {
  margin: 0 auto;
}

.mall-layout {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  max-width: 1100px;
  gap: 32px;
}

.mall-sidebar {
  width: 220px;
  flex-shrink: 0;
}

.mall-sidebar .filter-card {
  margin-bottom: 0;
}

.mall-main {
  flex: 1;
  min-width: 0;
}

.mall-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 14px 16px;
  margin-bottom: 10px;
  box-sizing: border-box;
}

.mall-card-title {
  color: #4d4d4d;
  font-weight: 600;
  font-size: 16px;
  line-height: 24px;
  margin: 0 0 10px;
}

.mall-card-title--tags {
  margin-top: 18px;
}

.mall-banner-wrap {
  padding: 0;
  overflow: hidden;
}

.mall-banner-carousel {
  border-radius: 16px;
  overflow: hidden;
}

.banner-slide {
  height: 140px;
  border-radius: 16px;
  border: none;
  display: flex !important;
  align-items: center;
  justify-content: center;
}

.banner-slide-a {
  background: linear-gradient(135deg, #5b8cff 0%, #4684e2 45%, #315c9e 100%);
}

.banner-slide-b {
  background: linear-gradient(135deg, #4684e2 0%, #3d7dcc 50%, #288dda 100%);
}

.banner-slide-c {
  background: linear-gradient(135deg, #6b9bd1 0%, #4684e2 55%, #2a4f88 100%);
}

.banner-inner {
  text-align: center;
  color: #fff;
  padding: 16px;
}

.banner-tag {
  display: inline-block;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.25);
  margin-bottom: 8px;
}

.banner-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.banner-sub {
  margin: 6px 0 0;
  font-size: 13px;
  opacity: 0.92;
}

.kingkong-card {
  padding: 12px 0 12px 12px;
}

.kingkong-scroll {
  display: flex;
  flex-direction: row;
  gap: 0;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 4px;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.kingkong-item {
  flex: 0 0 auto;
  width: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  margin-right: 12px;
  padding: 4px 2px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-family: inherit;
}

.kingkong-item:last-child {
  margin-right: 12px;
}

.kingkong-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(145deg, #ecf3fc, #dfeaf9);
  color: #4684e2;
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.kingkong-item--active .kingkong-icon {
  box-shadow: 0 2px 10px rgba(70, 132, 226, 0.35);
  transform: scale(1.04);
}

.kingkong-label {
  font-size: 11px;
  color: #333;
  text-align: center;
  line-height: 1.3;
  max-width: 64px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kingkong-item--active .kingkong-label {
  color: #4684e2;
  font-weight: 600;
}

.tree-wrap {
  :deep(.ant-tree) {
    background: transparent;
  }
  :deep(.ant-tree .ant-tree-node-content-wrapper.ant-tree-node-selected) {
    background: rgba(70, 132, 226, 0.12);
    color: #4684e2;
  }
}

.tag-sidebar {
  display: flex;
  flex-wrap: wrap;
  margin-top: 4px;
  gap: 0;
}

.tag-flex-view {
  display: flex;
}

.tag {
  background: #fff;
  border: 1px solid #a1adc6;
  box-sizing: border-box;
  border-radius: 16px;
  height: auto;
  min-height: 20px;
  line-height: 18px;
  padding: 4px 10px;
  margin: 8px 8px 0 0;
  cursor: pointer;
  font-size: 12px;
  color: #152833;
}

.tag:hover {
  background: #4684e3;
  color: #fff;
  border: 1px solid #4684e3;
}

.tag-select {
  background: #4684e3;
  color: #fff;
  border: 1px solid #4684e3;
}

.sort-card {
  padding: 6px 16px 10px;
}

.flex-view {
  display: flex;
}

.top-select-view {
  justify-content: flex-start;
  align-items: center;
  min-height: 40px;
  line-height: 40px;
}

.order-view {
  position: relative;
  color: #666;
  font-size: 14px;
  padding-bottom: 4px;
}

.order-view .title {
  margin-right: 8px;
}

.order-view .tab {
  color: #999;
  margin-right: 20px;
  cursor: pointer;
}

.order-view .tab-select {
  color: #152844;
  font-weight: 600;
}

.order-view .tab-underline {
  position: absolute;
  bottom: 2px;
  left: 84px;
  width: 16px;
  height: 3px;
  background: #4684e2;
  border-radius: 2px;
  transition: left 0.3s;
}

.mall-spin {
  min-height: 200px;
  display: block;
}

.mall-spin :deep(.ant-spin-container) {
  min-height: 120px;
}

.pc-thing-list {
  column-count: 2;
  column-gap: 8px;
}

.pc-thing-list .thing-item {
  break-inside: avoid;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  cursor: pointer;
  display: inline-block;
  width: 100%;
  vertical-align: top;
}

.pc-thing-list .img-view {
  width: 100%;
  height: auto;
  line-height: 0;
  background: #fafafa;
}

.pc-thing-list .img-view img {
  width: 100%;
  height: auto;
  min-height: 120px;
  max-height: 220px;
  object-fit: cover;
  display: block;
  border-radius: 12px 12px 0 0;
}

.pc-thing-list .info-view {
  padding: 8px 10px 10px;
  overflow: hidden;
}

.thing-name {
  line-height: 1.35;
  margin: 0;
  color: #0f1111;
  font-size: 14px;
  font-weight: 400;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-row {
  margin-top: 6px;
}

.a-price-symbol {
  position: relative;
  top: -0.35em;
  font-size: 11px;
  color: #0f1111;
  font-weight: 400;
}

.a-price {
  color: #0f1111;
  font-size: 17px;
  font-weight: 400;
}

.thing-sales {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.2;
}

.no-data {
  column-span: all;
  height: 160px;
  line-height: 160px;
  text-align: center;
  width: 100%;
  font-size: 14px;
  color: #999;
}

.page-card {
  padding: 12px 8px 16px;
}

.page-view {
  width: 100%;
  text-align: center;
  margin: 0;
}

.page-view :deep(.ant-pagination-item-active) {
  border-color: #4684e2;
}

.page-view :deep(.ant-pagination-item-active a) {
  color: #4684e2;
}

.mall-banner-wrap :deep(.slick-list) {
  border-radius: 16px;
}

.mall-banner-wrap :deep(.slick-dots-bottom) {
  bottom: 10px;
}

.mall-banner-wrap :deep(.slick-dots li button) {
  background: rgba(255, 255, 255, 0.5);
}

.mall-banner-wrap :deep(.slick-dots li.slick-active button) {
  background: #fff;
}
</style>
