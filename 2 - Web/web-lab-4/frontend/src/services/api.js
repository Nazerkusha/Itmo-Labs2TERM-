const AUTH_URL = "/api/auth";
const GEOM_URL = "/api/geometry";

const getHeaders = () => {
    const token = localStorage.getItem('token');
    console.log('Получение токена из localStorage:', token);
    
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
    
    console.log('Формирование заголовков:', headers);
    return headers;
};

async function handleResponse(response) {
    console.log('Ответ сервера:', {
        status: response.status,
        statusText: response.statusText,
        headers: Object.fromEntries(response.headers.entries())
    });

    const text = await response.text();
    
    console.log('Тело ответа:', text);

    if (!text) {
        if (!response.ok) {
            throw new Error(`Ошибка: ${response.status} ${response.statusText}`);
        }
        return null;
    }

    let data;
    try {
        data = JSON.parse(text);
    } catch (e) {
        if (!response.ok) {
            throw new Error(text || `Ошибка: ${response.status}`);
        }
        return text;
    }

    if (!response.ok) {
        throw new Error(data.message || data.error || `Ошибка: ${response.status}`);
    }
    
    return data;
}

export const authAPI = {
    login: async (username, password) => {
        console.log('Попытка логина:', { username });
        
        const response = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const data = await handleResponse(response);
        
        if (data && data.token) {
            console.log('Сохранение токена:', data.token);
            localStorage.setItem('token', data.token);
            localStorage.setItem('username', username);
        }
        
        return data;
    },

    register: async (username, password) => {
        console.log('Попытка регистрации:', { username });
        
        const response = await fetch(`${AUTH_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        return await handleResponse(response);
    }
};

export const geometryAPI = {
    checkPoint: async (x, y, r) => {
        console.log('Проверка точки на сервере:', { x, y, r });
        console.log('Отправка запроса с заголовками:', getHeaders());
        
        const response = await fetch(`${GEOM_URL}/check`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({ x, y, r })
        });
        
        return await handleResponse(response);
    },

    getPoints: async () => {
        console.log('Запрос точек с сервера');
        console.log('Отправка запроса с заголовками:', getHeaders());
        
        const response = await fetch(`${GEOM_URL}/points`, {
            method: 'GET',
            headers: getHeaders()
        });
        
        const data = await handleResponse(response);
        return data || [];
    },
};
