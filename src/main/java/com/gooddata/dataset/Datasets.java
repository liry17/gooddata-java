/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataset;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.List;

/**
 * Datasets
 */
@JsonTypeName("about")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
// todo this is almost copy of Gdc
public class Datasets {

    public static final String URI = "/gdc/md/{project}/ldm/singleloadinterface";

    private final String category;
    private final String summary;
    private final List<Link> links;

    @JsonCreator
    public Datasets(@JsonProperty("category") String category, @JsonProperty("summary") String summary,
               @JsonProperty("links") List<Link> links) {
        this.category = category;
        this.summary = summary;
        this.links = links;
    }

    public String getCategory() {
        return category;
    }

    public String getSummary() {
        return summary;
    }

    public Collection<Link> getLinks() {
        return links;
    }

    public Link getLink(String category) {
        for (Link link: links) {
            if (category.equals(link.getCategory())) {
                return link;
            }
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Link {
        private final String identifier;
        private final String category;
        private final String link;
        private final String summary;
        private final String title;

        @JsonCreator
        public Link(@JsonProperty("identifier") String identifier, @JsonProperty("category") String category, @JsonProperty("link") String link,
                    @JsonProperty("summary") String summary, @JsonProperty("title") String title) {
            this.identifier = identifier;
            this.category = category;
            this.link = link;
            this.summary = summary;
            this.title = title;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getCategory() {
            return category;
        }

        public String getLink() {
            return link;
        }

        public String getSummary() {
            return summary;
        }

        public String getTitle() {
            return title;
        }
    }
}
