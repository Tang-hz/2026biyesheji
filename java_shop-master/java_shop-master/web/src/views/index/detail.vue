<template>
  <div class="detail">
    <Header/>

    <div class="detail-content">
      <!-- 顶部商品信息 -->
      <div class="detail-content-top">
        <div class="glass-product-card">
          <!-- 左侧图片 -->
          <div class="product-image-section">
            <div class="main-image-wrapper">
              <img :src="detailData.cover" class="main-image" />
            </div>
          </div>

          <!-- 右侧信息 -->
          <div class="product-info-section">
            <div class="product-status">
              <span class="status-tag">上市销售</span>
            </div>
            <h1 class="product-title">{{ detailData.title }}</h1>

            <div class="price-container">
              <span class="current-price">¥{{ detailData.price }}</span>
            </div>

            <div class="info-divider"></div>

            <div class="info-row">
              <span class="info-label">分类</span>
              <span class="info-value">{{ detailData.classification_title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">库存</span>
              <span class="info-value">{{ detailData.repertory }} 件</span>
            </div>

            <div class="info-divider"></div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button type="button" class="btn-primary" @click="handleOrder(detailData)">
                立即购买
              </button>
              <button type="button" class="btn-secondary" @click="addToCart">
                加入购物车
              </button>
            </div>

            <!-- 互动统计 -->
            <div class="互动-stats">
              <div class="stat-item" @click="addToWish">
                <span class="stat-icon">♥</span>
                <span class="stat-label">加入心愿单</span>
                <span class="stat-count">{{ detailData.wishCount }}</span>
              </div>
              <div class="stat-item" @click="collect">
                <span class="stat-icon">★</span>
                <span class="stat-label">收藏</span>
                <span class="stat-count">{{ detailData.collectCount }}</span>
              </div>
              <div class="stat-item" @click="share">
                <span class="stat-icon">↗</span>
                <span class="stat-label">分享</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部详情 -->
      <div class="detail-content-bottom">
        <div class="glass-detail-card">
          <div class="detail-tabs">
            <span
              class="tab"
              :class="{ active: selectTabIndex === 0 }"
              @click="selectTab(0)"
            >
              简介
            </span>
            <span
              class="tab"
              :class="{ active: selectTabIndex === 1 }"
              @click="selectTab(1)"
            >
              评论 {{ commentData.length }}
            </span>
          </div>

          <!-- 简介内容 -->
          <div class="tab-content" v-show="selectTabIndex === 0">
            <p class="description-text">{{ detailData.description }}</p>
          </div>

          <!-- 评论内容 -->
          <div class="tab-content" v-show="selectTabIndex === 1">
            <!-- 发布评论 -->
            <div class="comment-publish">
              <img :src="AvatarIcon" class="comment-avatar">
              <input
                placeholder="说点什么..."
                class="comment-input"
                ref="commentRef"
              />
              <button class="send-btn" @click="sendComment">发送</button>
            </div>

            <!-- 排序 -->
            <div class="comment-sort">
              <span class="comment-count">共有 {{ commentData.length }} 条评论</span>
              <div class="sort-tabs" v-if="commentData.length > 0">
                <span
                  :class="{ active: sortIndex === 0 }"
                  @click="sortCommentList('recent')"
                >最新</span>
                <span class="sort-line">|</span>
                <span
                  :class="{ active: sortIndex === 1 }"
                  @click="sortCommentList('hot')"
                >热门</span>
              </div>
            </div>

            <!-- 评论列表 -->
            <div class="comments-list">
              <div class="comment-item" v-for="item in commentData" :key="item.id">
                <div class="comment-header">
                  <img :src="AvatarIcon" class="comment-avatar">
                  <div class="comment-meta">
                    <span class="comment-author">{{ item.username }}</span>
                    <span class="comment-time">{{ item.commentTime }}</span>
                  </div>
                  <div class="comment-actions">
                    <span @click="like(item.id)">推荐 {{ item.likeCount }}</span>
                  </div>
                </div>
                <p class="comment-text">{{ item.content }}</p>
              </div>
              <div class="no-more" v-if="commentData.length === 0">
                <p>暂无评论</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 热门推荐 -->
        <div class="recommend-section">
          <h3 class="recommend-title">热门推荐</h3>
          <div class="recommend-grid">
            <div
              class="recommend-card"
              v-for="item in recommendData"
              :key="item.id"
              @click="handleDetail(item)"
            >
              <div class="recommend-image-wrapper">
                <img :src="item.cover" class="recommend-image">
              </div>
              <div class="recommend-info">
                <h4 class="recommend-name">{{ item.title.substring(0, 12) }}</h4>
                <span class="recommend-price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer/>
  </div>
</template>

<script setup>
import {message} from "ant-design-vue";
import Header from '/@/views/index/components/header.vue'
import Footer from '/@/views/index/components/footer.vue'
import AvatarIcon from '/@/assets/images/avatar.jpg';
import {
  detailApi,
  listApi as listThingList,
} from '/@/api/thing'
import {listThingCommentsApi, createApi as createCommentApi, likeApi} from '/@/api/comment'
import {wishApi} from '/@/api/thingWish'
import {collectApi} from '/@/api/thingCollect'
import {addCartApi} from '/@/api/cart'
import {BASE_URL} from "/@/store/constants";
import {useRoute, useRouter} from "vue-router/dist/vue-router";
import {useUserStore, useCartStore} from "/@/store";
import {getFormatTime} from "/@/utils";

const router = useRouter()
const route = useRoute()
const userStore = useUserStore();
const cartStore = useCartStore();

let thingId = ref('')
let detailData = ref({})
let tabData = ref(['简介', '评论'])
let selectTabIndex = ref(0)

let commentData = ref([])
let recommendData = ref([])
let sortIndex = ref(0)
let order = ref('recent')

let commentRef = ref()

onMounted(() => {
  thingId.value = route.query.id.trim()
  getThingDetail()
  getRecommendThing()
  getCommentList()
})

const selectTab = (index) => {
  selectTabIndex.value = index
}

const getThingDetail = () => {
  detailApi({id: thingId.value}).then(res => {
    detailData.value = res.data
    detailData.value.cover = BASE_URL + '/api/staticfiles/image/' + detailData.value.cover
  }).catch(err => {
    message.error('获取详情失败')
  })
}

const addToWish = () => {
  let userId = userStore.user_id
  if (userId) {
    wishApi({thingId: thingId.value, userId: userId}).then(res => {
      message.success(res.msg)
      getThingDetail()
    }).catch(err => {
      console.log('操作失败')
    })
  } else {
    message.warn('请先登录')
  }
}

const collect = () => {
  let userId = userStore.user_id
  if (userId) {
    collectApi({thingId: thingId.value, userId: userId}).then(res => {
      message.success(res.msg)
      getThingDetail()
    }).catch(err => {
      console.log('收藏失败')
    })
  } else {
    message.warn('请先登录')
  }
}

const addToCart = () => {
  const userId = userStore.user_id
  if (!userId) {
    message.warn('请先登录')
    return
  }
  const fd = new FormData()
  fd.append('userId', String(userId))
  fd.append('thingId', String(thingId.value))
  fd.append('itemCount', '1')
  addCartApi(fd).then(res => {
    message.success(res.msg || '已加入购物车')
    cartStore.refreshCount()
  }).catch(err => {
    message.error(err.msg || '加入失败')
  })
}

const share = () => {
  let content = '分享一个非常好玩的网站 ' + window.location.href
  let shareHref = 'http://service.weibo.com/share/share.php?title=' + content
  window.open(shareHref)
}

const handleOrder = (detailData) => {
  console.log(detailData)
  const userId = userStore.user_id
  router.push({
    name: 'confirm',
    query: {
      id: detailData.id,
      title: detailData.title,
      cover: detailData.cover,
      price: detailData.price
    }
  })
}

const getRecommendThing = () => {
  listThingList({sort: 'recommend'}).then(res => {
    res.data.forEach((item, index) => {
      if (item.cover) {
        item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover
      }
    })
    console.log(res)
    recommendData.value = res.data.slice(0, 6)
  }).catch(err => {
    console.log(err)
  })
}

const handleDetail = (item) => {
  let text = router.resolve({name: 'detail', query: {id: item.id}})
  window.open(text.href, '_blank')
}

const sendComment = () => {
  console.log(commentRef.value)
  let text = commentRef.value.value.trim()
  console.log(text)
  if (text.length <= 0) {
    return
  }
  commentRef.value.value = ''
  let userId = userStore.user_id
  if (userId) {
    createCommentApi({content: text, thingId: thingId.value, userId: userId}).then(res => {
      getCommentList()
    }).catch(err => {
      console.log(err)
    })
  } else {
    message.warn('请先登录！')
    router.push({name: 'login'})
  }
}

const like = (commentId) => {
  likeApi({id: commentId}).then(res => {
    getCommentList()
  }).catch(err => {
    console.log(err)
  })
}

const getCommentList = () => {
  listThingCommentsApi({thingId: thingId.value, order: order.value}).then(res => {
    res.data.forEach(item => {
      item.commentTime = getFormatTime(item.commentTime, true)
    })
    commentData.value = res.data
  }).catch(err => {
    console.log(err)
  })
}

const sortCommentList = (sortType) => {
  if (sortType === 'recent') {
    sortIndex.value = 0
  } else {
    sortIndex.value = 1
  }
  order.value = sortType
  getCommentList()
}
</script>

<style scoped lang="less">
/* ==================== 变量定义 ==================== */
@primary-blue: #3b82f6;
@primary-blue-dark: #1d4ed8;
@primary-blue-light: #60a5fa;
@bg-gradient-start: #f0f4f8;
@bg-gradient-end: #e2e8f0;
@text-dark: #1e293b;
@text-muted: #64748b;
@glass-bg: rgba(255, 255, 255, 0.7);
@glass-border: rgba(255, 255, 255, 0.9);
@glass-shadow: 0 8px 32px rgba(59, 130, 246, 0.08);

/* ==================== 页面背景 ==================== */
.detail {
  min-height: 100vh;
  background: linear-gradient(180deg, @bg-gradient-start 0%, @bg-gradient-end 100%);
}

.detail-content {
  width: 1100px;
  margin: 0 auto;
  padding: 20px 16px 60px;
}

/* ==================== 玻璃效果基础 ==================== */
.glass-card {
  background: @glass-bg;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: 24px;
  box-shadow: @glass-shadow;
}

.glass-card-dark {
  background: rgba(59, 130, 246, 0.08);
  border: 1px solid rgba(59, 130, 246, 0.15);
}

/* ==================== 商品信息卡片 ==================== */
.glass-product-card {
  display: flex;
  gap: 32px;
  padding: 28px;
  margin-top: 80px;
  .glass-card();
}

/* 左侧图片 */
.product-image-section {
  flex: 0 0 360px;

  .main-image-wrapper {
    background: white;
    border-radius: 20px;
    padding: 16px;
    overflow: hidden;

    .main-image {
      width: 100%;
      height: 320px;
      object-fit: cover;
      border-radius: 16px;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.02);
      }
    }
  }
}

