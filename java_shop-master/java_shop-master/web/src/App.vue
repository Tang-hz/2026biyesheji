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

// Button styles
.btn-primary {
  background: @primary-blue;
  color: @white;
  border: none;
  border-radius: @radius-md;
  font-size: @font-size-base;
  font-weight: 500;
  height: 36px;
  line-height: 36px;
  padding: 0 @space-5;
  cursor: pointer;
  box-shadow: @shadow-button;
  transition: background @transition-fast,
              box-shadow @transition-fast,
              transform @transition-fast;
  display: inline-flex;
  align-items: center;
  justify-content: center;

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

.btn-pill {
  border-radius: @radius-full;
  padding: 0 @space-6;
  height: 32px;
  line-height: 32px;
}

.btn-outline {
  background: transparent;
  color: @primary-blue;
  border: 1px solid @primary-blue;
  border-radius: @radius-md;
  box-shadow: none;

  &:hover {
    background: @primary-blue-light;
    border-color: @primary-blue-hover;
  }
}

// Input base style
.input-base {
  background: @bg-input;
  border: 1px solid @border-light;
  border-radius: @radius-md;
  padding: @space-2 @space-3;
  font-size: @font-size-base;
  color: @text-primary;
  transition: border-color @transition-fast,
              box-shadow @transition-fast,
              background @transition-fast;

  &::placeholder {
    color: @text-hint;
  }

  &:hover {
    border-color: @border-subtle;
  }

  &:focus {
    outline: none;
    border-color: @primary-blue;
    box-shadow: 0 0 0 3px @primary-blue-subtle;
    background: @white;
  }
}

// Card hover lift animation
.card-lift {
  transition: transform @transition-base, box-shadow @transition-base;

  &:hover {
    transform: translateY(-2px);
    box-shadow: @shadow-card-hover;
  }
}

// Fade in animation
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fadeIn @transition-slow forwards;
}

// Staggered animation helper
.stagger-item {
  opacity: 0;
  animation: fadeIn @transition-slow forwards;
}
</style>
