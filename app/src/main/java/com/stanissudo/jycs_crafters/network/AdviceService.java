/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * Retrofit service interface for accessing the "Advice Slip" API.
 * This interface defines the HTTP request to retrieve a random piece of advice.
 *
 * Usage:
 * - Implemented automatically by Retrofit at runtime.
 * - Used in combination with `RetrofitClient` to create the service instance.
 * - Returns a `Call<AdviceResponse>` object for asynchronous or synchronous execution.
 *
 * Example:
 * AdviceService service = retrofit.create(AdviceService.class);
 * service.getAdvice().enqueue(callback);
 */

package com.stanissudo.jycs_crafters.network;

import retrofit2.Call;
import retrofit2.http.GET;

public interface AdviceService {

    /**
     * Sends an HTTP GET request to the "advice" endpoint of the API.
     * Returns a random advice slip in the form of an {@link AdviceResponse}.
     *
     * @return A Retrofit {@link Call} object containing an {@link AdviceResponse}.
     */
    @GET("advice")
    Call<AdviceResponse> getAdvice();
}
