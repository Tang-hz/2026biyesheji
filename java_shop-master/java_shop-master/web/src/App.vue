<template>
  <a-config-provider :locale="zhCN">
    <router-view />
  </a-config-provider>
</template>
<script setup lang="ts">

import zhCN from 'ant-design-vue/es/locale/zh_CN';

</script>

<style lang="less">
// base.less is auto-injected via vite.config.ts additionalData

#app {
  font-family: @font-body;
  height: 100vh;
  color: @text-primary;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

h1, .h1 {
  font-family: @font-display;
  font-weight: 800;
  font-size: 24px;
  letter-spacing: @letter-spacing-tight;
  line-height: @line-height-tight;
  color: @navy-dark;
}

h2, .h2 {
  font-family: @font-heading;
  font-weight: 700;
  font-size: 20px;
  letter-spacing: @letter-spacing-tight;
  line-height: @line-height-tight;
  color: @navy-dark;
}

h3, .h3 {
  font-family: @font-heading;
  font-weight: 700;
  font-size: 18px;
  line-height: @line-height-normal;
  color: @navy-dark;
}

h4, .h4 {
  font-family: @font-heading;
  font-weight: 600;
  font-size: 16px;
  line-height: @line-height-normal;
  color: @navy-dark;
}

// Card base style
.mall-card {
  background: @bg-card;
  border-radius: @radius-lg;
  box-shadow: @shadow-card;
  padding: @space-4 @space-5;
  margin-bottom: @space-3;
  border: 1px solid @border-light;
  transition: box-shadow @transition-base,
              transform @transition-base,
              border-color @transition-base;

  &:hover {
    box-shadow: @shadow-card-hover;
    border-color: @border-subtle;
  }
}

</style>
