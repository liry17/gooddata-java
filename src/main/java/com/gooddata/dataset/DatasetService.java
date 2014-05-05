/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataset;

import com.gooddata.AbstractService;
import com.gooddata.GoodDataRestException;
import com.gooddata.gdc.DataStoreException;
import com.gooddata.gdc.DataStoreService;
import com.gooddata.project.Project;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;
import static java.util.Collections.emptyMap;

/**
 */
public class DatasetService extends AbstractService {

    private static final String MANIFEST_FILE_NAME = "upload_info.json";
    private static final String STATUS_FILE_NAME = "upload_status.json";

    private final DataStoreService dataStoreService;

    public DatasetService(RestTemplate restTemplate, DataStoreService dataStoreService) {
        super(restTemplate);
        this.dataStoreService = notNull(dataStoreService, "dataStoreService");
    }

    public DatasetManifest getDatasetManifest(Project project, String datasetId) {
        notNull(project, "project");
        notEmpty(datasetId, "datasetId");
        try {
            return restTemplate.getForObject(DatasetManifest.URI, DatasetManifest.class, project.getId(), datasetId);
        } catch (GoodDataRestException e) {
            if (e.getStatusCode() == 404) {
                throw new DatasetNotFoundException(datasetId, e);
            } else {
                throw new DatasetException("Unable to get manifest", datasetId, e);
            }
        } catch (RestClientException e) {
            throw new DatasetException("Unable to get manifest", datasetId, e);
        }
    }

    public void loadDataset(Project project, DatasetManifest manifest, InputStream dataset) {
        notNull(project, "project");
        notNull(dataset, "dataset");
        notNull(manifest, "manifest");
        final Path dirPath = Paths.get("/", project.getId() + "_" + RandomStringUtils.randomAlphabetic(3), "/");
        try {
            dataStoreService.upload(dirPath.resolve(manifest.getFile()).toString(), dataset);
            final String manifestJson = mapper.writeValueAsString(manifest);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(manifestJson.getBytes(UTF_8));
            dataStoreService.upload(dirPath.resolve(MANIFEST_FILE_NAME).toString(), inputStream);

            final PullTask pullTask = restTemplate.postForObject(Pull.URI, new Pull(dirPath.toString()), PullTask.class, project.getId());
            final PullTaskStatus taskStatus = poll(pullTask.getUri(), new ConditionCallback() {
                @Override
                public boolean finished(ClientHttpResponse response) throws IOException {
                    final PullTaskStatus status = extractData(response, PullTaskStatus.class);
                    return status.isFinished();
                }
            }, PullTaskStatus.class);
            if (!taskStatus.isSuccess()) {
                String message = "status: " + taskStatus.getStatus();
                try {
                    final InputStream input = dataStoreService.download(dirPath.resolve(STATUS_FILE_NAME).toString());
                    final FailStatus status = mapper.readValue(input, FailStatus.class);
                    if (status != null && status.getError() != null) {
                        message = status.getError().getFormattedMessage();
                    }
                } catch (IOException | DataStoreException ignored) {
                    // todo log?
                }
                throw new DatasetException(message, manifest.getDataSet());
            }
        } catch (IOException e) {
            throw new DatasetException("Unable to serialize manifest", manifest.getDataSet(), e);
        } catch (DataStoreException | GoodDataRestException | RestClientException e) {
            throw new DatasetException("Unable to load", manifest.getDataSet(), e);
        } finally {
            try {
                dataStoreService.delete(dirPath.toString() + "/");
            } catch (DataStoreException ignored) {
                // todo log?
            }
        }

    }

    public void loadDataset(Project project, String datasetId, InputStream dataset) {
        notNull(project, "project");
        notEmpty(datasetId, "datasetId");
        notNull(dataset, "dataset");
        loadDataset(project, getDatasetManifest(project, datasetId), dataset);
    }

    public Map<String, String> listDatasets(Project project) {
        notNull(project, "project");
        final Datasets result = restTemplate.getForObject(Datasets.URI, Datasets.class, project.getId());
        if (result == null || result.getLinks() == null) {
            return emptyMap();
        }
        final Map<String, String> datasets = new LinkedHashMap<>(result.getLinks().size());
        for (Datasets.Link link: result.getLinks()) {
            datasets.put(link.getIdentifier(), link.getTitle());
        }
        return datasets;
    }
}
