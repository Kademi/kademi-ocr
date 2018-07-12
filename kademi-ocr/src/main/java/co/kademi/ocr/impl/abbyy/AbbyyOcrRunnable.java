/*
 * Copyright 2018 brad.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.kademi.ocr.impl.abbyy;

import co.kademi.ocr.api.OCRListener;
import co.kademi.ocr.api.OCRTable;
import com.abbyy.ocrsdk.AbbyErrorResponse;
import com.abbyy.ocrsdk.AbbyyResponse;
import com.abbyy.ocrsdk.AbbyySuccessResponse;
import com.abbyy.ocrsdk.CloudClient;
import com.abbyy.ocrsdk.ProcessingSettings;
import com.abbyy.ocrsdk.Task;
import com.abbyy.ocrsdk.finereader.Document;
import java.io.File;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dylan
 */
public class AbbyyOcrRunnable implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(AbbyyOcrRunnable.class);

    private final String inputFileName;
    private final String jobId;
    private final Set<OCRListener> listeners;

    private final String applicationId;
    private final String applicationPassword;

    public AbbyyOcrRunnable(String inputFileName, String jobId, String applicationId, String applicationPassword, Set<OCRListener> listeners) {
        this.inputFileName = inputFileName;
        this.jobId = jobId;
        this.applicationId = applicationId;
        this.applicationPassword = applicationPassword;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        CloudClient restClient = new CloudClient(applicationId, applicationPassword);

        ProcessingSettings settings = new ProcessingSettings();
        settings.setExportFormat(ProcessingSettings.ExportFormat.xmlForCorrectedImage);
        settings.setXml_writeRecognitionVariants(Boolean.TRUE);
        settings.setReadBarcodes(Boolean.FALSE);
        settings.setProfile("textExtraction");

        AbbyyResponse resp = null;

        try {
            File file = new File(inputFileName);

            resp = restClient.processImage(file, settings);
        } catch (Exception ex) {
            String reason = String.format("Error submitting image to Abby: %s", ex.getMessage());
            LOG.error(reason, ex);
            onFail(reason);
        }

        if (resp != null) {
            // Check if it was successful
            if (resp.isSuccess()) {
                try {
                    Task task = resp.getSuccessResponse().getTasks()[0];
                    waitAndDownloadResults(restClient, task.getId());
                } catch (Exception ex) {
                    String reason = String.format("Error fetching results from Abby: %s", ex.getMessage());
                    LOG.error(reason, ex);
                    onFail(reason);
                }
            } else {
                AbbyErrorResponse errorResponse = resp.getErrorResponse();
                String reason = String.format("Error submitting file to Abby: %s", errorResponse.getMessage());
                LOG.warn(reason);
                onFail(reason);
            }
        }
    }

    private void waitAndDownloadResults(CloudClient restClient, String taskId) throws Exception {
        LOG.info("Waiting for Abby to process document | Task ID: {}", taskId);

        boolean processing = true;

        while (processing) {
            if (StringUtils.isNotBlank(taskId)) {
                AbbyyResponse resp = restClient.getTaskStatus(taskId);

                if (resp.isSuccess()) {
                    AbbyySuccessResponse successResp = resp.getSuccessResponse();

                    if (successResp.getTasks() != null && successResp.getTasks().length > 0) {
                        Task task = successResp.getTasks()[0];

                        if (task != null) {
                            if (task.getStatus() != null) {
                                switch (task.getStatus()) {
                                    case Completed: {
                                        processing = false;

                                        // Download and process file
                                        Document doc = restClient.downloadXml(task);
                                        OCRTable ocrTable = generateTable(doc);

                                        // Send table to listeners
                                        onSuccess(ocrTable);

                                        // Delete task
                                        try {
                                            restClient.deleteTask(task.getId());
                                        } catch (Exception e) {
                                            LOG.warn("Failed to delete task: {}", e.getMessage(), e);
                                        }

                                        break;
                                    }
                                    case ProcessingFailed: {
                                        processing = false;

                                        String reason = task.getError();
                                        if (StringUtils.isBlank(reason)) {
                                            reason = "Processing failed.";
                                        }

                                        onFail(reason);
                                        break;
                                    }
                                    case NotEnoughCredits: {
                                        processing = false;

                                        onFail("Not enough credits to process file");
                                        break;
                                    }
                                    case Deleted: {
                                        processing = false;

                                        onFail("Task has been deleted");
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        // TODO Log an error
                    }
                }
            }
            Thread.sleep(2000);
        }
    }

    private OCRTable generateTable(Document document) {
        return AbbyyResultsParser.convertToTable(document);
    }

    private void onFail(String reason) {
        if (listeners != null) {
            for (OCRListener l : listeners) {
                l.onScanFailed(jobId, reason);
            }
        }
    }

    private void onSuccess(OCRTable table) {
        if (listeners != null) {
            for (OCRListener l : listeners) {
                l.onScanComplete(jobId, table);
            }
        }
    }

}
