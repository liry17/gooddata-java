/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.connectors;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Connector process
 */
@JsonTypeName("integration")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Process {

    public static final String URL = "/gdc/projects/{project}/connectors/{connector}/integration/processes";
    private final Status status;
    private final String started; // todo date
    private final String finished; // todo date

    @JsonCreator
    public Process(@JsonProperty("status") Status status, @JsonProperty("started") String started,
                   @JsonProperty("finished") String finished) {
        this.status = status;
        this.started = started;
        this.finished = finished;
    }

    public Status getStatus() {
        return status;
    }

    public String getStarted() {
        return started;
    }

    public String getFinished() {
        return finished;
    }

    public static class Status {
        private final String code;
        private final String detail;
        private final String description;

        @JsonCreator
        public Status(@JsonProperty("code") String code, @JsonProperty("detail") String detail,
                      @JsonProperty("description") String description) {
            this.code = code;
            this.detail = detail;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDetail() {
            return detail;
        }

        public String getDescription() {
            return description;
        }
    }
}
