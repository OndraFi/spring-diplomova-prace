import { defineStore } from 'pinia';
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/v1';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    loading: false,
    error: null as string | null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    isManager: (state) => state.user?.role === 'MANAGER',
  },
  actions: {
    async login(email: string, password: string) {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.post(`${API_URL}/auth/login`, { email, password });
        this.token = response.data.token;
        localStorage.setItem('token', this.token as string);
        
        // Fetch user profile after login
        await this.fetchProfile();
      } catch (err: any) {
        this.error = err.response?.data?.message || 'Login failed';
        throw err;
      } finally {
        this.loading = false;
      }
    },
    async fetchProfile() {
      try {
        const response = await axios.get(`${API_URL}/employees/me`, {
          headers: { Authorization: `Bearer ${this.token}` }
        });
        this.user = response.data;
        localStorage.setItem('user', JSON.stringify(this.user));
      } catch (err) {
        this.logout();
      }
    },
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  }
});
