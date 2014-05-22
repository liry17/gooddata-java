/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;

/**
 * Represents the result retrieved by polling on the REST API.
 */
public class PollFuture<T> implements Future<T> {

    private final AbstractService service;
    private final String pollingUri;
    private final AbstractService.ConditionCallback condition;
    private final Class<T> returnClass;
    private volatile T result;

    public PollFuture(final AbstractService service, final String pollingUri,
                      final AbstractService.ConditionCallback condition, final Class<T> returnClass) {
        this.service = notNull(service, "service");
        this.pollingUri = notEmpty(pollingUri, "pollingUri");
        this.condition = notNull(condition, "condition");
        this.returnClass = notNull(returnClass, "returnClass");
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
        if (result != null) {
            return result;
        }
        return service.poll(pollingUri, condition, returnClass);
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (result != null) {
            return result;
        }
        return service.poll(pollingUri, condition, returnClass, timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public String getPollingUri() {
        return pollingUri;
    }
}
