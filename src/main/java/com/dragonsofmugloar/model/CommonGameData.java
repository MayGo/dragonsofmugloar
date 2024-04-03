package com.dragonsofmugloar.model;

import lombok.Getter;

@Getter
public class CommonGameData {
    private int lives; // Amount of lives left after the attempt
    private int gold; // Amount of gold after the attempt
    private int score; // Score after the attempt
    private int highScore; // The current highest score
    private int turn; // Current turn number
}