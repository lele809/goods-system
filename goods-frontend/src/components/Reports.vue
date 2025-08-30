<template>
  <div class="reports">
    <div class="page-header">
      <h2>统计报表</h2>
    </div>
    
    <el-card>
      <div class="coming-soon">
        <el-icon size="64" class="icon"><DataLine /></el-icon>
        <h3>统计报表功能</h3>
        <h2 style="font-weight: bolder; color: #1a1a1a">上班别太累，放松一下吧</h2>
        <p style= "font-weight: bold; margin-top: 10px;">有时间帮我开发一下吧</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// 统计报表页面组件
</script>

<style scoped>
.reports {
  /* padding已在Dashboard布局中提供 */
}

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