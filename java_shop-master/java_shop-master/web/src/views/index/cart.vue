<template>
  <div class="cart-page-wrapper">
    <Header />

    <div class="cart-content">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">我的购物车</h1>
        <span class="item-count" v-if="cartRows.length">{{ cartRows.length }} 件商品</span>
      </div>

      <!-- 购物车为空 -->
      <div v-if="!cartRows.length" class="empty-cart">
        <div class="empty-icon">🛒</div>
        <h2 class="empty-title">购物车是空的</h2>
        <p class="empty-text">去首页逛逛，发现更多好物</p>
        <button class="btn-primary" @click="$router.push({ name: 'index' })">
          逛逛首页
        </button>
      </div>

      <!-- 购物车列表 -->
      <div v-else class="cart-main">
        <!-- 左侧：商品列表 -->
        <div class="cart-items-section">
          <div class="section-header">
            <span class="header-label">商品信息</span>
            <span class="header-unit">单价</span>
            <span class="header-unit">折扣价</span>
            <span class="header-unit">数量</span>
            <span class="header-action">操作</span>
          </div>

          <div class="items-list">
            <div
              v-for="item in cartRows"
              :key="item.id"
              class="cart-item"
            >
              <div class="item-product" @click="openDetail(item)">
                <div class="product-image">
                  <img :src="item.cover" :alt="item.title" />
                </div>
                <div class="product-info">
                  <h3 class="product-name">{{ item.title }}</h3>
                </div>
              </div>

              <div class="item-price origin">¥{{ item.price }}</div>
              <div class="item-price discounted">¥{{ item.finalPrice }}</div>

              <div class="item-quantity">
                <div class="quantity-control">
                  <span class="qty-btn" @click="decQty(item)">−</span>
                  <span class="qty-value">{{ item.count }}</span>
                  <span class="qty-btn" @click="incQty(item)">+</span>
                </div>
              </div>

              <div class="item-actions">
                <button class="delete-btn" @click="removeLine(item)" title="删除">
                  <span>🗑</span>
                </button>
              </div>
            </div>
          </div>

          <!-- 备注 -->
          <div class="remark-section">
            <label class="remark-label">订单备注</label>
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

            <!-- 收货地址 -->
            <div class="address-block">
              <div class="block-header">
                <span class="block-icon">📍</span>
                <span class="block-title">收货地址</span>
              </div>
              <div class="address-info" v-if="pageData.receiverAddress">
                <p class="receiver">{{ pageData.receiverName }}</p>
                <p class="phone">{{ pageData.receiverPhone }}</p>
                <p class="address-text">{{ pageData.receiverAddress }}</p>
              </div>
              <div class="no-address" v-else>
                <span>暂无地址，</span>
                <span class="link" @click="handleAdd">新建地址</span>
              </div>
            </div>

            <!-- 价格明细 -->
            <div class="price-block">
              <div class="price-row">
                <span class="price-label">商品总价</span>
                <span class="price-value">¥{{ orderSummary.totalSubtotal || '0.00' }}</span>
              </div>
              <div class="price-row">
                <span class="price-label">{{ orderSummary.memberLevelName || '会员' }}折扣</span>
                <span class="price-value discount">-¥{{ orderSummary.totalDiscountAmount || '0.00' }}</span>
              </div>
              <div class="price-row">
                <span class="price-label">积分抵扣</span>
                <div class="redeem-control">
                  <input
                    type="number"
                    v-model.number="redeemPointsInput"
                    :max="orderSummary.maxRedeemPoints || 0"
                    min="0"
                    placeholder="0"
                    class="redeem-input"
                    @input="onRedeemInput"
                  />
                  <span class="redeem-unit">积分</span>
                </div>
              </div>
              <div class="price-row">
                <span class="price-label">可获积分</span>
                <span class="price-value earned">{{ orderSummary.totalEarnedPoints || 0 }}</span>
              </div>

              <div class="price-total">
                <span class="total-label">实付金额</span>
                <span class="total-value">¥{{ finalPayment }}</span>
              </div>
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
import { message } from 'ant-design-vue';
import Header from '/@/views/index/components/header.vue';
import { createApi } from '/@/api/order';
import { listApi as listAddressListApi, createApi as createAddressApi } from '/@/api/address';
import {
  listCartApi,
  updateCartCountApi,
  removeCartApi,
  clearCartApi,
} from '/@/api/cart';
import { useUserStore, useCartStore } from '/@/store';
import { BASE_URL } from '/@/store/constants';

const router = useRouter();
const userStore = useUserStore();
const cartStore = useCartStore();

