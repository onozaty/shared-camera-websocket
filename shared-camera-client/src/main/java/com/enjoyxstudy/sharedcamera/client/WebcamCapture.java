package com.enjoyxstudy.sharedcamera.client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;

@Component
public class WebcamCapture {

    @Value("${camera.interval-millis}")
    private int intervalMillis;

    @Value("${camera.use-v4l4j}")
    private boolean useV4l4j;

    @Autowired
    private ImageWebSocketHandler webSocketHandler;

    public void startCapture() throws IOException, InterruptedException {

        if (useV4l4j) {
            Webcam.setDriver(new V4l4jDriver());
        }

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
