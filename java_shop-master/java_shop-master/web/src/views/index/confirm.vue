<template>
  <div class="confirm-page-wrapper">
    <Header />

    <div class="confirm-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">确认订单</h1>
      </div>

      <!-- 订单主体 -->
      <div class="order-main">
        <!-- 左侧：订单信息 -->
        <div class="order-info-section">
          <!-- 商品信息 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">📦</span>
              <span class="card-title">商品信息</span>
            </div>

            <div class="product-item">
              <div class="product-image" @click="openDetail">
                <img :src="pageData.cover" :alt="pageData.title" />
              </div>
              <div class="product-detail">
                <h3 class="product-name">{{ pageData.title }}</h3>
                <div class="product-price">
                  <span class="price-origin">¥{{ memberPrice.price }}</span>
                  <span class="price-final">¥{{ itemFinalPrice }}</span>
                </div>
              </div>
              <div class="product-quantity">
                <a-input-number
                  v-model:value="pageData.count"
                  :min="1"
                  :max="10"
                  @change="onCountChange"
                  class="qty-input"
                />
              </div>
            </div>
          </div>

          <!-- 收货地址 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">📍</span>
              <span class="card-title">收货地址</span>
            </div>

            <div class="address-display" v-if="pageData.receiverAddress">
              <div class="address-main">
                <span class="receiver-name">{{ pageData.receiverName }}</span>
                <span class="receiver-phone">{{ pageData.receiverPhone }}</span>
              </div>
              <p class="receiver-address">{{ pageData.receiverAddress }}</p>
            </div>
            <div class="no-address" v-else>
              <span>暂无收货地址，</span>
              <span class="link" @click="handleAdd">点击添加</span>
            </div>
          </div>

          <!-- 备注 -->
          <div class="info-card">
            <div class="card-header">
              <span class="card-icon">📝</span>
              <span class="card-title">订单备注</span>
            </div>
            <textarea
              v-model="pageData.remark"
              placeholder="输入备注信息，100字以内"
              class="remark-input"
              maxlength="100"
            ></textarea>
          </div>
        </div>

        <!-- 右侧：结算 -->
        <div class="order-summary-section">
          <div class="summary-card">
            <h3 class="summary-title">订单结算</h3>

            <!-- 价格明细 -->
            <div class="price-list">
              <div class="price-row">
                <span class="price-label">商品原价</span>
                <span class="price-value">¥{{ memberPrice.subtotal }}</span>
              </div>
              <div class="price-row">
                <span class="price-label">{{ memberPrice.memberLevelName }}折扣</span>
                <span class="price-value discount">-¥{{ memberPrice.discountAmount }}</span>
              </div>
              <div class="price-row">
                <span class="price-label">积分抵扣</span>
                <div class="redeem-control">
                  <input
                    type="number"
                    v-model.number="redeemPointsInput"
                    :max="memberPrice.maxRedeemPoints || 0"
                    min="0"
                    placeholder="0"
                    class="redeem-input"
                    @input="onRedeemInput"
                  />
                  <span class="redeem-unit">积分</span>
                </div>
              </div>
              <div class="price-row">
                <span class="price-label">本单可获积分</span>
                <span class="price-value earned">{{ memberPrice.earnedPoints }}</span>
              </div>
            </div>

            <!-- 会员提示 -->
            <div class="member-tip" v-if="memberPrice.nextLevelThreshold">
              <span class="tip-icon">⭐</span>
              <span class="tip-text">再消费 ¥{{ memberPrice.nextLevelThreshold }} 即可升级</span>
            </div>

            <!-- 实付金额 -->
            <div class="total-block">
              <span class="total-label">实付金额</span>
              <span class="total-value">¥{{ finalPayment }}</span>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button class="btn-back" @click="handleBack">返回</button>
              <button class="btn-submit" @click="handleJiesuan">提交订单</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增地址弹窗 -->
    <a-modal
      v-model:open="modal.visile"
      :force-render="true"
      title="新增地址"
      ok-text="确认"
      cancel-text="取消"
      @cancel="handleCancel"
      @ok="handleOk"
    >
      <a-form
        ref="myform"
        :label-col="{ style: { width: '80px' } }"
        :model="modal.form"
        :rules="modal.rules"
      >
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="modal.form.name" placeholder="请输入收货人姓名" />
        </a-form-item>
        <a-form-item label="电话号码" name="mobile">
          <a-input v-model:value="modal.form.mobile" placeholder="请输入手机号码" />
        </a-form-item>
        <a-form-item label="送货地址" name="desc">
          <a-input v-model:value="modal.form.desc" placeholder="请输入详细地址" />
        </a-form-item>
        <a-form-item label="默认地址">
          <a-switch v-model:checked="modal.form.default" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { message } from "ant-design-vue";
