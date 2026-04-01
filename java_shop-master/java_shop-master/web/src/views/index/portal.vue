<template>
  <div class="portal">
    <Header />
    <Content />
    <Footer />
    <a-modal
      :visible="adModal.visible"
      :footer="null"
      :maskClosable="false"
      :closable="true"
      width="520px"
      @cancel="handleCloseAd"
    >
      <div v-if="adModal.item" class="ad-popup">
        <img
          class="ad-image"
          :src="adModal.item.imageUrl"
          alt="广告"
          @click="handleAdClick(adModal.item)"
        />
        <div class="ad-slogan">{{ adModal.item.slogan }}</div>
        <div
          v-if="adModal.item.link"
          class="ad-link"
          @click="handleAdClick(adModal.item)"
        >
          {{ normalizeLink(adModal.item.link) }}
        </div>
      </div>
    </a-modal>
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
.ad-popup {
  text-align: center;
}

.ad-image {
  width: 100%;
  max-height: 460px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}

.ad-slogan {
  margin-top: 12px;
  text-align: center;
  color: #ff4d4f;
  font-size: 16px;
  font-weight: 600;
}

.ad-link {
  margin-top: 8px;
  text-align: center;
  color: #1677ff;
  font-size: 14px;
  text-decoration: underline;
  cursor: pointer;
  word-break: break-all;
}
</style>
