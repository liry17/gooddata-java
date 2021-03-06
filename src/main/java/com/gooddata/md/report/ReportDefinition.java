/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.md.report;

import com.gooddata.md.Meta;
import com.gooddata.md.Obj;
import com.gooddata.md.Queryable;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;

/**
 */
@JsonTypeName("reportDefinition")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReportDefinition extends Obj implements Queryable {

    private final ReportDefinitionContent content;

    @JsonCreator
    public ReportDefinition(@JsonProperty("meta") Meta meta, @JsonProperty("content") ReportDefinitionContent content) {
        super(meta);
        this.content = content;
    }

    public ReportDefinitionContent getContent() {
        return content;
    }

    public static class Content {
        private final Collection<String> definitions;

        @JsonCreator
        public Content(@JsonProperty("definitions") Collection<String> definitions) {
            this.definitions = definitions;
        }

        public Collection<String> getDefinitions() {
            return definitions;
        }
    }

}