/* 右侧信息 */
.product-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-status {
  margin-bottom: 12px;

  .status-tag {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(59, 130, 246, 0.1);
    color: @primary-blue;
    font-size: 13px;
    font-weight: 500;
    border-radius: 6px;
  }
}

.product-title {
  font-size: 24px;
  font-weight: 700;
  color: @text-dark;
  margin: 0 0 16px;
  line-height: 1.4;
}

.price-container {
  margin-bottom: 20px;

  .current-price {
    font-size: 36px;
    font-weight: 700;
    color: @primary-blue;
  }
}

.info-divider {
  height: 1px;
  background: rgba(59, 130, 246, 0.12);
  margin: 16px 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;

  .info-label {
    color: @text-muted;
    font-size: 14px;
    width: 60px;
  }

  .info-value {
    color: @text-dark;
    font-size: 14px;
    font-weight: 500;
  }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;

  .btn-primary {
    flex: 1;
    height: 48px;
    background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
    border: none;
    border-radius: 14px;
    color: white;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.35);
    transition: all 0.25s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(59, 130, 246, 0.45);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .btn-secondary {
    flex: 1;
    height: 48px;
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(59, 130, 246, 0.3);
    border-radius: 14px;
    color: @primary-blue;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      background: rgba(59, 130, 246, 0.08);
      border-color: @primary-blue;
    }
  }
}

