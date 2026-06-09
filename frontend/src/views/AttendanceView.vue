<script setup lang="ts">
import MainLayout from '@/components/MainLayout.vue';
import { ref, onMounted, computed } from 'vue';
import apiClient from '@/services/api';
import { Calendar, Loader2, FileText, CheckCircle, Clock } from 'lucide-vue-next';

interface AttendanceList {
  id: number;
  month: string;
  year: number;
  createdAt: string;
  approved: boolean;
  totalWorkedMinutes: number;
  totalOvertimeMinutes: number;
  pdfData: string | null;
}

interface BreakRecord {
  id: number;
  breakStart: string;
  breakEnd: string | null;
}

interface AttendanceRecord {
  id: number;
  attendanceStart: string;
  attendanceEnd: string | null;
  attendanceType: string;
  breaks: BreakRecord[];
}

const records = ref<AttendanceRecord[]>([]);
const attendanceLists = ref<AttendanceList[]>([]);
const loading = ref(true);
const error = ref<string | null>(null);
const activeTab = ref<'history' | 'lists'>('history');

const fetchAttendance = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get('/attendance/my-attendance');
    records.value = response.data;
  } catch (err: any) {
    error.value = 'Nepodařilo se načíst data docházky.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

onMounted(fetchAttendance);
</script>

<template>
  <MainLayout>
    <div class="attendance-view">
      <div class="header-card">
        <Calendar :size="48" />
        <div class="header-text">
          <h1>Moje docházka</h1>
          <p>Přehled odpracovaných hodin a historie záznamů.</p>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <Loader2 class="animate-spin" :size="48" />
        <p>Načítám data docházky...</p>
      </div>

      <div v-else-if="error" class="error-state">
        <p>{{ error }}</p>
        <button @click="fetchAttendance" class="retry-btn">Zkusit znovu</button>
      </div>

      <div v-else class="content-fade-in">
        <div class="history-section">
          <div class="history-table">
            <table>
              <thead>
                <tr>
                  <th>Datum</th>
                  <th>Příchod</th>
                  <th>Odchod</th>
                  <th>Pauzy</th>
                  <th>Celkem</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="record in records" :key="record.id">
                  <td>{{ formatDate(record.attendanceStart) }}</td>
                  <td>{{ formatTime(record.attendanceStart) }}</td>
                  <td>{{ formatTime(record.attendanceEnd) }}</td>
                  <td>{{ calculateTotalBreaks(record.breaks) }}</td>
                  <td class="font-bold">{{ calculateDuration(record.attendanceStart, record.attendanceEnd, record.breaks) }}</td>
                </tr>
                <tr v-if="records.length === 0">
                  <td colspan="5" class="empty-state">Zatím nemáte žádné záznamy docházky.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<style scoped>
.attendance-view {
  max-width: 1000px;
  margin: 0 auto;
}

.header-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background: white;
  padding: 2rem;
  border-radius: 16px;
  margin-bottom: 2rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  color: #4f46e5;
}

.header-text h1 {
  margin: 0;
  font-size: 1.75rem;
  color: #1e293b;
}

.header-text p {
  margin: 0.25rem 0 0 0;
  color: #64748b;
}

.tabs {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 1px;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border: none;
  background: none;
  color: #64748b;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.tab-btn:hover {
  color: #4f46e5;
}

.tab-btn.active {
  color: #4f46e5;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #4f46e5;
}

.history-table {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  text-align: left;
  padding: 1rem;
  background-color: #f8fafc;
  font-size: 0.875rem;
  font-weight: 600;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

td {
  padding: 1rem;
  font-size: 0.875rem;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
}

.font-bold {
  font-weight: 700;
}

/* Lists Styles */
.lists-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.list-card {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1px solid #f1f5f9;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.list-header h3 {
  margin: 0;
  font-size: 1.125rem;
  color: #1e293b;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  background-color: #fef3c7;
  color: #92400e;
}

.status-badge.approved {
  background-color: #dcfce7;
  color: #166534;
}

.list-stat {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.list-stat .label {
  color: #64748b;
}

.list-stat .value {
  font-weight: 600;
  color: #1e293b;
}

.has-overtime {
  color: #ef4444 !important;
}

.list-footer {
  margin-top: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid #f1f5f9;
}

.btn-download {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.6rem;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-download:hover:not(:disabled) {
  background-color: #f8fafc;
  border-color: #cbd5e1;
}

.btn-download:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.empty-lists {
  grid-column: 1 / -1;
  text-align: center;
  padding: 4rem;
  background: white;
  border-radius: 12px;
  color: #64748b;
  font-style: italic;
}

/* General States */
.loading-state, .error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  background: white;
  border-radius: 16px;
  gap: 1rem;
}

.animate-spin {
  animation: spin 1s linear infinite;
  color: #4f46e5;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.retry-btn {
  padding: 0.5rem 1rem;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.content-fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #64748b;
  font-style: italic;
}
</style>
