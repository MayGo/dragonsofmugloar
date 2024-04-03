package com.dragonsofmugloar.api;

import com.dragonsofmugloar.model.GameData;

import retrofit2.Call;
import retrofit2.http.POST;

public interface GameApi {
    @POST("/api/v2/game/start")
    Call<GameData> startGame();
}