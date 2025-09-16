package org.example;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.example.DotTest.checkHit;

public class Main{
    public static void main(String[] args) {
        FCGIInterface fcgi = new FCGIInterface();
        DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        while (fcgi.FCGIaccept() >= 0) {
            long startTime = System.currentTimeMillis();

            try {
                String requestBody = RequestFCGI.readRequest();

                // Парсинг JSON
                HashMap<String, String> parametrs = Parse.parseJson(requestBody);

                // Проверка наличия параметров
                if (!parametrs.containsKey("x") || !parametrs.containsKey("y") || !parametrs.containsKey("r")) {
                    ResponseFCGI.sendErrorResponse("Недостаточно параметров");
                    continue;
                }

                // Парсинг чисел
                float x = Float.parseFloat(parametrs.get("x"));
                float y = Float.parseFloat(parametrs.get("y"));
                float r = Float.parseFloat(parametrs.get("r"));

                // Проверка попадания
                boolean hit = checkHit(x, y, r);
                String currentTime = LocalDateTime.now().format(TIME_FORMATTER);
                long executionTime = System.currentTimeMillis() - startTime;

                String parsedInfo = Parse.parseToJSON(x, y, r, currentTime, hit, executionTime);

                // Отправка успешного ответа
                ResponseFCGI.sendSuccessResponse(parsedInfo);

            } catch (IOException e) {
                ResponseFCGI.sendErrorResponse("Ошибка чтения запроса: " + e.getMessage());
            } catch (NumberFormatException e) {
                ResponseFCGI.sendErrorResponse("Некорректный формат чисел");
            } catch (Exception e) {
                ResponseFCGI.sendErrorResponse("Внутренняя ошибка сервера");
            }
        }
    }

}