const cartRows = ref<any[]>([]);

const pageData = reactive({
  remark: undefined as string | undefined,
  receiverName: undefined as string | undefined,
  receiverPhone: undefined as string | undefined,
  receiverAddress: undefined as string | undefined,
});

const modal = reactive({
  visile: false,
  title: '',
  form: {
    name: undefined as string | undefined,
    mobile: undefined as string | undefined,
    desc: undefined as string | undefined,
    default: false,
  },
  rules: {
    name: [{ required: true, message: '请输入', trigger: 'change' }],
  },
});

const myform = ref();

// 积分抵扣相关
const redeemPointsInput = ref(0);
const orderSummary = ref<any>({});

const redeemMoneyDisplay = computed(() => {
  const points = redeemPointsInput.value || 0;
  return (points / 100).toFixed(2);
});

const finalPayment = computed(() => {
  const totalFinal = Number(orderSummary.value.totalFinalPrice) || 0;
  const redeemMoney = (redeemPointsInput.value || 0) / 100;
  const final = totalFinal - redeemMoney;
  return Math.max(0, final).toFixed(2);
});

onMounted(() => {
  listAddressData();
  loadCart();
});

const loadCart = () => {
  const userId = userStore.user_id;
  if (!userId) {
    cartRows.value = [];
    return;
  }
  listCartApi({ userId })
    .then((res: any) => {
      const rows = res.data || [];
      rows.forEach((item: any) => {
        if (item.cover) {
          item.cover = BASE_URL + '/api/staticfiles/image/' + item.cover;
        }
        item.count = Number(item.count);
        if (!item.finalPrice) {
          item.finalPrice = (Number(item.price) * item.count).toFixed(2);
        }
      });
      cartRows.value = rows;
      if (rows.length > 0 && rows[0]._orderSummary) {
        orderSummary.value = rows[0]._orderSummary;
      } else {
        orderSummary.value = {};
      }
      redeemPointsInput.value = 0;
    })
    .catch(() => {
      cartRows.value = [];
    });
};

const onRedeemInput = () => {
  const max = orderSummary.value.maxRedeemPoints || 0;
  if (redeemPointsInput.value > max) {
    redeemPointsInput.value = max;
  }
  if (redeemPointsInput.value < 0) {
    redeemPointsInput.value = 0;
  }
};

const openDetail = (item: any) => {
  const text = router.resolve({ name: 'detail', query: { id: String(item.thingId) } });
  window.open(text.href, '_blank');
};

const recalcOrderSummary = () => {
  const discountRate = orderSummary.value.discountRate || 1;
  let totalSubtotal = 0;
  let totalFinalPrice = 0;
  let totalEarnedPoints = 0;
  cartRows.value.forEach((row: any) => {
    const subtotal = Number(row.price) * row.count;
    totalSubtotal += subtotal;
    const finalPrice = subtotal * discountRate;
    row.finalPrice = finalPrice.toFixed(2);
    totalFinalPrice += finalPrice;
    totalEarnedPoints += Math.floor(finalPrice);
  });
  orderSummary.value = {
    ...orderSummary.value,
    totalSubtotal: totalSubtotal.toFixed(2),
    totalFinalPrice: totalFinalPrice.toFixed(2),
    totalDiscountAmount: (totalSubtotal - totalFinalPrice).toFixed(2),
    totalEarnedPoints,
  };
};

const decQty = (item: any) => {
  const c = Number(item.count);
  if (c <= 1) {
    removeLine(item);
    return;
  }
  const fd = new FormData();
  fd.append('id', String(item.id));
  fd.append('itemCount', String(c - 1));
  updateCartCountApi(fd)
    .then(() => {
      item.count = c - 1;
      recalcOrderSummary();
      cartStore.refreshCount();
    })
    .catch((err: any) => {
      message.error(err.msg || '更新失败');
    });
};

const incQty = (item: any) => {
  const c = Number(item.count);
  if (c >= 99) {
    message.warn('单件最多购买 99 件');
    return;
  }
  const fd = new FormData();
  fd.append('id', String(item.id));
  fd.append('itemCount', String(c + 1));
  updateCartCountApi(fd)
    .then(() => {
      item.count = c + 1;
      recalcOrderSummary();
      cartStore.refreshCount();
    })
    .catch((err: any) => {
      message.error(err.msg || '更新失败');
    });
};

const removeLine = (item: any) => {
  removeCartApi({ id: item.id })
    .then(() => {
      message.success('已删除');
      loadCart();
      cartStore.refreshCount();
    })
    .catch((err: any) => {
      message.error(err.msg || '删除失败');
    });
};

