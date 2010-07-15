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

package novoda.rest.interceptors;

import android.content.SharedPreferences;

/**
 * The Interface OAuthPreferences.
 */
public interface OAuthPreferences {
    
    /**
     * Gets the shared preference.
     * 
     * @return the shared preference
     */
    SharedPreferences getSharedPreference();

    /**
     * Gets the token key.
     * 
     * @return the token key
     */
    String getTokenKey();

    /**
     * Gets the token secret.
     * 
     * @return the token secret
     */
    String getTokenSecret();
    
    /**
     * Gets the consumer key.
     * 
     * @return the consumer key
     */
    String getConsumerKey();
    
    /**
     * Gets the consumer secret.
     * 
     * @return the consumer secret
     */
    String getConsumerSecret();
}
