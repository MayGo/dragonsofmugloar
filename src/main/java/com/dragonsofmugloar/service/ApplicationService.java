package com.dragonsofmugloar.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private static final Logger logger = Logger.getLogger(ApplicationService.class.getName());

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

        int n = 4; // Number of GameService instances

        // Create a CountDownLatch with a count of n
        CountDownLatch latch = new CountDownLatch(n);

        // Create an ExecutorService with a fixed thread pool size of n
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // Create a list to hold the GameService instances
        List<GameService> gameServices = new ArrayList<>();

        // Create and submit the GameService instances
        for (int i = 0; i < n; i++) {
            GameService gameService = new GameService(retrofit, "Player " + (i + 1));
            gameServices.add(gameService);

            executorService.submit(() -> {
                try {
                    gameService.playTheGame();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // Decrease the latch count by 1

                }
            });
        }

        // Wait until the latch count reaches 0
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("All games have finished.");

        for (GameService gameService : gameServices) {
            gameService.printGameResults();
        }

        GameStatisticsService gameStatisticsService = new GameStatisticsService(gameServices);
        gameStatisticsService.printGameStatistics();

        // Shutdown the executor service
        executorService.shutdown();

    }
}
