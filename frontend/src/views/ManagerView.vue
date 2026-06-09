<script setup lang="ts">
import MainLayout from '@/components/MainLayout.vue';
import { ref, onMounted } from 'vue';
import apiClient from '@/services/api';
import { useToastStore } from '@/stores/toast';
import { Search, User, X, Calendar, Loader2, Edit2, Trash2, Plus, FileCheck, CheckCircle, AlertCircle, Users } from 'lucide-vue-next';

const toast = useToastStore();

interface BreakRecord {
  id: number;
  breakStart: string;
  breakEnd: string | null;
  attendanceRecordId?: number;
}

interface AttendanceRecord {
  id: number;
  attendanceStart: string;
  attendanceEnd: string | null;
  attendanceType: string;
  breaks: BreakRecord[];
}

interface AttendanceList {
  id: number;
  month: string;
  year: number;
  approved: boolean;
  totalWorkedMinutes: number;
  totalOvertimeMinutes: number;
}

interface Employee {
  id: number;
  name: string;
  email: string;
  role: string;
  departmentName: string;
}

const searchQuery = ref('');
const employees = ref<Employee[]>([]);
const loading = ref(false);

// Modal state
const showModal = ref(false);
const selectedEmployee = ref<Employee | null>(null);
const attendanceRecords = ref<AttendanceRecord[]>([]);
const attendanceLists = ref<AttendanceList[]>([]);
const loadingAttendance = ref(false);
const isEditMode = ref(false);
const activeModalTab = ref<'attendance' | 'lists'>('attendance');

// Generation state
const genMonth = ref('MAY');
const genYear = ref(2026);

// Edit/Add state
const editingRecord = ref<Partial<AttendanceRecord> | null>(null);
const showEditForm = ref(false);

const handleSearch = async () => {
  loading.value = true;
  try {
    const response = await apiClient.get('/employees/search', {
      params: { query: searchQuery.value }
    });
    employees.value = response.data;
  } catch (err) {
    console.error('Search failed', err);
  } finally {
    loading.value = false;
  }
};

const departmentLists = ref<AttendanceList[]>([]);
const loadingDeptLists = ref(false);

const fetchDepartmentLists = async () => {
  loadingDeptLists.value = true;
  try {
    const response = await apiClient.get('/attendance-list/department');
    departmentLists.value = response.data;
  } catch (err) {
    console.error('Failed to fetch department lists', err);
  } finally {
    loadingDeptLists.value = false;
  }
};

// Initial load
onMounted(() => {
  handleSearch();
  fetchDepartmentLists();
});

const generateDepartmentList = async () => {
  try {
    const response = await apiClient.post('/attendance-list/generate', null, {
      params: { 
        month: genMonth.value,
        year: genYear.value
      }
    });
    toast.add('Docházkový list pro oddělení byl úspěšně vygenerován.', 'success');
    fetchDepartmentLists();
  } catch (err: any) {
    toast.add(err.response?.data?.message || 'Generování se nezdařilo.', 'error');
  }
};

const downloadPdf = (pdfData: string, month: string, year: number) => {
  try {
    const binaryString = window.atob(pdfData);
    const bytes = new Uint8Array(binaryString.length);
    for (let i = 0; i < binaryString.length; i++) {
      bytes[i] = binaryString.charCodeAt(i);
    }
    const blob = new Blob([bytes], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `dochazka_${month}_${year}.pdf`;
    link.click();
    window.URL.revokeObjectURL(url);
    toast.add('Soubor byl stažen.', 'info');
  } catch (e) {
    toast.add('Chyba při stahování souboru.', 'error');
  }
};

const openAttendanceModal = async (emp: Employee) => {
  selectedEmployee.value = emp;
  showModal.value = true;
  loadingAttendance.value = true;
  attendanceRecords.value = [];
  showEditForm.value = false;
  editingRecord.value = null;
  
  try {
    const response = await apiClient.get(`/attendance/employee/${emp.id}`);
    attendanceRecords.value = response.data;
  } catch (err) {
    console.error('Failed to fetch data', err);
  } finally {
    loadingAttendance.value = false;
  }
};

const approveList = async (listId: number) => {
  try {
    await apiClient.put(`/attendance-list/${listId}/approve`);
    const list = departmentLists.value.find(l => l.id === listId);
    if (list) list.approved = true;
    toast.add('Uzávěrka byla schválena.', 'success');
  } catch (err: any) {
    toast.add(err.response?.data?.message || 'Schválení se nezdařilo.', 'error');
  }
};

const closeModal = () => {
  showModal.value = false;
  selectedEmployee.value = null;
  attendanceRecords.value = [];
  isEditMode.value = false;
};

const formatDate = (isoString: string) => {
  return new Date(isoString).toLocaleDateString('cs-CZ');
};

const formatTime = (isoString: string | null) => {
  if (!isoString) return '--:--';
  return new Date(isoString).toLocaleTimeString('cs-CZ', { hour: '2-digit', minute: '2-digit' });
};

const formatMinutes = (minutes: number) => {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
};

const formatDateTimeLocal = (isoString: string | null) => {
  if (!isoString) return '';
  const date = new Date(isoString);
  const tzOffset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - tzOffset).toISOString().slice(0, 16);
};

