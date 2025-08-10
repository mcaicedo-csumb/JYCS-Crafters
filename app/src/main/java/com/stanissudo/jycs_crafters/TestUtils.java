package com.stanissudo.jycs_crafters;

import static java.util.concurrent.TimeUnit.SECONDS;

import androidx.lifecycle.LiveData;

import java.util.concurrent.CountDownLatch;

public final class TestUtils {
    private TestUtils() {}

    public static <T> T getOrAwait(LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);
        liveData.observeForever(o -> {
            data[0] = o;
            latch.countDown();
        });
        if (!latch.await(5, SECONDS)) throw new AssertionError("LiveData never emitted a value");
        @SuppressWarnings("unchecked")
        T out = (T) data[0];
        return out;
    }
}
