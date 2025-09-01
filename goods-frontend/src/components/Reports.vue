<template>
  <div class="reports">
    <div class="page-header">
      <h2>统计报表</h2>
    </div>
    
    <el-card>
      <div class="coming-soon">
        <h3>统计报表功能开发中</h3>
        <h2 style="font-weight: bolder; color: #1a1a1a">上班别太累，放松一下吧</h2>
        <p style= "font-weight: bold; margin-top: 10px;">想你了，有时间帮我开发一下吧</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// 统计报表页面组件
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.coming-soon {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.coming-soon .icon {
  color: #409eff;
  margin-bottom: 20px;
}

.bubu-animation {
  margin-bottom: 20px;
}

.bubu-gif {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.bubu-gif:hover {
  transform: scale(1.05);
}

.coming-soon h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}

.coming-soon p {
  margin: 0;
  font-size: 14px;
}
</style> 