const calculateDuration = (start: string, end: string | null, breaks: BreakRecord[]) => {
  if (!end) return 'Probíhá';
  const startTime = new Date(start).getTime();
  const endTime = new Date(end).getTime();
  let totalMs = endTime - startTime;
  breaks.forEach(b => {
    if (b.breakStart && b.breakEnd) {
      totalMs -= (new Date(b.breakEnd).getTime() - new Date(b.breakStart).getTime());
    }
  });
  const hours = Math.floor(totalMs / (1000 * 60 * 60));
  const minutes = Math.floor((totalMs % (1000 * 60 * 60)) / (1000 * 60));
  return `${hours}h ${minutes}m`;
};

const calculateTotalBreaks = (breaks: BreakRecord[]) => {
  let totalMs = 0;
  breaks.forEach(b => {
    if (b.breakStart && b.breakEnd) {
      totalMs += (new Date(b.breakEnd).getTime() - new Date(b.breakStart).getTime());
    }
  });
  const minutes = Math.floor(totalMs / (1000 * 60));
  return minutes > 0 ? `${minutes}m` : '0m';
};

const translateMonth = (month: string) => {
  const months: Record<string, string> = {
    'JANUARY': 'Leden', 'FEBRUARY': 'Únor', 'MARCH': 'Březen', 'APRIL': 'Duben',
    'MAY': 'Květen', 'JUNE': 'Červen', 'JULY': 'Červenec', 'AUGUST': 'Srpen',
    'SEPTEMBER': 'Září', 'OCTOBER': 'Říjen', 'NOVEMBER': 'Listopad', 'DECEMBER': 'Prosinec'
  };
  return months[month] || month;
};

const startEdit = (record: AttendanceRecord) => {
  editingRecord.value = { 
    ...record,
    attendanceStart: formatDateTimeLocal(record.attendanceStart),
    attendanceEnd: formatDateTimeLocal(record.attendanceEnd),
    breaks: record.breaks.map(b => ({
      ...b,
      breakStart: formatDateTimeLocal(b.breakStart),
      breakEnd: formatDateTimeLocal(b.breakEnd)
    }))
  };
  showEditForm.value = true;
};

const startAdd = () => {
  const now = new Date();
  editingRecord.value = {
    attendanceStart: formatDateTimeLocal(now.toISOString()),
    attendanceEnd: formatDateTimeLocal(now.toISOString()),
    attendanceType: 'IN_WORK',
    employeeId: selectedEmployee.value?.id as any,
    breaks: []
  };
  showEditForm.value = true;
};

const cancelEdit = () => {
  showEditForm.value = false;
  editingRecord.value = null;
};

const addBreakToEdit = () => {
  const startTime = editingRecord.value?.attendanceStart || new Date().toISOString();
  editingRecord.value?.breaks?.push({
    id: 0,
    breakStart: startTime,
    breakEnd: startTime,
    attendanceRecordId: editingRecord.value?.id
  });
};

const removeBreakFromEdit = async (index: number, breakId: number) => {
  if (breakId !== 0) {
    if (!confirm('Opravdu chcete smazat tuto pauzu z databáze?')) return;
    try {
      await apiClient.delete(`/break-record/${breakId}`);
      toast.add('Pauza byla smazána.', 'success');
    } catch (err) {
      toast.add('Smazání pauzy se nezdařilo.', 'error');
      return;
    }
  }
  editingRecord.value?.breaks?.splice(index, 1);
};

