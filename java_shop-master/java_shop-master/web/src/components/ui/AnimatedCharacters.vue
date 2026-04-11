<template>
  <div class="animated-characters" ref="containerRef">
    <!-- 紫色大矩形角色 - 最后一层 -->
    <div
      ref="purpleRef"
      class="character purple-character"
      :style="{
        transform: purpleTransform,
        height: isTyping || isHidingPassword ? '440px' : '400px'
      }"
    >
      <!-- 眼睛 -->
      <div class="eyes" :style="{ left: purpleEyesLeft, top: purpleEyesTop }">
        <div
          class="eye"
          :style="{
            width: '18px',
            height: isPurpleBlinking ? '2px' : '18px',
            backgroundColor: 'white',
            overflow: 'hidden',
            borderRadius: '50%'
          }"
        >
          <div
            v-if="!isPurpleBlinking"
            class="pupil"
            :style="{
              width: '7px',
              height: '7px',
              backgroundColor: '#2D2D2D',
              borderRadius: '50%',
              transform: `translate(${purplePupil.x}px, ${purplePupil.y}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
        <div
          class="eye"
          :style="{
            width: '18px',
            height: isPurpleBlinking ? '2px' : '18px',
            backgroundColor: 'white',
            overflow: 'hidden',
            borderRadius: '50%'
          }"
        >
          <div
            v-if="!isPurpleBlinking"
            class="pupil"
            :style="{
              width: '7px',
              height: '7px',
              backgroundColor: '#2D2D2D',
              borderRadius: '50%',
              transform: `translate(${purplePupil.x}px, ${purplePupil.y}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 黑色矩形角色 - 中间层 -->
    <div
      ref="blackRef"
      class="character black-character"
      :style="{ transform: blackTransform }"
    >
      <!-- 眼睛 -->
      <div class="eyes" :style="{ left: blackEyesLeft, top: blackEyesTop }">
        <div
          class="eye"
          :style="{
            width: '16px',
            height: isBlackBlinking ? '2px' : '16px',
            backgroundColor: 'white',
            overflow: 'hidden',
            borderRadius: '50%'
          }"
        >
          <div
            v-if="!isBlackBlinking"
            class="pupil"
            :style="{
              width: '6px',
              height: '6px',
              backgroundColor: '#2D2D2D',
              borderRadius: '50%',
              transform: `translate(${blackPupil.x}px, ${blackPupil.y}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
        <div
          class="eye"
          :style="{
            width: '16px',
            height: isBlackBlinking ? '2px' : '16px',
            backgroundColor: 'white',
            overflow: 'hidden',
            borderRadius: '50%'
          }"
        >
          <div
            v-if="!isBlackBlinking"
            class="pupil"
            :style="{
              width: '6px',
              height: '6px',
              backgroundColor: '#2D2D2D',
              borderRadius: '50%',
              transform: `translate(${blackPupil.x}px, ${blackPupil.y}px)`,
              transition: 'transform 0.1s ease-out'
            }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 橙色半圆角色 - 前排左侧 -->
    <div
      ref="orangeRef"
      class="character orange-character"
      :style="{ transform: orangeTransform }"
    >
      <!-- 眼睛（只有瞳孔） -->
      <div class="eyes" :style="{ left: orangeEyesLeft, top: orangeEyesTop }">
        <div
          class="pupil-only"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            borderRadius: '50%',
            transform: `translate(${orangePupil.x}px, ${orangePupil.y}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
        <div
          class="pupil-only"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            borderRadius: '50%',
            transform: `translate(${orangePupil.x}px, ${orangePupil.y}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
      </div>
    </div>

    <!-- 黄色矩形角色 - 前排右侧 -->
    <div
      ref="yellowRef"
      class="character yellow-character"
      :style="{ transform: yellowTransform }"
    >
      <!-- 眼睛（只有瞳孔） -->
      <div class="eyes" :style="{ left: yellowEyesLeft, top: yellowEyesTop }">
        <div
          class="pupil-only"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            borderRadius: '50%',
            transform: `translate(${yellowPupil.x}px, ${yellowPupil.y}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
        <div
          class="pupil-only"
          :style="{
            width: '12px',
            height: '12px',
            backgroundColor: '#2D2D2D',
            borderRadius: '50%',
            transform: `translate(${yellowPupil.x}px, ${yellowPupil.y}px)`,
            transition: 'transform 0.1s ease-out'
          }"
        ></div>
      </div>
      <!-- 嘴巴 -->
      <div
        class="mouth"
        :style="{
          width: '20px',
          height: '4px',
          backgroundColor: '#2D2D2D',
          borderRadius: '2px',
          position: 'absolute',
          left: yellowMouthLeft,
          top: yellowMouthTop
        }"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Props {
  isTyping?: boolean
  showPassword?: boolean
  passwordLength?: number
}

