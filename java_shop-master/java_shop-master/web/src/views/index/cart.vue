<template>
  <div>
    <Header />
    <section class="cart-page flex-view">
      <div class="left-flex">
        <div class="title flex-view">
          <h3>购物车</h3>
        </div>
        <div v-if="!cartRows.length" class="empty-hint">购物车暂无商品，去首页逛逛吧</div>
        <div v-else class="cart-list-view">
          <div class="list-th flex-view">
            <span class="line-1">商品名称</span>
            <span class="line-2">单价</span>
            <span class="line-3">折扣价</span>
            <span class="line-5">数量</span>
            <span class="line-6">操作</span>
          </div>
          <div class="list">
            <div v-for="item in cartRows" :key="item.id" class="items flex-view">
              <div class="book flex-view" @click="openDetail(item)">
                <img :src="item.cover" alt="" />
                <h2>{{ item.title }}</h2>
              </div>
              <div class="pay origin">¥{{ item.price }}</div>
              <div class="pay discounted">¥{{ item.finalPrice }}</div>
              <div class="num-box flex-view">
                <span class="num-btn" @click="decQty(item)">−</span>
                <span class="num-val">{{ item.count }}</span>
                <span class="num-btn" @click="incQty(item)">+</span>
              </div>
              <img :src="DeleteIcon" class="delete" alt="删除" @click="removeLine(item)" />
            </div>
          </div>
        </div>
        <div class="title flex-view">
          <h3>备注</h3>
        </div>
        <textarea
          v-model="pageData.remark"
          placeholder="输入备注信息，100字以内"
          class="remark"
        ></textarea>
      </div>
      <div class="right-flex">
        <div class="title flex-view">
          <h3>收货地址</h3>
        </div>
        <div class="address-view">
          <div class="info">
            <span>收件人：</span>
            <span class="name">{{ pageData.receiverName }}</span>
            <span class="tel">{{ pageData.receiverPhone }}</span>
          </div>
          <div v-if="pageData.receiverAddress" class="address">{{ pageData.receiverAddress }}</div>
          <div v-else class="info">
            <span>目前暂无地址信息，请</span>
            <span class="info-blue" @click="handleAdd">新建地址</span>
          </div>
        </div>
        <div class="title flex-view">
          <h3>结算</h3>
          <span class="click-txt">价格</span>
        </div>
        <div class="price-view">
          <div class="price-item flex-view">
            <div class="item-name">商品总价（原价）</div>
            <div class="price-txt">¥{{ orderSummary.totalSubtotal || '0.00' }}</div>
          </div>
          <div class="price-item flex-view">
            <div class="item-name">{{ orderSummary.memberLevelName || '会员' }}折扣</div>
            <div class="price-txt discount">-¥{{ orderSummary.totalDiscountAmount || '0.00' }}</div>
          </div>
          <div class="price-item flex-view">
            <div class="item-name">积分抵扣</div>
            <div class="price-txt redeem">
              <span class="redeem-input">
                <input
                  type="number"
                  v-model.number="redeemPointsInput"
                  :max="orderSummary.maxRedeemPoints || 0"
                  min="0"
                  placeholder="0"
                  @input="onRedeemInput"
                />
                <span>积分</span>
              </span>
              <span class="redeem-money">抵¥{{ redeemMoneyDisplay }}</span>
            </div>
          </div>
          <div class="price-item flex-view">
            <div class="item-name">本单可获积分</div>
            <div class="price-txt">≈{{ orderSummary.totalEarnedPoints || 0 }}积分</div>
          </div>
          <div class="total-price-view flex-view">
            <span>合计（实付）</span>
            <div class="price">
              <span class="font-big">¥{{ finalPayment }}</span>
            </div>
          </div>
          <div class="btns-view">
            <button type="button" class="btn buy" @click="handleBack">返回</button>
            <button type="button" class="btn pay jiesuan" @click="handleJiesuan">提交订单</button>
          </div>
        </div>
      </div>
    </section>

    <a-modal
      :visible="modal.visile"
      :force-render="true"
      :title="modal.title"
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
        <a-row :gutter="24">
          <a-col :span="24">
            <a-form-item label="姓名" name="name">
              <a-input v-model:value="modal.form.name" placeholder="请输入" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="24">
            <a-form-item label="电话号码" name="mobile">
              <a-input v-model:value="modal.form.mobile" placeholder="请输入" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="24">
            <a-form-item label="送货地址" name="desc">
              <a-input v-model:value="modal.form.desc" placeholder="请输入" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :span="24">
            <a-form-item label="默认地址">
              <a-switch v-model:checked="modal.form.default" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';
