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

import com.abbyy.ocrsdk.finereader.Document;
import com.amazonaws.util.StringInputStream;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author dylan
 */
public class CloudClient {

    private final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private final XStream xstream = new XStream();

    private String baseUrl = "https://cloud.ocrsdk.com";
    private String applicationId;
    private String password;

    public CloudClient() {
        this(null, null);
    }

    public CloudClient(String applicationId, String password) {
        this.applicationId = applicationId;
        this.password = password;

        xstream.registerConverter(new DateConverter(dateFormat, new String[]{dateFormat}));
        xstream.processAnnotations(AbbyyResponse.class);
        xstream.processAnnotations(Task.class);
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public AbbyyResponse processImage(File file, ProcessingSettings settings) throws Exception {
        byte[] fileBytes = FileUtils.readFileToByteArray(file);

        return processImage(fileBytes, settings);
    }

    public AbbyyResponse processImage(byte[] fileBytes, ProcessingSettings settings) throws Exception {
        String url = baseUrl + "/processImage?";
        if (settings != null) {
            url += settings.asUrlParams();
        }

        return postFileToUrl(fileBytes, url);
    }

    public AbbyyResponse getTaskStatus(String taskId) throws Exception {
        String url = baseUrl + "/getTaskStatus?taskId=" + taskId;

        return doGet(url);
    }

    public AbbyyResponse listTasks(ListTasksSettings settings) throws Exception {
        String url = baseUrl + "/listTasks";
        if (settings != null) {
            url += settings.asUrlParams();
        }

        return doGet(url);
    }

    public AbbyyResponse listFinishedTasks() throws Exception {
        String url = baseUrl + "/listFinishedTasks";

        return doGet(url);
    }

    public AbbyyResponse deleteTask(String taskId) throws Exception {
        String url = baseUrl + "/deleteTask?taskId=" + taskId;

        return doGet(url);
    }

    public Document downloadXml(Task task) throws IOException, JAXBException, Exception {
        if (task == null || task.getResultUrl() == null) {
            return null;
        } else {
            HttpUriRequest req = RequestBuilder.get(task.getResultUrl()).build();
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            CloseableHttpResponse resp = httpClient.execute(req);

            StatusLine statusLine = resp.getStatusLine();

            if (statusLine != null && statusLine.getStatusCode() >= 200 && statusLine.getStatusCode() <= 300) {
                HttpEntity entity = resp.getEntity();
                if (entity != null) {

                    JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    return (Document) unmarshaller.unmarshal(entity.getContent());
                } else {
                    throw new Exception("No body content received, Code: " + statusLine.getStatusCode() + " | Message: " + statusLine.getReasonPhrase());
                }
            } else if (statusLine != null) {
                throw new Exception("Error fetching content, Code: " + statusLine.getStatusCode() + " | Message: " + statusLine.getReasonPhrase());
            } else {
                throw new Exception("Error fetching content, Unknown Reason");
            }
        }
    }

    protected AbbyyResponse postFileToUrl(byte[] fileBytes, String url) throws IOException, Exception {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientBuilder.create().build();

            HttpEntity httpEntity = new ByteArrayEntity(fileBytes);
            HttpUriRequest req = RequestBuilder.post(url).setEntity(httpEntity).build();

            setupAuthorization(req);

            CloseableHttpResponse resp = httpClient.execute(req);

            StatusLine statusLine = resp.getStatusLine();

            HttpEntity entity = resp.getEntity();
            if (entity != null) {
                return parseResponse(entity.getContent());
            } else {
                throw new Exception("No body content received, Code: " + statusLine.getStatusCode() + " | Message: " + statusLine.getReasonPhrase());
            }
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    protected AbbyyResponse doGet(String url) throws IOException, Exception {
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpClientBuilder.create().build();

            HttpUriRequest req = RequestBuilder.get(url).build();

            setupAuthorization(req);

            CloseableHttpResponse resp = httpClient.execute(req);

            StatusLine statusLine = resp.getStatusLine();

            HttpEntity entity = resp.getEntity();
            if (entity != null) {
                return parseResponse(entity.getContent());
            } else {
                throw new Exception("No body content received, Code: " + statusLine.getStatusCode() + " | Message: " + statusLine.getReasonPhrase());
            }
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

    protected AbbyyResponse parseResponse(String xml) throws IOException {
        InputStream in = new StringInputStream(xml);

        return parseResponse(in);
    }

    protected AbbyyResponse parseResponse(InputStream xml) throws IOException {
        Object o = xstream.fromXML(xml);

        if (o instanceof AbbyySuccessResponse) {
            AbbyySuccessResponse s = (AbbyySuccessResponse) o;
            return new AbbyyResponse(s, null);
        } else if (o instanceof AbbyErrorResponse) {
            AbbyErrorResponse s = (AbbyErrorResponse) o;
            return new AbbyyResponse(null, s);
        }

        return null;
    }

    private void setupAuthorization(HttpUriRequest req) {
        String authString = "Basic " + encodeUserPassword();
        authString = authString.replaceAll("\n", "");
        req.addHeader("Authorization", authString);
    }

    private String encodeUserPassword() {
        String toEncode = applicationId + ":" + password;
        return Base64.encodeBase64String(toEncode.getBytes());
    }
}
