package com.stanissudo.jycs_crafters.network;

public class AdviceResponse {
    private Slip slip;

    public Slip getSlip() {
        return slip;
    }

    public static class Slip {
        private String advice;
        private String slip_id;

        public String getAdvice() {
            return advice;
        }

        public String getSlip_id() {
            return slip_id;
        }
    }
}
