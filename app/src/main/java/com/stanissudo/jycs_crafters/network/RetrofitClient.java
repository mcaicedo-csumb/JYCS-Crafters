/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * Singleton Retrofit client for accessing the "Advice Slip" API.
 * This class creates and configures a Retrofit instance with:
 * - Base URL pointing to the Advice Slip API
 * - Gson converter for automatic JSON parsing
 *
 * Usage:
 * - Call {@link #getInstance()} to obtain the Retrofit instance.
 * - Use the instance to create service interfaces (e.g., {@code AdviceService}).
 *
 * Example:
 * AdviceService service = RetrofitClient.getInstance().create(AdviceService.class);
 */
package com.stanissudo.jycs_crafters.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /** Base URL for the Advice Slip API. */
    private static final String BASE_URL = "https://api.adviceslip.com/";

    /** Singleton Retrofit instance. */
    private static Retrofit retrofit;

    /** Private constructor to prevent instantiation. */
    private RetrofitClient() {}

    /**
     * Returns the singleton Retrofit instance.
     * If the instance does not exist yet, it is created and configured.
     *
     * @return A configured Retrofit instance for making API calls.
     */
    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
