<script setup>
import { RouterLink } from 'vue-router'
import heroFood from '@/assets/real/beef-rice.jpg'
</script>

<template>
  <div class="home">
    <div class="hero card">
      <div class="hero-media" :style="{ backgroundImage: `url(${heroFood})` }" />
      <div class="hero-content">
        <p class="badge">校园外卖 · 毕设演示</p>
        <h1>悦食汇</h1>
        <p class="lead">从点餐到经营，一套完整的校园餐饮系统。</p>

        <div class="actions">
          <RouterLink class="tile user" to="/user/login">
            <span class="tile-icon">🍜</span>
            <span class="tile-title">顾客端</span>
            <span class="tile-desc">浏览菜品、购物车、订单与支付</span>
          </RouterLink>
          <RouterLink class="tile merchant" to="/merchant/login">
            <span class="tile-icon">🏪</span>
            <span class="tile-title">商家端</span>
            <span class="tile-desc">菜品、员工、订单全流程管理</span>
          </RouterLink>
          <RouterLink class="tile rider" to="/rider/login">
            <span class="tile-icon">🛵</span>
            <span class="tile-title">骑手端</span>
            <span class="tile-desc">接单、取餐、送达的轻量化移动网页</span>
          </RouterLink>
        </div>
      </div>
    </div>

    <p class="hint">后端默认 <code>http://127.0.0.1:8080</code>，请先启动 Spring Boot。</p>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem 1rem;
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgba(194, 65, 12, 0.16), transparent),
    linear-gradient(180deg, #fffaf5 0%, #fff4e6 100%);
}

.hero {
  max-width: 880px;
  width: 100%;
  overflow: hidden;
  border-radius: 18px;
}

.hero-media {
  height: 220px;
  background-size: cover;
  background-position: center;
  position: relative;
}

.hero-media::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.05), rgba(0, 0, 0, 0.45));
}

.hero-content {
  padding: 1.4rem 1.5rem 1.5rem;
}

.badge {
  display: inline-block;
  margin: 0 0 0.65rem;
  padding: 0.25rem 0.65rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 700;
  color: #9a3412;
  background: #ffedd5;
}

h1 {
  margin: 0;
  font-size: 2.2rem;
  font-weight: 900;
  letter-spacing: -0.03em;
  color: #7c2d12;
}

.lead {
  margin: 0.6rem 0 1.2rem;
  color: #78716c;
}

.actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.9rem;
}

.tile {
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: auto auto;
  column-gap: 0.8rem;
  row-gap: 0.15rem;
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid #fed7aa;
  color: inherit;
  text-decoration: none;
  transition: transform 0.12s ease, box-shadow 0.15s ease;
}

.tile:hover {
  text-decoration: none;
  transform: translateY(-2px);
  box-shadow: 0 10px 26px -16px rgba(124, 45, 18, 0.7);
}

.tile.user { background: #fff7ed; }
.tile.merchant { background: #fff1e6; }
.tile.rider { background: #eff6ff; border-color: #bfdbfe; }

.tile-icon {
  grid-row: span 2;
  font-size: 1.9rem;
  line-height: 1;
}

.tile-title { font-weight: 800; }
.tile-desc { font-size: 0.85rem; color: #78716c; }

.hint {
  margin-top: 1.2rem;
  color: #78716c;
  font-size: 0.85rem;
}

@media (max-width: 760px) {
  .actions { grid-template-columns: 1fr; }
  .hero-media { height: 180px; }
}
</style>
