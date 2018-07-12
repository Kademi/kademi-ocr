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
import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import co.kademi.ocr.impl.bean.OCRRowBean;
import co.kademi.ocr.impl.bean.OCRTableBean;
import com.abbyy.ocrsdk.finereader.BlockType;
import com.abbyy.ocrsdk.finereader.CharParamsType;
import com.abbyy.ocrsdk.finereader.Document;
import com.abbyy.ocrsdk.finereader.FormattingType;
import com.abbyy.ocrsdk.finereader.LineType;
import com.abbyy.ocrsdk.finereader.ParagraphType;
import com.abbyy.ocrsdk.finereader.TextType;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.bind.JAXBElement;
import org.apache.commons.beanutils.BeanComparator;

/**
 *
 * @author dylan
 */
public class AbbyyResultsParser {

    private static final double EPSILON = 3; // Margin of error

    public static OCRTable convertToTable(Document document) {
        double cellsConfidence = 0d;
        double cellsCount = 0d;

        List<AbbyyOCRCell> cells = new ArrayList<>();

        // Create rows and For-Loop-Ception!
        if (document != null) {
            if (document.getPage() != null) {
                for (Document.Page page : document.getPage()) {
                    if (page.getBlock() != null) {
                        for (BlockType blockType : page.getBlock()) {
                            if ("text".equalsIgnoreCase(blockType.getBlockType())) {
                                if (blockType.getText() != null) {
                                    for (TextType textType : blockType.getText()) {
                                        if (textType.getPar() != null) {
                                            for (ParagraphType parType : textType.getPar()) {
                                                if (parType.getLine() != null) {
                                                    for (LineType lineType : parType.getLine()) {
                                                        AbbyyOCRCell cell = processRow(lineType);
                                                        cells.add(cell);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Process rows
        List<OCRRow> rows = processRows(cells);

        // Calculate confidence
        for (OCRRow row : rows) {
            for (OCRCell cell : row.getCells()) {
                cellsConfidence += cell.getConfidence();
                cellsCount++;
            }
        }

        double totalConfidence = 0d;
        if (!(cellsConfidence == 0 || cellsCount == 0)) {
            totalConfidence = cellsConfidence / cellsCount;
        }

        return new OCRTableBean(totalConfidence, rows);
    }

    private static List<OCRRow> processRows(List<AbbyyOCRCell> cells) {
        Map<BigInteger, List<AbbyyOCRCell>> rowMap = new HashMap();

        // Sort cells so we can create rows
        cells.sort((AbbyyOCRCell o1, AbbyyOCRCell o2) -> {
            if (o2 == null || o2.getTop() == null) {
                return 1;
            } else if (o1 == null || o1.getTop() == null) {
                return -1;
            }

            int result = compareDouble(o1.getTop().doubleValue(), o2.getTop().doubleValue());

            if (result == 0) {
                result = compareDouble(o1.getLeft().doubleValue(), o2.getLeft().doubleValue());
            }

            return result;
        });

        // Move rows
        for (AbbyyOCRCell cell : cells) {
            BigInteger rowKey = getClosestRowKey(rowMap, cell);

            List<AbbyyOCRCell> cellList = rowMap.get(rowKey);

            if (cellList == null) {
                cellList = new ArrayList();
                rowMap.put(rowKey, cellList);
            }

            cellList.add(cell);
        }

        // Sort rows and add to list
        List<OCRRow> ocrRows = new ArrayList();

        SortedSet<BigInteger> keys = new TreeSet<>(rowMap.keySet());

        for (BigInteger key : keys) {
            List<AbbyyOCRCell> cellList = rowMap.get(key);

            cellList.sort(new BeanComparator<>("left"));

            List<OCRCell> sortedCellsList = new ArrayList();
            sortedCellsList.addAll(cellList);

            OCRRowBean rowBean = new OCRRowBean(sortedCellsList);
            ocrRows.add(rowBean);
        }

        return ocrRows;
    }

    private static AbbyyOCRCell processRow(LineType lineType) {
        String cellContents = "";
        double cellConfidenceSum = 0d;
        double cellCount = 0d;

        for (FormattingType formattingType : lineType.getFormatting()) {
            if (formattingType.getContent() != null) {
                for (Serializable ser : formattingType.getContent()) {
                    if (ser instanceof JAXBElement) {
                        JAXBElement jaxbe = (JAXBElement) ser;
                        Object jaxVal = jaxbe.getValue();

                        if (jaxVal instanceof CharParamsType) {
                            CharParamsType charParamsType = (CharParamsType) jaxVal;

                            if (charParamsType.getCharConfidence() != null) {
                                cellConfidenceSum += charParamsType.getCharConfidence().doubleValue();
                            }
                            cellCount++;

                            String stringContent = "";
                            List<Serializable> content = charParamsType.getContent();
                            for (Serializable ser2 : content) {
                                if (ser2 instanceof String) {
                                    stringContent += ser2;
                                }
                            }

                            cellContents += stringContent.replace("\n", "");
                        }
                    }
                }
            }
        }

        double cellConfidence = 0d;

        if (!(cellCount == 0 || cellConfidenceSum == 0)) {
            cellConfidence = cellConfidenceSum / cellCount;
        }

        return new AbbyyOCRCell(cellContents, cellConfidence, lineType.getL(), lineType.getR(), lineType.getT(), lineType.getB());
    }

    private static BigInteger getClosestRowKey(Map<BigInteger, List<AbbyyOCRCell>> rowMap, AbbyyOCRCell cell) {
        if (rowMap.isEmpty()) {
            return cell.getTop();
        }

        for (BigInteger key : rowMap.keySet()) {
            if (integerEqualTo(cell.getTop(), key, EPSILON)) {
                return key;
            }
        }

        return cell.getTop();
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

    private static boolean integerEqualTo(BigInteger left, BigInteger right, double epsilon) {
        int leftInt = left != null ? left.intValue() : 0;
        int rightInt = right != null ? right.intValue() : 0;

        return Math.abs(leftInt - rightInt) <= epsilon;
    }
}
