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

import com.thoughtworks.xstream.core.util.ThreadSafeSimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author dylan
 */
public class AbbyyUtils {

    private final static ThreadSafeSimpleDateFormat sdf = new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", null, 2, 10, true);

    public static String mapToUrlParams(Map<String, String> params, boolean ignoreBlank) {
        StringBuilder sb = new StringBuilder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (ignoreBlank) {
                    if (StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue())) {
                        if (sb.length() > 0) {
                            sb.append("&");
                        }
                        sb.append(entry.getKey()).append("=").append(entry.getValue());
                    }
                } else {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }

        return sb.toString();
    }

    public static String formatDate(Date date) {
        if (date != null) {
            return sdf.format(date);
        }
        return null;
    }
}
