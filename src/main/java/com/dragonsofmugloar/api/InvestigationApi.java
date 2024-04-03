package com.dragonsofmugloar.api;

import com.dragonsofmugloar.model.Investigation;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InvestigationApi {
    @POST("/api/v2/{gameId}/investigate/reputation")
    Call<Investigation> runInvestigation(@Path("gameId") String gameId);
}