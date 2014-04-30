/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.connectors;

import com.gooddata.AbstractService;
import com.gooddata.GoodDataException;
import com.gooddata.gdc.UriResponse;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectService;
import com.gooddata.project.ProjectTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;

/**
 * Connectors
 */
public class ConnectorsService extends AbstractService {

    private final ProjectService projectService;

    public ConnectorsService(final RestTemplate restTemplate, final ProjectService projectService) {
        super(restTemplate);
        this.projectService = notNull(projectService, "projectService");
    }

    public Integration createIntegration(final Project project, final Settings settings) {
        notNull(project, "project");
        notNull(settings, "settings");
        final Collection<ProjectTemplate> projectTemplates = projectService.getProjectTemplates(project);
        if (projectTemplates == null || projectTemplates.isEmpty()) {
            throw new GoodDataException("Project " + project.getId() + "doesn't contain a template reference");
        }
        final ProjectTemplate template = notNull(projectTemplates.iterator().next(), "project template");
        final Integration integration = createIntegration(project, settings.getConnectorName(), new Integration(template.getUrl()));
        updateSettings(project, settings);
        return integration;
    }

    public Integration createIntegration(final Project project, final String connector, final Integration integration) {
        notNull(project, "project");
        notEmpty(connector, "connector");
        notNull(integration, "integration");
        return restTemplate.postForObject(Integration.URL, integration, Integration.class, project.getId(), connector);
    }

    public <T extends Settings> void updateSettings(final Project project, final T settings) {
        notNull(settings, "settings");
        notNull(project, "project");
        restTemplate.put(Settings.URL, settings, project.getId(), settings.getConnectorName());
    }

    public Process startProcess(Project project, ProcessExecution execution) {
        notNull(project, "project");
        notNull(execution, "execution");
        final String connectorName = execution.getConnectorName();
        final UriResponse response = restTemplate.postForObject(Process.URL, execution, UriResponse.class, project.getId(), connectorName);
        // todo poll
        return restTemplate.getForObject(response.getUri(), Process.class);
    }
}
