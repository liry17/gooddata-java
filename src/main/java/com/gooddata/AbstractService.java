/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.gooddata.Validate.notNull;
import static java.lang.String.format;
import static org.springframework.http.HttpMethod.GET;

/**
 */
public abstract class AbstractService {

    public static Integer WAIT_BEFORE_RETRY_IN_MILLIS = 5 * 1000;
    public static Integer MAX_ATTEMPTS = 5;

    protected final RestTemplate restTemplate;

    protected final ObjectMapper mapper = new ObjectMapper();

    private final RequestCallback noopRequestCallback = new RequestCallback() {
        @Override
        public void doWithRequest(final ClientHttpRequest request) throws IOException {
        }
    };
    private final ResponseExtractor<ClientHttpResponse> reusableResponseExtractor = new ResponseExtractor<ClientHttpResponse>() {
        @Override
        public ClientHttpResponse extractData(final ClientHttpResponse response) throws IOException {
            return new ReusableClientHttpResponse(response);
        }
    };


    public AbstractService(RestTemplate restTemplate) {
        this.restTemplate = notNull(restTemplate, "restTemplate");
    }

    public <T> T poll(String pollingUri, Class<T> cls) {
        return poll(pollingUri, new StatusOkConditionCallback(), cls);
    }

    @Deprecated // todo remove
    public <T> T poll(String pollingUri, ConditionCallback condition, Class<T> returnClass) {
        try {
            return poll(pollingUri, condition, returnClass, 0, null);
        } catch (TimeoutException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T poll(String pollingUri, ConditionCallback condition, Class<T> returnClass, long timeout, TimeUnit unit)
            throws TimeoutException, InterruptedException {
        final long start = System.currentTimeMillis();
        while (true) {
            final PollResult<T> result = pollInternal(pollingUri, condition, returnClass);
            if (!result.isContinue()) {
                return result.getResult();
            }
            if (unit != null && start + unit.toMillis(timeout) > System.currentTimeMillis()) {
                throw new TimeoutException();
            }

            Thread.sleep(WAIT_BEFORE_RETRY_IN_MILLIS);
        }
    }

    protected <T> PollResult<T> pollInternal(String pollingUri, ConditionCallback condition, Class<T> returnClass) {
        final ClientHttpResponse response = restTemplate.execute(pollingUri, GET, noopRequestCallback,
                reusableResponseExtractor);

        try {
            if (condition.finished(response)) {
                final List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
                final ResponseExtractor<T> extractor = new HttpMessageConverterExtractor<>(returnClass, converters);
                final T data = extractor.extractData(response);
                return PollResult.result(data);
            } else if (HttpStatus.Series.CLIENT_ERROR.equals(response.getStatusCode().series())) {
                throw new GoodDataException(
                        format("Polling returned client error HTTP status %s", response.getStatusCode().value())
                );
            }
        } catch (IOException e) {
            throw new GoodDataException("I/O error occurred during HTTP response extraction", e);
        }
        return PollResult.letsContinue();
    }

    protected <T> T extractData(ClientHttpResponse response, Class<T> cls) throws IOException {
        return new HttpMessageConverterExtractor<>(cls, restTemplate.getMessageConverters()).extractData(response);
    }

    public static interface ConditionCallback {
        boolean finished(ClientHttpResponse response) throws IOException;
    }

    public static class StatusOkConditionCallback implements ConditionCallback {
        @Override
        public boolean finished(ClientHttpResponse response) throws IOException {
            return HttpStatus.OK.equals(response.getStatusCode());
        }
    }


    private class ReusableClientHttpResponse implements ClientHttpResponse {

        private final byte[] body;
        private final HttpStatus statusCode;
        private final int rawStatusCode;
        private final String statusText;
        private final HttpHeaders headers;

        public ReusableClientHttpResponse(ClientHttpResponse response) {
            try {
                body = FileCopyUtils.copyToByteArray(response.getBody());
                statusCode = response.getStatusCode();
                rawStatusCode = response.getRawStatusCode();
                statusText = response.getStatusText();
                headers = response.getHeaders();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read from HTTP response", e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }

        @Override
        public HttpStatus getStatusCode() throws IOException {
            return statusCode;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return rawStatusCode;
        }

        @Override
        public String getStatusText() throws IOException {
            return statusText;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public InputStream getBody() throws IOException {
            return new ByteArrayInputStream(body);
        }

        @Override
        public void close() {
            //already closed
        }
    }

}