const listAddressData = () => {
  const userId = userStore.user_id;
  if (!userId) return;
  listAddressListApi({ userId }).then((res: any) => {
    if (res.data?.length > 0) {
      pageData.receiverName = res.data[0].name;
      pageData.receiverPhone = res.data[0].mobile;
      pageData.receiverAddress = res.data[0].description;
      res.data.forEach((item: any) => {
        if (item.default) {
          pageData.receiverName = item.name;
          pageData.receiverPhone = item.mobile;
          pageData.receiverAddress = item.description;
        }
      });
    }
  });
};

const handleAdd = () => {
  modal.visile = true;
  modal.title = '新增地址';
  modal.form.name = undefined;
  modal.form.mobile = undefined;
  modal.form.desc = undefined;
  modal.form.default = false;
};

const handleOk = () => {
  if (!userStore.user_id) {
    message.warn('请先登录');
    return;
  }
  myform.value
    ?.validate()
    .then(() => {
      const formData = new FormData();
      formData.append('userId', String(userStore.user_id));
      formData.append('def', modal.form.default ? '1' : '0');
      if (modal.form.name) formData.append('name', modal.form.name);
      if (modal.form.mobile) formData.append('mobile', modal.form.mobile);
      if (modal.form.desc) formData.append('description', modal.form.desc);
      createAddressApi(formData)
        .then(() => {
          modal.visile = false;
          pageData.receiverName = modal.form.name;
          pageData.receiverAddress = modal.form.desc;
          pageData.receiverPhone = modal.form.mobile;
          listAddressData();
        })
        .catch((err: any) => {
          message.error(err.msg || '新建失败');
        });
    })
    .catch(() => {});
};

const handleCancel = () => {
  modal.visile = false;
};

const handleBack = () => {
  router.back();
};

const handleJiesuan = async () => {
  const userId = userStore.user_id;
  if (!userId) {
    message.warn('请先登录！');
    return;
  }
  if (!cartRows.value.length) {
    message.warn('购物车为空');
    return;
  }
  if (!pageData.receiverName) {
    message.warn('请先填写收货地址！');
    return;
  }
  try {
    const formData = new FormData();
    formData.append('userId', String(userId));

    const items = cartRows.value.map(item => ({
      thingId: String(item.thingId),
      count: String(item.count),
      remark: pageData.remark || ''
    }));
    formData.append('items', JSON.stringify(items));

    formData.append('receiverName', pageData.receiverName!);
    formData.append('receiverPhone', pageData.receiverPhone!);
    formData.append('receiverAddress', pageData.receiverAddress!);

    if (redeemPointsInput.value > 0) {
      formData.append('redeemPoints', String(redeemPointsInput.value));
    }

    const res = await createApi(formData);
    await clearCartApi({ userId: String(userId) });
    await cartStore.refreshCount();
    message.success('订单创建成功，请支付');
    const orderNumber = res.data?.orderNumber || Date.now().toString();
    router.push({ name: 'pay', query: { amount: finalPayment.value, orderNumber: orderNumber } });
  } catch (e: any) {
    message.error(e.msg || e.message || '下单失败');
  }
};
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
.cart-page-wrapper {
  min-height: 100vh;
  background: linear-gradient(180deg, @bg-gradient-start 0%, @bg-gradient-end 100%);
}

.cart-content {
  width: 1100px;
  margin: 0 auto;
  padding: 100px 16px 60px;
}

/* ==================== 页面标题 ==================== */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: @text-dark;
    margin: 0;
  }

  .item-count {
    font-size: 14px;
    color: @text-muted;
    background: rgba(59, 130, 246, 0.1);
    padding: 4px 12px;
    border-radius: 20px;
  }
}

/* ==================== 空购物车 ==================== */
.empty-cart {
  text-align: center;
  padding: 80px 20px;
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: @radius-lg;
  box-shadow: @glass-shadow;

  .empty-icon {
    font-size: 64px;
    margin-bottom: 20px;
  }

  .empty-title {
    font-size: 20px;
    font-weight: 600;
    color: @text-dark;
    margin: 0 0 8px;
  }

  .empty-text {
    font-size: 14px;
    color: @text-muted;
    margin: 0 0 24px;
  }

  .btn-primary {
    background: linear-gradient(135deg, @primary-blue 0%, @primary-blue-dark 100%);
    border: none;
    border-radius: @radius-md;
    color: white;
    font-size: 15px;
    font-weight: 600;
    padding: 12px 32px;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
    }
  }
}

