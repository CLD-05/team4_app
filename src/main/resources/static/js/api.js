const API_BASE_URL = '/api';

const api = {
    async request(endpoint, options = {}) {
        const token = localStorage.getItem('accessToken');
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }


        const config = {
            ...options,
            headers,
        };

        const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

        if (response.status === 401) {
            // 토큰 만료 처리 (리프레시 로직 생략 또는 간단히 로그인 페이지 이동)
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
            return;
        }

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Something went wrong');
        }

        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        return await response.text();
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body),
        });
    },


    patch(endpoint, body) {
        return this.request(endpoint, {
            method: 'PATCH',
            body: JSON.stringify(body),
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    requestWithParams(endpoint, method, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const url = queryString ? `${endpoint}?${queryString}` : endpoint;
        return this.request(url, { method });
    }
};

const diaryApi = {
    updateEmotion(date, emotion) {
        return api.requestWithParams('/diaries/emotion', 'PATCH', { date, emotion });
    },
    getDiaryByDate(date) {
        return api.requestWithParams('/diaries/by-date', 'GET', { date });
    }
};

const sleepApi = {
    recordSleep(date, bedTime, wakeTime) {
        return api.post('/sleeps', { date, bedTime, wakeTime });
    },
    getSleep(date) {
        return api.requestWithParams('/sleeps', 'GET', { date });
    }
};

const exerciseApi = {
    recordExercise(date, category, minutes) {
        return api.post('/exercises', { date, category, minutes });
    },
    getExercises(date) {
        return api.requestWithParams('/exercises', 'GET', { date });
    }
};

const todoApi = {
    getTodos(date) {
        return api.requestWithParams('/todos', 'GET', { date });
    }
};

const auth = {
    async login(email, password) {
        const data = await api.post('/auth/login', { email, password });
        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);
        return data;
    },


    async signUp(email, password, nickname) {
        return await api.post('/auth/sign-up', { email, password, nickname });
    },


    logout() {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        document.cookie = 'accessToken=; path=/; max-age=0'; // 쿠키 삭제
        window.location.href = '/login';
    },
    isLoggedIn() {
        return !!localStorage.getItem('accessToken');
    }
};
