package org.example;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class ResponseFCGI {
    private static final String RESPONSE_TEMPLATE = "content-Type: application/json\nContent-Length: %d\n\n%s";
    public static void sendSuccessResponse(String parsedInfo){
        System.out.printf(RESPONSE_TEMPLATE + "%n", parsedInfo.getBytes(StandardCharsets.UTF_8).length, parsedInfo);
    }
    public static void sendErrorResponse(String parsedInfo){
        System.out.printf(RESPONSE_TEMPLATE + "%n", parsedInfo.getBytes(StandardCharsets.UTF_8).length, String.format("{\"error\": \"%s\"}", parsedInfo));

    }
}
