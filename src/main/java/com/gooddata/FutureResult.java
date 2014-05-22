/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import java.util.concurrent.TimeUnit;

import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;

/**
 * Represents the result retrieved by polling on the REST API.
 */
public class FutureResult<T> {

    private final AbstractService service;
    private final String pollingUri;
    private final AbstractService.ConditionCallback condition;
    private final Class<T> returnClass;
    private volatile T result;

    public FutureResult(final AbstractService service, final String pollingUri,
                        final AbstractService.ConditionCallback condition, final Class<T> returnClass) {
        this.service = notNull(service, "service");
        this.pollingUri = notEmpty(pollingUri, "pollingUri");
        this.condition = notNull(condition, "condition");
        this.returnClass = notNull(returnClass, "returnClass");
    }

    public boolean isDone() {
        if (result != null) {
            return true;
        }
        final PollResult<T> pollResult = service.pollOnce(pollingUri, condition, returnClass);
        if (pollResult.isContinue()) {
            return false;
        } else {
            result = pollResult.getResult();
            return true;
        }
    }

    public T get() {
        if (result != null) {
            return result;
        }
        return service.poll(pollingUri, condition, returnClass, 0, null);
    }

    public T get(final long timeout, final TimeUnit unit) {
        if (result != null) {
            return result;
        }
        return service.poll(pollingUri, condition, returnClass, timeout, unit);
    }

    public String getPollingUri() {
        return pollingUri;
    }
}