const saveRecord = async () => {
  if (!editingRecord.value || !selectedEmployee.value) return;

  // Prepare correctly formatted dates for the main payload
  const payload = {
    ...editingRecord.value,
    attendanceStart: new Date(editingRecord.value.attendanceStart as string).toISOString(),
    attendanceEnd: editingRecord.value.attendanceEnd ? new Date(editingRecord.value.attendanceEnd as string).toISOString() : null,
    employeeId: selectedEmployee.value.id,
    breaks: editingRecord.value.breaks?.map(b => ({
      ...b,
      breakStart: new Date(b.breakStart).toISOString(),
      breakEnd: b.breakEnd ? new Date(b.breakEnd).toISOString() : null
    }))
  };

  try {
    let savedRecord: any;
    if (editingRecord.value.id) {
      const response = await apiClient.put(`/attendance-record/${editingRecord.value.id}`, payload);
      savedRecord = response.data;
    } else {
      const response = await apiClient.post('/attendance-record', payload);
      savedRecord = response.data;
    }

    if (editingRecord.value.breaks) {
      for (const b of editingRecord.value.breaks) {
        const breakPayload = {
          ...b,
          breakStart: new Date(b.breakStart).toISOString(),
          breakEnd: b.breakEnd ? new Date(b.breakEnd).toISOString() : null,
          attendanceRecordId: savedRecord.id
        };
        if (b.id === 0) {
          await apiClient.post('/break-record', breakPayload);
        } else {
          await apiClient.put(`/break-record/${b.id}`, breakPayload);
        }
      }
    }

    const response = await apiClient.get(`/attendance/employee/${selectedEmployee.value.id}`);
    attendanceRecords.value = response.data;
    toast.add('Záznam byl úspěšně uložen.', 'success');
    cancelEdit();
  } catch (err) {
    toast.add('Uložení se nezdařilo.', 'error');
  }
};

const deleteRecord = async (id: number) => {
  if (!confirm('Opravdu chcete smazat tento záznam?')) return;
  try {
    await apiClient.delete(`/attendance-record/${id}`);
    attendanceRecords.value = attendanceRecords.value.filter(r => r.id !== id);
    toast.add('Záznam byl smazán.', 'success');
  } catch (err) {
    toast.add('Smazání se nezdařilo.', 'error');
  }
};
</script>

