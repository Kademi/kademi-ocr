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
import co.kademi.ocr.api.OCRService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dylan
 */
public class AbbyyOCRService implements OCRService {

    private final static Logger LOG = LoggerFactory.getLogger(AbbyyOCRService.class);

    private final Set<OCRListener> listeners = new LinkedHashSet<>();
    private final BlockingQueue<Runnable> processingQueue;
    private final ThreadPoolExecutor processingThreadPool;

    private final String applicationId;
    private final String applicationPassword;

    public AbbyyOCRService(String applicationId, String applicationPassword) {
        this.applicationId = applicationId;
        this.applicationPassword = applicationPassword;

        this.processingQueue = null;
        this.processingThreadPool = null;
    }

    @Override
    public void registerListener(OCRListener l) {
        listeners.add(l);
    }

    @Override
    public void scanToTable(InputStream in, String jobId) {
        try {
            String tempFileName = saveToTempFile(in, jobId);

            AbbyyOcrRunnable r = new AbbyyOcrRunnable(tempFileName, jobId, applicationId, applicationPassword, listeners);
            processingThreadPool.submit(r);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String saveToTempFile(InputStream in, String jobId) throws IOException {
        File temp = File.createTempFile(jobId + "-", ".tmp");

        FileOutputStream fileOut = new FileOutputStream(temp);

        IOUtils.copy(in, fileOut);

        return temp.getAbsolutePath();
    }

}
