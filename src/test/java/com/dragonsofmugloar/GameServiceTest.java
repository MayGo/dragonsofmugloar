package com.dragonsofmugloar;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dragonsofmugloar.model.Message;
import com.dragonsofmugloar.service.GameService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class GameServiceTest {

    public static MockWebServer mockBackEnd;
    Retrofit retrofit;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> System.out.println(message));
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

    }

    String getFileString(String path) throws IOException {
        InputStream in = getClass().getResourceAsStream(path);
        return new String(in.readAllBytes());
    }

    @Test
    void testFindEasiestJobs() throws IOException {
        // Create some sample messages
        Message message1 = new Message("Job1", "Message", "50", 9, "Sure thing");
        Message message2 = new Message("Job2", "Message", "50", 10, "Quite likely");
        Message message3 = new Message("Job3", "Message", "50", 10, "Sure thing");
        Message message4 = new Message("Job4", "Message", "50", 1, "Walk in the park");

        Message[] messages = { message1, message2, message3, message4 };

        int maxProbability = 3;

        GameService gameService = new GameService(retrofit, "Player1");

        // Call the findEasiestJobs method
        Message[] result = gameService.findEasiestJobs(messages, maxProbability);

        // Expect easiest and most durable jobs be first
        Message[] expected = { message3, message1, message4 };

        // Collect adIds
        List<String> adIds = Arrays.stream(result)
                .map(Message::getAdId)
                .collect(Collectors.toList());

        List<String> expectedAdIds = Arrays.stream(expected)
                .map(Message::getAdId)
                .collect(Collectors.toList());

        // Assert that the adIds are equal
        Assertions.assertEquals(expectedAdIds, adIds);

    }

    @Test
    void testBuyRandomItem() throws IOException {

        GameService gameService = new GameService(retrofit, "Player1");

        mockBackEnd.enqueue(new MockResponse()
                .setBody(getFileString("/game_sample.json"))
                .addHeader("Content-Type", "application/json"));

        gameService.startGame();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(getFileString("/shop_sample.json"))
                .addHeader("Content-Type", "application/json"));

        mockBackEnd.enqueue(new MockResponse()
                .setBody(getFileString("/buy_sample.json"))
                .addHeader("Content-Type", "application/json"));

        boolean success = gameService.buyRandomItem();

        // Verify that the gameData's gold was updated after the purchase

        // 26 comes from the buy_sample.json file
        Assertions.assertEquals(26, gameService.getGameData().getGold());

        // Verify that the purchase was successful
        Assertions.assertTrue(success);
    }
}