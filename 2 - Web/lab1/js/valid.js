/*метод валидации у*/
function validateY() {
    const y = document.getElementById('y');
    const error = document.getElementById('yError');
    const value = parseFloat(y.value);

    if (isNaN(value) || value < -5 || value > 5) {
        error.style.display = 'block';
        y.setCustomValidity('Y должен быть числом от -5 до 5');
        return false;
    } else {
        error.style.display = 'none';
        y.setCustomValidity('');
        return true;
    }
}
/*метод валидации х и r*/
function validateXR() {
    let x = parseFloat(document.querySelector('input[name="x"]:checked'));
    let r = parseFloat(document.querySelector('input[name="r"]:checked'));
    let isValid = true;
    if (isNaN(r) || r < 1 || r > 3) {
        const rErr = document.getElementById("rError");
        rErr.style.display = 'block';
        r.setCustomValidity('R должен быть числом от 1 до 3');
        isValid = false;
    }
    if (isNaN(x) || x > 5 || x < -3) {
        const xErr = document.getElementById('xError')
        xErr.style.display = 'block';
        x.setCustomValidity('X должен быть числом от -3 до 5');
        console.log(wrongX.classList);
        isValid = false;
    }
    return isValid;
}
/*метод валидации всех значений*/
function validate(){
    const valXR = validateXR();
    const valY = validateY();
    return (valY && valXR);
}
