package com.enjoyxstudy.sharedcamera.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.sarxos.webcam.Webcam;

@Component
public class WebcamCapture {

    @Value("${capture.interval-millis}")
    private int intervalMillis;

    @Autowired
    private ImageWebSocketHandler webSocketHandler;

    public void startCapture() throws IOException, InterruptedException {

        Webcam webcam = Webcam.getDefault();

        if (webcam == null) {
            throw new IOException("Camera could not be found.");
        }

        webcam.open();

        while (true) {

            if (webSocketHandler.isConnected()) {
                BufferedImage image = webcam.getImage();

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    ImageIO.write(image, "jpeg", outputStream);
                    webSocketHandler.sendMessage(outputStream.toByteArray());
                }
            }

            Thread.sleep(intervalMillis);
        }
    }
}
