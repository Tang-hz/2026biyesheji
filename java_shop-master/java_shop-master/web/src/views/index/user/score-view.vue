<template>
  <div class="content-list">
    <div class="list-title">我的积分</div>
    <div class="my-score-view">
      <div class="score-card">
        <div class="score-balance">
          <span class="label">积分余额</span>
          <span class="value">{{score}}</span>
        </div>
        <div class="sign-btn-wrap" v-if="!hasSignedToday">
          <button class="sign-btn" @click="handleSign">每日签到</button>
        </div>
        <div class="signed-tip" v-else>
          今日已签到，连续签到{{consecutiveDays}}天
        </div>
      </div>

      <div class="member-info" v-if="memberLevel > 1">
        <span class="member-name">{{memberLevelName}}</span>
        <span class="member-benefit">享受{{discountRate}}折优惠</span>
      </div>

      <div class="log-section">
        <div class="section-title">积分明细</div>
        <div class="log-list">
          <div class="log-item" v-for="item in pointsLog" :key="item.id">
            <div class="log-info">
              <span class="log-type">{{getTypeName(item.type)}}</span>
              <span class="log-remark">{{item.remark}}</span>
            </div>
            <div class="log-points" :class="item.points > 0 ? 'positive' : 'negative'">
              {{item.points > 0 ? '+' : ''}}{{item.points}}
            </div>
          </div>
          <div class="empty-tip" v-if="pointsLog.length === 0">
            暂无积分记录
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {message} from 'ant-design-vue';
import {detailApi} from '/@/api/user'
import {useUserStore} from "/@/store";
import {getPointsApi, signApi, getPointsLogApi, checkSignedApi} from '/@/api/points';

const userStore = useUserStore();

let score = ref(0)
let hasSignedToday = ref(false)
let consecutiveDays = ref(0)
let memberLevel = ref(1)
let memberLevelName = ref('普通会员')
let discountRate = ref(9.8)
let pointsLog = ref([])

onMounted(()=>{
  getUserInfo()
  checkSigned()
  getPointsLog()
})

const getUserInfo =()=> {
  let userId = userStore.user_id
  detailApi({userId: userId}).then(res => {
    if (res.data) {
      score.value = res.data.score || 0
      memberLevel.value = res.data.memberLevel || 1
      memberLevelName.value = getMemberLevelName(memberLevel.value)
      discountRate.value = getDiscountRate(memberLevel.value)
    }
  }).catch(err => {
    console.log(err)
  })
}

const checkSigned = () => {
  let userId = userStore.user_id
  checkSignedApi({userId: userId}).then(res => {
    if (res.data) {
      hasSignedToday.value = res.data.signed
      consecutiveDays.value = res.data.consecutiveDays || 0
    }
  }).catch(err => {
    console.log(err)
  })
}

const getPointsLog = () => {
  let userId = userStore.user_id
  getPointsLogApi({userId: userId}).then(res => {
    if (res.data) {
      pointsLog.value = res.data
    }
  }).catch(err => {
    console.log(err)
  })
}

const handleSign = () => {
  let userId = userStore.user_id
  signApi({userId: userId}).then(res => {
    if (res.code === 200) {
      score.value = res.data.totalPoints
      hasSignedToday.value = true
      consecutiveDays.value = consecutiveDays.value + 1
      getPointsLog()
      message.success('签到成功！获得 ' + res.data.earnedPoints + ' 积分')
    } else {
      message.error(res.msg || '签到失败')
    }
  }).catch(err => {
    console.log(err)
  })
}

const getMemberLevelName = (level) => {
  const names = {1: '普通会员', 2: '白银会员', 3: '黄金会员', 4: '钻石会员'}
  return names[level] || '普通会员'
}

const getDiscountRate = (level) => {
  const rates = {1: 9.8, 2: 9.5, 3: 9.0, 4: 8.5}
  return rates[level] || 9.8
}

const getTypeName = (type) => {
  const names = {
    'ORDER': '购物获得',
    'EVAL': '评价晒单',
    'SIGN': '每日签到',
    'REDEEM': '积分抵扣'
  }
  return names[type] || type
}
</script>
<style scoped lang="less">
.flex-view {
  display: flex;
}

.content-list {
  flex: 1;

  .list-title {
    color: #152844;
    font-weight: 600;
    font-size: 18px;
    height: 48px;
    margin-bottom: 4px;
    border-bottom: 1px solid #cedce4;
  }
}

.my-score-view {
  margin-top: 16px;
}

.score-card {
  background: #4684e2;
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  margin-bottom: 16px;

  .score-balance {
    display: flex;
    flex-direction: column;

    .label {
      font-size: 14px;
      opacity: 0.9;
    }

    .value {
      font-size: 32px;
      font-weight: 700;
      margin-top: 8px;
    }
  }

  .sign-btn-wrap {
    margin-top: 16px;

    .sign-btn {
      background: rgba(255,255,255,0.2);
      border: 1px solid rgba(255,255,255,0.3);
      color: #fff;
      padding: 8px 24px;
      border-radius: 20px;
      cursor: pointer;
      font-size: 14px;

      &:hover {
        background: rgba(255,255,255,0.3);
      }
    }
  }

  .signed-tip {
    margin-top: 16px;
    font-size: 14px;
    opacity: 0.9;
  }
}

.member-info {
  background: #fff3e0;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;

  .member-name {
    color: #e65100;
    font-weight: 600;
    margin-right: 12px;
  }

  .member-benefit {
    color: #ff9800;
    font-size: 14px;
  }
}

.log-section {
  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #152844;
    margin-bottom: 12px;
    padding-bottom: 8px;
    border-bottom: 1px solid #e8e8e8;
  }

  .log-list {
    .log-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .log-info {
        display: flex;
        flex-direction: column;

        .log-type {
          font-size: 14px;
          color: #333;
        }

        .log-remark {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }

      .log-points {
        font-size: 16px;
        font-weight: 600;

        &.positive {
          color: #52c41a;
        }

        &.negative {
          color: #ff4d4f;
        }
      }
    }

    .empty-tip {
      text-align: center;
      color: #999;
      padding: 20px 0;
      font-size: 14px;
    }
  }
}
</style>