import Header from '/@/views/index/components/header.vue';
import DeleteIcon from '/@/assets/images/delete-icon.svg';
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
        // finalPrice 由后端返回，若无则用原价
        if (!item.finalPrice) {
          item.finalPrice = (Number(item.price) * item.count).toFixed(2);
        }
      });
      cartRows.value = rows;
      // 从第一个元素取订单汇总信息
      if (rows.length > 0 && rows[0]._orderSummary) {
        orderSummary.value = rows[0]._orderSummary;
      } else {
        // 购物车为空时重置订单汇总
        orderSummary.value = {};
      }
      // 重置积分输入
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
    // 使用会员折扣率计算折后价
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
    // 积分抵扣分摊到每个商品（按折后价比例）
    const totalFinalPrice = Number(orderSummary.value.totalFinalPrice) || 0;
    const usedPoints = redeemPointsInput.value || 0;
    const usedMoney = usedPoints / 100;

    for (const item of cartRows.value) {
      const itemFinalPrice = Number(item.finalPrice) || 0;
      // 按商品折后价占整单比例分摊积分抵扣
      const itemRedeemMoney = totalFinalPrice > 0 ? (itemFinalPrice / totalFinalPrice) * usedMoney : 0;
      const itemRedeemPoints = Math.floor(itemRedeemMoney * 100);

      const formData = new FormData();
      formData.append('userId', String(userId));
      formData.append('thingId', String(item.thingId));
      formData.append('count', String(item.count));
      if (pageData.remark) {
        formData.append('remark', pageData.remark);
      }
      formData.append('receiverName', pageData.receiverName!);
      formData.append('receiverPhone', pageData.receiverPhone!);
      formData.append('receiverAddress', pageData.receiverAddress!);
      if (itemRedeemPoints > 0) {
        formData.append('redeemPoints', String(itemRedeemPoints));
      }
      await createApi(formData);
    }
    await clearCartApi({ userId: String(userId) });
    await cartStore.refreshCount();
    message.success('请支付订单');
    router.push({ name: 'pay', query: { amount: finalPayment.value } });
  } catch (e: any) {
    message.error(e.msg || e.message || '下单失败');
  }
};
</script>

<style scoped lang="less">
.flex-view {
  display: flex;
}

.cart-page {
  width: 1024px;
  min-height: 50vh;
  margin: 100px auto;
}

.empty-hint {
  color: #909090;
  padding: 24px 0 40px;
  font-size: 14px;
}

.left-flex {
  flex: 17;
  padding-right: 20px;
}

.title {
  justify-content: space-between;
  align-items: center;

  h3 {
    color: #152844;
    font-weight: 600;
    font-size: 18px;
    height: 26px;
    line-height: 26px;
    margin: 0;
  }
}

.cart-list-view {
  margin: 4px 0 40px;

  .list-th {
    height: 42px;
    line-height: 42px;
    border-bottom: 1px solid #cedce4;
    color: #152844;
    font-size: 14px;

    .line-1 {
      flex: 1;
      margin-right: 20px;
    }

    .line-2 {
      width: 65px;
      margin-right: 10px;
      text-decoration: line-through;
      color: #999;
    }

    .line-3 {
      width: 65px;
      margin-right: 10px;
      color: #ff6600;
      font-weight: 600;
    }

    .line-5 {
      width: 100px;
      margin-right: 40px;
    }

    .line-6 {
      width: 28px;
    }
  }
}

