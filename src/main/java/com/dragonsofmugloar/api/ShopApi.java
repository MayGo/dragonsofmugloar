package com.dragonsofmugloar.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import com.dragonsofmugloar.model.ShopItem;
import com.dragonsofmugloar.model.BuyItemSuccess;

public interface ShopApi {
    @GET("/api/v2/{gameId}/shop")
    Call<ShopItem[]> getItems(@Path("gameId") String gameId);

    @POST("/api/v2/{gameId}/shop/buy/{itemId}")
    Call<BuyItemSuccess> purchaseItem(@Path("gameId") String gameId, @Path("itemId") String itemId);
}