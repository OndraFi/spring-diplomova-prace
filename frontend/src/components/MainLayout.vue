<script setup lang="ts">
import { RouterLink, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { 
  LayoutDashboard, 
  Clock, 
  Users, 
  LogOut, 
  UserCircle 
} from 'lucide-vue-next';

const auth = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  auth.logout();
  router.push('/login');
};
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <Clock :size="24" />
        <span>Docházka</span>
      </div>
      
      <nav class="nav">
        <RouterLink to="/" class="nav-item">
          <LayoutDashboard :size="20" />
          <span>Dashboard</span>
        </RouterLink>
        
        <RouterLink to="/attendance" class="nav-item">
          <Clock :size="20" />
          <span>Moje docházka</span>
        </RouterLink>
        
        <RouterLink v-if="auth.isManager" to="/manager" class="nav-item">
          <Users :size="20" />
          <span>Tým</span>
        </RouterLink>
      </nav>
      
      <div class="user-footer">
        <div class="user-info">
          <UserCircle :size="20" />
          <div class="user-details">
            <span class="user-name">{{ auth.user?.name }}</span>
            <span class="user-role">{{ auth.user?.role }}</span>
          </div>
        </div>
        <button @click="handleLogout" class="logout-btn" title="Odhlásit se">
          <LogOut :size="20" />
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="top-header">
        <h2>{{ $route.name === 'dashboard' ? 'Dashboard' : $route.name === 'attendance' ? 'Moje docházka' : 'Správa týmu' }}</h2>
      </header>
      <div class="page-content">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background-color: #f8fafc;
}

.sidebar {
  width: 260px;
  background-color: #1e293b;
  color: white;
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  position: fixed;
  height: 100vh;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 2.5rem;
  color: #38bdf8;
}

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
}

.nav-item:hover {
  background-color: #334155;
  color: white;
}

.nav-item.router-link-active {
  background-color: #38bdf8;
  color: #0f172a;
}

.user-footer {
  margin-top: auto;
  padding-top: 1.5rem;
  border-top: 1px solid #334155;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 600;
}

.user-role {
  font-size: 0.75rem;
  color: #94a3b8;
}

.logout-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: #334155;
  color: #ef4444;
}

.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.top-header {
  background-color: white;
  padding: 1rem 2rem;
  border-bottom: 1px solid #e2e8f0;
}

.top-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
}

.page-content {
  padding: 2rem;
}
</style>