const props = withDefaults(defineProps<Props>(), {
  isTyping: false,
  showPassword: false,
  passwordLength: 0
})

// Refs
const containerRef = ref<HTMLElement | null>(null)
const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)

// Mouse position
const mouseX = ref(0)
const mouseY = ref(0)

// Blinking states
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)

// Purple peeking state
const isPurplePeeking = ref(false)

// Looking at each other
const isLookingAtEachOther = ref(false)

// Compute positions
const calculatePosition = (refEl: HTMLElement | null) => {
  if (!refEl) return { faceX: 0, faceY: 0, bodySkew: 0 }

  const rect = refEl.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3

  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY

  const faceX = Math.max(-15, Math.min(15, deltaX / 20))
  const faceY = Math.max(-10, Math.min(10, deltaY / 30))
  const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120))

  return { faceX, faceY, bodySkew }
}

// Pupils follow mouse
const purplePupil = ref({ x: 0, y: 0 })
const blackPupil = ref({ x: 0, y: 0 })
const orangePupil = ref({ x: 0, y: 0 })
const yellowPupil = ref({ x: 0, y: 0 })

const updatePupils = () => {
  if (purpleRef.value) {
    const rect = purpleRef.value.getBoundingClientRect()
    const eyeCenterX = rect.left + rect.width / 2 + 70
    const eyeCenterY = rect.top + 40

    const deltaX = mouseX.value - eyeCenterX
    const deltaY = mouseY.value - eyeCenterY
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), 5)

    const angle = Math.atan2(deltaY, deltaX)
    purplePupil.value = {
      x: Math.cos(angle) * distance,
      y: Math.sin(angle) * distance
    }
  }

  if (blackRef.value) {
    const rect = blackRef.value.getBoundingClientRect()
    const eyeCenterX = rect.left + rect.width / 2 + 50
    const eyeCenterY = rect.top + 32

    const deltaX = mouseX.value - eyeCenterX
    const deltaY = mouseY.value - eyeCenterY
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), 4)

    const angle = Math.atan2(deltaY, deltaX)
    blackPupil.value = {
      x: Math.cos(angle) * distance,
      y: Math.sin(angle) * distance
    }
  }

  if (orangeRef.value) {
    const rect = orangeRef.value.getBoundingClientRect()
    const eyeCenterX = rect.left + 100
    const eyeCenterY = rect.top + 90

    const deltaX = mouseX.value - eyeCenterX
    const deltaY = mouseY.value - eyeCenterY
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), 5)

    const angle = Math.atan2(deltaY, deltaX)
    orangePupil.value = {
      x: Math.cos(angle) * distance,
      y: Math.sin(angle) * distance
    }
  }

  if (yellowRef.value) {
    const rect = yellowRef.value.getBoundingClientRect()
    const eyeCenterX = rect.left + 70
    const eyeCenterY = rect.top + 40

    const deltaX = mouseX.value - eyeCenterX
    const deltaY = mouseY.value - eyeCenterY
    const distance = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), 5)

    const angle = Math.atan2(deltaY, deltaX)
    yellowPupil.value = {
      x: Math.cos(angle) * distance,
      y: Math.sin(angle) * distance
    }
  }
}

// Computed transforms
const isHidingPassword = computed(() => props.passwordLength > 0 && !props.showPassword)

const purpleTransform = computed(() => {
  const pos = calculatePosition(purpleRef.value)
  if (props.passwordLength > 0 && props.showPassword) {
    return 'skewX(0deg)'
  }
  if (props.isTyping || isHidingPassword.value) {
    return `skewX(${(pos.bodySkew || 0) - 12}deg) translateX(40px)`
  }
  return `skewX(${pos.bodySkew || 0}deg)`
})

