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

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The Class OAuthLoginWebHack.
 */
public abstract class OAuthLoginWebHack {

    private WebView view;

    public void init(Context context) {
        view = new WebView(context);
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setSaveFormData(false);
        view.getSettings().setSavePassword(false);
    }

    public void login(final String username, final String password) {
        if (view == null) {
            throw new IllegalStateException("have you called init?");
        }
        view.setWebViewClient(new WebViewClient() {
            private boolean loggedIn = false;

            @Override
            public void onPageFinished(WebView view, String url) {
                if (loggedIn) {
                    onCallback(url);
                }
                view.loadUrl("javascript:document.getElementById(\"" + getLoginDOMID()
                        + "\").value = '" + username + "'");
                view.loadUrl("javascript:document.getElementById(\"" + getPasswordDOMID()
                        + "\").value = '" + password + "'");
                onPreSubmit(view);
                view.loadUrl("javascript:document.body.getElementsByTagName(\"form\")["
                        + getFormIndex() + "].submit()");
                loggedIn  = true;
                super.onPageFinished(view, url);
            }
            
        });
        view.loadUrl(getOAuthLoginUrl());
    }

    protected abstract void onCallback(String url);

    /**
     * Gets the o auth login url.
     * 
     * @return the o auth login url
     */
    protected abstract String getOAuthLoginUrl();

    /**
     * Gets the login domid.
     * 
     * @return the login domid
     */
    protected abstract String getLoginDOMID();

    /**
     * Gets the password domid.
     * 
     * @return the password domid
     */
    protected abstract String getPasswordDOMID();

    /**
     * If we have more then one form in the HTML document, return the index of
     * the form that submits the login information.
     * 
     * @return the form index
     */
    protected int getFormIndex() {
        return 0;
    }

    /**
     * Gives you a chance to hook into the WebView beform the form is submitted
     * (i.e. maybe there is a checkbox which needs to be checked)
     * 
     * @param webkit the webkit
     */
    protected void onPreSubmit(WebView webkit) {
    }
    
    public void destroy() {
        view.destroy();
    }
}
