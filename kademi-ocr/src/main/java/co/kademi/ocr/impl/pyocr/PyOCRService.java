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
package co.kademi.ocr.impl.pyocr;

import co.kademi.ocr.api.OCRListener;
import co.kademi.ocr.api.OCRService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dylan
 */
public class PyOCRService implements OCRService {

    private final static Logger LOG = LoggerFactory.getLogger(PyOCRService.class);

    private final Set<OCRListener> listeners = new LinkedHashSet<>();

    private final BlockingQueue<Runnable> processingQueue;
    private final ThreadPoolExecutor processingThreadPool;

    public PyOCRService() {
        this.processingQueue = new LinkedBlockingQueue<>(100);
        this.processingThreadPool = new ThreadPoolExecutor(10, 20, 30, TimeUnit.SECONDS, processingQueue);
    }

    @Override
    public void registerListener(OCRListener l) {
        listeners.add(l);
    }

    @Override
    public void scanToTable(InputStream in, String jobId) {
        try {
            String tempFileName = saveToTempFile(in, jobId);

            PyOCRRunnable r = new PyOCRRunnable(tempFileName, jobId, listeners);
            processingThreadPool.submit(r);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Saves the InputStream to a temp file and returns the fill path to the
     * file
     *
     * @param in
     * @return
     */
    private String saveToTempFile(InputStream in, String jobId) throws IOException {
        File temp = File.createTempFile(jobId + "-", ".tmp");

        FileOutputStream fileOut = new FileOutputStream(temp);

        IOUtils.copy(in, fileOut);

        return temp.getAbsolutePath();
    }

    /**
     * This is only used for testing
     *
     * @return
     */
    protected boolean isAllComplete() {
        return processingQueue.isEmpty() && processingThreadPool.getActiveCount() == 0;
    }
}
