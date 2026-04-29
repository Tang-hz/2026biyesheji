<template>
  <div class="pay-page-wrapper">
    <Header />

    <div class="pay-content">
      <!-- 成功图标 -->
      <div class="success-icon-wrapper">
        <div class="success-circle">
          <span class="check-icon">✓</span>
        </div>
        <div class="success-glow"></div>
      </div>

      <!-- 标题 -->
      <h1 class="page-title">订单提交成功</h1>
      <p class="page-subtitle">感谢您的购买，订单已成功提交</p>

      <!-- 订单金额卡片 -->
      <div class="order-card">
        <div class="order-info">
          <span class="order-label">实付金额</span>
          <span class="order-amount">¥{{ amount }}</span>
        </div>
        <div class="order-divider"></div>
        <div class="order-time">
          <span class="time-label">下单时间</span>
          <span class="time-value">{{ currentTime }}</span>
        </div>
      </div>

      <!-- 温馨提示 -->
      <div class="tips-card">
        <h3 class="tips-title">
          <span class="tips-icon">💡</span>
          温馨提示
        </h3>
        <ul class="tips-list">
          <li>请在规定时间内完成支付，逾期订单将自动取消</li>
          <li>支付完成后，可在"我的订单"中查看订单状态</li>
          <li>如有疑问，请联系客服获取帮助</li>
        </ul>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
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
import {message} from "ant-design-vue";

const router = useRouter();
const route = useRoute();

let amount = ref()
let currentTime = ref()

onMounted(() => {
  amount.value = route.query.amount
  currentTime.value = formatDate(new Date().getTime(), 'YYYY-MM-DD HH:mm:ss')
})

const handleViewOrder = () => {
  router.push({name: 'orderView'})
}

const handleBackHome = () => {
  router.push({name: 'index'})
}

const formatDate = (time, format = 'YYYY-MM-DD HH:mm:ss') => {
  const date = new Date(time)

  const year = date.getFullYear(),
      month = String(date.getMonth() + 1).padStart(2, '0'),
      day = String(date.getDate()).padStart(2, '0'),
      hour = String(date.getHours()).padStart(2, '0'),
      min = String(date.getMinutes()).padStart(2, '0'),
      sec = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hour}:${min}:${sec}`
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

/* ==================== 成功图标 ==================== */
.success-icon-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
}

.success-circle {
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

  .check-icon {
    font-size: 48px;
    color: white;
    font-weight: bold;
  }
}

.success-glow {
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
    color: @primary-blue;
    letter-spacing: -1px;
  }
}

.order-divider {
  height: 1px;
  background: rgba(59, 130, 246, 0.12);
  margin: 20px 0;
}

.order-time {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .time-label {
    font-size: 13px;
    color: @text-muted;
  }

  .time-value {
    font-size: 13px;
    color: @text-dark;
    font-weight: 500;
  }
}

/* ==================== 提示卡片 ==================== */
.tips-card {
  background: rgba(59, 130, 246, 0.04);
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: @radius-md;
  padding: 20px;
  margin-bottom: 32px;
  text-align: left;

  .tips-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: @text-dark;
    margin: 0 0 12px;

    .tips-icon {
      font-size: 16px;
    }
  }

  .tips-list {
    margin: 0;
    padding-left: 20px;

    li {
      font-size: 13px;
      color: @text-muted;
      line-height: 1.8;
      margin-bottom: 4px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
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
  background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
  border: none;
  border-radius: @radius-md;
  font-size: 16px;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.35);

  .btn-icon {
    font-size: 18px;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.45);
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

  .success-icon-wrapper {
    width: 100px;
    height: 100px;
    margin-bottom: 24px;
  }

  .success-circle {
    width: 80px;
    height: 80px;

    .check-icon {
      font-size: 40px;
    }
  }

  .success-glow {
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
