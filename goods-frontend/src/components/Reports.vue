<template>
  <div class="reports">
    <div class="page-header">
      <h2>统计报表</h2>
    </div>
    
    <el-card>
      <div class="coming-soon">
        <div class="bubu-animation">
          <div class="bubu-character">
            <div class="bubu-face">
              <div class="bubu-eyes">
                <div class="eye left"></div>
                <div class="eye right"></div>
              </div>
              <div class="bubu-mouth"></div>
            </div>
            <div class="bubu-body"></div>
          </div>
        </div>
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
  display: flex;
  justify-content: center;
}

.bubu-character {
  width: 120px;
  height: 120px;
  position: relative;
  animation: bubu-bounce 2s infinite ease-in-out;
}

.bubu-face {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
  border-radius: 50%;
  position: relative;
  margin: 0 auto;
  box-shadow: 0 4px 15px rgba(255, 154, 158, 0.3);
}

.bubu-eyes {
  position: absolute;
  top: 25px;
  width: 100%;
  display: flex;
  justify-content: space-around;
  padding: 0 20px;
}

.eye {
  width: 8px;
  height: 8px;
  background: #333;
  border-radius: 50%;
  animation: bubu-blink 3s infinite;
}

.bubu-mouth {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 10px;
  border: 2px solid #ff6b6b;
  border-top: none;
  border-radius: 0 0 20px 20px;
}

.bubu-body {
  width: 60px;
  height: 40px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  border-radius: 30px;
  margin: 10px auto 0;
  box-shadow: 0 2px 10px rgba(255, 154, 158, 0.2);
}

@keyframes bubu-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes bubu-blink {
  0%, 90%, 100% {
    transform: scaleY(1);
  }
  95% {
    transform: scaleY(0.1);
  }
}

.bubu-character:hover {
  animation-duration: 1s;
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