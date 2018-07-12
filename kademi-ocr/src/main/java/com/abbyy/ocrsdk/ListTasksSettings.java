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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author dylan
 */
public class ListTasksSettings {

    private Date fromDate;
    private Date toDate;
    private Boolean excludeDeleted;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Boolean getExcludeDeleted() {
        return excludeDeleted;
    }

    public void setExcludeDeleted(Boolean excludeDeleted) {
        this.excludeDeleted = excludeDeleted;
    }

    public String asUrlParams() {
        Map<String, String> params = new HashMap();

        params.put("fromDate", AbbyyUtils.formatDate(fromDate));
        params.put("toDate", AbbyyUtils.formatDate(toDate));
        params.put("excludeDeleted", excludeDeleted != null ? excludeDeleted.toString() : "");

        return AbbyyUtils.mapToUrlParams(params, true);
    }
}
