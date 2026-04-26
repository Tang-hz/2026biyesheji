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
import { marked } from 'marked';
import Header from '/@/views/index/components/header.vue';
import Footer from '/@/views/index/components/footer.vue';
import { useUserStore } from '/@/store';
import { useRouter } from 'vue-router';
import { BASE_URL } from '/@/store/constants';
import { userCollectListApi } from '/@/api/thingCollect';
import { userWishListApi } from '/@/api/thingWish';
import { detailApi } from '/@/api/user';
import AvatarIcon from '/@/assets/images/avatar.jpg';

// 配置marked
marked.setOptions({
  breaks: true,
  gfm: true,
});

// 解析 JSON 表格消息
const parseJsonTable = (text: string): { title?: string; columns: string[]; rows: string[][] } | null => {
  try {
    const parsed = JSON.parse(text.trim());
    if (parsed && parsed.type === 'table') {
      return {
        title: parsed.title || '',
        columns: parsed.columns || [],
        rows: parsed.rows || [],
      };
    }
  } catch {}
  return null;
};

// 解析 markdown 表格，返回 { columns, rows }
const parseMarkdownTable = (text: string): { title?: string; columns: string[]; rows: string[][] } | null => {
  try {
    const lines = text.trim().split('\n').filter((line) => line.trim() !== '');
    if (lines.length < 2) return null;

    // 过滤出表格行（以 | 开头和结尾）
    const tableLines = lines.filter((line) => {
      const t = line.trim();
      return t.startsWith('|') && t.endsWith('|');
    });

    if (tableLines.length < 2) return null;

    // 解析单元格
    const parseRow = (line: string): string[] => {
      return line
        .trim()
        .split('|')
        .slice(1, -1) // 去掉首尾空元素
        .map((cell) => cell.trim());
    };

    // 第一行是表头，第二行是分隔线（----），之后是数据行
    const headerLine = tableLines[0];
    const columns = parseRow(headerLine);

    // 找到分隔线所在索引，跳过它
    const separatorIdx = tableLines.findIndex((line) => /^[\s|:-]+$/.test(line.trim()));
    const dataLines = separatorIdx >= 0 ? tableLines.slice(separatorIdx + 1) : tableLines.slice(1);

    const rows = dataLines.map(parseRow);

    return { columns, rows };
  } catch {
    return null;
  }
};

// 保留原有 marked 渲染，供 renderMessage 内部调用
const renderMarkdown = (text: string): string => {
  if (!text) return '';
  return marked.parse(text) as string;
};

// 渲染消息文本
const renderMessage = (text: string, role: 'user' | 'ai'): string => {
  if (!text) return '';

  // AI 消息：优先检测 JSON 表格
  if (role === 'ai') {
    const jsonData = parseJsonTable(text);
    if (jsonData) {
      return renderTable(jsonData);
    }

    // 检测 markdown 表格并转换
    const mdTableData = parseMarkdownTable(text);
    if (mdTableData) {
      return renderTable(mdTableData);
    }

    // 其他走原有 markdown 渲染
    return renderMarkdown(text);
  }

  // 用户消息直接返回
  return text;
};

// 渲染 JSON 表格为 HTML
const renderTable = (data: { title?: string; columns: string[]; rows: string[][] }): string => {
  const { title, columns, rows } = data;

  // 生成表头
  const headerCells = (columns ?? [])
    .map((col) => `<th style="background:#e8eff7;color:#152844;font-size:14px;padding:8px 12px;border:1px solid #ddd;text-align:left;font-weight:600;">${escapeHtml(col)}</th>`)
    .join('');

  // 生成数据行
  const bodyRows = (rows ?? [])
    .map(
      (row, rowIndex) =>
        `<tr style="background:${rowIndex % 2 === 0 ? '#fff' : '#fafafa'};">` +
        row.map((cell) => `<td style="padding:8px 12px;border:1px solid #ddd;color:#152844;font-size:14px;">${escapeHtml(cell)}</td>`).join('') +
        '</tr>',
    )
    .join('');

  const titleHtml = title ? `<div style="font-weight:600;color:#152844;font-size:14px;margin-bottom:8px;">${escapeHtml(title)}</div>` : '';

  return `
    <div style="margin:8px 0;overflow-x:auto;">
      ${titleHtml}
      <table style="border-collapse:collapse;width:100%;font-size:14px;table-layout:auto;">
        <thead><tr>${headerCells}</tr></thead>
        <tbody>${bodyRows}</tbody>
      </table>
    </div>
  `;
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

// 左侧用户卡片数据：当前先做 UI 结构，统计值从业务接口接入后再替换。
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
  // 用户中心页面在 root.ts 的子路由里，直接按 name 跳转即可。
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

  // SSE：RAG + LangChain4j 多轮记忆（userId 与后端会话隔离；未登录用 guest）
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
    const msg = chatMessages.value.find((m) => m.id === aiMsgId);
    if (msg) {
      if (msg.status === 'thinking') msg.status = 'speaking';
      msg.text += chunk;
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
  border: 1px solid #cedce4; // 保留最外层边框
  border-top: 2px solid #4684e2; // 上框蓝色
  border-bottom: 2px solid #4684e2; // 下框蓝色
  border-radius: 2px;
  padding: 0 0;
}

.user-card {
  width: 290px;
  background: transparent; // 外层由 consultation-box 统一控制边框
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
  background: transparent; // 外层由 consultation-box 统一控制边框
  display: flex;
  flex-direction: column;
  height: calc(100vh - 140px); // 留出 header/footer 的空间
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

  table {
    border-collapse: collapse;
    width: 100%;
    margin: 10px 0;
  }
  th, td {
    border: 1px solid #ddd;
    padding: 8px 12px;
    text-align: left;
  }
  th {
    background: #e8eff7;
    font-weight: 600;
  }
  tr:nth-child(even) {
    background: #fafafa;
  }
  tr:hover {
    background: #f0f7ff;
  }
  ul, ol {
    margin: 8px 0;
    padding-left: 20px;
  }
  li {
    margin: 4px 0;
  }
  code {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: monospace;
  }
  pre {
    background: #f5f5f5;
    padding: 10px;
    border-radius: 6px;
    overflow-x: auto;
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

