import { authAPI } from '../services/api.js';

class AuthComponent extends HTMLElement {

    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.mode = 'login';
    }
    connectedCallback() {
        this.render();
        this.attachEventListeners();
    }
    render() {
        const isLoggedIn = localStorage.getItem('token');
        const username = localStorage.getItem('username');

        if (isLoggedIn) {
            this.shadowRoot.innerHTML = `
                <link rel="stylesheet" href="/src/styles/auth.css">
                
                <div class="auth-container">
                    <div class="user-info">
                        <span class="username"> ${username}</span>
                        <button class="logout-btn" id="logout-btn">Выход</button>
                    </div>
                </div>
            `;
        } else {
            this.shadowRoot.innerHTML = `
                <link rel="stylesheet" href="/src/styles/auth.css">
                
                <div class="auth-container">
                    <div class="auth-card">

                        <h2 id="auth-title">${this.mode === 'login' ? 'Вход' : 'Регистрация'}</h2>
                        
                        <form id="auth-form">
                            <div class="form-group">
                                <label>Имя пользователя</label>

                                <input type="text" id="username" placeholder="Введите имя" required autocomplete="username">
                            </div>
                            
                            <div class="form-group">
                                <label>Пароль</label>
                                <input type="password" id="password" placeholder="Введите пароль" required autocomplete="current-password">
                            </div>

                            <div id="error" class="error-message" style="display: none;"></div>

                            <button type="submit" class="submit-btn">
                                ${this.mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
                            </button>
                        </form>
                        
                        <div class="mode-switch">
                            <span>${this.mode === 'login' ? 'Нет аккаунта?' : 'Есть аккаунт?'} </span>
                            <button class="link-btn" id="switch-btn">
                                ${this.mode === 'login' ? 'Регистрация' : 'Войти'}
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }
        this.attachEventListeners();
    }


    attachEventListeners() {

        const form = this.shadowRoot.getElementById('auth-form');
        const switchBtn = this.shadowRoot.getElementById('switch-btn');
        const logoutBtn = this.shadowRoot.getElementById('logout-btn');

        if (form) {
            form.addEventListener('submit', (e) => this.handleSubmit(e));
        }

        if (switchBtn) {
            switchBtn.addEventListener('click', () => this.switchMode());
        }
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.logout());
        }
    }

    switchMode() {
        this.mode = this.mode === 'login' ? 'register' : 'login';
        this.render(); // Перерисовываем с новым режимом
    }


    async handleSubmit(e) {

        e.preventDefault();

        const username = this.shadowRoot.getElementById('username').value;
        const password = this.shadowRoot.getElementById('password').value;
        const errorEl = this.shadowRoot.getElementById('error');

        try {
            
            let result;
            if (this.mode === 'login') {
                result = await authAPI.login(username, password);
            } else {
                result = await authAPI.register(username, password);
                result = await authAPI.login(username, password);
            }

            this.dispatchEvent(new CustomEvent('auth-success', {
                detail: { token: result.token, username },
                bubbles: true,
                composed: true
            }));

            this.render();

        } catch (error) {
            errorEl.textContent = error.message;
            errorEl.style.display = 'block';
        }
    }

    logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('username');

        this.dispatchEvent(new CustomEvent('auth-logout', {
            bubbles: true,
            composed: true
        }));

        this.render();
    }
}

customElements.define('auth-component', AuthComponent);
