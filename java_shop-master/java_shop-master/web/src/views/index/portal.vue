<template>
  <div class="portal">
    <Header />
    <Content />
    <Footer />
    <transition name="slide-in">
      <div v-if="adModal.visible" class="slide-in-notification" @click="handleAdClick(adModal.item)">
        <div class="notification-close" @click.stop="handleCloseAd">×</div>
        <img v-if="adModal.item" :src="adModal.item.imageUrl" class="notification-img" alt="广告" />
        <div v-if="adModal.item" class="notification-slogan">{{ adModal.item.slogan }}</div>
        <div v-if="adModal.item && adModal.item.link" class="notification-link">
          {{ normalizeLink(adModal.item.link) }}
        </div>
      </div>
    </transition>
  </div>
</template>
<script>
import Header from '/@/views/index/components/header.vue'
import Footer from '/@/views/index/components/footer.vue'
import Content from '/@/views/index/components/content.vue'
import { listApi as listAdApi } from '/@/api/ad'
import { BASE_URL } from '/@/store/constants'

export default {
  components: {
    Footer,
    Header,
    Content
  },
  data () {
    return {
      adModal: {
        visible: false,
        item: null
      }
    }
  },
  mounted() {
    this.loadPopupAd()
  },
  methods: {
    loadPopupAd() {
      listAdApi({})
        .then((res) => {
          const adList = (res.data || [])
            .filter(item => item && item.image)
            .sort((a, b) => Number(b.createTime || 0) - Number(a.createTime || 0))
            .map((item) => ({
              ...item,
              imageUrl: BASE_URL + '/api/staticfiles/image/' + item.image
            }))

          const targetAd = adList[0]
          if (targetAd) {
            this.adModal.item = targetAd
            this.adModal.visible = true
          }
        })
        .catch(() => {
          // Ignore ad popup errors so homepage remains available.
        })
    },
    handleCloseAd() {
      this.adModal.visible = false
    },
    normalizeLink(link) {
      let target = String(link || '').trim()
      if (!/^https?:\/\//i.test(target)) {
        target = `https://${target}`
      }
      return target
    },
    handleAdClick(item) {
      if (item && item.link) {
        const target = this.normalizeLink(item.link)
        if (/^https?:\/\//i.test(target)) {
          window.open(target, '_blank', 'noopener,noreferrer')
        }
      }
      this.handleCloseAd()
    }
  }
}
</script>
<style scoped lang="less">
// ============================================
// Slide-in Notification Card (Replaces Modal)
// ============================================
.slide-in-notification {
  position: fixed;
  right: 24px;
  top: 100px;
  width: 280px;
  background: @white;
  border-radius: @radius-xl;
  box-shadow: @shadow-xl;
  border: 1px solid @border-light;
  padding: 16px;
  cursor: pointer;
  z-index: @z-popover;
  transition: transform @transition-base, box-shadow @transition-base;

  &:hover {
    transform: translateX(-4px);
    box-shadow: @shadow-xl;
  }
}

.notification-close {
  position: absolute;
  top: 8px;
  right: 12px;
  font-size: 20px;
  color: @text-hint;
  cursor: pointer;
  transition: color @transition-fast;
  line-height: 1;

  &:hover {
    color: @text-secondary;
  }
}

.notification-img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: @radius-lg;
  margin-bottom: 12px;
}

.notification-slogan {
  font-size: @font-size-base;
  color: @text-primary;
  font-weight: 500;
  text-align: center;
  margin-bottom: 8px;
}

.notification-link {
  font-size: @font-size-sm;
  color: @primary-blue;
  text-align: center;
  cursor: pointer;
  word-break: break-all;
  transition: color @transition-fast;

  &:hover {
    color: @primary-blue-hover;
  }
}

// ============================================
// Slide-in Transition Animation
// ============================================
.slide-in-enter-active {
  animation: slideInRight 400ms cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.slide-in-leave-active {
  animation: slideOutRight 300ms ease-in forwards;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes slideOutRight {
  from {
    transform: translateX(0);
    opacity: 1;
  }
  to {
    transform: translateX(100%);
    opacity: 0;
  }
}
</style>
