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
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
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
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
            builder.setAcceptAnyCertificate(true);
            AsyncHttpClient client = new AsyncHttpClient(builder.build());

            AsyncHttpClient.BoundRequestBuilder requestBuilder = client.prepareGet(task.getResultUrl());

            ListenableFuture<Response> asyncProcess = requestBuilder.execute();

            Response resp = asyncProcess.get();

            if (resp.getStatusCode() >= 200 && resp.getStatusCode() <= 300) {
                if (resp.hasResponseBody()) {
                    JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

                    return (Document) unmarshaller.unmarshal(resp.getResponseBodyAsStream());
                } else {
                    throw new Exception("No body content received, Code: " + resp.getStatusCode() + " | Message: " + resp.getStatusText());
                }
            } else {
                throw new Exception("No body content received, Code: " + resp.getStatusCode() + " | Message: " + resp.getStatusText());
            }
        }
    }

    protected AbbyyResponse postFileToUrl(byte[] fileBytes, String url) throws IOException, Exception {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setAcceptAnyCertificate(true);
        AsyncHttpClient client = new AsyncHttpClient(builder.build());

        AsyncHttpClient.BoundRequestBuilder requestBuilder = client.preparePost(url);

        try {
            requestBuilder.setBody(fileBytes);

            // Set auth
            setupAuthorization(requestBuilder);

            ListenableFuture<Response> asyncProcess = requestBuilder.execute();

            Response resp = asyncProcess.get();

            if (resp.hasResponseBody()) {
                return parseResponse(resp.getResponseBodyAsStream());
            } else {
                throw new Exception("No body content received, Code: " + resp.getStatusCode() + " | Message: " + resp.getStatusText());
            }
        } finally {
            try {
                client.close();
            } catch (Exception e) {
            }
        }
    }

    protected AbbyyResponse doGet(String url) throws IOException, Exception {
        AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
        builder.setAcceptAnyCertificate(true);
        AsyncHttpClient client = new AsyncHttpClient(builder.build());

        AsyncHttpClient.BoundRequestBuilder requestBuilder = client.prepareGet(url);

        try {
            // Set auth
            setupAuthorization(requestBuilder);

            ListenableFuture<Response> asyncProcess = requestBuilder.execute();

            Response resp = asyncProcess.get();

            if (resp.hasResponseBody()) {
                return parseResponse(resp.getResponseBodyAsStream());
            } else {
                throw new Exception("No body content received, Code: " + resp.getStatusCode() + " | Message: " + resp.getStatusText());
            }
        } finally {
            try {
                client.close();
            } catch (Exception e) {
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

    private void setupAuthorization(AsyncHttpClient.BoundRequestBuilder req) {
        String authString = "Basic " + encodeUserPassword();
        authString = authString.replaceAll("\n", "");
        req.addHeader("Authorization", authString);
    }

    private String encodeUserPassword() {
        String toEncode = applicationId + ":" + password;
        return Base64.encodeBase64String(toEncode.getBytes());
    }
}
