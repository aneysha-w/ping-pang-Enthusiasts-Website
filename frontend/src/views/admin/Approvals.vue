<template>
  <div>
    <div class="pp-card">
      <div class="pp-card-header">
        <h3>📋 俱乐部加入申请</h3>
        <el-radio-group v-model="filterStatus" @change="loadRequests">
          <el-radio-button label="PENDING">待审核</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="REJECTED">已拒绝</el-radio-button>
          <el-radio-button label="">全部</el-radio-button>
        </el-radio-group>
      </div>
      <div class="pp-card-body" style="padding: 0;">
        <div v-if="requests.length" class="request-list">
          <div v-for="r in requests" :key="r.id" class="request-item">
            <div class="request-info">
              <span class="request-id">#{{ r.id }}</span>
              <span class="request-detail">俱乐部 #{{ r.clubId }} · 用户 #{{ r.applicantId }}</span>
              <span class="pp-badge" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
            </div>
            <div class="request-meta">
              <span class="request-time">申请: {{ formatDate(r.applyTime) }}</span>
              <span v-if="r.rejectReason" class="request-reason">原因: {{ r.rejectReason }}</span>
            </div>
            <div v-if="r.status === 'PENDING'" class="request-actions">
              <el-button type="success" size="small" @click="handleApprove(r.id, true)">通过</el-button>
              <el-button type="danger" size="small" @click="openRejectDialog(r.id)">拒绝</el-button>
            </div>
          </div>
        </div>
        <div v-else class="pp-empty" style="padding: 60px;">暂无申请记录</div>
      </div>
    </div>

    <el-dialog v-model="rejectDialog" title="拒绝申请" width="400px">
      <el-form label-position="top">
        <el-form-item label="拒绝原因"><el-input v-model="rejectReason" type="textarea" placeholder="请输入拒绝原因" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialog = false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api'

const requests = ref([])
const filterStatus = ref('PENDING')
const rejectDialog = ref(false)
const rejectReason = ref('')
const currentRequestId = ref(null)

const statusText = (s) => ({ PENDING: '待审核', APPROVED: '已通过', REJECTED: '已拒绝' }[s] || s)
const statusClass = (s) => ({ PENDING: 'pp-badge-warning', APPROVED: 'pp-badge-success', REJECTED: 'pp-badge-danger' }[s] || 'pp-badge-primary')
const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadRequests = async () => {
  try {
    const res = await api.get('/admin/join-requests', { params: { status: filterStatus.value } })
    if (res.code === 0) requests.value = res.data
  } catch (e) { ElMessage.error(e.message || '加载失败') }
}
const handleApprove = async (id, approved) => {
  try {
    const res = await api.put(`/admin/join-requests/${id}`, { approved })
    if (res.code === 0) { ElMessage.success('已通过'); loadRequests() }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}
const openRejectDialog = (id) => { currentRequestId.value = id; rejectReason.value = ''; rejectDialog.value = true }
const handleReject = async () => {
  try {
    const res = await api.put(`/admin/join-requests/${currentRequestId.value}`, { approved: false, rejectReason: rejectReason.value })
    if (res.code === 0) { ElMessage.success('已拒绝'); rejectDialog.value = false; loadRequests() }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '操作失败') }
}
onMounted(loadRequests)
</script>

<style scoped>
.request-list { padding: 8px 0; }
.request-item {
  padding: 16px 24px; border-bottom: 1px solid #f5f5f5;
  display: flex; flex-direction: column; gap: 8px;
}
.request-item:last-child { border-bottom: none; }
.request-info { display: flex; align-items: center; gap: 12px; }
.request-id { font-weight: 600; color: var(--pp-text); }
.request-detail { font-size: 14px; color: var(--pp-text-secondary); flex: 1; }
.request-meta { display: flex; gap: 16px; font-size: 12px; color: var(--pp-text-light); }
.request-reason { color: var(--pp-danger); }
.request-actions { display: flex; gap: 8px; }
</style>
