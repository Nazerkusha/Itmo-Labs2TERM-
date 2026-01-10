import { geometryAPI } from '../services/api.js';

class ResultComponent extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.points = [];
        this.currentR = 1;
        this.isAuth = false;
    }

    connectedCallback() {
        this.checkAuth();
        this.render();

        window.addEventListener('auth-success', () => this.handleAuthSuccess());
        window.addEventListener('auth-logout', () => this.handleAuthLogout());
        window.addEventListener('point-checked', (e) => this.handlePointChecked(e.detail));
        window.addEventListener('r-changed', (e) => this.handleRChange(e.detail.r));
    }

    checkAuth() {
        this.isAuth = !!localStorage.getItem('token');
    }


    async handleAuthSuccess() {
        this.checkAuth();
        await this.loadPoints();
        this.render();
    }

    handleAuthLogout() {
        this.isAuth = false;
        this.points = [];
        this.render();
    }


    handlePointChecked(point) {
        this.points.unshift(point);
        this.render();
    }

    handleRChange(newR) {
        this.currentR = newR;
        this.render();
    }

    async loadPoints() {
        try {
            this.points = await geometryAPI.getPoints();
        } catch (error) {
            console.error('Ошибка загрузки точек:', error);
            this.points = [];
        }
    }

    render() {
        if (!this.isAuth) {
            this.shadowRoot.innerHTML = `
                <link rel="stylesheet" href="../styles/main.css">
                <div class="results-section">
                    <div class="info-message">Войдите в систему, чтобы видеть результаты</div>
                </div>
            `;
            return;
        }

        const filteredPoints = this.points.filter(p => p.r === this.currentR);

        this.shadowRoot.innerHTML = `
            <link rel="stylesheet" href="/src/styles/main.css">
            <div class="results-section">
                <h3>Результаты проверки (R = ${this.currentR})</h3>
                
                ${filteredPoints.length === 0 ? `
                    <div class="empty-message">
                        Пока нет результатов. Кликните на график или введите координаты!
                    </div>
                ` : `
                    <div class="results-table">
                        <table>
                            <thead>
                                <tr>
                                    <th>X</th>
                                    <th>Y</th>
                                    <th>R</th>
                                    <th>Результат</th>
                                    <th>Время (нс)</th>
                                    <th>Дата и время</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${filteredPoints.map(point => `
                                    <tr class="${point.hit ? 'hit' : 'miss'}">
                                        <td>${point.x.toFixed(2)}</td>
                                        <td>${point.y.toFixed(2)}</td>
                                        <td>${point.r.toFixed(2)}</td>
                                        <td>${point.hit ? 'Попал' : 'Мимо'}</td>
                                        <td>${point.scriptTime || '-'}</td>
                                        <td>${point.currentTime ? new Date(point.currentTime).toLocaleString() : '-'}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `}
            </div>
        `;
    }
}

customElements.define('result-component', ResultComponent);