/* 互动统计 */
.互动-stats {
  display: flex;
  gap: 24px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(59, 130, 246, 0.1);

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
      opacity: 0.7;
    }

    .stat-icon {
      font-size: 16px;
      color: @primary-blue;
    }

    .stat-label {
      font-size: 13px;
      color: @text-muted;
    }

    .stat-count {
      font-size: 14px;
      font-weight: 600;
      color: @text-dark;
    }
  }
}

/* ==================== 详情底部 ==================== */
.detail-content-bottom {
  margin-top: 24px;
}

.glass-detail-card {
  padding: 28px;
  .glass-card();
}

/* Tab 切换 */
.detail-tabs {
  display: flex;
  gap: 32px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.12);
  margin-bottom: 24px;

  .tab {
    padding: 12px 0;
    font-size: 15px;
    font-weight: 500;
    color: @text-muted;
    cursor: pointer;
    position: relative;
    transition: color 0.2s;

    &.active {
      color: @primary-blue;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        bottom: -1px;
        left: 0;
        right: 0;
        height: 2px;
        background: @primary-blue;
        border-radius: 2px;
      }
    }
  }
}

/* 简介内容 */
.description-text {
  color: @text-muted;
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

/* 评论发布 */
.comment-publish {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .comment-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
  }

  .comment-input {
    flex: 1;
    height: 40px;
    padding: 0 16px;
    background: rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(59, 130, 246, 0.2);
    border-radius: 10px;
    font-size: 14px;
    outline: none;
    transition: all 0.2s;

    &:focus {
      border-color: @primary-blue;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }

    &::placeholder {
      color: @text-muted;
    }
  }

  .send-btn {
    height: 40px;
    padding: 0 24px;
    background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
    border: none;
    border-radius: 10px;
    color: white;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(59, 130, 246, 0.35);
    }
  }
}

