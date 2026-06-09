<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');
const error = ref('');
const loading = ref(false);

const handleLogin = async () => {
  loading.ref = true;
  error.value = '';
  try {
    await auth.login(email.value, password.value);
    router.push('/');
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Přihlášení se nezdařilo. Zkontrolujte údaje.';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Docházkový systém</h1>
      <p>Přihlaste se ke svému účtu</p>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Email</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            required 
            placeholder="např. jmeno@firma.cz"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Heslo</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            required 
            placeholder="Vaše heslo"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" :disabled="loading">
          {{ loading ? 'Přihlašování...' : 'Přihlásit se' }}
        </button>
      </form>
      
      <div class="test-creds">
        <p>Testovací údaje:</p>
        <code>manager@example.com / password</code><br/>
        <code>employee@example.com / password</code>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fb;
}

.login-card {
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  width: 100%;
  max-width: 400px;
}

h1 {
  margin: 0 0 0.5rem 0;
  color: #1a1f36;
  font-size: 1.75rem;
  text-align: center;
}

p {
  color: #4f566b;
  margin-bottom: 2rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #4f566b;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

input:focus {
  outline: none;
  border-color: #4f46e5;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

button:hover {
  background-color: #4338ca;
}

button:disabled {
  background-color: #a5b4fc;
  cursor: not-allowed;
}

.error-message {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
  text-align: center;
}

.test-creds {
  margin-top: 2rem;
  padding: 1rem;
  background-color: #f8fafc;
  border-radius: 6px;
  font-size: 0.75rem;
  color: #64748b;
}

code {
  color: #4f46e5;
}
</style>
