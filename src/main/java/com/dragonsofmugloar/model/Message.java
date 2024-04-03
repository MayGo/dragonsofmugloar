package com.dragonsofmugloar.model;

import lombok.Getter;

@Getter
public class Message {
    private String adId;
    private String message;
    private String reward;
    private int expiresIn;
    private String probability;

}