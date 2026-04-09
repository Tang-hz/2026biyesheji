<template>
  <div>
    <Header/>
    <section class="cart-page flex-view">
      <div class="left-flex">
        <div class="title flex-view">
          <h3>订单明细</h3>
        </div>
        <div class="cart-list-view">
          <div class="list-th flex-view">
            <span class="line-1">商品名称</span>
            <span class="line-2">单价</span>
            <span class="line-3">折扣价</span>
            <span class="line-5">数量</span>
          </div>
          <div class="list">
            <div class="items flex-view">
              <div class="book flex-view" @click="openDetail">
                <img :src="pageData.cover">
                <h2>{{ pageData.title }}</h2>
              </div>
              <div class="pay origin">¥{{ memberPrice.price }}</div>
              <div class="pay discounted">¥{{ itemFinalPrice }}</div>
              <a-input-number v-model:value="pageData.count" :min="1" :max="10" @change="onCountChange"/>
            </div>
          </div>
        </div>
        <div class="title flex-view">
          <h3>备注</h3>
        </div>
        <textarea v-model="pageData.remark" placeholder="输入备注信息，100字以内" class="remark">
    </textarea>
      </div>
      <div class="right-flex">
        <div class="title flex-view">
          <h3>收货地址</h3>
        </div>
        <div class="address-view">
          <div class="info" style="">
            <span>收件人：</span>
            <span class="name">{{ pageData.receiverName }}
          </span>
            <span class="tel">{{ pageData.receiverPhone }}
          </span>
          </div>
          <div class="address" v-if="pageData.receiverAddress"> {{ pageData.receiverAddress }}</div>
          <div class="info" v-else>
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
            <div class="price-txt">¥{{ memberPrice.subtotal }}</div>
          </div>
          <div class="price-item flex-view">
            <div class="item-name">{{ memberPrice.memberLevelName }}折扣</div>
            <div class="price-txt discount">-¥{{ memberPrice.discountAmount }}</div>
          </div>
          <div class="price-item flex-view">
            <div class="item-name">积分抵扣</div>
            <div class="price-txt redeem">
              <span class="redeem-input">
                <input
                  type="number"
                  v-model.number="redeemPointsInput"
                  :max="memberPrice.maxRedeemPoints || 0"
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
            <div class="price-txt">≈{{ memberPrice.earnedPoints }}积分</div>
          </div>
          <div class="total-price-view flex-view">
            <span>合计（实付）</span>
            <div class="price">
              <span class="font-big">¥{{ finalPayment }}</span>
            </div>
          </div>
          <div class="btns-view">
            <button class="btn buy" @click="handleBack()">返回</button>
            <button class="btn pay jiesuan" @click="handleJiesuan()">提交</button>
          </div>
        </div>
      </div>
    </section>

    <!--选择弹窗区域-->
    <div>
      <a-modal
          :visible="modal.visile"
          :forceRender="true"
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
            <a-col span="24">
              <a-form-item label="姓名" name="name">
                <a-input placeholder="请输入" v-model:value="modal.form.name"></a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col span="24">
              <a-form-item label="电话号码" name="mobile">
                <a-input placeholder="请输入" v-model:value="modal.form.mobile"></a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col span="24">
              <a-form-item label="送货地址" name="desc">
                <a-input placeholder="请输入" v-model:value="modal.form.desc"></a-input>
              </a-form-item>
            </a-col>
          </a-row>
          <a-row :gutter="24">
            <a-col span="24">
              <a-form-item label="默认地址">
                <a-switch v-model:checked="modal.form.default"></a-switch>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-modal>
    </div>
  </div>
</template>

<script setup lang="ts">
import {message} from "ant-design-vue";
import Header from '/@/views/index/components/header.vue'
import Footer from '/@/views/index/components/footer.vue'
import DeleteIcon from '/@/assets/images/delete-icon.svg'
import {createApi} from '/@/api/order'
import {listApi as listAddressListApi, createApi as createAddressApi} from '/@/api/address'
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

// 弹窗数据
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

.flex-view {
  display: flex;
}

.cart-page {
  width: 1024px;
  min-height: 50vh;
  margin: 100px auto;
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
      width: 80px;
      margin-right: 0;
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
}

.remark {
  width: 100%;
  background: #f6f9fb;
  border: 0;
  border-radius: 4px;
  padding: 6px 12px;
  margin-top: 16px;
  resize: none;
  height: 56px;
  line-height: 22px;
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
      color: #ff6600;
      font-size: 16px;
      height: 36px;
      line-height: 36px;
    }
  }

  .btns-view {
    margin-top: 24px;
    text-align: right;

    .buy {
      background: #fff;
      color: #4684e2;
      border: 1px solid #4684e2;
    }

    .jiesuan {
      cursor: pointer;
      background: #4684e2;
      color: #fff;
    }

    .btn {
      cursor: pointer;
      width: 96px;
      height: 36px;
      line-height: 33px;
      margin-left: 16px;
      text-align: center;
      border-radius: 32px;
      font-size: 16px;
      outline: none;
    }
  }
}

</style>