import Header from '/@/views/index/components/header.vue'
import { createApi } from '/@/api/order'
import { listApi as listAddressListApi, createApi as createAddressApi } from '/@/api/address'
import {calcPriceApi} from '/@/api/member'
import {useUserStore} from "/@/store";
import {BASE_URL} from "/@/store/constants";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const pageData = reactive({
  id: undefined,
  title: undefined,
  cover: undefined,
  price: 0,
  remark: undefined,
  count: 1,
  amount: undefined,
  receiverName: undefined,
  receiverPhone: undefined,
  receiverAddress: undefined
})

const memberPrice = reactive({
  price: 0,
  subtotal: 0,
  discountRate: 1,
  discountAmount: 0,
  finalPrice: 0,
  earnedPoints: 0,
  memberLevel: 1,
  memberLevelName: '普通会员',
  userPoints: 0,
  maxRedeemPoints: 0,
  maxRedeemMoney: 0,
  nextLevelThreshold: null,
})

const redeemPointsInput = ref(0)

const modal = reactive({
  visile: false,
  editFlag: false,
  title: '',
  form: {
    name: undefined,
    mobile: undefined,
    desc: undefined,
    default: undefined
  },
  rules: {
    name: [{required: true, message: '请输入', trigger: 'change'}],
  },
})

const myform = ref()

const redeemMoneyDisplay = computed(() => {
  return ((redeemPointsInput.value || 0) / 100).toFixed(2);
});

const itemFinalPrice = computed(() => {
  const p = Number(memberPrice.price) || 0;
  return (p * pageData.count).toFixed(2);
});

const finalPayment = computed(() => {
  const totalFinal = Number(memberPrice.finalPrice) || 0;
  const redeemMoney = (redeemPointsInput.value || 0) / 100;
  const final = totalFinal - redeemMoney;
  return Math.max(0, final).toFixed(2);
});

onMounted(() => {
  pageData.id = route.query.id
  pageData.title = route.query.title
  pageData.cover = route.query.cover
  pageData.price = Number(route.query.price) || 0
  pageData.count = 1
  pageData.amount = pageData.price

  if (pageData.cover) {
    pageData.cover = BASE_URL + '/api/staticfiles/image/' + pageData.cover
  }

  listAddressData()
  loadMemberPrice()
})

const loadMemberPrice = () => {
  if (!userStore.user_id || !pageData.id) return
  calcPriceApi({
    thingId: pageData.id,
    count: pageData.count,
    userId: userStore.user_id
  }).then((res: any) => {
    const data = res.data || {}
    memberPrice.price = Number(data.price) || 0
    memberPrice.subtotal = Number(data.subtotal) || 0
    memberPrice.discountRate = Number(data.discountRate) || 1
    memberPrice.discountAmount = Number(data.discountAmount) || 0
    memberPrice.finalPrice = Number(data.finalPrice) || 0
    memberPrice.earnedPoints = Number(data.earnedPoints) || 0
    memberPrice.memberLevel = Number(data.memberLevel) || 1
    memberPrice.memberLevelName = data.memberLevelName || '普通会员'
    memberPrice.userPoints = Number(data.userPoints) || 0
    memberPrice.maxRedeemPoints = Number(data.maxRedeemPoints) || 0
    memberPrice.maxRedeemMoney = Number(data.maxRedeemMoney) || 0
    memberPrice.nextLevelThreshold = data.nextLevelThreshold
    redeemPointsInput.value = 0
  }).catch(() => {})
}

const onCountChange = (value: number) => {
  pageData.count = value
  loadMemberPrice()
}

const onRedeemInput = () => {
  const max = memberPrice.maxRedeemPoints || 0
  if (redeemPointsInput.value > max) redeemPointsInput.value = max
  if (redeemPointsInput.value < 0) redeemPointsInput.value = 0
}

