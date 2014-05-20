/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

/**
 */
class PollResult<T> {

    static final PollResult<Void> CONTINUE = new PollResult<>(null);

    private final T result;

    PollResult(final T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    public boolean isContinue() {
        return this == CONTINUE;
    }

    public static <T> PollResult<T> letsContinue() {
        return (PollResult<T>) CONTINUE;
    }

    public static <T> PollResult<T> result(T result) {
        return new PollResult<>(result);
    }
}
