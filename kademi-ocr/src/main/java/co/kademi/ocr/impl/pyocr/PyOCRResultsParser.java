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
import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import co.kademi.ocr.impl.bean.OCRCellBean;
import co.kademi.ocr.impl.bean.OCRRowBean;
import co.kademi.ocr.impl.bean.OCRTableBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.beanutils.BeanComparator;

/**
 *
 * @author dylan
 */
public class PyOCRResultsParser {

    private static final double EPSILON = 3; // Margin of error

    private static ObjectMapper objectMapper;

    private static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {

            objectMapper = new ObjectMapper();

            SimpleModule module = new SimpleModule();

            module.addDeserializer(BoxPosition.class, new PositionDeserializer());

            objectMapper.registerModule(module);
        }

        return objectMapper;
    }

    public static PyOCRResult parseResults(File file) throws IOException {
        ObjectMapper om = getObjectMapper();

        PyOCRResult result = om.readValue(file, PyOCRResult.class);

        return result;
    }

    public static OCRTable convertToTable(PyOCRResult pyOCRResult) {
        List<LineBox> results = new ArrayList(pyOCRResult.getResult());
        double confidence = pyOCRResult.getConfidence();

        List<OCRRow> rows = processRows(results);

        return new OCRTableBean(confidence, rows);
    }

    private static List<OCRRow> processRows(List<LineBox> results) {
        Map<Integer, List<LineBox>> rowMap = new HashMap();

        // Sort results into Rows
        results.sort((LineBox o1, LineBox o2) -> {
            if (o2 == null) {
                return 1;
            }

            BoxPosition p1 = o1.getPosition();
            BoxPosition p2 = o2.getPosition();

            int result = compareDouble(p1.getY(), p2.getY());

            if (result == 0) {
                result = compareDouble(p1.getX1(), p2.getX1());
            }

            return result;
        });

        // Move rows
        for (LineBox lb : results) {
            BoxPosition position = lb.getPosition();

            Integer rowKey = getClosestRowKey(rowMap, position);

            List<LineBox> cellList = rowMap.get(rowKey);

            if (cellList == null) {
                cellList = new ArrayList();
                rowMap.put(rowKey, cellList);
            }

            cellList.add(lb);
        }

        // Sort rows and add to list
        List<OCRRow> ocrRows = new ArrayList();

        SortedSet<Integer> keys = new TreeSet<>(rowMap.keySet());
        for (Integer key : keys) {
            List<LineBox> cellList = rowMap.get(key);

            cellList.sort(new BeanComparator<>("position.x1"));

            List<OCRCell> cells = new ArrayList();

            for (LineBox lb : cellList) {
                OCRCellBean cellBean = new OCRCellBean(lb.getContent(), lb.getAverageConfidence());
                cells.add(cellBean);
            }

            OCRRowBean rowBean = new OCRRowBean(cells);
            ocrRows.add(rowBean);
        }

        return ocrRows;
    }

    private static Integer getClosestRowKey(Map<Integer, List<LineBox>> rowMap, BoxPosition position) {
        if (rowMap.isEmpty()) {
            return position.getY();
        }

        for (Integer key : rowMap.keySet()) {
            if (integerEqualTo(position.getY(), key, EPSILON)) {
                return key;
            }
        }

        return position.getY();
    }

    private static int compareDouble(double left, double right) {
        if (doubleGreaterThan(left, right, EPSILON, false)) {
            return 1;
        } else if (doubleLessThan(left, right, EPSILON, false)) {
            return -1;
        } else if (doubleEqualTo(left, right, EPSILON)) {
            return 0;
        }

        return 0;
    }

    private static boolean doubleGreaterThan(double left, double right, double epsilon, boolean orEqualTo) {
        if (doubleEqualTo(left, right, epsilon)) {
            // within epsilon, so considered equal
            return orEqualTo;
        }
        return left > right;
    }

    private static boolean doubleLessThan(double left, double right, double epsilon, boolean orEqualTo) {
        if (doubleEqualTo(left, right, epsilon)) {
            // within epsilon, so considered equal
            return orEqualTo;
        }
        return left < right;
    }

    private static boolean doubleEqualTo(double left, double right, double epsilon) {
        return Math.abs(left - right) <= epsilon;
    }

    private static boolean integerEqualTo(int left, int right, double epsilon) {
        return Math.abs(left - right) <= epsilon;
    }
}
