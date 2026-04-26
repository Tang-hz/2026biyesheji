<template>
  <div class="portal">
    <Header />

    <div class="consultation-wrap">
      <div class="consultation-box">
        <!-- 左侧：用户信息/快捷入口 -->
        <div class="user-card">
          <div class="user-top">
            <img :src="userStore.avatar || AvatarIcon" class="user-avatar-img">
            <div class="user-meta">
              <div class="user-name">{{ userName }}</div>
              <div class="user-active">活跃 {{ activeDays }} 天</div>
            </div>
          </div>

          <div class="user-stats">
            <div class="stat">
              <div class="stat-title">收藏</div>
              <div class="stat-value">{{ favoriteCount }}</div>
            </div>
            <div class="stat-split" />
            <div class="stat">
              <div class="stat-title">心愿单</div>
              <div class="stat-value">{{ wishCount }}</div>
            </div>
          </div>

          <div class="menu-section">
            <div class="section-title">订单中心</div>
            <div class="menu-item" @click="go('orderView')">我的订单</div>
            <div class="menu-item" @click="go('commentView')">我的评论</div>
            <div class="menu-item" @click="go('addressView')">地址管理</div>
            <div class="menu-item" @click="go('scoreView')">我的积分</div>
          </div>

          <div class="menu-section">
            <div class="section-title">个人设置</div>
            <div class="menu-item" @click="go('userInfoEditView')">编辑资料</div>
            <div class="menu-item" @click="go('securityView')">账号安全</div>
            <div class="menu-item" @click="go('pushView')">推送设置</div>
            <div class="menu-item" @click="go('messageView')">消息管理</div>
          </div>
        </div>

        <!-- 右侧：AI 对话面板 -->
        <div class="consultation-panel">
          <div class="chat-history" ref="historyRef">
            <div
              v-for="item in chatMessages"
              :key="item.id"
              class="chat-row"
              :class="item.role === 'user' ? 'chat-row-user' : 'chat-row-ai'"
            >
              <div class="avatar" :class="item.role === 'user' ? 'avatar-user' : 'avatar-ai'">
                <img v-if="item.role === 'user'" :src="userStore.avatar || AvatarIcon" class="avatar-img">
                <span v-else-if="item.status === 'thinking'">🤔</span>
                <span v-else-if="item.status === 'speaking'">💬</span>
                <span v-else>🎧</span>
              </div>
              <div class="bubble">
                <div class="text" v-html="renderMessage(item.text, item.role)"></div>
                <div class="time">{{ item.createTime }}</div>
              </div>
            </div>
          </div>

          <div class="chat-input">
            <a-input
              v-model:value="inputText"
              class="input"
              placeholder="请输入你的问题"
              @pressEnter="handleSend"
            />
            <button class="send-btn" @click="handleSend">发送</button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue';
import Header from '/@/views/index/components/header.vue';
import Footer from '/@/views/index/components/footer.vue';
import { useUserStore } from '/@/store';
import { useRouter } from 'vue-router';
import { BASE_URL } from '/@/store/constants';
import { userCollectListApi } from '/@/api/thingCollect';
import { userWishListApi } from '/@/api/thingWish';
import { detailApi } from '/@/api/user';
import AvatarIcon from '/@/assets/images/avatar.jpg';

// ========== JSON 块解析 ==========

// JSON 块缓冲：当检测到 JSON_START 时，进入 buffer 模式直到 JSON_END
const jsonBuffer = ref('');

// 检测 JSON 块边界，返回 { text, jsonBlock }
const detectJsonBlock = (chunk: string): { text: string; jsonBlock: string | null } => {
  if (jsonBuffer.value) {
    jsonBuffer.value += chunk;
    const jsonEndIdx = jsonBuffer.value.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = jsonBuffer.value.substring(0, jsonEndIdx);
      jsonBuffer.value = '';
      return { text: '', jsonBlock: jsonContent };
    }
    return { text: '', jsonBlock: null };
  }

  const jsonStartIdx = chunk.indexOf('___JSON_START___');
  if (jsonStartIdx !== -1) {
    const beforeJson = chunk.substring(0, jsonStartIdx);
    const afterStart = chunk.substring(jsonStartIdx + '___JSON_START___'.length);
    const jsonEndIdx = afterStart.indexOf('___JSON_END___');
    if (jsonEndIdx !== -1) {
      const jsonContent = afterStart.substring(0, jsonEndIdx);
      const afterJson = afterStart.substring(jsonEndIdx + '___JSON_END___'.length);
      if (afterJson) jsonBuffer.value = afterJson;
      return { text: beforeJson, jsonBlock: jsonContent };
    } else {
      jsonBuffer.value = afterStart;
      return { text: beforeJson, jsonBlock: null };
    }
  }

  return { text: chunk, jsonBlock: null };
};

// HTML 实体转义
const escapeHtml = (str: string): string => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
};

