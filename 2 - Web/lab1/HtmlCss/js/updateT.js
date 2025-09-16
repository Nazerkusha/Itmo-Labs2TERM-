
/*метод отправки данных точек на сервер и получение ответа*/

async function update() {
    try {
        const xVal = parseFloat(document.querySelector('input[name="x"]:checked'));
        const yVal = parseFloat(document.getElementById("y").value.trim().replace(",", "."));
        const rVal = parseFloat(document.querySelector('input[name="r"]:checked'));

        const valid = validate();
        if (valid) {
            const request = {
                method: "post",
                headers: {
                    "content-type": "application/json",
                },
                body: JSON.stringify({
                    x: xVal,
                    y: yVal,
                    r: rVal
                })
            };

            const url = '/api/';

            try {
                const response = await fetch(url, request);
                const data = await response.json();
                addResultToTable(data.r, data.x, data.y, data.currentTime, data.hit, data.executionTime)
            } catch (err) {
                console.error(err);
                alert('Произошла ошибка при отправке данных на сервер');
            }
        } else alert('Произошла ошибка при валидации');
    } catch (err){
        console.error(err);
        alert('Произошла ошибка при отправке данных на сервер');
    }
}
/*метод изменения таблицы*/

function addResultToTable(r, x, y, time, hit, executionTime) {
    try{
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
    }catch(err){
        console.error(err);
        alert('Произошла ошибка при отправке данных на сервер');
    }
}
