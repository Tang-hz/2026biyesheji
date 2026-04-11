<template>
  <div class="login-container">
    <!-- 左侧品牌展示区 -->
    <div class="left-section">
      <!-- Logo -->
      <div class="logo-area">
        <img :src="LogoIcon" alt="logo" class="logo-image">
        <span class="logo-text">E购商城</span>
      </div>

      <!-- 动画角色 -->
      <div class="characters-area">
        <AnimatedCharacters
          :isTyping="isTyping"
          :showPassword="showPassword || showConfirmPassword"
          :passwordLength="passwordLength + confirmPasswordLength"
        />
      </div>
    </div>

    <!-- 右侧注册表单区 -->
    <div class="right-section">
      <div class="login-form-container">
        <!-- 移动端 Logo -->
        <div class="mobile-logo">
          <img :src="LogoIcon" alt="logo" class="logo-image">
          <span class="logo-text">E购商城</span>
        </div>

        <!-- 标题 -->
        <div class="form-header">
          <h1 class="title">创建新账号</h1>
          <p class="subtitle">请填写以下信息完成注册</p>
        </div>

        <!-- 注册表单 -->
        <a-form
          ref="formRef"
          layout="vertical"
          :model="formData"
          :rules="rules"
          @finish="handleRegister"
        >
          <a-form-item label="用户名" name="username">
            <a-input
              v-model:value="formData.username"
              size="large"
              placeholder="请输入用户名"
              @focus="isTyping = true"
              @blur="isTyping = false"
            >
              <template #prefix>
                <UserOutlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="密码" name="password">
            <a-input
              v-model:value="formData.password"
              size="large"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              @focus="isTyping = true"
              @blur="isTyping = false"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
              <template #suffix>
                <EyeOutlined
                  v-if="!showPassword"
                  class="eye-icon"
                  @click="showPassword = true"
                />
                <EyeInvisibleOutlined
                  v-else
                  class="eye-icon"
                  @click="showPassword = false"
                />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="确认密码" name="repassword">
            <a-input
              v-model:value="formData.repassword"
              size="large"
              :type="showConfirmPassword ? 'text' : 'password'"
              placeholder="请再次输入密码"
              @focus="isTyping = true"
              @blur="isTyping = false"
            >
              <template #prefix>
                <LockOutlined class="input-icon" />
              </template>
              <template #suffix>
                <EyeOutlined
                  v-if="!showConfirmPassword"
                  class="eye-icon"
                  @click="showConfirmPassword = true"
                />
                <EyeInvisibleOutlined
                  v-else
                  class="eye-icon"
                  @click="showConfirmPassword = false"
                />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item>
            <InteractiveHoverButton
              text="注册"
              :disabled="registerLoading"
              class="login-btn"
              @click="handleRegister"
            />
          </a-form-item>
        </a-form>

        <!-- 登录链接 -->
        <div class="signup-section">
          <span class="signup-text">已有账户？</span>
          <a @click="handleGoLogin" class="signup-link">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons-vue'
import { userRegisterApi } from '/@/api/user'
import AnimatedCharacters from '/@/components/ui/AnimatedCharacters.vue'
import InteractiveHoverButton from '/@/components/ui/InteractiveHoverButton.vue'
import LogoIcon from '/@/assets/images/k-logo.png'

const router = useRouter()

const formRef = ref()
const registerLoading = ref(false)
const isTyping = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const formData = reactive({
  username: '',
  password: '',
  repassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  repassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' }
  ]
}

const passwordLength = computed(() => formData.password.length)
const confirmPasswordLength = computed(() => formData.repassword.length)

const handleRegister = async () => {
  if (formData.password !== formData.repassword) {
    message.warn('两次输入的密码不一致')
    return
  }

  try {
    await formRef.value?.validate()
    registerLoading.value = true

    await userRegisterApi({
      username: formData.username,
      password: formData.password,
      rePassword: formData.repassword
    })

    message.success('注册成功！')
    router.push({ name: 'login' })
  } catch (err: any) {
    if (err.errorFields) {
      return
    }
    message.error(err.msg || '注册失败')
  } finally {
    registerLoading.value = false
  }
}

const handleGoLogin = () => {
  router.push({ name: 'login' })
}
</script>

<style scoped lang="less">
.login-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
  font-family: "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
}

/* 左侧品牌区 */
.left-section {
  position: relative;
  background: #ffffff;
  padding: 48px;
  color: #333;
  overflow: hidden;
}

.logo-area {
  position: absolute;
  top: 48px;
  left: 48px;
  display: flex;
  align-items: center;
  gap: 12px;
  z-index: 10;

  .logo-image {
    width: 40px;
    height: 40px;
    border-radius: 8px;
  }

  .logo-text {
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
}

.characters-area {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}

/* 右侧注册表单区 */
.right-section {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  padding: 48px;
}

.login-form-container {
  width: 100%;
  max-width: 420px;
}

.mobile-logo {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 48px;

  .logo-image {
    width: 32px;
    height: 32px;
    border-radius: 6px;
  }

  .logo-text {
    font-size: 18px;
    font-weight: 600;
    color: #333;
  }
}

.form-header {
  text-align: center;
  margin-bottom: 40px;

  .title {
    font-size: 28px;
    font-weight: 700;
    color: #1a1a1a;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #666;
    margin: 0;
  }
}

.input-icon {
  color: #999;
}

.eye-icon {
  cursor: pointer;
  color: #999;
  transition: color 0.2s;

  &:hover {
    color: #666;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.signup-section {
  text-align: center;
  margin-top: 32px;
  font-size: 14px;
  color: #666;

  .signup-text {
    margin-right: 4px;
  }

  .signup-link {
    color: #1890ff;
    cursor: pointer;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .login-container {
    grid-template-columns: 1fr;
  }

  .left-section {
    display: none;
  }

  .mobile-logo {
    display: flex;
  }
}

@media (max-width: 480px) {
  .right-section {
    padding: 24px;
  }

  .form-header .title {
    font-size: 24px;
  }
}
</style>
