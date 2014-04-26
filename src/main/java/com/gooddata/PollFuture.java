/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 */
public class PollFuture<T> implements Future<T> {

    private final AbstractService service;
    private final String pollingUri;
    private final AbstractService.ConditionCallback condition;
    private final Class<T> returnClass;
    private volatile T result;

    public PollFuture(final AbstractService service, String pollingUri, AbstractService.ConditionCallback condition, Class<T> returnClass) {
        this.service = service;
        this.pollingUri = pollingUri;
        this.condition = condition;
        this.returnClass = returnClass;
    }

    @Override
    public boolean isDone() {
        if (result != null) {
            return true;
        }
        final PollResult<T> pollResult = service.pollInternal(pollingUri, condition, returnClass);
        if (pollResult.isContinue()) {
            return false;
        } else {
            result = pollResult.getResult();
            return true;
        }
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }
}
