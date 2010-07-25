/*
 *   Copyright 2010, Novoda ltd 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package novoda.rest.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;

/**
 * A collection of utility methods for handling parameters.
 */
public class HTTPUtils {

    /**
     * Given a selection string and selectionArgs, returns the key/value pair to
     * be used in a HTTP request. Usually the selection and selectionArgs are
     * given during a query/delete/update/insert call against the
     * RESTProvider/ContentProvider.
     * 
     * @param selection string used in conjunction with selectionArgs
     * @param selectionArgs each string will be replaced with the orderly '?'
     *            character in the selection string
     * @return key/value pairs mapped between the selection string and the
     *         selectionArgs replacing the '?'
     */
    public static Map<String, String> convertToParams(String selection, String[] selectionArgs) {
        if (selection == null)
            return new HashMap<String, String>();

        String t = selection.replaceAll("\\sAND\\s|\\sand\\s", "&");
        if (selectionArgs != null) {
            for (int i = 0; i < selectionArgs.length; i++) {
                t = t.replaceFirst("\\?", selectionArgs[i]);
            }
        }
        return parseQuerystring(t);
    }

    /**
     * Parse a query string into a map of key/value pairs.
     * 
     * @param queryString the string to parse (without the '?')
     * @return key/value pairs mapping
     */
    public static Map<String, String> parseQuerystring(String queryString) {
        Map<String, String> map = new HashMap<String, String>();
        if ((queryString == null) || (queryString.equals(""))) {
            return map;
        }
        String[] params = queryString.split("&");
        for (String param : params) {
            try {
                String[] keyValuePair = param.split("=", 2);
                String name = URLDecoder.decode(keyValuePair[0], "UTF-8");
                if (name == "") {
                    continue;
                }
                String value = keyValuePair.length > 1 ? URLDecoder
                        .decode(keyValuePair[1], "UTF-8") : "";
                map.put(name, value);
            } catch (UnsupportedEncodingException e) {
            }
        }
        return map;
    }

    /**
     * Convert to params.
     * 
     * @param values the values
     * @return the map
     */
    public static Map<String, String> convertToParams(ContentValues values) {
        Map<String, String> ret = new HashMap<String, String>();
        for (Entry<String, Object> entry : values.valueSet()) {
            ret.put(entry.getKey(), entry.getValue().toString());
        }
        return ret;
    }

    /**
     * Convert to query string.
     * 
     * @param param the param
     * @return the string
     */
    public static String convertToQueryString(Map<String, String> param) {
        if (param == null)
            return null;

        StringBuffer buf = new StringBuffer("?");
        for (Map.Entry<String, String> entry : param.entrySet()) {
            buf.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        buf.deleteCharAt(buf.length() - 1);
        return buf.toString();
    }

    public static List<NameValuePair> convertToHCParams(Map<String, String> param) {
        if (param == null)
            return new ArrayList<NameValuePair>();

        List<NameValuePair> r = new ArrayList<NameValuePair>(param.size());
        for (Entry<String, String> entry : param.entrySet()) {
            r.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return r;
    }

    public static List<NameValuePair> getParamsFrom(String... params) {
        if (params == null)
            return new ArrayList<NameValuePair>();

        if (params.length % 2 == 1)
            throw new IllegalStateException("you need to have a paired number of parameters");

        List<NameValuePair> r = new ArrayList<NameValuePair>(params.length / 2);
        for (int i = 0; i < params.length; i++) {
            r.add(new BasicNameValuePair(params[i], params[++i]));
        }
        return r;
    }
}
