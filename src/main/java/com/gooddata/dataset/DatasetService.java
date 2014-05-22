/*
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.dataset;

import com.gooddata.AbstractService;
import com.gooddata.FutureResult;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.gooddata.Validate.notEmpty;
import static com.gooddata.Validate.notNull;

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

    public FutureResult<PullTaskStatus> loadDataset(final Project project, final DatasetManifest manifest, final InputStream dataset) {
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
            return new FutureResult<>(this, pullTask.getUri(), new ConditionCallback() {
                @Override
                public boolean finished(ClientHttpResponse response) throws IOException {
                    final PullTaskStatus status = extractData(response, PullTaskStatus.class);
                    final boolean finished = status.isFinished();
                    if (finished && !status.isSuccess()) {
                        String message = "status: " + status.getStatus();
                        try {
                            final InputStream input = dataStoreService.download(dirPath.resolve(STATUS_FILE_NAME).toString());
                            final FailStatus failStatus = mapper.readValue(input, FailStatus.class);
                            if (failStatus != null && failStatus.getError() != null) {
                                message = failStatus.getError().getFormattedMessage();
                            }
                        } catch (IOException | DataStoreException ignored) {
                            // todo log?
                        }
                        throw new DatasetException(message, manifest.getDataSet());
                    }
                    return finished;
                }
            }, PullTaskStatus.class);
        } catch (IOException e) {
            throw new DatasetException("Unable to serialize manifest", manifest.getDataSet(), e);
        } catch (DataStoreException | GoodDataRestException | RestClientException e) {
            throw new DatasetException("Unable to load", manifest.getDataSet(), e);
        } finally {
            try {
                // todo this handling should be part of future result
                //dataStoreService.delete(dirPath.toString() + "/");
            } catch (DataStoreException ignored) {
                // todo log?
            }
        }

    }

    public FutureResult<PullTaskStatus> loadDataset(Project project, String datasetId, InputStream dataset) {
        notNull(project, "project");
        notEmpty(datasetId, "datasetId");
        notNull(dataset, "dataset");
        return loadDataset(project, getDatasetManifest(project, datasetId), dataset);
    }
}
