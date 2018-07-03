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
package co.kademi.ocr.impl.pyocr;

import co.kademi.ocr.api.OCRCell;
import co.kademi.ocr.api.OCRListener;
import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.junit.After;
import org.junit.Test;

/**
 *
 * @author dylan
 */
public class PyOCRServiceTest {

    public PyOCRServiceTest() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testScanToTable() throws FileNotFoundException, InterruptedException {
        File file = new File("src/test/resources/test_data/pyocr.png");

        FileInputStream in = new FileInputStream(file);

        String jobId = "myJobId";
        PyOCRService instance = new PyOCRService();

        instance.registerListener(new OCRListener() {
            @Override
            public void onScanComplete(String jobId, OCRTable table) {
                System.out.println("onScanComplete: " + table);
            }

            @Override
            public void onScanFailed(String jobId, String reason) {
                System.out.println("onScanFailed: " + reason);
            }
        });

        instance.scanToTable(in, jobId);

        Thread.sleep(5000);

        while (!instance.isAllComplete()) {
            Thread.sleep(1000);
        }
    }

    @Test
    public void testScanToTable_test2() throws FileNotFoundException, InterruptedException {
        File file = new File("src/test/resources/test_data/scanSample.PNG");

        FileInputStream in = new FileInputStream(file);

        String jobId = "myJobId";
        PyOCRService instance = new PyOCRService();

        instance.registerListener(new OCRListener() {
            @Override
            public void onScanComplete(String jobId, OCRTable table) {
                System.out.println("onScanComplete: " + table);
                System.out.println("Total Confidence " + table.getTotalConfidence());

                int rowNum = 0;
                for (OCRRow row : table.getRows()) {
                    rowNum++;

                    int cellNum = 0;
                    for (OCRCell cell : row.getCells()) {
                        cellNum++;

                        System.out.println("Row " + rowNum + " Cell " + cellNum + " Confidence: " + cell.getConfidence());
                    }
                }
            }

            @Override
            public void onScanFailed(String jobId, String reason) {
                System.out.println("onScanFailed: " + reason);
            }
        });

        instance.scanToTable(in, jobId);

        Thread.sleep(5000);

        while (!instance.isAllComplete()) {
            Thread.sleep(1000);
        }
    }

}
