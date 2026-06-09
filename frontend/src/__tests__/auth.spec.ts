import { describe, it, expect, beforeEach, vi } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAuthStore } from '../stores/auth';
import axios from 'axios';

vi.mock('axios');
const mockedAxios = axios as vi.Mocked<typeof axios>;

describe('Auth Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
    vi.clearAllMocks();
  });

  it('initializes with null token and user', () => {
    const auth = useAuthStore();
    expect(auth.token).toBeNull();
    expect(auth.user).toBeNull();
    expect(auth.isAuthenticated).toBe(false);
  });

  it('login sets token and fetches profile', async () => {
    const auth = useAuthStore();
    const mockToken = 'test-token';
    const mockUser = { id: 1, email: 'test@test.cz', role: 'EMPLOYEE' };

    mockedAxios.post.mockResolvedValueOnce({ data: { token: mockToken } });
    mockedAxios.get.mockResolvedValueOnce({ data: mockUser });

    await auth.login('test@test.cz', 'password');

    expect(auth.token).toBe(mockToken);
    expect(auth.user).toEqual(mockUser);
    expect(auth.isAuthenticated).toBe(true);
    expect(localStorage.getItem('token')).toBe(mockToken);
  });

  it('logout clears state and localStorage', () => {
    const auth = useAuthStore();
    auth.token = 'some-token';
    auth.user = { id: 1, email: 'test@test.cz' } as any;
    localStorage.setItem('token', 'some-token');

    auth.logout();

    expect(auth.token).toBeNull();
    expect(auth.user).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });
});
