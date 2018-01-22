package com.enjoyxstudy.sharedcamera.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class SpringBootCaptureClientApplication implements CommandLineRunner {

    private final WebcamCapture webcamCapture;
    
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SpringBootCaptureClientApplication.class);
        application.setWebEnvironment(false);
        ConfigurableApplicationContext context = application.run(args);
        context.close();
    }

    @Override
    public void run(String... args) throws Exception {
        webcamCapture.startCapture();
    }
}
