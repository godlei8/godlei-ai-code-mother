<template>
  <section class="home-view">
    <div class="hero">
      <p class="eyebrow">Global Layout Ready</p>
      <h1>欢迎来到 Godlei AI 代码应用生成平台</h1>
      <p class="description">
        这里已经接入了通用的全局基础布局。你可以继续在当前内容区域中按路由扩展业务页面。
      </p>
    </div>
  </section>
</template>

<style scoped>
.home-view {
  min-height: 100%;
}

.hero {
  padding: clamp(28px, 6vw, 56px);
  background: rgb(255 255 255 / 85%);
  border: 1px solid rgb(148 163 184 / 18%);
  border-radius: 28px;
  box-shadow: 0 18px 50px rgb(15 23 42 / 10%);
}

.eyebrow {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

h1 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(32px, 5vw, 48px);
  line-height: 1.15;
}

.description {
  max-width: 720px;
  margin: 18px 0 0;
  color: #475569;
  font-size: 16px;
  line-height: 1.75;
}
</style>