// 解析 JSON 表格为 HTML
const parseTableJson = (jsonStr: string): string => {
  try {
    const data = JSON.parse(jsonStr);
    if (data.type !== 'table') return '';
    const { title, columns, rows } = data;
    const titleHtml = title ? `<div class="ai-table-title">${escapeHtml(title)}</div>` : '';
    const headerCells = (columns ?? []).map(col => `<th>${escapeHtml(col)}</th>`).join('');
    const bodyRows = (rows ?? []).map((row) => {
      const cells = row.map(cell => `<td>${escapeHtml(cell)}</td>`).join('');
      return `<tr>${cells}</tr>`;
    }).join('');
    return `<div class="ai-table">${titleHtml}<table><thead><tr>${headerCells}</tr></thead><tbody>${bodyRows}</tbody></table></div>`;
  } catch {
    return '';
  }
};

// 渲染消息文本
const renderMessage = (text: string, role: 'user' | 'ai'): string => {
  if (!text) return '';
  if (role === 'user') {
    return escapeHtml(text);
  }
  // AI 消息直接输出（JSON 块已在 SSE 处理时转为 HTML 表格）
  return text;
};

// ========== 组件逻辑 ==========

type ChatRole = 'user' | 'ai';
type ChatStatus = 'thinking' | 'speaking' | 'done';
type ChatMessage = {
  id: number;
  role: ChatRole;
  text: string;
  createTime: string;
  status?: ChatStatus;
};

const router = useRouter();
const userStore = useUserStore();

const historyRef = ref<HTMLDivElement | null>(null);
const inputText = ref('');

const userName = ref<string>(userStore.user_name || 'tls');
const avatarText = ref<string>((userName.value || 'tls').slice(0, 1));
const activeDays = ref<number>(123);
const favoriteCount = ref<number>(0);
const wishCount = ref<number>(0);

const refreshCounts = async () => {
  const userId = userStore.user_id;
  if (!userId) {
    favoriteCount.value = 0;
    wishCount.value = 0;
    return;
  }

  try {
    const [collectRes, wishRes] = await Promise.all([
      userCollectListApi({ userId }),
      userWishListApi({ userId }),
    ]);
    favoriteCount.value = Array.isArray(collectRes?.data) ? collectRes.data.length : 0;
    wishCount.value = Array.isArray(wishRes?.data) ? wishRes.data.length : 0;
  } catch {
    favoriteCount.value = 0;
    wishCount.value = 0;
  }
};

const chatMessages = ref<ChatMessage[]>([
  {
    id: 1,
    role: 'ai',
    text: '您好！我是 E购 AI 客服。您有什么问题可以直接告诉我。',
    createTime: new Date().toLocaleTimeString().slice(0, 5),
    status: 'done',
  },
]);

const formatTime = (d: Date) => d.toLocaleTimeString().slice(0, 5);

const go = (name: string) => {
  router.push({ name });
};

const scrollToBottom = () => {
  nextTick(() => {
    if (!historyRef.value) return;
    historyRef.value.scrollTo({
      top: historyRef.value.scrollHeight,
      behavior: 'smooth',
    });
  });
};

onMounted(() => {
  userName.value = userStore.user_name || userName.value || 'tls';
  avatarText.value = (userName.value || 'tls').slice(0, 1);
  refreshCounts();
  scrollToBottom();
  if (userStore.user_id) {
    detailApi({userId: userStore.user_id}).then(res => {
      if (res.data && res.data.avatar) {
        userStore.setAvatar(BASE_URL + '/api/staticfiles/avatar/' + res.data.avatar)
      }
    })
  }
});

watch(
  () => userStore.user_id,
  () => {
    refreshCounts();
  },
);

const handleSend = () => {
  const text = inputText.value.trim();
  if (!text) return;

  chatMessages.value.push({
    id: Date.now(),
    role: 'user',
    text,
    createTime: formatTime(new Date()),
  });
  inputText.value = '';
  scrollToBottom();

  const aiMsgId = Date.now() + 1;
  chatMessages.value.push({
    id: aiMsgId,
    role: 'ai',
    text: '',
    createTime: formatTime(new Date()),
    status: 'thinking',
  });
  scrollToBottom();

  const uid = userStore.user_id ? String(userStore.user_id) : 'guest';
  const url =
    (BASE_URL ? BASE_URL.replace(/\/$/, '') : '') +
    `/api/ai/react/stream?message=${encodeURIComponent(text)}&userId=${encodeURIComponent(uid)}`;

  const es = new EventSource(url);
  es.onmessage = (evt) => {
    const chunk = evt.data ?? '';
    const { text: textChunk, jsonBlock } = detectJsonBlock(chunk);
    const msg = chatMessages.value.find((m) => m.id === aiMsgId);
    if (msg) {
      if (msg.status === 'thinking') msg.status = 'speaking';
      if (textChunk) {
        msg.text += textChunk;
      }
      if (jsonBlock) {
        msg.text += parseTableJson(jsonBlock);
      }
    }
    scrollToBottom();
  };
  es.onerror = () => {
    es.close();
    const msg = chatMessages.value.find((m) => m.id === aiMsgId);
    if (msg) {
      msg.status = 'done';
      if (!msg.text) {
        msg.text = '连接失败或已中断，请稍后重试。';
      }
    }
    scrollToBottom();
  };
};
</script>

