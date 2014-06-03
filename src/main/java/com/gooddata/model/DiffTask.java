/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * TODO may be move somewhere to common
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName("asyncTask")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DiffTask {

    @JsonProperty("link")
    private Link pollLink;

    @JsonCreator
    public DiffTask(@JsonProperty("link") Link pollLink) {
        this.pollLink = pollLink;
    }

    @JsonIgnore
    public String getPollUri() {
        return pollLink.getPollUri();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Link {

        @JsonProperty("poll")
        private String pollUri;

        @JsonCreator
        public Link(@JsonProperty("poll") String pollUri) {
            this.pollUri = pollUri;
        }

        @JsonIgnore
        public String getPollUri() {
            return pollUri;
        }
    }

}