/* 评论排序 */
.comment-sort {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .comment-count {
    font-size: 14px;
    color: @text-muted;
  }

  .sort-tabs {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;

    span {
      color: @text-muted;
      cursor: pointer;
      transition: color 0.2s;

      &.active {
        color: @primary-blue;
        font-weight: 600;
      }
    }

    .sort-line {
      color: #d1d5db;
    }
  }
}

/* 评论列表 */
.comments-list {
  .comment-item {
    padding: 16px 0;
    border-bottom: 1px dashed rgba(59, 130, 246, 0.15);

    &:last-child {
      border-bottom: none;
    }
  }

  .comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .comment-avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      object-fit: cover;
      margin-right: 10px;
    }

    .comment-meta {
      flex: 1;

      .comment-author {
        font-size: 14px;
        font-weight: 600;
        color: @text-dark;
        display: block;
      }

      .comment-time {
        font-size: 12px;
        color: @text-muted;
      }
    }

    .comment-actions {
      font-size: 13px;
      color: @primary-blue;
      cursor: pointer;

      span {
        margin-left: 16px;
      }
    }
  }

  .comment-text {
    margin: 0 0 0 46px;
    font-size: 14px;
    color: @text-muted;
    line-height: 1.6;
  }

  .no-more {
    text-align: center;
    padding: 40px 0;
    color: @text-muted;
  }
}

/* ==================== 推荐区域 ==================== */
.recommend-section {
  margin-top: 32px;

  .recommend-title {
    font-size: 18px;
    font-weight: 600;
    color: @text-dark;
    margin: 0 0 20px;
  }
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.recommend-card {
  padding: 16px;
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(59, 130, 246, 0.12);
  }

  .recommend-image-wrapper {
    width: 100%;
    aspect-ratio: 1;
    background: white;
    border-radius: 16px;
    overflow: hidden;
    margin-bottom: 12px;

    .recommend-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .recommend-info {
    .recommend-name {
      font-size: 14px;
      font-weight: 500;
      color: @text-dark;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .recommend-price {
      font-size: 16px;
      font-weight: 700;
      color: @primary-blue;
    }
  }
}

/* ==================== 响应式 ==================== */
@media (max-width: 768px) {
  .detail-content {
    width: 100%;
    padding: 16px 12px 40px;
  }

  .glass-product-card {
    flex-direction: column;
    padding: 20px;
    margin-top: 70px;

    .product-image-section {
      flex: none;

      .main-image-wrapper .main-image {
        height: 280px;
      }
    }
  }

  .recommend-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