<style scoped lang="less">
.consultation-wrap {
  width: 1108px;
  margin: 80px auto 40px;
}

.consultation-box {
  display: flex;
  flex-direction: row;
  gap: 0;
  background: #fff;
  border: 1px solid #cedce4;
  border-top: 2px solid #4684e2;
  border-bottom: 2px solid #4684e2;
  border-radius: 2px;
  padding: 0 0;
}

.user-card {
  width: 290px;
  background: transparent;
  padding: 0;
  border-right: 1px solid #cedce4;
}

.user-top {
  display: flex;
  align-items: center;
  padding: 22px 18px 10px;
}

.user-avatar {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: #ecf3fc;
  color: #2a4f88;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.user-avatar-img {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  object-fit: cover;
}

.user-meta {
  margin-left: 14px;
}

.user-name {
  font-size: 16px;
  color: #152844;
  font-weight: 600;
  margin-bottom: 6px;
}

.user-active {
  font-size: 12px;
  color: #a1adc5;
}

.user-stats {
  display: flex;
  align-items: center;
  padding: 14px 18px 16px;
  border-top: 1px solid #cedce4;
  border-bottom: 1px solid #cedce4;
}

.stat {
  flex: 1;
  text-align: center;
}

.stat-title {
  font-size: 12px;
  color: #a1adc5;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 18px;
  color: #152844;
  font-weight: 600;
}

.stat-split {
  width: 1px;
  height: 34px;
  background: #cedce4;
}

.menu-section {
  padding: 16px 18px;
}

.section-title {
  font-size: 14px;
  color: #152844;
  font-weight: 600;
  margin-bottom: 10px;
}

.menu-item {
  font-size: 14px;
  color: #315c9e;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px dashed #e9e9e9;
}

.menu-item:last-child {
  border-bottom: 0;
}

.consultation-panel {
  flex: 1;
  background: transparent;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px);
  min-height: 520px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 16px 18px;
}

.chat-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 14px;
}

.chat-row-user {
  justify-content: flex-end;
}

.chat-row-ai {
  justify-content: flex-start;
}

.avatar {
  width: 34px;
  height: 34px;
  line-height: 34px;
  text-align: center;
  border-radius: 50%;
  font-size: 12px;
  margin: 0 10px;
  color: #fff;
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-user {
  background: #4684e2;
}

.avatar-ai {
  background: #a1adc6;
  animation: breathe 2s ease-in-out infinite;
}

@keyframes breathe {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(161, 173, 198, 0.4);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(161, 173, 198, 0);
  }
}

.bubble {
  max-width: 70%;
  background: #f6f9fb;
  border: 1px solid #e9f0fb;
  border-radius: 10px;
  padding: 10px 12px 8px;
}

.chat-row-user .bubble {
  background: #ecf3fc;
  border-color: #cfe1ff;
}

.text {
  color: #152844;
  font-size: 14px;
  line-height: 20px;
  white-space: pre-wrap;
  word-break: break-word;

  :deep(.ai-table) {
    margin: 12px 0;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e8ecf0;
  }
  :deep(.ai-table-title) {
    background: linear-gradient(135deg, #4684e2 0%, #5a9aed 100%);
    color: #fff;
    font-weight: 600;
    padding: 10px 16px;
    font-size: 14px;
    border-radius: 8px 8px 0 0;
  }
  :deep(.ai-table table) {
    width: 100%;
    border-collapse: collapse;
  }
  :deep(.ai-table th) {
    background: #f0f5ff;
    color: #152844;
    font-weight: 600;
    padding: 10px 16px;
    text-align: left;
    border-bottom: 1px solid #e8ecf0;
  }
  :deep(.ai-table td) {
    padding: 10px 16px;
    border-bottom: 1px solid #f0f0f0;
    color: #333;
  }
  :deep(.ai-table tr:last-child td) {
    border-bottom: none;
  }
  :deep(.ai-table tr:hover td) {
    background: #f8faff;
  }
  :deep(.ai-table tr:nth-child(even) td) {
    background: #fafcff;
  }
  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 20px;
  }
  :deep(li) {
    margin: 4px 0;
  }
  :deep(code) {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 13px;
  }
  :deep(pre) {
    background: #f5f5f5;
    padding: 12px 16px;
    border-radius: 8px;
    overflow-x: auto;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.06);
  }
  :deep(pre) :deep(code) {
    background: none;
    padding: 0;
  }
  :deep(blockquote) {
    border-left: 3px solid #4684e2;
    margin: 10px 0;
    padding: 8px 16px;
    background: #f8fafc;
    border-radius: 0 8px 8px 0;
  }
}

.time {
  color: #a1adc5;
  font-size: 12px;
  margin-top: 6px;
}

.chat-input {
  padding: 14px 18px;
  border-top: 1px solid #cedce4;
  display: flex;
  gap: 12px;
  align-items: center;
}

.input {
  flex: 1;
}

.send-btn {
  width: 84px;
  height: 32px;
  border-radius: 16px;
  border: 0;
  cursor: pointer;
  background: #4684e2;
  color: #fff;
  font-size: 14px;
}
</style>