/* ==================== 购物车主内容 ==================== */
.cart-main {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

/* ==================== 商品列表区 ==================== */
.cart-items-section {
  background: @glass-bg;
  backdrop-filter: blur(20px);
  border: 1px solid @glass-border;
  border-radius: @radius-lg;
  box-shadow: @glass-shadow;
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.12);
  font-size: 13px;
  font-weight: 600;
  color: @text-muted;
  text-transform: uppercase;
  letter-spacing: 0.5px;

  .header-label {
    flex: 1;
  }

  .header-unit {
    width: 90px;
    text-align: center;
  }

  .header-action {
    width: 50px;
    text-align: center;
  }
}

.items-list {
  .cart-item {
    display: flex;
    align-items: center;
    padding: 20px 0;
    border-bottom: 1px dashed rgba(59, 130, 246, 0.12);
    transition: background 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: rgba(59, 130, 246, 0.02);
    }
  }
}

.item-product {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;

  .product-image {
    width: 72px;
    height: 72px;
    border-radius: 12px;
    overflow: hidden;
    background: white;
    flex-shrink: 0;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .product-info {
    .product-name {
      font-size: 14px;
      font-weight: 500;
      color: @text-dark;
      margin: 0;
      line-height: 1.4;
    }
  }
}

.item-price {
  width: 90px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;

  &.origin {
    color: #999;
    text-decoration: line-through;
  }

  &.discounted {
    color: #ff6600;
    font-weight: 700;
  }
}

.item-quantity {
  width: 90px;
  text-align: center;
}

.quantity-control {
  display: inline-flex;
  align-items: center;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;

  .qty-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #152844;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }
  }

  .qty-value {
    width: 36px;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    color: #152844;
  }
}

.item-actions {
  width: 50px;
  text-align: center;
}

.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  opacity: 0.5;
  transition: all 0.2s;

  &:hover {
    opacity: 1;
    transform: scale(1.1);
  }
}

/* ==================== 备注区域 ==================== */
.remark-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid rgba(59, 130, 246, 0.12);

  .remark-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: #909090;
    margin-bottom: 8px;
  }

  .remark-input {
    width: 100%;
    background: white;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    padding: 12px 14px;
    font-size: 14px;
    color: #152844;
    resize: none;
    height: 60px;
    transition: all 0.2s;

    &::placeholder {
      color: #c0c0c0;
    }

    &:focus {
      outline: none;
      border-color: @primary-blue;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
    }
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

/* 收货地址 */
.address-block {
  background: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;

  .block-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .block-icon {
      font-size: 16px;
    }

    .block-title {
      font-size: 13px;
      font-weight: 600;
      color: #909090;
    }
  }

  .address-info {
    .receiver {
      font-size: 15px;
      font-weight: 600;
      color: #152844;
      margin: 0 0 4px;
    }

    .phone {
      font-size: 13px;
      color: #666;
      margin: 0 0 4px;
    }

    .address-text {
      font-size: 13px;
      color: #666;
      margin: 0;
    }
  }

  .no-address {
    font-size: 13px;
    color: #909090;

    .link {
      color: @primary-blue;
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}

/* 价格明细 */
.price-block {
  border-top: 1px solid rgba(59, 130, 246, 0.12);
  padding-top: 16px;
  margin-bottom: 20px;

  .price-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
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

  .price-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 12px;
    border-top: 1px solid rgba(59, 130, 246, 0.12);
    margin-top: 8px;

    .total-label {
      font-size: 14px;
      font-weight: 600;
      color: #152844;
    }

    .total-value {
      font-size: 24px;
      font-weight: 700;
      color: #ff6600;
    }
  }
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;

  .btn-back {
    flex: 1;
    height: 48px;
    background: white;
    border: 1px solid @primary-blue;
    border-radius: @radius-md;
    font-size: 15px;
    font-weight: 600;
    color: @primary-blue;
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      background: rgba(59, 130, 246, 0.05);
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
  .cart-main {
    grid-template-columns: 1fr;
  }

  .order-summary-section {
    position: static;
  }
}

@media (max-width: 600px) {
  .cart-content {
    padding: 80px 12px 40px;
  }

  .section-header {
    display: none;
  }

  .cart-item {
    flex-wrap: wrap;

    .item-product {
      width: 100%;
      margin-bottom: 12px;
    }

    .item-price {
      width: 50%;
      text-align: left;
      padding-left: 88px;
    }

    .item-quantity {
      width: 50%;
    }

    .item-actions {
      position: absolute;
      right: 0;
      top: 20px;
    }
  }
}
</style>
