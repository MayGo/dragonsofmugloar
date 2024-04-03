package com.dragonsofmugloar.service;

import java.util.List;
import java.util.logging.Logger;

public class GameStatisticsService {
    private static final Logger logger = Logger.getLogger(GameStatisticsService.class.getName());

    private List<GameService> gameServices;

    public GameStatisticsService(List<GameService> gameServices) {
        this.gameServices = gameServices;
    }

    public double calculateAverageScore() {
        int totalScore = 0;
        for (GameService gameService : gameServices) {
            totalScore += gameService.getGameData().getScore();
        }

        return (double) totalScore / gameServices.size();
    }

    public String findBestPlayer() {
        int maxScore = 0;
        String bestPlayer = "";
        for (GameService gameService : gameServices) {
            if (gameService.getGameData().getScore() > maxScore) {
                maxScore = gameService.getGameData().getScore();
                bestPlayer = gameService.getPlayerName();
            }
        }

        return bestPlayer;
    }

    public int findWinningPercentage() {
        int wins = 0;
        for (GameService gameService : gameServices) {
            if (gameService.getGameData().getLives() > 0) {
                wins++;
            }
        }

        return (int) ((double) wins / (gameServices.size()) * 100);
    }

    public int findAverageTurns() {
        int totalTurns = 0;
        for (GameService gameService : gameServices) {
            totalTurns += gameService.getGameData().getTurn();
        }

        return totalTurns / gameServices.size();
    }

    public int findBuggedGames() {
        int buggedGames = 0;
        for (GameService gameService : gameServices) {
            if (gameService.foundBug) {
                buggedGames++;
            }
        }

        return buggedGames;
    }

    public void printGameStatistics() {
        logger.info("-----------------Game statistics-----------------");
        logger.info("Average score: " + calculateAverageScore());
        logger.info("Best player: " + findBestPlayer());
        logger.info("Winning percentage: " + findWinningPercentage() + "%");
        logger.info("Average turns: " + findAverageTurns());
        logger.info("Bugged games: " + findBuggedGames());
    }
}