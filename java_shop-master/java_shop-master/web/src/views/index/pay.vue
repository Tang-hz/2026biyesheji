<template>
  <div class="pay-page-wrapper">
    <Header />

    <div class="pay-content">
      <!-- 支付图标 -->
      <div class="pay-icon-wrapper">
        <div class="pay-circle" :class="{'pay-success': payStatus === 'success'}">
          <span v-if="payStatus === 'pending'" class="pay-icon">💳</span>
          <span v-else-if="payStatus === 'paying'" class="loading-icon">⏳</span>
          <span v-else class="check-icon">✓</span>
        </div>
        <div class="pay-glow" :class="{'glow-success': payStatus === 'success'}"></div>
      </div>

      <!-- 标题 -->
      <h1 class="page-title" v-if="payStatus === 'pending'">等待支付</h1>
      <h1 class="page-title" v-else-if="payStatus === 'paying'">支付中...</h1>
      <h1 class="page-title" v-else>支付成功</h1>

      <p class="page-subtitle" v-if="payStatus === 'pending'">请完成支付操作</p>
      <p class="page-subtitle" v-else-if="payStatus === 'paying'">正在确认支付结果，请稍候...</p>
      <p class="page-subtitle" v-else>感谢您的购买</p>

      <!-- 订单金额卡片 -->
      <div class="order-card">
        <div class="order-info">
          <span class="order-label">支付金额</span>
          <span class="order-amount">¥{{ amount }}</span>
        </div>
        <div class="order-divider"></div>
        <div class="order-detail">
          <span class="detail-label">订单编号</span>
          <span class="detail-value">{{ orderNumber }}</span>
        </div>
      </div>

      <!-- 支付操作区 -->
      <div class="pay-action" v-if="payStatus === 'pending'">
        <button class="btn-alipay" @click="handlePay">
          <span class="btn-icon">💰</span>
          打开支付宝支付
        </button>
        <p class="pay-tip">点击按钮后将打开支付宝收银台页面</p>
      </div>

      <!-- 支付中提示 -->
      <div class="paying-tip" v-if="payStatus === 'paying'">
        <div class="loading-spinner"></div>
        <p>请在支付宝页面完成支付</p>
        <p class="sub-tip">支付完成后会自动跳转</p>
      </div>

      <!-- 支付成功操作 -->
      <div class="action-buttons" v-if="payStatus === 'success'">
        <button class="btn-view-order" @click="handleViewOrder">
          <span class="btn-icon">📋</span>
          查看订单
        </button>
        <button class="btn-back-home" @click="handleBackHome">
          返回首页
        </button>
      </div>

      <!-- 底部装饰 -->
      <div class="bottom-decoration">
        <div class="decoration-dot"></div>
        <div class="decoration-line"></div>
        <div class="decoration-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import Header from '/@/views/index/components/header.vue'
import { message } from "ant-design-vue"
import { createPayApi, queryPayStatusApi } from '/@/api/pay'

const router = useRouter()
const route = useRoute()

const amount = ref('')
const orderNumber = ref('')
const payStatus = ref('pending') // pending, paying, success
let pollTimer = null

onMounted(() => {
  amount.value = route.query.amount
  orderNumber.value = route.query.orderNumber

  if (!orderNumber.value) {
    message.error('订单号不存在')
    router.push({ name: 'index' })
    return
  }
})

onUnmounted(() => {
  stopPolling()
})

const handlePay = async () => {
  try {
    const res = await createPayApi({ orderNumber: orderNumber.value })

    if (res.data && res.data.form) {
      // 在新窗口打开支付宝支付页面
      const newWindow = window.open('', '_blank')
      if (newWindow) {
        newWindow.document.write(res.data.form)
        newWindow.document.close()
      } else {
        message.warning('浏览器拦截了弹窗，请允许弹窗后重试')
        return
      }

      // 开始轮询支付状态
      payStatus.value = 'paying'
      startPolling()
    }
  } catch (e) {
    message.error('创建支付失败: ' + (e.msg || e.message || '未知错误'))
  }
}

