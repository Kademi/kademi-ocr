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
package com.abbyy.ocrsdk;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author dylan
 */
public class ProcessingSettings {

    public enum ExportFormat {
        txt, rtf, docx, xlsx, pptx, pdfSearchable, pdfTextAndImages, xml, xmlForCorrectedImage, alto
    }

    private String language;
    private String profile;
    private String textType;
    private String imageSource;
    private Boolean correctOrientation;
    private Boolean correctSkew;
    private Boolean readBarcodes;
    private ExportFormat exportFormat;
    private Boolean xml_writeFormatting;
    private Boolean xml_writeRecognitionVariants;
    private String pdf_writeTags;
    private String description;
    private String pdfPassword;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTextType() {
        return textType;
    }

    public void setTextType(String textType) {
        this.textType = textType;
    }

    public String getImageSource() {
        return imageSource;
    }

    public void setImageSource(String imageSource) {
        this.imageSource = imageSource;
    }

    public Boolean getCorrectOrientation() {
        return correctOrientation;
    }

    public void setCorrectOrientation(Boolean correctOrientation) {
        this.correctOrientation = correctOrientation;
    }

    public Boolean getCorrectSkew() {
        return correctSkew;
    }

    public void setCorrectSkew(Boolean correctSkew) {
        this.correctSkew = correctSkew;
    }

    public Boolean getReadBarcodes() {
        return readBarcodes;
    }

    public void setReadBarcodes(Boolean readBarcodes) {
        this.readBarcodes = readBarcodes;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public Boolean getXml_writeFormatting() {
        return xml_writeFormatting;
    }

    public void setXml_writeFormatting(Boolean xml_writeFormatting) {
        this.xml_writeFormatting = xml_writeFormatting;
    }

    public Boolean getXml_writeRecognitionVariants() {
        return xml_writeRecognitionVariants;
    }

    public void setXml_writeRecognitionVariants(Boolean xml_writeRecognitionVariants) {
        this.xml_writeRecognitionVariants = xml_writeRecognitionVariants;
    }

    public String getPdf_writeTags() {
        return pdf_writeTags;
    }

    public void setPdf_writeTags(String pdf_writeTags) {
        this.pdf_writeTags = pdf_writeTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPdfPassword() {
        return pdfPassword;
    }

    public void setPdfPassword(String pdfPassword) {
        this.pdfPassword = pdfPassword;
    }

    public String asUrlParams() {
        Map<String, String> params = new LinkedHashMap();

        params.put("language", language);
        params.put("profile", profile);
        params.put("textType", textType);
        params.put("imageSource", imageSource);

        params.put("correctOrientation", correctOrientation != null ? correctOrientation.toString() : "");
        params.put("correctSkew", correctSkew != null ? correctSkew.toString() : "");
        params.put("readBarcodes", readBarcodes != null ? readBarcodes.toString() : "");
        params.put("xml:writeFormatting", xml_writeFormatting != null ? xml_writeFormatting.toString() : "");
        params.put("xml:writeRecognitionVariants", xml_writeRecognitionVariants != null ? xml_writeRecognitionVariants.toString() : "");

        params.put("exportFormat", exportFormat != null ? exportFormat.toString() : "");

        params.put("pdf:writeTags", pdf_writeTags);
        params.put("pdfPassword", pdfPassword);
        params.put("description", description);

        return AbbyyUtils.mapToUrlParams(params, true);
    }

    public String getOutputFileExt() {
        switch (exportFormat) {
            case txt:
                return ".txt";
            case rtf:
                return ".rtf";
            case docx:
                return ".docx";
            case xlsx:
                return ".xlsx";
            case pptx:
                return ".pptx";
            case pdfSearchable:
            case pdfTextAndImages:
                return ".pdf";
            case xml:
                return ".xml";
        }
        return ".ocr";
    }
}
