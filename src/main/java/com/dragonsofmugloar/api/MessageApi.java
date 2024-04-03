package com.dragonsofmugloar.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import com.dragonsofmugloar.model.Message;
import com.dragonsofmugloar.model.MessageSuccess;

public interface MessageApi {
    @GET("/api/v2/{gameId}/messages")
    Call<Message[]> getMessages(@Path("gameId") String gameId);

    @POST("/api/v2/{gameId}/solve/{adId}")
    Call<MessageSuccess> solveMessage(@Path("gameId") String gameId, @Path("adId") String adId);
}