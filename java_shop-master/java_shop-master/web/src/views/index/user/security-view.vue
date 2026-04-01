<template>
  <div class="content-list">
    <div class="list-title">帐号安全</div>
    <div class="list-content">
      <div class="safe-view">
        <div class="safe-info-box">
          <div class="item flex-view">
            <div class="label">账号安全等级</div>
            <div class="right-box flex-center flex-view">
              <div class="safe-text">低风险</div>
              <progress max="3" class="safe-line" value="2">
              </progress>
            </div>
          </div>
          <div class="item flex-view">
            <div class="label">用户名</div>
            <div class="right-box">
              <input
                class="input-dom input-readonly"
                type="text"
                readonly
                :value="displayUsername"
                placeholder="加载中…"
              />
              <a-button type="link" @click="openUsernameModal">更换</a-button>
            </div>
          </div>
        </div>
        <div class="edit-pwd-box" style="display;">
          <div class="pwd-edit">
            <div class="item flex-view">
              <div class="label">当前密码</div>
              <div class="right-box">
                <a-input-password placeholder="输入当前密码" v-model:value="password"/>
              </div>
            </div>
            <div class="item flex-view">
              <div class="label">新密码</div>
              <div class="right-box">
                <a-input-password placeholder="输入新密码" v-model:value="newPassword1"/>
              </div>
            </div>
            <div class="item flex-view">
              <div class="label">确认新密码</div>
              <div class="right-box">
                <a-input-password placeholder="重复输入密码" v-model:value="newPassword2"/>
              </div>
            </div>
            <div class="item flex-view">
              <div class="label">
              </div>
              <div class="right-box">
                <a-button type="primary" @click="handleUpdatePwd()">修改密码</a-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <a-modal
      v-model:visible="usernameModalVisible"
      title="更换用户名"
      :confirm-loading="usernameSubmitting"
      ok-text="确定"
      cancel-text="取消"
      @ok="submitUsernameChange"
      @cancel="resetUsernameModal"
    >
      <div class="username-modal-form">
        <div class="modal-row">
          <span class="modal-label">当前密码</span>
          <a-input-password v-model:value="usernameModalPwd" placeholder="请输入当前登录密码" />
        </div>
        <div class="modal-row">
          <span class="modal-label">新用户名</span>
          <a-input v-model:value="newUsername" placeholder="2-32 个字符" maxlength="32" />
        </div>
        <div class="modal-row">
          <span class="modal-label">确认用户名</span>
          <a-input v-model:value="confirmUsername" placeholder="再次输入新用户名" maxlength="32" />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import {message} from "ant-design-vue";

import {detailApi, updateUserPwdApi, updateUsernameApi} from '/@/api/user'
import {useUserStore} from "/@/store";
import {USER_NAME, USER_TOKEN} from "/@/store/constants";

const userStore = useUserStore();

let password = ref('')
let newPassword1 = ref('')
let newPassword2 = ref('')

const displayUsername = ref('')
const usernameModalVisible = ref(false)
const usernameSubmitting = ref(false)
const usernameModalPwd = ref('')
const newUsername = ref('')
const confirmUsername = ref('')

const loadUser = () => {
  const userId = userStore.user_id
  if (!userId) {
    displayUsername.value = ''
    return
  }
  detailApi({ userId }).then(res => {
    if (res.data && res.data.username) {
      displayUsername.value = res.data.username
    }
  }).catch(() => {
    displayUsername.value = userStore.user_name || ''
  })
}

onMounted(() => {
  displayUsername.value = userStore.user_name || ''
  loadUser()
})

const openUsernameModal = () => {
  resetUsernameModal()
  usernameModalVisible.value = true
}

const resetUsernameModal = () => {
  usernameModalPwd.value = ''
  newUsername.value = ''
  confirmUsername.value = ''
}

const submitUsernameChange = () => {
  const pwd = usernameModalPwd.value
  const name = (newUsername.value || '').trim()
  const name2 = (confirmUsername.value || '').trim()

  if (!pwd) {
    message.warn('请输入当前密码')
    return Promise.reject()
  }
  if (!name || !name2) {
    message.warn('请输入新用户名并确认')
    return Promise.reject()
  }
  if (name !== name2) {
    message.warn('两次输入的用户名不一致')
    return Promise.reject()
  }
  if (name.length < 2 || name.length > 32) {
    message.warn('用户名长度为 2-32 个字符')
    return Promise.reject()
  }
  if (name === displayUsername.value) {
    message.warn('与当前用户名相同')
    return Promise.reject()
  }

  const userId = userStore.user_id
  if (!userId) {
    message.error('未登录')
    return Promise.reject()
  }

  usernameSubmitting.value = true
  return updateUsernameApi({
    userId,
    password: pwd,
    newUsername: name,
  }).then(res => {
    const u = res.data
    if (u && u.username && u.token) {
      userStore.$patch({
        user_name: u.username,
        user_token: u.token,
      })
      localStorage.setItem(USER_NAME, u.username)
      localStorage.setItem(USER_TOKEN, u.token)
      displayUsername.value = u.username
    }
    message.success(res.msg || '修改成功')
    resetUsernameModal()
  }).catch(err => {
    message.error(err.msg || err.message || '修改失败')
    return Promise.reject(err)
  }).finally(() => {
    usernameSubmitting.value = false
  })
}

const handleUpdatePwd = () => {
  if (!password.value || !newPassword1.value || !newPassword2.value) {
    message.warn('不能为空')
    return
  }
  if (newPassword1.value !== newPassword2.value) {
    message.warn('密码不一致')
    return
  }

  let userId = userStore.user_id
  updateUserPwdApi({
    userId:  userId,
    password: password.value,
    newPassword: newPassword1.value,
  }).then(res => {
    message.success('修改成功')
  }).catch(err => {
    message.error(err.msg)
  })
}

</script>
<style scoped lang="less">
progress {
  vertical-align: baseline;
}

.flex-view {
  display: flex;
}

input, textarea {
  outline: none;
  border-style: none;
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

.safe-view {
  .item {
    align-items: center;
    margin: 24px 0;

    .label {
      width: 100px;
      color: #152844;
      font-weight: 600;
      font-size: 14px;
    }

    .flex-center {
      align-items: center;
    }

    .safe-text {
      color: #f62a2a;
      font-weight: 600;
      font-size: 14px;
      margin-right: 18px;
    }

    .safe-line {
      background: #d3dce6;
      border-radius: 8px;
      width: 280px;
      height: 8px;
      overflow: hidden;
      color: #f6982a;
    }

    .input-dom {
      background: #f8fafb;
      border-radius: 4px;
      width: 240px;
      height: 40px;
      line-height: 40px;
      font-size: 14px;
      color: #5f77a6;
      padding: 0 12px;
      margin-right: 16px;
    }

    .input-readonly {
      cursor: default;
    }

    .change-btn {
      color: #4684e2;
      font-size: 14px;
      border: none;
      outline: none;
      cursor: pointer;
    }

    .wx-text {
      color: #5f77a6;
      font-size: 14px;
      margin-right: 16px;
    }

    .edit-pwd-btn {
      color: #4684e2;
      font-size: 14px;
      cursor: pointer;
    }
  }
}

.username-modal-form {
  .modal-row {
    display: flex;
    align-items: center;
    margin-bottom: 16px;

    .modal-label {
      width: 88px;
      flex-shrink: 0;
      color: #152844;
    }

    :deep(.ant-input-password),
    :deep(.ant-input) {
      flex: 1;
    }
  }
}
</style>
