package org.example;

import com.fastcgi.FCGIInterface;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestFCGI {
    public static String readRequest() throws IOException {
        FCGIInterface.request.inStream.fill();
        int contentLength = FCGIInterface.request.inStream.available();

        if (contentLength <= 0) {
            return "";
        }

        byte[] buffer = new byte[contentLength];
        int bytesRead = FCGIInterface.request.inStream.read(buffer, 0, contentLength);

        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }
}
