

const CANVAS_SIZE = 400;
const CENTER = CANVAS_SIZE / 2;


export function drawCoordinatePlane(ctx, r) {

    ctx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    
    const scale = CENTER / (r * 1.2); // масштаб
    

    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    

    ctx.strokeStyle = '#000';
    ctx.lineWidth = 2;

    ctx.beginPath();
    ctx.moveTo(0, CENTER);
    ctx.lineTo(CANVAS_SIZE, CENTER);
    ctx.stroke();
    

    ctx.beginPath();
    ctx.moveTo(CENTER, 0);
    ctx.lineTo(CENTER, CANVAS_SIZE);
    ctx.stroke();


    drawArrow(ctx, CANVAS_SIZE - 10, CENTER, 10, 0);
    drawArrow(ctx, CENTER, 10, 0, -10);
    

    drawLabels(ctx, r, scale);
    

    drawShapes(ctx, r, scale);
}


function drawArrow(ctx, x, y, dx, dy) {
    ctx.beginPath();
    ctx.moveTo(x, y);
    ctx.lineTo(x - dx - dy * 0.3, y - dy + dx * 0.3);
    ctx.moveTo(x, y);
    ctx.lineTo(x - dx + dy * 0.3, y - dy - dx * 0.3);
    ctx.stroke();
}


function drawLabels(ctx, r, scale) {
    ctx.fillStyle = '#000';
    ctx.font = '12px Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    

    const labels = [-r, -r/2, r/2, r];
    const labelNames = ['-R', '-R/2', 'R/2', 'R'];
    
    labels.forEach((val, i) => {
        const x = CENTER + val * scale;
        

        ctx.beginPath();
        ctx.moveTo(x, CENTER - 5);
        ctx.lineTo(x, CENTER + 5);
        ctx.stroke();
        

        ctx.fillText(labelNames[i], x, CENTER + 15);
    });
    

    labels.forEach((val, i) => {
        const y = CENTER - val * scale;
        

        ctx.beginPath();
        ctx.moveTo(CENTER - 5, y);
        ctx.lineTo(CENTER + 5, y);
        ctx.stroke();

        if (val !== 0) {
            ctx.fillText(labelNames[i], CENTER - 20, y);
        }
    });
    

    ctx.fillText('X', CANVAS_SIZE - 15, CENTER - 15);
    ctx.fillText('Y', CENTER + 15, 15);
}


function drawShapes(ctx, r, scale) {
    ctx.fillStyle = 'rgba(255,160,246,0.6)';
    ctx.strokeStyle = 'rgb(255,0,221)';
    ctx.lineWidth = 1;
    
    const rScaled = r * scale;
    

    ctx.beginPath();
    ctx.arc(CENTER, CENTER, rScaled, -Math.PI/2, 0);
    ctx.lineTo(CENTER, CENTER);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    

    ctx.beginPath();
    ctx.moveTo(CENTER, CENTER);
    ctx.lineTo(CENTER + rScaled, CENTER);
    ctx.lineTo(CENTER, CENTER + rScaled/2);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    

    ctx.beginPath();
    ctx.rect(CENTER - rScaled, CENTER, rScaled, rScaled/2);
    ctx.fill();
    ctx.stroke();
}

export function drawPoint(ctx, x, y, r, isHit) {
    const scale = CENTER / (r * 1.2);
    const canvasX = CENTER + x * scale;
    const canvasY = CENTER - y * scale;
    
    ctx.fillStyle = isHit ? '#4CAF50' : '#F44336';
    ctx.beginPath();
    ctx.arc(canvasX, canvasY, 5, 0, Math.PI * 2);
    ctx.fill();
    
    ctx.strokeStyle = '#000';
    ctx.lineWidth = 2;
    ctx.stroke();
}

export function canvasToGraphCoords(canvasX, canvasY, r) {
    const scale = CENTER / (r * 1.2);
    const x = (canvasX - CENTER) / scale;
    const y = (CENTER - canvasY) / scale;
    return { x, y };
}
