/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
* TODO
*/
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
class MaqlDdlLinks {

    private static final String TASKS_STATUS = "tasks-status";

    private final List<LinkEntry> entries;

    @JsonCreator
    MaqlDdlLinks(@JsonProperty("entries") List<LinkEntry> entries) {
        this.entries = entries;
    }

    public String getStatusLink() {
        for (LinkEntry linkEntry : entries) {
            if (TASKS_STATUS.equals(linkEntry.getCategory())) {
                return linkEntry.getLink();
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private static class LinkEntry {
        private final String link;
        private final String category;

        @JsonCreator
        private LinkEntry(@JsonProperty("link") String link, @JsonProperty("category") String category) {
            this.link = link;
            this.category = category;
        }

        public String getLink() {
            return link;
        }

        public String getCategory() {
            return category;
        }
    }
}