const openDetail = () => {
  const text = router.resolve({ name: 'detail', query: { id: String(pageData.id) } });
  window.open(text.href, '_blank');
}

const handleAdd = () => {
  resetModal();
  modal.visile = true;
  modal.editFlag = false;
  modal.title = '新增';
  for (const key in modal.form) {
    (modal.form as any)[key] = undefined;
  }
};

const handleOk = () => {
  if(!userStore.user_id){
    message.warn('请先登录')
    return
  }
  myform.value?.validate()
      .then(() => {
        const formData = new FormData()
        formData.append('userId', userStore.user_id)
        formData.append('def', modal.form.default ? '1':'0')
        if (modal.form.name) formData.append('name', modal.form.name)
        if (modal.form.mobile) formData.append('mobile', modal.form.mobile)
        if (modal.form.desc) formData.append('description', modal.form.desc)
        createAddressApi(formData).then(res => {
          hideModal()
          pageData.receiverName = modal.form.name
          pageData.receiverAddress = modal.form.desc
          pageData.receiverPhone = modal.form.mobile
        }).catch(err => {
          message.error(err.msg || '新建失败')
        })
      })
      .catch(() => {})
};

const handleCancel = () => hideModal();
const resetModal = () => myform.value?.resetFields();
const hideModal = () => { modal.visile = false; };

const listAddressData = () => {
  let userId = userStore.user_id
  listAddressListApi({userId}).then(res => {
    if (res.data?.length > 0) {
      pageData.receiverName = res.data[0].name
      pageData.receiverPhone = res.data[0].mobile
      pageData.receiverAddress = res.data[0].description
      res.data.forEach((item: any) => {
        if (item.default) {
          pageData.receiverName = item.name
          pageData.receiverPhone = item.mobile
          pageData.receiverAddress = item.description
        }
      })
    }
  }).catch(err => {})
}

const handleBack = () => { router.back() }

const handleJiesuan = () => {
  if (!userStore.user_id) {
    message.warn('请先登录！')
    return
  }
  if (!pageData.receiverName) {
    message.warn('请选择地址！')
    return
  }
  const formData = new FormData()
  formData.append('userId', userStore.user_id)
  formData.append('thingId', pageData.id)
  formData.append('count', String(pageData.count))
  if (pageData.remark) formData.append('remark', pageData.remark)
  formData.append('receiverName', pageData.receiverName)
  formData.append('receiverPhone', pageData.receiverPhone)
  formData.append('receiverAddress', pageData.receiverAddress)
  if (redeemPointsInput.value > 0) {
    formData.append('redeemPoints', String(redeemPointsInput.value))
  }
  createApi(formData).then(() => {
    message.success('请支付订单')
    router.push({'name': 'pay', query: {'amount': finalPayment.value}})
  }).catch(err => {
    message.error(err.msg || '下单失败')
  })
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
@radius-lg: 20px;
@radius-md: 14px;

/* ==================== 页面背景 ==================== */
.confirm-page-wrapper {
  min-height: 100vh;
  background: linear-gradient(180deg, @bg-gradient-start 0%, @bg-gradient-end 100%);
}

.confirm-content {
  width: 1100px;
  margin: 0 auto;
  padding: 100px 16px 60px;
}

/* ==================== 页面标题 ==================== */
.page-header {
  margin-bottom: 32px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: @text-dark;
    margin: 0;
  }
}

/* ==================== 订单主体 ==================== */
.order-main {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

/* ==================== 信息卡片 ==================== */
.order-info-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: @radius-lg;
  box-shadow: @glass-shadow;
  padding: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.12);

  .card-icon {
    font-size: 18px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 700;
    color: @text-dark;
  }
}

