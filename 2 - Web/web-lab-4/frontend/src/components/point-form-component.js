import { geometryAPI } from '../services/api.js';
import { drawCoordinatePlane, drawPoint, canvasToGraphCoords } from '../utils/canvas.js';


class PointFormComponent extends HTMLElement {
    constructor() {
        super();
        this.attachShadow({ mode: 'open' });
        this.currentR = 1;
        this.isAuth = false;
    }

    connectedCallback() {
        this.checkAuth();
        this.render();
        

        window.addEventListener('auth-success', () => this.handleAuthChange());
        window.addEventListener('auth-logout', () => this.handleAuthChange());
        window.addEventListener('r-changed', (e) => this.handleRChange(e.detail.r));
    }


    checkAuth() {
        this.isAuth = !!localStorage.getItem('token');
    }

    handleAuthChange() {
        this.checkAuth();
        this.render();
    }

    handleRChange(newR) {
        this.currentR = newR;
        const canvas = this.shadowRoot.getElementById('coordinate-plane');
        if (canvas) {
            const ctx = canvas.getContext('2d');
            drawCoordinatePlane(ctx, this.currentR);
        }
    }

    render() {
        if (!this.isAuth) {
            // Если НЕ авторизован - показываем сообщение
            this.shadowRoot.innerHTML = `
                <link rel="stylesheet" href="../styles/main.css">
                <div class="main-content">
                    <div class="info-message">Войдите в систему, чтобы проверять точки</div>
                </div>
            `;
            return;
        }

        this.shadowRoot.innerHTML = `
            <link rel="stylesheet" href="/src/styles/main.css">
            <div class="main-content">
                <div class="content-grid">
                    <div class="canvas-section">
                        <h3>График области</h3>
                        <div class="canvas-wrapper">
                            <canvas id="coordinate-plane" width="400" height="400"></canvas>
                        </div>
                    </div>

                    <div class="form-section">
                        <h3>Ввод координат</h3>
                        <form id="point-form">
                            <div class="form-group">
                                <label>X координата</label>
                                <input type="number" step="0.1" id="point-x" placeholder="Введите X" required>
                                <small>от -3 до 5</small>
                            </div>
                            
                            <div class="form-group">
                                <label>Y координата</label>
                                <input type="number" step="0.1" id="point-y" placeholder="Введите Y" required>
                                <small>от -5 до 3</small>
                            </div>
                            
                            <div class="form-group">
                                <label>R радиус</label>
                                <div class="radio-group">
                                    ${[0.5, 1, 1.5, 2, 2.5, 3].map(r => `
                                        <label>
                                            <input type="radio" name="r" value="${r}" ${r === this.currentR ? 'checked' : ''}>
                                            ${r}
                                        </label>
                                    `).join('')}
                                </div>
                            </div>
                            
                            <div id="error" class="error-message" style="display: none;"></div>
                            
                            <button type="submit" class="submit-btn">Проверить точку</button>
                        </form>
                    </div>
                </div>
            </div>
        `;

        this.attachEventListeners();
        this.initCanvas();
    }

    attachEventListeners() {
        const form = this.shadowRoot.getElementById('point-form');
        const canvas = this.shadowRoot.getElementById('coordinate-plane');
        const radios = this.shadowRoot.querySelectorAll('input[name="r"]');

        if (form) {
            form.addEventListener('submit', (e) => this.handleSubmit(e));
        }

        if (canvas) {
            canvas.addEventListener('click', (e) => this.handleCanvasClick(e));
        }

        radios.forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.currentR = parseFloat(e.target.value);
                window.dispatchEvent(new CustomEvent('r-changed', {
                    detail: { r: this.currentR }
                }));
            });
        });
    }


    initCanvas() {
        const canvas = this.shadowRoot.getElementById('coordinate-plane');
        if (canvas) {
            const ctx = canvas.getContext('2d');
            drawCoordinatePlane(ctx, this.currentR);
        }
    }

    async handleCanvasClick(e) {
        const canvas = e.target;
        const rect = canvas.getBoundingClientRect();
        const canvasX = e.clientX - rect.left;
        const canvasY = e.clientY - rect.top;

        const { x, y } = canvasToGraphCoords(canvasX, canvasY, this.currentR);
        
        await this.checkPoint(x, y);
    }


    async handleSubmit(e) {
        e.preventDefault();

        const x = parseFloat(this.shadowRoot.getElementById('point-x').value);
        const y = parseFloat(this.shadowRoot.getElementById('point-y').value);

        await this.checkPoint(x, y);

        this.shadowRoot.getElementById('point-x').value = '';
        this.shadowRoot.getElementById('point-y').value = '';
    }

    async checkPoint(x, y) {
        const errorEl = this.shadowRoot.getElementById('error');

        if (isNaN(x) || isNaN(y)) {
            errorEl.textContent = 'Введите числа';
            errorEl.style.display = 'block';
            return;
        }

        try {
            const result = await geometryAPI.checkPoint(x, y, this.currentR);

            const canvas = this.shadowRoot.getElementById('coordinate-plane');
            if (canvas) {
                const ctx = canvas.getContext('2d');
                drawPoint(ctx, result.x, result.y, result.r, result.hit);
            }

            window.dispatchEvent(new CustomEvent('point-checked', {
                detail: result
            }));

            errorEl.style.display = 'none';
        } catch (error) {
            errorEl.textContent = error.message;
            errorEl.style.display = 'block';
        }
    }
}

customElements.define('point-form-component', PointFormComponent);
