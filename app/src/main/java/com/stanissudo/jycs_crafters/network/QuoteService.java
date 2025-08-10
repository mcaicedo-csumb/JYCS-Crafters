package com.stanissudo.jycs_crafters.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuoteService {
    @GET("random")
    Call<AdviceResponse> getRandomQuote();
}
