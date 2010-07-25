
package novoda.rest.interceptors;

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

import java.io.IOException;
import java.util.Map.Entry;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

/**
 * The Class OAuthInterceptor which adds OAuth signing to the request using the
 * pattern approach.
 */
@SuppressWarnings("serial")
public class OAuthInterceptor extends CommonsHttpOAuthConsumer implements HttpRequestInterceptor {

    /** The Constant TAG. */
    private static final String TAG = OAuthInterceptor.class.getSimpleName();

    public OAuthInterceptor(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.http.HttpRequestInterceptor#process(org.apache.http.HttpRequest
     * , org.apache.http.protocol.HttpContext)
     */
    public void process(final HttpRequest request, final HttpContext context) throws HttpException,
            IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (request instanceof RequestWrapper) {
            try {
                for (final Entry<String, String> e : super.sign(
                        ((RequestWrapper)request).getOriginal()).getAllHeaders().entrySet()) {
                    request.addHeader(e.getKey(), e.getValue());
                }
            } catch (final OAuthMessageSignerException e) {
                Log.i(TAG, "failure " + e);
            } catch (final OAuthExpectationFailedException e) {
                Log.i(TAG, "failure " + e);
            } catch (final OAuthCommunicationException e) {
                Log.i(TAG, "failure " + e);
            }
        }
    }
}
