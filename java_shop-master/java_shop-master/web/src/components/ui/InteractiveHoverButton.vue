<template>
  <button
    ref="buttonRef"
    class="interactive-hover-button"
    :class="className"
    :disabled="disabled"
    v-bind="$attrs"
    @click="$emit('click', $event)"
  >
    <span class="button-text">{{ text }}</span>
    <span class="button-icon-wrapper">
      <span class="button-text-hover">{{ text }}</span>
      <span class="arrow-icon">
        <slot name="icon">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M5 12h14" />
            <path d="m12 5 7 7-7 7" />
          </svg>
        </slot>
      </span>
    </span>
  </button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  text?: string
  disabled?: boolean
  className?: string
}

withDefaults(defineProps<Props>(), {
  text: '按钮',
  disabled: false,
  className: ''
})

defineEmits(['click'])

const buttonRef = ref<HTMLButtonElement | null>(null)
</script>

<style scoped lang="less">
.interactive-hover-button {
  position: relative;
  width: 100%;
  height: 48px;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  background: #ffffff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 0 24px;

  &:hover {
    border-color: rgba(0, 0, 0, 0.2);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.button-text {
  display: inline-block;
  transition: all 0.3s ease;
  transform: translateX(0);
  opacity: 1;
}

.button-icon-wrapper {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(128deg, #00aaeb, #00c1cd 59%, #0ac2b0 100%);
  color: white;
  border-radius: 8px;
  opacity: 0;
  transition: all 0.3s ease;
  transform: translateX(-100%);
}

.interactive-hover-button:hover .button-text {
  transform: translateX(100%);
  opacity: 0;
}

.interactive-hover-button:hover .button-icon-wrapper {
  opacity: 1;
  transform: translateX(0);
}

.arrow-icon {
  display: flex;
  align-items: center;
}
</style>
