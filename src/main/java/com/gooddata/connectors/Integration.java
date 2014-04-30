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

import static com.gooddata.Validate.notEmpty;

/**
 * Connectors integration
 */
@JsonTypeName("integration")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Integration {

    public static final String URL = "/gdc/projects/{project}/connectors/{connector}/integration";

    private String projectTemplate;
    private boolean active;
    private final Process lastFinishedProcess;
    private final Process lastSuccessfulProcess;
    private final Process runningProcess;

    public Integration(final String projectTemplate) {
        this(projectTemplate, true, null, null, null);
    }

    @JsonCreator
    public Integration(@JsonProperty("projectTemplate") String projectTemplate, @JsonProperty("active") boolean active,
                       @JsonProperty("lastFinishedProcess") Process lastFinishedProcess,
                       @JsonProperty("lastSuccessfulProcess") Process lastSuccessfulProcess,
                       @JsonProperty("runningProcess") Process runningProcess) {
        this.projectTemplate = notEmpty(projectTemplate, "projectTemplate");
        this.active = active;
        this.lastFinishedProcess = lastFinishedProcess;
        this.lastSuccessfulProcess = lastSuccessfulProcess;
        this.runningProcess = runningProcess;
    }

    public String getProjectTemplate() {
        return projectTemplate;
    }

    public void setProjectTemplate(final String projectTemplate) {
        this.projectTemplate = notEmpty(projectTemplate, "projectTemplate");
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Process getLastFinishedProcess() {
        return lastFinishedProcess;
    }

    public Process getLastSuccessfulProcess() {
        return lastSuccessfulProcess;
    }

    public Process getRunningProcess() {
        return runningProcess;
    }
}
