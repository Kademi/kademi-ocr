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
package co.kademi.ocr.impl.mock;

import co.kademi.ocr.api.OCRCell;
import co.kademi.ocr.api.OCRListener;
import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRService;
import co.kademi.ocr.impl.bean.OCRCellBean;
import co.kademi.ocr.impl.bean.OCRRowBean;
import co.kademi.ocr.impl.bean.OCRTableBean;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author brad
 */
public class MockOCRService implements OCRService {

    private Set<OCRListener> listeners;

    @Override
    public void registerListener(OCRListener l) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        listeners.add(l);
    }

    @Override
    public void scanToTable(InputStream in, String jobId) {
        List<OCRRow> rows = new ArrayList<>();

        List<OCRCell> cells = addRow(rows);
        addCell(cells, "ipad1", 100d); // SKU
        addCell(cells, "user1", 100d); // user
        addCell(cells, "3", 100d); // quantity
        addCell(cells, "26.66", 100d); // total cost

        addCell(cells, "ipad2", 100d); // SKU
        addCell(cells, "user1", 100d); // user
        addCell(cells, "2", 100d); // quantity
        addCell(cells, "16.66", 100d); // total cost

        addCell(cells, "ipad1", 100d); // SKU
        addCell(cells, "user2", 100d); // user
        addCell(cells, "2", 100d); // quantity
        addCell(cells, "16.66", 100d); // total cost


        OCRTableBean t = new OCRTableBean(randomConfidence(), rows);

        for (OCRListener l : listeners) {
            l.onScanComplete(jobId, t);
        }
    }

    private Double randomConfidence() {
        return Math.random();
    }

    private List<OCRCell> addRow(List<OCRRow> rows) {
        List<OCRCell> cells = new ArrayList();
        OCRRowBean r = new OCRRowBean(cells);
        rows.add(r);
        return cells;
    }

    private void addCell(List<OCRCell> cells, String val, Double conf) {
        OCRCellBean c = new OCRCellBean(val, conf);
        cells.add(c);
    }
}
