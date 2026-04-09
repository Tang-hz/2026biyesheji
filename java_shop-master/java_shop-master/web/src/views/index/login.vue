<template>
  <div class="container">
    <div class="login-page pc-style">
      <img :src="LogoIcon" alt="logo" class="logo-icon">
      <div class="login-tab">
        <div class="tab-selected">
          <span>用户名登录</span>
          <span class="tabline tabline-width"></span>
        </div>
      </div>
      <div class="mail-login" type="login">
        <div class="common-input">
          <img :src="MailIcon" class="left-icon">
          <div class="input-view">
            <input placeholder="请输入用户名" v-model="pageData.loginForm.username" type="text" class="input">
            <p class="err-view">
            </p>
          </div>
          <!---->
        </div>
        <div class="common-input">
          <img :src="PwdIcon" class="left-icon">
          <div class="input-view">
            <input placeholder="请输入密码" v-model="pageData.loginForm.password" type="password" class="input">
            <p class="err-view">
            </p>
          </div>
<!--          <img src="@/assets/pwd-hidden.svg" class="right-icon">-->
          <!---->
        </div>
        <div class="next-btn-view">
          <button class="next-btn btn-active" style="margin: 16px 0px;" @click="handleLogin">登录</button>
        </div>
      </div>
      <div class="operation">
        <a @click="handleCreateUser" class="forget-pwd" style="text-align: left;">注册新帐号</a>
        <a class="forget-pwd" style="text-align: right;">忘记密码？</a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useUserStore} from '/@/store';
import {message} from "ant-design-vue";
import LogoIcon from '/@/assets/images/k-logo.png';
import MailIcon from '/@/assets/images/mail-icon.svg';
import PwdIcon from '/@/assets/images/pwd-icon.svg';


const router = useRouter();
const userStore = useUserStore();

const pageData = reactive({
  loginForm: {
    username: '',
    password: ''
  }
})

const handleLogin = ()=> {
  userStore.login({
    username: pageData.loginForm.username,
    password: pageData.loginForm.password
  }).then(res=> {
    loginSuccess()
    console.log('success==>', userStore.user_name)
    console.log('success==>', userStore.user_id)
    console.log('success==>', userStore.user_token)
  }).catch(err => {
    message.warn(err.msg || '登录失败')
  })
}

const handleCreateUser = () => {
  router.push({name:'register'})
}

const loginSuccess= ()=> {
  router.push({ name: 'portal' })
  message.success('登录成功！')
}


</script>
<style scoped lang="less">
div {
  display: block;
}

.container {
  //background-color: #f1f1f1;
  background-image: url('../images/admin-login-bg.jpg');
  background-size: cover;
  object-fit: cover;
  height: 100%;
  max-width: 100%;
  display:flex;
  justify-content: center;
  align-items:center;
}

.new-content {
  position: absolute;
  left: 0;
  right: 0;
  margin: 80px auto 0;
  width: 980px;
}

.logo-img {
  width: 125px;
  display: block;
  margin-left: 137.5px;
}

.login-page {
  overflow: hidden;
  background: #fff;

  .logo-icon {
    margin-top: 20px;
    margin-left: 175px;
    width: 48px;
    height: 48px;
  }
}

.pc-style {
  position: relative;
  width: 400px;
  height: 480px;
  background: @white;
  border-radius: @radius-xl;
  box-shadow: @shadow-xl;
  overflow: hidden;
}

.login-tab {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  color: @text-primary;
  font-size: @font-size-base;
  font-weight: 500;
  height: 46px;
  line-height: 44px;
  margin-bottom: 40px;
  border-bottom: 1px solid @border-light;

  div {
    position: relative;
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    text-align: center;
    cursor: pointer;
  }

  .tabline {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    margin: 0 auto;
    display: inline-block;
    width: 0;
    height: 3px;
    background: @primary-blue;
    -webkit-transition: width .5s cubic-bezier(.46, 1, .23, 1.52);
    transition: width .5s cubic-bezier(.46, 1, .23, 1.52);
    border-radius: 2px 2px 0 0;
  }

  tab-selected {
    color: @text-primary;
    font-weight: 500;
  }

  .mail-login, .tel-login {
    padding: 0 28px;
  }

}

.mail-login {
  margin: 0px 24px;
}

.common-input {
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: start;
  -ms-flex-align: start;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 12px 0;
  border-bottom: 1px solid @border-light;
  transition: border-color @transition-fast;

  &:focus-within {
    border-color: @primary-blue;
  }

  .left-icon {
    margin-right: 14px;
    width: 22px;
    opacity: 0.6;
  }

  .input-view {
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;

    .input {
      font-weight: 500;
      font-size: @font-size-base;
      color: @text-primary;
      height: 26px;
      line-height: 26px;
      border: none;
      padding: 0;
      display: block;
      width: 100%;
      letter-spacing: 1px;
      background: transparent;

      &::placeholder {
        color: @text-hint;
        font-weight: 400;
      }
    }

    err-view {
      margin-top: 4px;
      height: 16px;
      line-height: 16px;
      font-size: @font-size-sm;
      color: @error;
    }
  }
}

.next-btn {
  background: @primary-blue;
  border-radius: @radius-md;
  color: @white;
  font-size: @font-size-base;
  font-weight: 500;
  height: 44px;
  line-height: 44px;
  text-align: center;
  width: 100%;
  outline: none;
  cursor: pointer;
  box-shadow: @shadow-button;
  transition: all @transition-fast;
  margin: 20px 0px !important;

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

button {
  background: transparent;
  padding: 0;
  border-width: 0px;
}

button, input, select, textarea {
  margin: 0;
  padding: 0;
  outline: none;
}

.operation {
  display: flex;
  flex-direction: row;
  margin: 0 24px;
  padding-top: 8px;
}

.forget-pwd {
  display: block;
  overflow: hidden;
  flex:1;
  margin: 0 auto;
  color: @primary-blue;
  font-size: @font-size-sm;
  cursor: pointer;
  transition: color @transition-fast;

  &:hover {
    color: @primary-blue-hover;
  }
}

</style>
