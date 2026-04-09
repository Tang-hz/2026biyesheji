<template>
  <div class="main-bar-view">
    <div class="logo">
      <img :src="logoImage" class="search-icon" @click="$router.push({name:'portal'})">
    </div>
    <div class="search-entry">
      <img :src="SearchIcon" class="search-icon">
      <input placeholder="输入商品名称" ref="keywordRef" @keyup.enter="search" />
    </div>
    <div class="right-view">
      <template v-if="userStore.user_token">
        <a-dropdown>
          <a class="ant-dropdown-link" @click="e => e.preventDefault()">
            <img :src="AvatarIcon" class="self-img" >
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item>
                <a @click="goUserCenter('orderView')">订单中心</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="goUserCenter('userInfoEditView')">个人设置</a>
              </a-menu-item>
              <a-menu-item>
                <a @click="quit()">退出</a>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <!--        <div class="right-icon">-->
        <!--          <img src="@/assets/cart-icon.svg">-->
        <!--          <span>3</span>-->
        <!--        </div>-->
      </template>
      <template v-else>
        <button class="login btn hidden-sm" @click="goLogin()">登录</button>
      </template>

      <div class="right-icon" @click="msgVisible=true">
        <img :src="MessageIcon">
        <span class="msg-point" style=""></span>
      </div>
      <div>
        <a-drawer
            title="我的消息"
            placement="right"
            :closable="true"
            :maskClosable="true"
            :visible="msgVisible"
            @close="onClose"
        >
          <a-spin :spinning="loading" style="min-height: 200px;">
            <div class="list-content">
              <div class="notification-view">
                <div class="list">
                  <div class="notification-item flex-view" v-for="item in msgData">
                    <!---->
                    <div class="content-box">
                      <div class="header">
                        <span class="title-txt">{{item.title}}</span>
                        <br/>
                        <span class="time">{{ item.create_time }}</span>
                      </div>
                      <div class="head-text">
                      </div>
                      <div class="content">
                        <p>{{ item.content }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </a-spin>
        </a-drawer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {listApi} from '/@/api/notice'
import {useUserStore} from "/@/store";
import logoImage from '/@/assets/images/k-logo.png';
import SearchIcon from '/@/assets/images/search-icon.svg';
import AvatarIcon from '/@/assets/images/avatar.jpg';
import MessageIcon from '/@/assets/images/message-icon.svg';


const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const keywordRef = ref()

let loading = ref(false)
let msgVisible = ref(false)
let msgData = ref([] as any)

onMounted(()=>{
  getMessageList()
})

const getMessageList = ()=> {
  loading.value = true
  listApi({}).then(res => {
    msgData.value = res.data
    loading.value = false
  }).catch(err => {
    console.log(err)
    loading.value = false
  })
}
const search = () => {
  const keyword = keywordRef.value.value
  if (route.name === 'search') {
    router.push({name: 'search', query: {keyword: keyword}})
  } else {
    let text = router.resolve({name: 'search', query: {keyword: keyword}})
    window.open(text.href, '_blank')
  }
}
const goLogin = () => {
  router.push({name: 'login'})
}

const goUserCenter = (menuName) => {
  router.push({name: menuName})
}
const quit= () => {
  userStore.logout().then(res => {
    router.push({name: 'portal'})
  })
}
const onClose = () => {
  msgVisible.value = false;
}

</script>

<style scoped lang="less">
.main-bar-view {
  position: fixed;
  top: 0;
  left: 0;
  height: 64px;
  width: 100%;
  background: @white;
  border-bottom: 1px solid @border-light;
  padding-left: 48px;
  z-index: 16;
  display: flex;
  flex-direction: row;
  align-items: center;
  box-shadow: @shadow-xs;
}

.logo {
  margin-right: 24px;
  img {
    width: 36px;
    height: 36px;
    cursor: pointer;
    transition: transform @transition-fast;
    &:hover {
      transform: scale(1.05);
    }
  }
}

.search-entry {
  position: relative;
  width: 420px;
  min-width: 200px;
  height: 40px;
  background: @bg-input;
  padding: 0 16px;
  border-radius: @radius-full;
  font-size: 0;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all @transition-base;

  img {
    max-width: 100%;
    height: auto;
  }
  .search-icon {
    width: 18px;
    margin: 11px 10px 0 0;
    opacity: 0.6;
    transition: opacity @transition-fast;
  }
  input {
    position: absolute;
    top: 8px;
    width: 85%;
    height: 24px;
    border: 0px;
    outline: none;
    color: @text-primary;
    background: transparent;
    font-size: 14px;

    &::placeholder {
      color: @text-hint;
    }
  }

  &:hover {
    background: @bg-hover;
    border-color: @border-light;

    .search-icon {
      opacity: 0.8;
    }
  }

  &:focus-within {
    background: @white;
    border-color: @primary-blue;
    box-shadow: 0 0 0 3px @primary-blue-subtle, @shadow-sm;

    .search-icon {
      opacity: 1;
    }
  }
}

.right-view {
  padding-right: 36px;
  flex: 1;
  display: flex;
  flex-direction: row;
  gap: 20px;
  justify-content: flex-end;
  align-items: center;

  .username {
    height: 32px;
    line-height: 32px;
    text-align: center;
  }
  button {
    outline: none;
    border: none;
    cursor: pointer;
  }
  img {
    cursor: pointer;
    transition: opacity @transition-fast;
    &:hover {
      opacity: 0.8;
    }
  }
  .right-icon {
    position: relative;
    width: 24px;
    margin: 4px 0 0 4px;
    cursor: pointer;
    display: inline-block;
    font-size: 0;

    span {
      position: absolute;
      right: -12px;
      top: -3px;
      font-size: 11px;
      color: @white;
      background: @primary-blue;
      border-radius: @radius-full;
      padding: 0 5px;
      height: 16px;
      line-height: 16px;
      font-weight: 600;
      min-width: 18px;
      text-align: center;
      box-shadow: 0 1px 3px rgba(70, 132, 226, 0.4);
    }
    .msg-point {
      position: absolute;
      right: -4px;
      top: 0;
      min-width: 8px;
      width: 8px;
      height: 8px;
      background: @primary-blue;
      border-radius: 50%;
      box-shadow: 0 0 0 2px @white;
    }
  }

  .self-img {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    vertical-align: middle;
    cursor: pointer;
    border: 2px solid @border-light;
    transition: border-color @transition-fast, box-shadow @transition-fast;

    &:hover {
      border-color: @primary-blue;
      box-shadow: 0 0 0 3px @primary-blue-subtle;
    }
  }
  .btn {
    background: @primary-blue;
    font-size: 14px;
    color: @white;
    border-radius: @radius-full;
    text-align: center;
    width: 72px;
    height: 34px;
    line-height: 34px;
    vertical-align: middle;
    margin-left: 32px;
    font-weight: 500;
    box-shadow: @shadow-button;
    transition: all @transition-fast;

    &:hover {
      background: @primary-blue-hover;
      box-shadow: @shadow-button-hover;
      transform: translateY(-1px);
    }

    &:active {
      background: @primary-blue-active;
      transform: translateY(0);
      box-shadow: @shadow-xs;
    }
  }
}

.content-list {
  flex: 1;

  .list-title {
    color: #152844;
    font-weight: 600;
    font-size: 18px;
    //line-height: 24px;
    height: 48px;
    margin-bottom: 4px;
    border-bottom: 1px solid #cedce4;
  }
}

.notification-item {
  padding-top: 16px;

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    margin-right: 8px;
  }

  .content-box {
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    border-bottom: 1px dashed #e9e9e9;
    padding: 4px 0 16px;
  }

  .header {
    margin-bottom: 12px;
  }

  .title-txt {
    color: #315c9e;
    font-weight: 500;
    font-size: 14px;
  }

  .time {
    color: #a1adc5;
    font-size: 14px;
  }

  .head-text {
    color: #152844;
    font-weight: 500;
    font-size: 14px;
    line-height: 22px;

    .name {
      margin-right: 8px;
    }
  }

  .content {
    margin-top: 4px;
    color: #484848;
    font-size: 14px;
    line-height: 22px;
  }

}

</style>
