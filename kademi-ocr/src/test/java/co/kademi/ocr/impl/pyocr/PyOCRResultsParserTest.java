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

import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import java.io.File;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 *
 * @author dylan
 */
public class PyOCRResultsParserTest {

    public PyOCRResultsParserTest() {
    }

    @Test
    public void testParseResults() throws Exception {
        File file = new File("src/test/resources/test_data/pyocr.results");
        PyOCRResult result = PyOCRResultsParser.parseResults(file);

        assertNotNull(result);
        assertEquals(64, result.getResult().size());
        assertEquals(95.30122767857141d, result.getConfidence());
    }

    @Test
    public void testConvertToTable() throws Exception {
        File file = new File("src/test/resources/test_data/pyocr.results");
        PyOCRResult result = PyOCRResultsParser.parseResults(file);

        OCRTable table = PyOCRResultsParser.convertToTable(result);

        assertEquals(result.getConfidence(), table.getTotalConfidence());
        assertEquals(22, table.getRows().size());

        // Check first row
        OCRRow firstRow = table.getRows().get(0);
        assertNotNull(firstRow);
        assertEquals(1, firstRow.getCells().size());
        assertEquals("Itinerary", firstRow.getCells().get(0).getText());

        // Check Last Row
        OCRRow lastRow = table.getRows().get(table.getRows().size() - 1);
        assertNotNull(lastRow);
        assertEquals(3, lastRow.getCells().size());
        assertEquals("19", lastRow.getCells().get(0).getText());
    }

}
