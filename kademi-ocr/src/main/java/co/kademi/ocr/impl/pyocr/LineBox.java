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
public class LineBox {

    private transient Double confidence;

    private String content;
    private BoxPosition position;
    private List<WordBox> wordBoxes;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BoxPosition getPosition() {
        return position;
    }

    public void setPosition(BoxPosition position) {
        this.position = position;
    }

    public List<WordBox> getWordBoxes() {
        return wordBoxes;
    }

    public void setWordBoxes(List<WordBox> wordBoxes) {
        this.wordBoxes = wordBoxes;
    }

    @Transient
    public double getAverageConfidence() {
        if (wordBoxes == null || wordBoxes.isEmpty()) {
            return 0d;
        }

        if (confidence == null) {
            if (wordBoxes.size() == 1) {
                confidence = wordBoxes.get(0).getConfidence();
            } else {
                double count = 0;
                double sum = 0;

                for (WordBox wb : wordBoxes) {
                    sum += wb.getConfidence();
                    count++;
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
