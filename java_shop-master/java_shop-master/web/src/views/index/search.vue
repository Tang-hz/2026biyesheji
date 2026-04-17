<template>
  <div class="search">
    <Header />
    <div class="search-layout">
      <aside class="search-sidebar">
        <div class="filter-panel">
          <h4>筛选</h4>
          <p class="filter-hint">更多筛选功能持续更新中</p>
        </div>
      </aside>
      <div class="search-main">
        <SearchContentView />
      </div>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import Header from '/@/views/index/components/header.vue'
import Footer from '/@/views/index/components/footer.vue'
import SearchContentView from '/@/views/index/components/search-content-view.vue'

</script>

<style scoped lang="less">

.search-layout {
  display: flex;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 0 0 0;
}

.search-sidebar {
  width: 180px;
  flex-shrink: 0;
  margin-top: 80px;
}

.filter-panel {
  background: @white;
  border-radius: @radius-lg;
  padding: 16px;
  box-shadow: @shadow-card;
  border: 1px solid @border-light;

  h4 {
    font-size: @font-size-lg;
    color: @navy-dark;
    margin: 0 0 12px;
    font-weight: 600;
  }

  .filter-hint {
    font-size: @font-size-sm;
    color: @text-hint;
    margin: 8px 0 0;
  }
}

.search-main {
  flex: 1;
  min-width: 0;
}

.search-content {
  width: 100%;
  margin: 4px auto;
}
</style>
