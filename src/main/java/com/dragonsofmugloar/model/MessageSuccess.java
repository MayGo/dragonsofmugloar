package com.dragonsofmugloar.model;

import lombok.Getter;

@Getter
public class MessageSuccess extends CommonGameData {
    private boolean success; // Whether the attempt to solve the message was successful.
    private String message; // Text explanation of what happened on the last turn.
}