<template>
  <MainLayout>
    <div class="manager-view">
      <div class="header-section">
        <h1>Správa týmu</h1>
        <p>Hromadné uzavírání měsíce a správa docházky podřízených.</p>
      </div>

      <!-- Department Management Card -->
      <div class="department-mgmt-card">
        <div class="dept-header">
          <div class="dept-title-group">
            <Users :size="24" />
            <h2>Uzávěrka oddělení</h2>
          </div>
          <p class="dept-subtitle">Hromadné generování docházkových listů pro všechny podřízené.</p>
        </div>
        <div class="dept-content">
          <div class="gen-form">
            <div class="input-group">
              <label>Měsíc</label>
              <select v-model="genMonth">
                <option value="JANUARY">Leden</option>
                <option value="FEBRUARY">Únor</option>
                <option value="MARCH">Březen</option>
                <option value="APRIL">Duben</option>
                <option value="MAY">Květen</option>
                <option value="JUNE">Červen</option>
                <option value="JULY">Červenec</option>
                <option value="AUGUST">Srpen</option>
                <option value="SEPTEMBER">Září</option>
                <option value="OCTOBER">Říjen</option>
                <option value="NOVEMBER">Listopad</option>
                <option value="DECEMBER">Prosinec</option>
              </select>
            </div>
            <div class="input-group">
              <label>Rok</label>
              <input type="number" v-model="genYear" />
            </div>
            <button @click="generateDepartmentList" class="btn-generate-bulk">
              <FileCheck :size="20" />
              <span>Uzavřít měsíc pro oddělení</span>
            </button>
          </div>

          <div class="dept-reports-list" v-if="departmentLists.length > 0">
            <h3>Historie uzávěrek</h3>
            <div class="reports-grid">
              <div v-for="list in departmentLists" :key="list.id" class="report-item">
                <div class="report-info">
                  <span class="report-period">{{ translateMonth(list.month) }} {{ list.year }}</span>
                  <span :class="['status-badge', { approved: list.approved }]">
                    {{ list.approved ? 'Schváleno' : 'Ke kontrole' }}
                  </span>
                </div>
                <div class="report-actions">
                  <button @click="downloadPdf(list.pdfData, list.month, list.year)" class="btn-download-sm">
                    <FileCheck :size="16" />
                    <span>Zobrazit/Stáhnout</span>
                  </button>
                  <button v-if="!list.approved" @click="approveList(list.id)" class="btn-approve-sm">
                    <CheckCircle :size="16" />
                    <span>Schválit</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="loadingDeptLists" class="reports-loading">
            <Loader2 class="animate-spin" :size="20" />
            <span>Načítám uzávěrky...</span>
          </div>
        </div>
      </div>

      <div class="search-section">
        <div class="search-bar">
          <Search :size="20" class="search-icon" />
          <input 
            v-model="searchQuery" 
            @input="handleSearch"
            type="text" 
            placeholder="Vyhledat zaměstnance..."
          />
        </div>
      </div>

      <div class="results-section">
        <div v-if="loading" class="loading-state">
          <Loader2 class="animate-spin" :size="32" />
          <p>Vyhledávám...</p>
        </div>
        
        <div v-else-if="employees.length > 0" class="employee-list">
          <div v-for="emp in employees" :key="emp.id" class="employee-card">
            <div class="emp-info">
              <div class="emp-avatar">
                <User :size="24" />
              </div>
              <div class="emp-details">
                <h3>{{ emp.name }}</h3>
                <p>{{ emp.email }}</p>
              </div>
            </div>
            
            <div class="emp-meta">
              <span class="dept-badge">{{ emp.departmentName }}</span>
              <span class="role-badge">{{ emp.role }}</span>
            </div>
            
            <div class="emp-actions">
              <button @click="openAttendanceModal(emp)" class="btn-primary">Spravovat docházku</button>
            </div>
          </div>
        </div>
        
        <div v-else-if="searchQuery" class="no-results">
          Nebyly nalezeni žádní zaměstnanci odpovídající dotazu.
        </div>
      </div>

      <!-- Attendance Modal -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal-content">
          <div class="modal-header">
            <div class="modal-title">
              <Calendar :size="24" />
              <h2>Docházka: {{ selectedEmployee?.name }}</h2>
            </div>
            <div class="modal-actions-top">
              <button @click="startAdd" class="btn-add">
                <Plus :size="18" />
                <span>Přidat záznam</span>
              </button>
              <button @click="closeModal" class="close-btn">
                <X :size="24" />
              </button>
            </div>
          </div>

          <div class="modal-body">
            <div v-if="loadingAttendance" class="modal-loading">
              <Loader2 class="animate-spin" :size="32" />
              <p>Načítám data...</p>
            </div>
            
            <div v-else>
              <div v-if="attendanceRecords.length > 0" class="modal-table-container">
                <table class="modal-table">
                  <thead>
                    <tr>
                      <th>Datum</th>
                      <th>Příchod</th>
                      <th>Odchod</th>
                      <th>Pauzy</th>
                      <th>Celkem</th>
                      <th>Akce</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="record in attendanceRecords" :key="record.id">
                      <td>{{ formatDate(record.attendanceStart) }}</td>
                      <td>{{ formatTime(record.attendanceStart) }}</td>
                      <td>{{ formatTime(record.attendanceEnd) }}</td>
                      <td>{{ calculateTotalBreaks(record.breaks) }}</td>
                      <td class="font-bold">{{ calculateDuration(record.attendanceStart, record.attendanceEnd, record.breaks) }}</td>
                      <td class="row-actions">
                        <button @click="startEdit(record)" title="Upravit"><Edit2 :size="16" /></button>
                        <button @click="deleteRecord(record.id)" title="Smazat" class="btn-delete"><Trash2 :size="16" /></button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div v-else class="modal-empty">Žádné záznamy docházky.</div>
            </div>
          </div>
        </div>

        <!-- Edit/Add Form Overlay -->
        <div v-if="showEditForm" class="edit-form-overlay" @click.self="cancelEdit">
          <div class="edit-form-content">
            <h3>{{ editingRecord?.id ? 'Upravit záznam' : 'Nový záznam' }}</h3>
            <div class="scroll-area">
              <div class="form-section">
                <h4>Pracovní doba</h4>
                <div class="form-group"><label>Příchod</label><input type="datetime-local" v-model="editingRecord!.attendanceStart" /></div>
                <div class="form-group"><label>Odchod</label><input type="datetime-local" v-model="editingRecord!.attendanceEnd" /></div>
              </div>
              <div class="form-section">
                <div class="section-header"><h4>Pauzy</h4><button @click="addBreakToEdit" class="btn-icon-add"><Plus :size="16" /></button></div>
                <div v-for="(b, index) in editingRecord?.breaks" :key="index" class="break-edit-row">
                  <div class="form-group"><label>Od</label><input type="datetime-local" v-model="b.breakStart" /></div>
                  <div class="form-group"><label>Do</label><input type="datetime-local" v-model="b.breakEnd" /></div>
                  <button @click="removeBreakFromEdit(index, b.id)" class="btn-remove-break"><Trash2 :size="16" /></button>
                </div>
              </div>
            </div>
            <div class="form-actions">
              <button @click="cancelEdit" class="btn-outline">Zrušit</button>
              <button @click="saveRecord" class="btn-primary">Uložit</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<style scoped>
