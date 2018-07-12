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
package com.abbyy.ocrsdk;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import java.util.Date;

/**
 *
 * @author dylan
 */
@XStreamAlias(value = "task")
public class Task {

    public enum TaskStatus {
        Unknown, Submitted, Queued, InProgress, Completed, ProcessingFailed, Deleted, NotEnoughCredits
    }

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private TaskStatus status = TaskStatus.Unknown;

    @XStreamAsAttribute
    private String error;

    @XStreamAsAttribute
    private Date registrationTimeDate;

    @XStreamAsAttribute
    private Date statusChangeTime;

    @XStreamAsAttribute
    private long filesCount;

    @XStreamAsAttribute
    private long credits;

    @XStreamAsAttribute
    private long estimatedProcessingTime;

    @XStreamAsAttribute
    private String description;

    @XStreamAsAttribute
    private String resultUrl;

    @XStreamAsAttribute
    private String resultUrl2;

    @XStreamAsAttribute
    private String resultUrl3;

    public String getId() {
        return id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Date getRegistrationTimeDate() {
        return registrationTimeDate;
    }

    public Date getStatusChangeTime() {
        return statusChangeTime;
    }

    public long getFilesCount() {
        return filesCount;
    }

    public long getCredits() {
        return credits;
    }

    public long getEstimatedProcessingTime() {
        return estimatedProcessingTime;
    }

    public String getDescription() {
        return description;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public String getResultUrl2() {
        return resultUrl2;
    }

    public String getResultUrl3() {
        return resultUrl3;
    }
}
