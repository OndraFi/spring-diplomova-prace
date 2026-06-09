<script setup lang="ts">
import MainLayout from '@/components/MainLayout.vue';
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useToastStore } from '@/stores/toast';
import apiClient from '@/services/api';
import { 
  LogIn, 
  LogOut, 
  Coffee, 
  Play, 
  Square,
  CheckCircle2,
  AlertCircle
} from 'lucide-vue-next';

const auth = useAuthStore();
const toast = useToastStore();
const loading = ref(false);

const handleClockAction = async (action: string) => {
  if (!auth.user?.cardId) {
    toast.add('Chybí ID karty zaměstnance.', 'error');
    return;
  }

  loading.value = true;
  try {
    let endpoint = '';
    switch (action) {
      case 'in': endpoint = '/attendance/clock-in'; break;
      case 'out': endpoint = '/attendance/clock-out'; break;
      case 'break-start': endpoint = '/attendance/break-start'; break;
      case 'break-end': endpoint = '/attendance/break-end'; break;
    }

    await apiClient.post(endpoint, null, {
      params: { cardId: auth.user.cardId }
    });

    const messages: Record<string, string> = {
      'in': 'Příchod úspěšně zaznamenán.',
      'out': 'Odchod úspěšně zaznamenán.',
      'break-start': 'Pauza zahájena.',
      'break-end': 'Pauza ukončena.'
    };
    
    toast.add(messages[action], 'success');
  } catch (err: any) {
    toast.add(err.response?.data?.message || 'Akce se nezdařila.', 'error');
  } finally {
    loading.value = false;
  }
};

const formatContractType = (type: string | undefined) => {
  if (!type) return 'Neuvedeno';
  const mapping: Record<string, string> = {
    'HPP_NA_DOBU_NEURCITOU': 'HPP (Doba neurčitá)',
    'HPP_NA_DOBU_URCITOU': 'HPP (Doba určitá)',
    'DPP': 'DPP',
    'DPC': 'DPČ',
    'ICO': 'IČO (Externista)'
  };
  return mapping[type] || type;
};
</script>

<template>
  <MainLayout>
    <div class="dashboard">
      <div class="welcome-card">
        <h1>Dobrý den, {{ auth.user?.name }}!</h1>
        <p>Vítejte v docházkovém systému. Vyberte akci pro záznam času.</p>
      </div>

      <div class="action-grid">
        <button 
          @click="handleClockAction('in')" 
          class="action-btn clock-in"
          :disabled="loading"
        >
          <LogIn :size="32" />
          <span>Příchod</span>
        </button>

        <button 
          @click="handleClockAction('out')" 
          class="action-btn clock-out"
          :disabled="loading"
        >
          <LogOut :size="32" />
          <span>Odchod</span>
        </button>

        <button 
          @click="handleClockAction('break-start')" 
          class="action-btn break-start"
          :disabled="loading"
        >
          <Play :size="32" />
          <span>Začít pauzu</span>
        </button>

        <button 
          @click="handleClockAction('break-end')" 
          class="action-btn break-end"
          :disabled="loading"
        >
          <Square :size="32" />
          <span>Konec pauzy</span>
        </button>
      </div>

      <div class="info-cards">
        <div class="info-card">
          <h3>Pracovní poměr</h3>
          <p>{{ formatContractType(auth.user?.contractType) }}</p>
          <span class="badge">{{ auth.user?.departmentName || 'Bez oddělení' }}</span>
        </div>
        <div class="info-card">
          <h3>Dovolená (Nárok)</h3>
          <p class="huge-text">{{ auth.user?.earnedVacationHours?.toFixed(1) || '0.0' }}</p>
          <span>nasbíraných hodin</span>
        </div>
        <div class="info-card">
          <h3>Dovolená (Vyčerpáno)</h3>
          <p class="huge-text danger-text">{{ auth.user?.usedVacationHours?.toFixed(1) || '0.0' }}</p>
          <span>vyčerpaných hodin</span>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<style scoped>
.dashboard {
  max-width: 900px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  color: white;
  padding: 2.5rem;
  border-radius: 16px;
  margin-bottom: 2rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.welcome-card h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
}

.welcome-card p {
  opacity: 0.9;
  font-size: 1.125rem;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 2rem;
  border-radius: 16px;
  border: none;
  background: white;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
  color: #1e293b;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.clock-in { color: #059669; }
.action-btn.clock-out { color: #dc2626; }
.action-btn.break-start { color: #d97706; }
.action-btn.break-end { color: #2563eb; }

.action-btn span {
  font-weight: 600;
  font-size: 1.125rem;
}

.info-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.info-card {
  background: white;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
  margin: 0 0 1rem 0;
  color: #64748b;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-card p {
  margin: 0 0 0.5rem 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
}

.huge-text {
  font-size: 3rem !important;
  color: #4f46e5 !important;
  margin-bottom: 0 !important;
}

.danger-text {
  color: #ef4444 !important;
}

.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background-color: #f1f5f9;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #475569;
}
</style>