/* 商品信息 */
.product-item {
  display: flex;
  align-items: center;
  gap: 16px;

  .product-image {
    width: 80px;
    height: 80px;
    border-radius: 12px;
    overflow: hidden;
    background: white;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: scale(1.05);
    }

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .product-detail {
    flex: 1;

    .product-name {
      font-size: 15px;
      font-weight: 600;
      color: @text-dark;
      margin: 0 0 8px;
      line-height: 1.4;
    }

    .product-price {
      display: flex;
      align-items: baseline;
      gap: 10px;

      .price-origin {
        font-size: 14px;
        color: @text-light;
        text-decoration: line-through;
      }

      .price-final {
        font-size: 18px;
        font-weight: 700;
        color: @primary-blue;
      }
    }
  }

  .product-quantity {
    .qty-input {
      width: 100px;

      :deep(.ant-input-number) {
        width: 100%;
        background: rgba(255, 255, 255, 0.6);
        border-color: rgba(59, 130, 246, 0.3);
        border-radius: 10px;

        &:hover {
          border-color: @primary-blue;
        }

        &.ant-input-number-focused {
          border-color: @primary-blue;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }
    }
  }
}

/* 收货地址 */
.address-display {
  .address-main {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;

    .receiver-name {
      font-size: 16px;
      font-weight: 700;
      color: @text-dark;
    }

    .receiver-phone {
      font-size: 14px;
      color: @text-muted;
    }
  }

  .receiver-address {
    font-size: 14px;
    color: @text-muted;
    margin: 0;
    line-height: 1.5;
  }
}

.no-address {
  font-size: 14px;
  color: @text-muted;

  .link {
    color: @primary-blue;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* 备注 */
.remark-input {
  width: 100%;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(59, 130, 246, 0.2);
  border-radius: 10px;
  padding: 12px 14px;
  font-size: 14px;
  color: @text-dark;
  resize: none;
  height: 70px;
  transition: all 0.2s;

  &::placeholder {
    color: @text-light;
  }

  &:focus {
    outline: none;
    border-color: @primary-blue;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  }
}

/* ==================== 结算区 ==================== */
.order-summary-section {
  position: sticky;
  top: 100px;
}

.summary-card {
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: @radius-lg;
  box-shadow: @glass-shadow;
  padding: 24px;

  .summary-title {
    font-size: 18px;
    font-weight: 700;
    color: @text-dark;
    margin: 0 0 20px;
  }
}

/* 价格明细 */
.price-list {
  border-top: 1px solid rgba(59, 130, 246, 0.12);
  padding-top: 16px;

  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 14px;

    .price-label {
      color: #909090;
    }

    .price-value {
      font-weight: 500;
      color: #152844;

      &.discount {
        color: #52c41a;
      }

      &.earned {
        color: @primary-blue;
      }
    }
  }

  .redeem-control {
    display: flex;
    align-items: center;
    gap: 4px;

    .redeem-input {
      width: 60px;
      height: 28px;
      border: 1px solid #ddd;
      border-radius: 4px;
      text-align: center;
      font-size: 13px;
      color: #152844;
      background: white;

      &:focus {
        outline: none;
        border-color: @primary-blue;
      }
    }

    .redeem-unit {
      font-size: 12px;
      color: #666;
    }
  }
}

/* 会员提示 */
.member-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px;
  background: rgba(59, 130, 246, 0.06);
  border-radius: 10px;

  .tip-icon {
    font-size: 14px;
  }

  .tip-text {
    font-size: 13px;
    color: @primary-blue;
  }
}

/* 实付金额 */
.total-block {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(59, 130, 246, 0.12);

  .total-label {
    font-size: 15px;
    font-weight: 600;
    color: #152844;
  }

  .total-value {
    font-size: 28px;
    font-weight: 700;
    color: #ff6600;
  }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 20px;

  .btn-back {
    flex: 1;
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

  .btn-submit {
    flex: 1;
    height: 48px;
    background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
    border: none;
    border-radius: @radius-md;
    font-size: 15px;
    font-weight: 600;
    color: white;
    cursor: pointer;
    transition: all 0.25s ease;
    box-shadow: 0 4px 15px rgba(59, 130, 246, 0.35);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(59, 130, 246, 0.45);
    }
  }
}

/* ==================== 响应式 ==================== */
@media (max-width: 900px) {
  .order-main {
    grid-template-columns: 1fr;
  }

  .order-summary-section {
    position: static;
  }
}

@media (max-width: 600px) {
  .confirm-content {
    padding: 80px 12px 40px;
  }

  .product-item {
    flex-wrap: wrap;

    .product-image {
      width: 60px;
      height: 60px;
    }

    .product-detail {
      width: calc(100% - 76px);
    }

    .product-quantity {
      width: 100%;
      margin-top: 12px;
    }
  }
}
</style>
