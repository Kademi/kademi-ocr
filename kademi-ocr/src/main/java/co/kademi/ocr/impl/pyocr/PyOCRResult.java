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

import java.beans.Transient;
import java.util.List;

/**
 *
 * @author dylan
 */
public class PyOCRResult {

    private transient Double confidence;

    private List<LineBox> result;

    public List<LineBox> getResult() {
        return result;
    }

    public void setResult(List<LineBox> result) {
        this.result = result;
    }

    @Transient
    public double getConfidence() {
        if (result == null || result.isEmpty()) {
            return 0d;
        }

        if (confidence == null) {
            if (result.size() == 1) {
                confidence = result.get(0).getAverageConfidence();
            } else {
                double count = 0;
                double sum = 0;

                for (LineBox lb : result) {
                    count++;
                    sum += lb.getAverageConfidence();
                }

                if (count == 0 || sum == 0) {
                    confidence = 0d;
                } else {
                    confidence = sum / count;
                }
            }
        }

        return confidence;
    }
}
