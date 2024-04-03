package com.dragonsofmugloar.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    @Value("${api.base.url}")
    private String baseUrl;

    public void startApplication() throws IOException {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> System.out.println(message));
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        // Create a CountDownLatch with a count of 2
        CountDownLatch latch = new CountDownLatch(2);

        // Create an ExecutorService with a fixed thread pool size of 2
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // Create and submit the first GameService instance
        GameService gameService1 = new GameService(retrofit, "Player 1");
        executorService.submit(() -> {
            try {
                gameService1.playTheGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
            latch.countDown(); // Decrease the latch count by 1
        });

        // Create and submit the second GameService instance
        GameService gameService2 = new GameService(retrofit, "Player 2");
        executorService.submit(() -> {

            try {
                gameService2.playTheGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
            latch.countDown(); // Decrease the latch count by 1
        });

        // Wait until the latch count reaches 0
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Both games have finished.");
        gameService1.printGameData();
        gameService2.printGameData();

        // Shutdown the executor service
        executorService.shutdown();

    }
}
