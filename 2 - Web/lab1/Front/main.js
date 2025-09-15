function YValidate(){
    const yVal = document.getElementById('yVAL')
    const yErr = document.getElementById('yERR')
    const parseVal = parseFloat(yVAL.value)
    if (isNaN(parseVal) || parseVal < -5 || parseVal > 5){
        yErr.style.display = 'block';
        yVal.setCustomValidity('Y должен быть числом от -5 до 5');
        return false;
    } else{
        yErr.style.display = 'none';
        yVal.setCustomValidity('');
        return true;
    }
}
document.getElementById('form-getData').addEventListener('submit', function(e) {
    e.preventDefault();
    YValidate();
    if (!YValidate() || !this.checkValidity()) {
        return;
    } else
        const r = document.querySelector('input[name="rValue"]:checked')?.value;
        const x = document.querySelector('input[name="xValue"]:checked')?.value;
        const y = document.getElementById('yVAL').value;
        if (!r || !x || !y) {
            alert('Пожалуйста, заполните все поля');
            return;
        }
        sendDataToServer(r, x, y);
    });


async function sendDataToServer(r, x, y) {
    try{
        const data = {
            r: parseFloat(r),
            x: parseFloat(x),
            y: parseFloat(y)
        };
        const response = await fetch('/check-point', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        })
        if (!response.ok) {
            throw new Error('Ошибка сети');
            alert('Произошла ошибка при отправке данных на сервер');
        } else {
            const data = await response.json();
            addResultToTable(data.currentTime, data.r, data.x, data.y, data.hit, data.executionTime);
        }
    }catch(error){
        console.error('Ошибка:', error);
        alert('Произошла ошибка при отправке данных на сервер');
    };
}

function addResultToTable(time, r, x, y, hit, executionTime) {
    const tableBody = document.querySelector('#results-table tbody');
    const newRow = document.createElement('tr');
    newRow.innerHTML = `
                <td>${r}</td>
                <td>${x}</td>
                <td>${y}</td>
                <td>${time}</td>
                <td class="${hit ? 'hit' : 'miss'}">${hit ? 'Попадание' : 'Промах'}</td>
                <td>${executionTime}</td>
            `;

    tableBody.prepend(newRow);
}

