/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.project;

import com.gooddata.AbstractService;
import com.gooddata.GoodDataException;
import com.gooddata.GoodDataRestException;
import com.gooddata.gdc.UriResponse;
import com.gooddata.account.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collection;

import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;

/**
 */
public class ProjectService extends AbstractService {

    private final AccountService accountService;

    public ProjectService(RestTemplate restTemplate, AccountService accountService) {
        super(restTemplate);
        this.accountService = notNull(accountService, "accountService");
    }

    public Collection<Project> getProjects() {
        try {
            final String id = accountService.getCurrent().getId();
            final Projects projects = restTemplate.getForObject(Project.PROJECTS_URI, Projects.class, id);
            return projects.getProjects();
        } catch (GoodDataException | RestClientException e) {
            throw new GoodDataException("Unable to get projects", e);
        }
    }

    public Project createProject(Project project) {
        notNull(project, "project");

        final UriResponse uri = restTemplate.postForObject(Projects.URI, project, UriResponse.class);

        return poll(uri.getUri(),
                new ConditionCallback() {
                    @Override
                    public boolean finished(ClientHttpResponse response) throws IOException {
                        final Project project = extractData(response, Project.class);
                        return "ENABLED".equalsIgnoreCase(project.getContent().getState());
                    }
                }, Project.class
        );
    }

    public Project getProjectByUri(final String uri) {
        notEmpty(uri, "uri");
        try {
            return restTemplate.getForObject(uri, Project.class);
        } catch (GoodDataRestException e) {
            if (HttpStatus.NOT_FOUND.value() == e.getStatusCode()) {
                throw new ProjectNotFoundException(uri, e);
            } else {
                throw e;
            }
        } catch (RestClientException e) {
            throw new GoodDataException("Unable to get project " + uri, e);
        }
    }

    public Project getProjectById(String id) {
        notEmpty(id, "id");
        return getProjectByUri(Project.PROJECT_TEMPLATE.expand(id).toString());
    }

    public Collection<ProjectTemplate> getProjectTemplates(Project project) {
        notNull(project, "project");
        try {
            final ProjectTemplates templates = restTemplate.getForObject(ProjectTemplate.URI, ProjectTemplates.class, project.getId());
            return templates.getTemplatesInfo();
        } catch (GoodDataRestException | RestClientException e) {
            throw new GoodDataException("Unable to get project templates", e);
        }
    }
}
