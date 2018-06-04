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
import co.kademi.ocr.api.OCRTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Thread.interrupted;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dylan
 */
public class PyOCRRunnable implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(PyOCRRunnable.class);

    private final String inputFileName;
    private final String jobId;
    private final Set<OCRListener> listeners;

    private String outputTempName;

    public PyOCRRunnable(String inputFileName, String jobId, Set<OCRListener> listeners) {
        this.inputFileName = inputFileName;
        this.jobId = jobId;
        this.listeners = listeners;
    }

    @Override
    public void run() {
        try {
            File outputTemp = File.createTempFile(jobId + "-", ".output");
            FileUtils.write(outputTemp, "", Charset.defaultCharset(), true);

            outputTempName = outputTemp.getAbsolutePath();

            ProcessBuilder builder = new ProcessBuilder("kademi-ocr.py", "--convert", "--format", "json", "--input", inputFileName, "--output", outputTempName);

            Process process = builder.start();

            int exitCode = process.waitFor();

            StringBuilder sb = new StringBuilder();
            int len;
            if ((len = process.getErrorStream().available()) > 0) {
                byte[] buf = new byte[len];
                process.getErrorStream().read(buf);
                sb.append("Command error:\t\"").append(new String(buf)).append("\"");
            }

            if (exitCode == 0) {
                PyOCRResult pyResults = PyOCRResultsParser.parseResults(new File(outputTempName));

                OCRTable ocrTable = PyOCRResultsParser.convertToTable(pyResults);

                onSuccess(ocrTable);
            } else {
                String errorOutput = sb.toString();
                if (StringUtils.isNotBlank(errorOutput)) {
                    onFail(errorOutput);
                } else {
                    // It's an error, So get the output file with the reason
                    ObjectMapper mapper = new ObjectMapper();
                    HashMap<String, String> map = mapper.readValue(new File(this.outputTempName), new TypeReference<HashMap<String, String>>() {
                    });

                    onFail(map.get("error"));
                }
            }
        } catch (IOException | InterruptedException ex) {
            LOG.error("Error processing OCR: {}", ex.getMessage(), ex);

            String reason = String.format("Error processing OCR: %s", ex.getMessage());
            onFail(reason);
        } finally {
            // Cleanup any tmp files that were created
            FileUtils.deleteQuietly(new File(inputFileName));
            FileUtils.deleteQuietly(new File(outputTempName));
        }
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

    private abstract static class StreamReader extends Thread {

        private InputStream is;

        protected abstract void processLine(String line);

        public StreamReader(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                try (BufferedReader br = new BufferedReader(isr)) {
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if (interrupted()) {
                            break;
                        }
                        processLine(line);
                    }
                }
            } catch (IOException ioe) {
                LOG.error("Exception in StreamReader", ioe);
            }
        }
    }

    private static class ScriptOutputReader extends StreamReader {

        private StringBuffer sb = new StringBuffer();

        private int maxLength = 1000000;

        public ScriptOutputReader(InputStream is) {
            super(is);
        }

        @Override
        protected void processLine(String cmdOut) {
            sb.append(cmdOut).append("\n");
            if (sb.length() > maxLength) {
                sb = (StringBuffer) sb.subSequence(maxLength, sb.length() - 1);
            }
        }

        @Override
        public String toString() {
            return sb.toString();
        }
    }
}
