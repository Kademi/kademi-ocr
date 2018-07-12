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
import java.math.BigInteger;

/**
 *
 * @author dylan
 */
public class AbbyyOCRCell implements OCRCell {

    private String text;
    private Double confidence;

    private BigInteger left;
    private BigInteger right;
    private BigInteger top;
    private BigInteger bottom;

    public AbbyyOCRCell(String text, Double confidence, BigInteger left, BigInteger right, BigInteger top, BigInteger bottom) {
        this.text = text;
        this.confidence = confidence;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public BigInteger getLeft() {
        return left;
    }

    public void setLeft(BigInteger left) {
        this.left = left;
    }

    public BigInteger getRight() {
        return right;
    }

    public void setRight(BigInteger right) {
        this.right = right;
    }

    public BigInteger getTop() {
        return top;
    }

    public void setTop(BigInteger top) {
        this.top = top;
    }

    public BigInteger getBottom() {
        return bottom;
    }

    public void setBottom(BigInteger bottom) {
        this.bottom = bottom;
    }

}
