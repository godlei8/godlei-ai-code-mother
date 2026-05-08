<template>
  <div class="app-markdown-content" v-html="renderedHtml"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import 'highlight.js/styles/github.css'
import { renderChatMarkdown } from '@/utils/markdownRenderer'

const props = defineProps<{
  content: string
}>()

const renderedHtml = computed(() => renderChatMarkdown(props.content))
</script>

<style scoped>
.app-markdown-content {
  color: #0f172a;
  line-height: 1.85;
  word-break: break-word;
}

.app-markdown-content :deep(> :first-child) {
  margin-top: 0;
}

.app-markdown-content :deep(> :last-child) {
  margin-bottom: 0;
}

.app-markdown-content :deep(h1),
.app-markdown-content :deep(h2),
.app-markdown-content :deep(h3),
.app-markdown-content :deep(h4) {
  margin: 0 0 14px;
  color: #0f172a;
  font-weight: 700;
  line-height: 1.35;
}

.app-markdown-content :deep(h1) {
  font-size: 28px;
}

.app-markdown-content :deep(h2) {
  font-size: 22px;
}

.app-markdown-content :deep(h3) {
  font-size: 18px;
}

.app-markdown-content :deep(h4) {
  font-size: 16px;
}

.app-markdown-content :deep(p),
.app-markdown-content :deep(ul),
.app-markdown-content :deep(ol),
.app-markdown-content :deep(blockquote),
.app-markdown-content :deep(table),
.app-markdown-content :deep(pre) {
  margin: 0 0 14px;
}

.app-markdown-content :deep(ul),
.app-markdown-content :deep(ol) {
  padding-left: 20px;
}

.app-markdown-content :deep(li + li) {
  margin-top: 6px;
}

.app-markdown-content :deep(a) {
  color: #155eef;
  font-weight: 600;
  text-decoration: none;
}

.app-markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.app-markdown-content :deep(blockquote) {
  padding: 12px 14px;
  color: #475569;
  background: linear-gradient(135deg, rgb(241 245 249 / 88%), rgb(248 250 252 / 92%));
  border-left: 4px solid rgb(37 99 235 / 42%);
  border-radius: 16px;
}

.app-markdown-content :deep(code:not(pre code)) {
  padding: 2px 8px;
  font-family:
    'Cascadia Code',
    'JetBrains Mono',
    'SFMono-Regular',
    Consolas,
    monospace;
  font-size: 0.92em;
  color: #0f172a;
  background: rgb(226 232 240 / 72%);
  border-radius: 999px;
}

.app-markdown-content :deep(.chat-code-block) {
  padding: 16px 18px;
  overflow-x: auto;
  background:
    linear-gradient(180deg, rgb(255 255 255 / 96%), rgb(248 250 252 / 98%)),
    linear-gradient(135deg, rgb(37 99 235 / 8%), rgb(20 184 166 / 6%));
  border: 1px solid rgb(148 163 184 / 20%);
  border-radius: 18px;
  box-shadow:
    0 10px 24px rgb(15 23 42 / 4%),
    inset 0 1px 0 rgb(255 255 255 / 72%);
}

.app-markdown-content :deep(.chat-code-block code) {
  display: block;
  min-width: max-content;
  padding: 0;
  color: #0f172a;
  background: transparent;
  border-radius: 0;
}

.app-markdown-content :deep(.hljs) {
  font-family:
    'Cascadia Code',
    'JetBrains Mono',
    'SFMono-Regular',
    Consolas,
    monospace;
  font-size: 13px;
  line-height: 1.75;
  background: transparent;
}

.app-markdown-content :deep(table) {
  width: 100%;
  overflow: hidden;
  border-spacing: 0;
  border: 1px solid rgb(148 163 184 / 22%);
  border-radius: 18px;
}

.app-markdown-content :deep(th),
.app-markdown-content :deep(td) {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid rgb(148 163 184 / 16%);
}

.app-markdown-content :deep(th) {
  font-weight: 700;
  background: rgb(248 250 252 / 92%);
}

.app-markdown-content :deep(tr:last-child td) {
  border-bottom: 0;
}

.app-markdown-content :deep(img) {
  display: block;
  max-width: 100%;
  margin: 8px 0;
  border-radius: 16px;
}

.app-markdown-content :deep(hr) {
  margin: 18px 0;
  border: 0;
  border-top: 1px solid rgb(148 163 184 / 18%);
}
</style>
