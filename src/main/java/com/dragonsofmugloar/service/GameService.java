package com.dragonsofmugloar.service;

import com.dragonsofmugloar.api.GameApi;
import com.dragonsofmugloar.api.InvestigationApi;
import com.dragonsofmugloar.api.MessageApi;
import com.dragonsofmugloar.api.ShopApi;
import com.dragonsofmugloar.model.GameData;
import com.dragonsofmugloar.model.Investigation;
import com.dragonsofmugloar.model.Message;
import com.dragonsofmugloar.model.MessageSuccess;
import com.dragonsofmugloar.model.ShopItem;
import com.dragonsofmugloar.model.BuyItemSuccess;
import com.dragonsofmugloar.model.CommonGameData;

import retrofit2.Call;
import retrofit2.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class GameService {

    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    String playerName;

    GameApi gameApi;
    MessageApi messageApi;
    ShopApi shopApi;
    InvestigationApi investigationApi;

    Map<String, Integer> probabilities = new HashMap<>();

    String gameId;
    CommonGameData gameData;
    Investigation reputation;

    String finishStatus;

    public boolean foundBug = false;

    public GameService(retrofit2.Retrofit retrofit, String name) throws IOException {
        gameApi = retrofit.create(GameApi.class);
        messageApi = retrofit.create(MessageApi.class);
        shopApi = retrofit.create(ShopApi.class);
        investigationApi = retrofit.create(InvestigationApi.class);

        playerName = name;

        probabilities.put("Sure thing", 1);
        probabilities.put("Piece of cake", 2);
        probabilities.put("Walk in the park", 3);
        probabilities.put("Quite likely", 4);
        probabilities.put("Gamble", 5);
        probabilities.put("Hmmm....", 6);
        probabilities.put("Risky", 7);
        probabilities.put("Playing with fire", 8);
        probabilities.put("Rather detrimental", 9);
        probabilities.put("Suicide mission", 10);
    }

    public CommonGameData getGameData() {
        return gameData;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Message[] findEasiestJobs(Message[] messages,
            int maxProbability) {
        return Arrays.stream(messages)
                .filter(message -> {
                    if (probabilities.containsKey(message.getProbability())) {
                        return probabilities.get(message.getProbability()) <= maxProbability;
                    } else {
                        logger.info("Unknown probability: " + message.getProbability());
                        return false;
                    }
                })
                .sorted(Comparator.comparing(Message::getProbability)
                        .thenComparing(Message::getExpiresIn, Comparator.reverseOrder()))
                .toArray(Message[]::new);

    }

    public ShopItem[] getShopItems() throws IOException {
        Call<ShopItem[]> callShop = shopApi.getItems(gameId);
        Response<ShopItem[]> responseShop = callShop.execute();
        return responseShop.body();

    }

    public boolean buyHealth() throws IOException {
        BuyItemSuccess data = buyProduct("hpot");
        return data.isShoppingSuccess();
    }

    public boolean buyRandomItem() throws IOException {

        // Filter items by affordability and exclude health potions
        ShopItem[] items = Arrays.stream(getShopItems())
                .filter(item -> {
                    logger.info(" [ i ] Item: " + item.getName() + " Cost: " + item.getCost() + " Gold: "
                            + gameData.getGold());
                    return item.getCost() <= gameData.getGold() && !item.getId().equals("hpot");
                })
                .toArray(ShopItem[]::new);

        if (items.length == 0) {
            logger.info(" [ x ] No items to buy");
            return false;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(items.length);
        ShopItem item = items[randomIndex];
        BuyItemSuccess data = buyProduct(item.getId());

        logger.info(" [ $ ] Bought item: " + item.getName() + " Cost: " + item.getCost() + " Gold: "
                + gameData.getGold());

        return data.isShoppingSuccess();
    }

    public BuyItemSuccess buyProduct(String itemId) throws IOException {
        Call<BuyItemSuccess> callShop = shopApi.purchaseItem(gameId, itemId);
        Response<BuyItemSuccess> responseShop = callShop.execute();
        BuyItemSuccess data = responseShop.body();

        gameData = data;
        return data;
    }

    public CommonGameData startGame() throws IOException {
        Call<GameData> call = gameApi.startGame();
        Response<GameData> response = call.execute();
        GameData data = response.body();
        gameData = data;
        gameId = data.getGameId();

        return gameData;
    }

    public Message[] getMessages() throws IOException {
        Call<Message[]> callMessages = messageApi.getMessages(gameId);
        Response<Message[]> responseMessages = callMessages.execute();
        return responseMessages.body();
    }

    public Response<MessageSuccess> solveMessage(String adId) throws IOException {
        Call<MessageSuccess> callSolve = messageApi.solveMessage(gameId, adId);
        Response<MessageSuccess> resp = callSolve.execute();

        if (resp.isSuccessful()) {
            MessageSuccess newData = resp.body();
            if (newData.getTurn() <= gameData.getTurn()) {// Turn should always increase
                foundBug = true;
                // Perhaps gold can be stolen from player, but turn should always increase
                logger.warning(
                        "Found a bug. Old turn: " + gameData.getTurn() + ", new turn: " + newData.getTurn()
                                + ". Had gold:"
                                + gameData.getGold() + ", new gold:" + newData.getGold());
            }
            gameData = newData;
        }

        return resp;
    }

    public Investigation getReputation() throws IOException {
        Call<Investigation> runInvestigation = investigationApi.runInvestigation(gameId);
        Response<Investigation> responseMessages = runInvestigation.execute();
        return responseMessages.body();
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void playTheGame() throws IOException {
        logger.info("Starting game...");

        startGame();

        // We start with the easiest jobs. And after collecting enough gold we will
        // buy something and then we will start accepting harder jobs
        int maxProbability = 3;

        /*
         * Strategy:
         * Solve easiest jobs first, if we will not solve the message we will decrement
         * probability in next iteration
         * If we will solve all jobs, we will increment probability in next iteration
         */

        int turnsWithoutBuying = 0;

        while (gameData.getLives() > 0 && gameData.getGold() < 1000) {
            sleep();

            Message[] messages = getMessages();

            // Find the message (job) with the easiest statuses
            Message[] easiestMessages = findEasiestJobs(messages, maxProbability);

            if (easiestMessages.length == 0) {
                maxProbability = maxProbability + 1;
            }

            for (Message easiestMessage : easiestMessages) {
                sleep();

                logger.info("Solving message: " + easiestMessage.getProbability());
                Response<MessageSuccess> resp = solveMessage(easiestMessage.getAdId());

                MessageSuccess data = resp.body();

                if (!resp.isSuccessful()) {
                    if ("{\"error\":\"No ad by this ID exists\"}".equals(resp.errorBody().string())) {
                        // we could use "Expires in" data to no try to solve expired message,
                        // but this will do for now
                        logger.info("[ - ] No ad by this ID exists:" + easiestMessage.getAdId());
                    } else {
                        logger.info("[ x ] Other Error");

                        break;
                    }
                }

                if (data != null) {
                    turnsWithoutBuying = turnsWithoutBuying + 1;

                    if (data.isSuccess()) {
                        logger.info("[ v ] Solved message: " + data.getMessage());
                    } else {
                        logger.info("[ x ] Failed to solve message: " + data.getMessage());
                        break;
                    }
                }

            }

            if (gameData.getLives() == 0) {
                logger.info("Out of lives");
                break;
            }

            // refresh reputation
            reputation = getReputation();

            if (gameData.getLives() <= 2 && gameData.getGold() >= 50) {
                logger.info("[ H ] Buying health");
                buyHealth();
            }

            // buy something in every 7 turns if collected enough gold
            if (turnsWithoutBuying > 7 && gameData.getGold() >= 100) {
                logger.info("[-----------------SHOP-----------------");
                buyRandomItem();
                maxProbability = maxProbability + 1;
                turnsWithoutBuying = 0;
            }

        }

        if (gameData.getLives() == 0) {
            logger.info("[-----------------Game Over-----------------]");
            finishStatus = "Game Over";
        } else {
            logger.info("[-----------------Game Won-----------------]");
            finishStatus = "Game Won";
        }
    }

    void printStats() {
        // Display the game data
        logger.info("[-----------------Stats-----------------]");

        logger.info("Lives: " + gameData.getLives());
        logger.info("Gold: " + gameData.getGold());
        logger.info("Turn: " + gameData.getTurn());
        logger.info("Score: " + gameData.getScore());
        logger.info("High Score: " + gameData.getHighScore());

        // Display the reputation
        logger.info("[-----------------Reputation-----------------]");
        logger.info("People: " + reputation.getPeople());
        logger.info("State: " + reputation.getState());
        logger.info("Underworld: " + reputation.getUnderworld());
    }

    void printGameResults() {
        logger.info("----------------------------------" + playerName + "----------------------------------");
        logger.info("Game Status: " + finishStatus);
        if (foundBug) {
            logger.warning("Found a bug in this game");
        }
        printStats();
    }
}
