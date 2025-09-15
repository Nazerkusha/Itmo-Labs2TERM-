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