const blackTransform = computed(() => {
  const pos = calculatePosition(blackRef.value)
  if (props.passwordLength > 0 && props.showPassword) {
    return 'skewX(0deg)'
  }
  if (isLookingAtEachOther.value) {
    return `skewX(${(pos.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
  }
  if (props.isTyping || isHidingPassword.value) {
    return `skewX(${(pos.bodySkew || 0) * 1.5}deg)`
  }
  return `skewX(${pos.bodySkew || 0}deg)`
})

const orangeTransform = computed(() => {
  const pos = calculatePosition(orangeRef.value)
  if (props.passwordLength > 0 && props.showPassword) {
    return 'skewX(0deg)'
  }
  return `skewX(${pos.bodySkew || 0}deg)`
})

const yellowTransform = computed(() => {
  const pos = calculatePosition(yellowRef.value)
  if (props.passwordLength > 0 && props.showPassword) {
    return 'skewX(0deg)'
  }
  return `skewX(${pos.bodySkew || 0}deg)`
})

// Eye positions
const purpleEyesLeft = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '20px'
  if (isLookingAtEachOther.value) return '55px'
  return `${45 + calculatePosition(purpleRef.value).faceX}px`
})

const purpleEyesTop = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '35px'
  if (isLookingAtEachOther.value) return '65px'
  return `${40 + calculatePosition(purpleRef.value).faceY}px`
})

const blackEyesLeft = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '10px'
  if (isLookingAtEachOther.value) return '32px'
  return `${26 + calculatePosition(blackRef.value).faceX}px`
})

const blackEyesTop = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '28px'
  if (isLookingAtEachOther.value) return '12px'
  return `${32 + calculatePosition(blackRef.value).faceY}px`
})

const orangeEyesLeft = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '50px'
  return `${82 + (calculatePosition(orangeRef.value).faceX || 0)}px`
})

const orangeEyesTop = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '85px'
  return `${90 + (calculatePosition(orangeRef.value).faceY || 0)}px`
})

const yellowEyesLeft = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '20px'
  return `${52 + (calculatePosition(yellowRef.value).faceX || 0)}px`
})

const yellowEyesTop = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '35px'
  return `${40 + (calculatePosition(yellowRef.value).faceY || 0)}px`
})

const yellowMouthLeft = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '10px'
  return `${40 + (calculatePosition(yellowRef.value).faceX || 0)}px`
})

const yellowMouthTop = computed(() => {
  if (props.passwordLength > 0 && props.showPassword) return '88px'
  return `${88 + (calculatePosition(yellowRef.value).faceY || 0)}px`
})

// Event handlers
const handleMouseMove = (e: MouseEvent) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  updatePupils()
}

// Blinking for purple
let purpleBlinkTimer: number | null = null
const schedulePurpleBlink = () => {
  const delay = Math.random() * 4000 + 3000
  purpleBlinkTimer = window.setTimeout(() => {
    isPurpleBlinking.value = true
    setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, delay)
}

// Blinking for black
let blackBlinkTimer: number | null = null
const scheduleBlackBlink = () => {
  const delay = Math.random() * 4000 + 3000
  blackBlinkTimer = window.setTimeout(() => {
    isBlackBlinking.value = true
    setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, delay)
}

// Purple peeking when password is visible
let peekTimer: number | null = null
const schedulePeek = () => {
  if (props.passwordLength > 0 && props.showPassword) {
    peekTimer = window.setTimeout(() => {
      isPurplePeeking.value = true
      setTimeout(() => {
        isPurplePeeking.value = false
        schedulePeek()
      }, 800)
    }, Math.random() * 3000 + 2000)
  }
}

// Watch for typing to trigger "looking at each other"
watch(() => props.isTyping, (newVal) => {
  if (newVal) {
    isLookingAtEachOther.value = true
    setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  }
})

// Watch for password visibility to trigger peek
watch([() => props.passwordLength, () => props.showPassword], () => {
  if (props.passwordLength > 0 && props.showPassword) {
    schedulePeek()
  }
})

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  schedulePurpleBlink()
  scheduleBlackBlink()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (purpleBlinkTimer) clearTimeout(purpleBlinkTimer)
  if (blackBlinkTimer) clearTimeout(blackBlinkTimer)
  if (peekTimer) clearTimeout(peekTimer)
})
</script>

<style scoped lang="less">
.animated-characters {
  position: relative;
  width: 550px;
  height: 400px;
}

.character {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  transform-origin: bottom center;

  .eyes {
    display: flex;
    gap: 32px;
    position: absolute;
    transition: all 0.7s ease-in-out;
  }

  .eye {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .pupil-only {
    flex-shrink: 0;
  }
}

.purple-character {
  left: 70px;
  width: 180px;
  height: 400px;
  background-color: #6C3FF5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
}

.black-character {
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: #2D2D2D;
  border-radius: 8px 8px 0 0;
  z-index: 2;
}

.orange-character {
  left: 0px;
  width: 240px;
  height: 200px;
  background-color: #FF9B6B;
  border-radius: 120px 120px 0 0;
  z-index: 3;
}

.yellow-character {
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: #E8D754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
}

.mouth {
  transition: all 0.2s ease-out;
}
</style>
