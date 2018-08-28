/*
 * Copyright 2018 dylan.
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

import co.kademi.ocr.api.OCRCell;
import co.kademi.ocr.api.OCRListener;
import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author dylan
 */
public class AbbyyOcrRunnableTest {

    public AbbyyOcrRunnableTest() {
    }

    /**
     * Test of run method, of class AbbyyOcrRunnable.
     */
    //@Test
    public void testRun() {
        Set<OCRListener> listeners = new HashSet<>();
        listeners.add(new OCRListener() {
            @Override
            public void onScanComplete(String jobId, OCRTable table) {
                System.out.println("onScanComplete");
                printTable(table);
            }

            @Override
            public void onScanFailed(String jobId, String reason) {
                System.out.println("onScanFailed | JobId: " + jobId + " | Reason: " + reason);
            }
        });

        AbbyyOcrRunnable instance = new AbbyyOcrRunnable("src/test/resources/test_data/pyocr.png", "myJobId", "KademiOCR", "b8/ZarO7mU0CFuTj0ra/chnJ", listeners);
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            instance.run();
            return null;
        });
    }

    private void printTable(OCRTable table) {
        System.out.println("onScanComplete: " + table);
        System.out.println("Total Confidence " + table.getTotalConfidence());

        for (OCRRow row : table.getRows()) {
            for (OCRCell cell : row.getCells()) {
                System.out.print(cell.getText() + " (" + cell.getConfidence() + ") | ");
            }
            System.out.println("");
        }
    }

}
