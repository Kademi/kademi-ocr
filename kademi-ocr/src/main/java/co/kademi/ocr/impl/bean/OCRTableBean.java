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
package co.kademi.ocr.impl.bean;

import co.kademi.ocr.api.OCRRow;
import co.kademi.ocr.api.OCRTable;
import java.util.List;

/**
 *
 * @author brad
 */
public class OCRTableBean implements OCRTable{

    private final Double confidence;
    private final List<OCRRow> rows;

    public OCRTableBean() {
        this.confidence = null;
        this.rows = null;
    }

    public OCRTableBean(Double confidence, List<OCRRow> rows) {
        this.confidence = confidence;
        this.rows = rows;
    }
    
    
    @Override
    public Double getTotalConfidence() {
        return confidence;
    }

    @Override
    public List<OCRRow> getRows() {
        return rows;
    }
    
}
