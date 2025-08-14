/**
 * Author: Jose
 * Date: 2025-08-13
 *
 * Represents the JSON response structure from the "Advice Slip" API.
 * This class is used by Retrofit to deserialize API responses into Java objects.
 *
 * Structure of a sample JSON:
 * {
 *   "slip": {
 *     "advice": "Some useful life advice.",
 *     "slip_id": "123"
 *   }
 * }
 *
 * Notes:
 * - The `Slip` inner class holds the actual advice text and its unique ID.
 * - Used in combination with Retrofit's `AdviceService` interface.
 */

package com.stanissudo.jycs_crafters.network;

public class AdviceResponse {

    /** The container object holding the advice slip details. */
    private Slip slip;

    /**
     * Gets the slip object containing advice details.
     *
     * @return Slip object with advice text and ID.
     */
    public Slip getSlip() {
        return slip;
    }

    /**
     * Represents the "slip" object inside the API response.
     * Contains the actual advice text and its unique identifier.
     */
    public static class Slip {

        /** The advice text provided by the API. */
        private String advice;

        /** The unique identifier for this advice slip. */
        private String slip_id;

        /**
         * Gets the advice text.
         *
         * @return A string containing the advice.
         */
        public String getAdvice() {
            return advice;
        }

        /**
         * Gets the slip ID.
         *
         * @return A string representing the unique slip ID.
         */
        public String getSlip_id() {
            return slip_id;
        }
    }
}
