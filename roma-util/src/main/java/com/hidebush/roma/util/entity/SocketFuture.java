package com.hidebush.roma.util.entity;

import java.util.concurrent.*;

/**
 * Created by htf on 2021/8/9.
 */
public class SocketFuture<T> implements Future<T> {

    private T response;
    private final CountDownLatch latch = new CountDownLatch(1);

    public void set(T response) {
        this.response = response;
        latch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return latch.getCount() == 0;
    }

    @Override
    public T get() throws InterruptedException {
        latch.await();
        return response;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        latch.await(timeout, unit);
        if (response == null) {
            throw new TimeoutException();
        }
        return response;
    }
}
