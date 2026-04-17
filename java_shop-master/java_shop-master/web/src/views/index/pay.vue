// pay.vue

<template>
  <div>
    <Header/>
    <div class="pay-content">
      <div class="title">订单提交成功</div>
      <div class="price">实付金额：<span class="num">¥{{ amount }}</span></div>
      <div class="pay-choose-view" style="">
        <button class="pay-btn pay-btn-active" @click="handlePay()">查看订单</button>
      </div>
    </div>
  </div>

</template>

<script setup>
import Header from '/@/views/index/components/header.vue'
import {message} from "ant-design-vue";
import WxPayIcon from '/@/assets/images/wx-pay-icon.svg';
import AliPayIcon from '/@/assets/images/ali-pay-icon.svg';

import {useUserStore} from "/@/store";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

let ddlTime = ref()
let amount = ref()

onMounted(() => {
  amount.value = route.query.amount
  ddlTime.value = formatDate(new Date().getTime(), 'YY-MM-DD hh:mm:ss')
})

const handlePay = () => {
  router.push({name: 'orderView'})
}
const formatDate = (time, format = 'YY-MM-DD hh:mm:ss') => {
  const date = new Date(time)

  const year = date.getFullYear(),
      month = date.getMonth() + 1,
      day = date.getDate() + 1,
      hour = date.getHours(),
      min = date.getMinutes(),
      sec = date.getSeconds()
  const preArr = Array.apply(null, Array(10)).map(function (elem, index) {
    return '0' + index
  })

  const newTime = format.replace(/YY/g, year)
      .replace(/MM/g, preArr[month] || month)
      .replace(/DD/g, preArr[day] || day)
      .replace(/hh/g, preArr[hour] || hour)
      .replace(/mm/g, preArr[min] || min)
      .replace(/ss/g, preArr[sec] || sec)

  return newTime
}

</script>

<style scoped lang="less">
.flex-view {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
}

.pay-content {
  position: relative;
  margin: 120px auto 0;
  width: 500px;
  background: @white;
  overflow: hidden;
  border-radius: @radius-xl;
  box-shadow: @shadow-card;
  padding: 60px 20px;

  .title {
    color: @navy-dark;
    font-weight: 600;
    font-size: @font-size-3xl;
    line-height: 32px;
    text-align: center;
    margin-bottom: 16px;
  }

  .time-margin {
    margin: 11px 0 24px;
  }

  .text {
    height: 22px;
    line-height: 22px;
    font-size: 14px;
    text-align: center;
    color: #152844;
  }

  .time {
    color: #f62a2a;
  }

  .text {
    height: 22px;
    line-height: 22px;
    font-size: 14px;
    text-align: center;
    color: #152844;
  }

  .price {
    color: @text-secondary;
    font-weight: 500;
    font-size: @font-size-xl;
    height: 36px;
    line-height: 36px;
    text-align: center;
    margin-bottom: 32px;

    .num {
      color: @primary-blue;
      font-weight: 600;
      font-size: @font-size-2xl;
    }
  }

  .pay-choose-view {
    margin-top: 24px;

    .choose-box {
      width: 140px;
      height: 126px;
      border: 1px solid #cedce4;
      border-radius: 4px;
      text-align: center;
      cursor: pointer;
    }

    .pay-choose-box {
      -webkit-box-pack: justify;
      -ms-flex-pack: justify;
      justify-content: space-between;
      max-width: 300px;
      margin: 0 auto;

      img {
        height: 40px;
        margin: 24px auto 16px;
        display: block;
      }
    }

    .tips {
      color: #6f6f6f;
      font-size: 14px;
      line-height: 22px;
      height: 22px;
      text-align: center;
      margin: 16px 0 24px;
    }

    .choose-box-active {
      border: 1px solid #4684e2;
    }

    .tips {
      color: #6f6f6f;
      font-size: 14px;
      line-height: 22px;
      height: 22px;
      text-align: center;
      margin: 16px 0 24px;
    }

    .pay-btn {
      cursor: pointer;
      background: @primary-blue;
      border-radius: @radius-full;
      width: 140px;
      height: 44px;
      line-height: 44px;
      border: none;
      outline: none;
      font-size: @font-size-base;
      font-weight: 500;
      color: @white;
      text-align: center;
      display: block;
      margin: 0 auto;
      box-shadow: @shadow-button;
      transition: all @transition-fast;

      &:hover {
        background: @primary-blue-hover;
        box-shadow: @shadow-button-hover;
        transform: translateY(-2px);
      }

      &:active {
        background: @primary-blue-active;
        transform: translateY(0);
        box-shadow: @shadow-xs;
      }
    }

    .pay-btn-active {
      background: #4684e2;
    }
  }
}

</style>