.manager-view { max-width: 1000px; margin: 0 auto; padding-bottom: 4rem; }

.header-section {
  margin-bottom: 2rem;
}

.header-section h1 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.header-section p {
  color: #64748b;
  margin-top: 0.25rem;
}

.department-mgmt-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  margin-bottom: 2.5rem;
  border: 1px solid #e2e8f0;
}

.dept-header {
  margin-bottom: 1.5rem;
}

.dept-title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: #4f46e5;
}

.dept-title-group h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #1e293b;
}

.dept-subtitle {
  margin: 0.25rem 0 0 0;
  font-size: 0.875rem;
  color: #64748b;
}

.gen-form {
  display: flex;
  align-items: flex-end;
  gap: 1.5rem;
  background: #f8fafc;
  padding: 1.25rem;
  border-radius: 12px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.input-group label {
  font-size: 0.75rem;
  font-weight: 700;
  color: #475569;
  text-transform: uppercase;
}

.input-group select, .input-group input {
  padding: 0.5rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  font-size: 0.875rem;
  background: white;
}

.btn-generate-bulk {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-generate-bulk:hover {
  background: #4338ca;
  transform: translateY(-1px);
}

.dept-reports-list {
  margin-top: 2rem;
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.dept-reports-list h3 {
  font-size: 1rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.report-item {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 0.2s;
}

.report-item:hover {
  border-color: #4f46e5;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.report-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-period {
  font-weight: 700;
  color: #1e293b;
}

.report-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-download-sm, .btn-approve-sm {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.375rem;
  padding: 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-download-sm {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-download-sm:hover {
  background: #e2e8f0;
}

.btn-approve-sm {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.btn-approve-sm:hover {
  background: #bbf7d0;
}

.reports-loading {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #64748b;
  padding: 1rem;
  font-size: 0.875rem;
}

.search-section { margin-bottom: 2rem; }
.search-bar { position: relative; display: flex; align-items: center; }
.search-icon { position: absolute; left: 1rem; color: #94a3b8; }
.search-bar input { width: 100%; padding: 1rem 1rem 1rem 3rem; border-radius: 12px; border: 1px solid #e2e8f0; background-color: white; font-size: 1rem; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1); }

.employee-list { display: grid; gap: 1rem; }
.employee-card { background: white; padding: 1.5rem; border-radius: 12px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1); border: 1px solid #f1f5f9; }
.emp-info { display: flex; align-items: center; gap: 1rem; flex: 1; }
.emp-avatar { width: 48px; height: 48px; background-color: #f1f5f9; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #64748b; }
.emp-details h3 { margin: 0; font-size: 1.125rem; color: #1e293b; }
.emp-details p { margin: 0; font-size: 0.875rem; color: #64748b; }
.emp-meta { display: flex; gap: 0.5rem; margin: 0 2rem; }
.dept-badge, .role-badge { padding: 0.25rem 0.75rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 600; }
.dept-badge { background-color: #e0f2fe; color: #0369a1; }
.role-badge { background-color: #f1f5f9; color: #475569; }
.emp-actions { display: flex; gap: 0.75rem; }
.btn-primary { padding: 0.5rem 1rem; background-color: #4f46e5; color: white; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; }
.btn-outline { padding: 0.5rem 1rem; background-color: transparent; border: 1px solid #e2e8f0; color: #475569; border-radius: 6px; font-weight: 500; cursor: pointer; }

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem;
  color: #64748b;
  gap: 1rem;
}

.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 50; padding: 1rem; }
.modal-content { background: white; border-radius: 16px; width: 100%; max-width: 900px; max-height: 90vh; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25); }
.modal-header { padding: 1.5rem; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; }
.modal-title { display: flex; align-items: center; gap: 0.75rem; color: #4f46e5; }
.modal-title h2 { margin: 0; font-size: 1.25rem; color: #1e293b; }
.modal-actions-top { display: flex; align-items: center; gap: 1rem; }
.btn-add { display: flex; align-items: center; gap: 0.5rem; padding: 0.5rem 1rem; color: white; border: none; border-radius: 8px; font-weight: 600; font-size: 0.875rem; cursor: pointer; background-color: #10b981; }
.close-btn { background: none; border: none; color: #64748b; cursor: pointer; padding: 0.5rem; border-radius: 8px; }
.modal-tabs { display: flex; padding: 0 1.5rem; border-bottom: 1px solid #e2e8f0; }
.modal-tab-btn { padding: 1rem 1.5rem; border: none; background: none; font-weight: 600; color: #64748b; cursor: pointer; position: relative; }
.modal-tab-btn.active { color: #4f46e5; }
.modal-tab-btn.active::after { content: ''; position: absolute; bottom: -1px; left: 0; right: 0; height: 2px; background-color: #4f46e5; }
.modal-body { padding: 1.5rem; overflow-y: auto; }
.modal-table-container { border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
.modal-table { width: 100%; border-collapse: collapse; }
.modal-table th { background-color: #f8fafc; padding: 0.75rem 1rem; text-align: left; font-size: 0.875rem; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.modal-table td { padding: 0.75rem 1rem; font-size: 0.875rem; border-bottom: 1px solid #f1f5f9; }

.status-badge { padding: 0.25rem 0.5rem; border-radius: 9999px; font-size: 0.7rem; font-weight: 700; background: #fee2e2; color: #991b1b; }
.status-badge.approved { background: #dcfce7; color: #166534; }
.row-actions { display: flex; gap: 0.5rem; }
.btn-approve { 
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.75rem;
  cursor: pointer;
}
.btn-approve:hover { background: #bbf7d0; }
.text-success { color: #10b981; }
.has-overtime { color: #ef4444; font-weight: 700; }

.edit-form-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 60; padding: 1rem; }
.edit-form-content { background: white; padding: 2rem; border-radius: 12px; width: 100%; max-width: 500px; display: flex; flex-direction: column; max-height: 80vh; }
.scroll-area { overflow-y: auto; margin-bottom: 1.5rem; }
.form-section { margin-bottom: 2rem; border-bottom: 1px solid #f1f5f9; padding-bottom: 1rem; }
.form-group { margin-bottom: 1rem; }
.form-group label { display: block; font-size: 0.875rem; color: #64748b; margin-bottom: 0.25rem; }
.form-group input { width: 100%; padding: 0.6rem; border: 1px solid #e2e8f0; border-radius: 8px; }
.break-edit-row { display: grid; grid-template-columns: 1fr 1fr auto; gap: 0.75rem; align-items: flex-end; background: #f8fafc; padding: 0.75rem; border-radius: 8px; margin-bottom: 0.75rem; }
.form-actions { display: flex; justify-content: flex-end; gap: 0.75rem; padding-top: 1rem; border-top: 1px solid #e2e8f0; }

.animate-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.modal-empty { text-align: center; padding: 3rem; color: #64748b; font-style: italic; }
</style>