const startPolling = () => {
  stopPolling()
  pollTimer = setInterval(async () => {
    try {
      const res = await queryPayStatusApi({ orderNumber: orderNumber.value })
      if (res.data && res.data.paid) {
        payStatus.value = 'success'
        stopPolling()
        message.success('支付成功！')
      }
    } catch (e) {
      console.error('查询支付状态失败', e)
    }
  }, 3000) // 每3秒查询一次
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleViewOrder = () => {
  router.push({ name: 'orderView' })
}

const handleBackHome = () => {
  router.push({ name: 'index' })
}
</script>

<style scoped lang="less">
/* ==================== 变量定义 ==================== */
@primary-blue: #3b82f6;
@primary-blue-dark: #1d4ed8;
@primary-blue-light: #60a5fa;
@success-green: #10b981;
@bg-gradient-start: #f0f4f8;
@bg-gradient-end: #e2e8f0;
@text-dark: #1e293b;
@text-muted: #64748b;
@text-light: #94a3b8;
@glass-bg: rgba(255, 255, 255, 0.7);
@glass-border: rgba(255, 255, 255, 0.9);
@glass-shadow: 0 8px 32px rgba(59, 130, 246, 0.08);
@radius-lg: 24px;
@radius-md: 16px;
@radius-sm: 10px;

/* ==================== 页面背景 ==================== */
.pay-page-wrapper {
  min-height: 100vh;
  background: linear-gradient(180deg, @bg-gradient-start 0%, @bg-gradient-end 100%);
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.pay-content {
  width: 100%;
  max-width: 500px;
  padding: 40px 20px;
  text-align: center;
}

/* ==================== 支付图标 ==================== */
.pay-icon-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
}

.pay-circle {
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 8px 30px rgba(59, 130, 246, 0.4);
  transition: all 0.3s ease;

  &.pay-success {
    background: linear-gradient(135deg, @success-green 0%, #059669 100%);
    box-shadow: 0 8px 30px rgba(16, 185, 129, 0.4);
  }

  .pay-icon {
    font-size: 48px;
  }

  .loading-icon {
    font-size: 48px;
    animation: spin 1s linear infinite;
  }

  .check-icon {
    font-size: 48px;
    color: white;
    font-weight: bold;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pay-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 140px;
  height: 140px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 1;
  animation: pulse 2s ease-in-out infinite;

  &.glow-success {
    background: radial-gradient(circle, rgba(16, 185, 129, 0.2) 0%, transparent 70%);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.7;
  }
}

/* ==================== 标题 ==================== */
.page-title {
  font-size: 28px;
  font-weight: 700;
  color: @text-dark;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 15px;
  color: @text-muted;
  margin: 0 0 32px;
}

/* ==================== 订单卡片 ==================== */
.order-card {
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: @radius-lg;
  box-shadow: @glass-shadow;
  padding: 24px;
  margin-bottom: 24px;
}

.order-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .order-label {
    font-size: 14px;
    color: @text-muted;
  }

  .order-amount {
    font-size: 36px;
    font-weight: 700;
    color: #ff6600;
    letter-spacing: -1px;
  }
}

.order-divider {
  height: 1px;
  background: rgba(59, 130, 246, 0.12);
  margin: 20px 0;
}

.order-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .detail-label {
    font-size: 13px;
    color: @text-muted;
  }

  .detail-value {
    font-size: 13px;
    color: @text-dark;
    font-weight: 500;
    font-family: monospace;
  }
}

/* ==================== 支付操作 ==================== */
.pay-action {
  margin-bottom: 24px;
}

.btn-alipay {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #1677ff 0%, #0958d9 100%);
  border: none;
  border-radius: @radius-md;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 15px rgba(22, 119, 255, 0.35);

  .btn-icon {
    font-size: 18px;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(22, 119, 255, 0.45);
  }

  &:active {
    transform: translateY(0);
  }
}

.pay-tip {
  font-size: 13px;
  color: @text-muted;
  margin-top: 12px;
}

/* ==================== 支付中提示 ==================== */
.paying-tip {
  margin-bottom: 24px;

  p {
    font-size: 15px;
    color: @text-dark;
    margin: 8px 0;
  }

  .sub-tip {
    font-size: 13px;
    color: @text-muted;
  }
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(59, 130, 246, 0.2);
  border-top-color: @primary-blue;
  border-radius: 50%;
  margin: 0 auto 16px;
  animation: spin 0.8s linear infinite;
}

/* ==================== 操作按钮 ==================== */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-view-order {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, @success-green 0%, #059669 100%);
  border: none;
  border-radius: @radius-md;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.35);

  .btn-icon {
    font-size: 18px;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(16, 185, 129, 0.45);
  }

  &:active {
    transform: translateY(0);
  }
}

.btn-back-home {
  width: 100%;
  height: 48px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: @radius-md;
  font-size: 15px;
  font-weight: 600;
  color: @primary-blue;
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    background: rgba(59, 130, 246, 0.08);
    border-color: @primary-blue;
  }
}

/* ==================== 底部装饰 ==================== */
.bottom-decoration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 48px;

  .decoration-dot {
    width: 8px;
    height: 8px;
    background: @primary-blue;
    border-radius: 50%;
    opacity: 0.4;
  }

  .decoration-line {
    width: 40px;
    height: 2px;
    background: linear-gradient(90deg, transparent, @primary-blue, transparent);
    opacity: 0.3;
  }
}

/* ==================== 响应式 ==================== */
@media (max-width: 480px) {
  .pay-page-wrapper {
    padding-top: 60px;
  }

  .pay-content {
    padding: 30px 16px;
  }

  .pay-icon-wrapper {
    width: 100px;
    height: 100px;
    margin-bottom: 24px;
  }

  .pay-circle {
    width: 80px;
    height: 80px;

    .pay-icon,
    .loading-icon,
    .check-icon {
      font-size: 40px;
    }
  }

  .pay-glow {
    width: 120px;
    height: 120px;
  }

  .page-title {
    font-size: 24px;
  }

  .order-amount {
    font-size: 32px;
  }
}
</style>