.items {
  align-items: center;
  margin-top: 20px;

  .book {
    flex: 1;
    align-items: center;
    margin-right: 20px;
    cursor: pointer;

    img {
      width: 48px;
      margin-right: 16px;
      border-radius: 4px;
    }

    h2 {
      flex: 1;
      font-size: 14px;
      line-height: 22px;
      color: #152844;
      font-weight: normal;
      margin: 0;
    }
  }

  .pay {
    font-weight: 600;
    font-size: 16px;
    width: 65px;
    margin-right: 10px;
  }

  .pay.origin {
    color: #999;
    text-decoration: line-through;
  }

  .pay.discounted {
    color: #ff6600;
    font-weight: 700;
  }

  .num-box {
    width: 100px;
    margin-right: 40px;
    border-radius: @radius-md;
    border: 1px solid @border-light;
    justify-content: space-between;
    align-items: center;
    height: 34px;
    padding: 0 8px;
    user-select: none;
    background: @white;
    transition: border-color @transition-fast;

    &:hover {
      border-color: @border-subtle;
    }
  }

  .num-btn {
    cursor: pointer;
    color: @primary-blue;
    font-size: 18px;
    line-height: 1;
    padding: 0 4px;
    transition: color @transition-fast;

    &:hover {
      color: @primary-blue-hover;
    }
  }

  .num-val {
    font-size: @font-size-base;
    color: @navy-dark;
    font-weight: 500;
    min-width: 24px;
    text-align: center;
  }

  .delete {
    width: 24px;
    cursor: pointer;
    opacity: 0.6;
    transition: opacity @transition-fast;

    &:hover {
      opacity: 1;
    }
  }
}

.remark {
  width: 100%;
  background: @bg-input;
  border: 1px solid @border-light;
  border-radius: @radius-md;
  padding: 10px 14px;
  margin-top: 16px;
  resize: none;
  height: 60px;
  line-height: 22px;
  font-size: @font-size-base;
  color: @text-primary;
  transition: all @transition-fast;

  &::placeholder {
    color: @text-hint;
  }

  &:focus {
    outline: none;
    border-color: @primary-blue;
    background: @white;
    box-shadow: 0 0 0 3px @primary-blue-subtle;
  }
}

.right-flex {
  flex: 8;
  padding-left: 24px;
  border-left: 1px solid #cedce4;
}

.click-txt {
  color: #4684e2;
  font-size: 14px;
  cursor: pointer;
}

.address-view {
  margin: 12px 0 24px;

  .info {
    color: #909090;
    font-size: 14px;

    .info-blue {
      cursor: pointer;
      color: #4684e2;
    }
  }

  .name {
    color: #152844;
    font-weight: 500;
  }

  .tel {
    color: #152844;
    float: right;
  }

  .address {
    color: #152844;
    margin-top: 4px;
  }
}

.price-view {
  overflow: hidden;
  margin-top: 16px;

  .price-item {
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    font-size: 14px;

    .item-name {
      color: #152844;
    }

    .price-txt {
      font-weight: 500;
      color: #ff8a00;
    }

    .price-txt.discount {
      color: #52c41a;
    }

    .price-txt.redeem {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 2px;
    }

    .redeem-input {
      display: flex;
      align-items: center;
      gap: 4px;

      input {
        width: 60px;
        height: 24px;
        border: 1px solid #ddd;
        border-radius: 4px;
        text-align: center;
        font-size: 12px;
        color: #152844;
        padding: 0 4px;

        &:focus {
          outline: none;
          border-color: #4684e2;
        }
      }

      span {
        color: #666;
        font-size: 12px;
      }
    }

    .redeem-money {
      color: #52c41a;
      font-size: 12px;
    }
  }

  .total-price-view {
    margin-top: 12px;
    border-top: 1px solid #cedce4;
    justify-content: space-between;
    align-items: flex-start;
    padding-top: 10px;
    color: #152844;
    font-weight: 500;

    .price {
      color: #ff8a00;
      font-size: 16px;
      height: 36px;
      line-height: 36px;
    }
  }

  .btns-view {
    margin-top: 24px;
    text-align: right;

    .buy {
      background: @white;
      color: @primary-blue;
      border: 1px solid @primary-blue;
    }

    .jiesuan {
      cursor: pointer;
      background: @primary-blue;
      color: @white;
      box-shadow: @shadow-button;
    }

    .btn {
      cursor: pointer;
      width: 96px;
      height: 36px;
      line-height: 33px;
      margin-left: 16px;
      text-align: center;
      border-radius: @radius-full;
      font-size: @font-size-base;
      font-weight: 500;
      outline: none;
      transition: all @transition-fast;

      &:hover {
        transform: translateY(-1px);
        box-shadow: @shadow-button-hover;
      }

      &:active {
        transform: translateY(0);
        box-shadow: @shadow-xs;
      }
    }
  }
}
</style>
