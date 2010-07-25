
package novoda.rest;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;

import novoda.rest.auth.OAuthOnSharedPreferenceChangeListener;
import novoda.rest.cache.UriCache;
import novoda.rest.cursors.ErrorCursor;
import novoda.rest.cursors.One2ManyMapping;
import novoda.rest.interceptors.OAuthInterceptor;
import novoda.rest.interceptors.OAuthPreferences;
import novoda.rest.logging.DebugLogConfig;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpRequestInterceptorList;
import org.apache.http.protocol.HttpResponseInterceptorList;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.SSLCertificateSocketFactory;
import android.net.Uri;
import android.util.Log;

/**
 * Note: some code is taken from droidfu by Mathias Kaeppler:
 * http://github.com/kaeppler/droid-fu/
 */
public abstract class RESTProvider extends ContentProvider {

    protected static final String TAG = RESTProvider.class.getSimpleName();

    private static final int MAX_CONNECTIONS = 6;

    private static final int CONNECTION_TIMEOUT = 10 * 1000;

    protected static final String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

    protected static final String HTTP_USER_AGENT = "Android/RESTProvider";

    protected static AbstractHttpClient httpClient;

    static {
        setupHttpClient();
    }
    
    @Override
    public boolean onCreate() {
        if (DEBUG) {
            try {
                new DebugLogConfig(getContext().getAssets().open("httpclient.logging"));
            } catch (IOException e) {
                Log.w(TAG, "To enable http logging, ensure you have a "
                        + "file called httpclient.logging in your assets folder");
            }
        }
        if (this instanceof OAuthPreferences) {
            Log.i(TAG, "setting oauth pref");
            setOAuthPreferences((OAuthPreferences)this);
        }
        return true;
    }

    public void setOAuthPreferences(OAuthPreferences pref) {
        
        interceptor = new OAuthInterceptor(pref.getConsumerKey(), pref
                .getConsumerSecret());
        
        httpClient.addRequestInterceptor(interceptor);
        
        SharedPreferences p = pref.getSharedPreference();
        
        if (p.contains(pref.getTokenKey())) {
            interceptor.setTokenWithSecret(p.getString(pref.getTokenKey(), ""), p.getString(pref
                    .getTokenSecret(), ""));
        }
        
        pref.getSharedPreference().registerOnSharedPreferenceChangeListener(
                new OAuthOnSharedPreferenceChangeListener(pref.getTokenKey(),
                        pref.getTokenSecret(), interceptor));
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        try {
            return getDeleteHandler(uri).handleResponse(
                    httpClient
                            .execute((HttpUriRequest)deleteRequest(uri, selection, selectionArgs)));
        } catch (ClientProtocolException e) {
            Log.e(TAG, "an error occured in delete", e);
            return -1;
        } catch (IOException e) {
            Log.e(TAG, "an error occured in delete", e);
            return -1;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            return getInsertHandler(uri).handleResponse(
                    httpClient.execute((HttpUriRequest)insertRequest(uri, values)));
        } catch (ClientProtocolException e) {
            Log.e(TAG, "an error occured in insert", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "an error occured in insert", e);
            return null;
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        // Check cache first
        if (UriCache.getInstance().canRespondTo(uri)) {
            Log.i(TAG, uri.toString() + " will be taken from cache");
            return UriCache.getInstance().get(uri);
        }

        try {
            HttpUriRequest request = queryRequest(uri, projection, selection, selectionArgs,
                    sortOrder);

            if (DEBUG)
                Log.i(TAG, "will query: " + request.getURI());

            Cursor cursor = getQueryHandler(uri).handleResponse(httpClient.execute(request));
            registerMappedCursor(cursor, uri);
            return cursor;
        } catch (ConnectException e) {
            Log.w(TAG, "an error occured in query", e);
            return ErrorCursor.getCursor(0, e.getMessage());
        } catch (ClientProtocolException e) {
            Log.w(TAG, "an error occured in query", e);
            return ErrorCursor.getCursor(0, e.getMessage());
        } catch (IOException e) {
            Log.w(TAG, "an error occured in query", e);
            return ErrorCursor.getCursor(0, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "an error occured in query", e);
            return ErrorCursor.getCursor(0,
                    "Unknown URI (not in cache or not answerable by the implementator): "
                            + uri.toString());
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            return getUpdateHandler(uri).handleResponse(
                    httpClient.execute(updateRequest(uri, values, selection, selectionArgs)));
        } catch (ClientProtocolException e) {
            Log.e(TAG, "an error occured in update", e);
            return -1;
        } catch (IOException e) {
            Log.e(TAG, "an error occured in update", e);
            return -1;
        }
    }

    /**
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public abstract HttpUriRequest deleteRequest(Uri uri, String selection, String[] selectionArgs);

    /**
     * @param uri
     * @param values
     * @return
     */
    public abstract HttpUriRequest insertRequest(Uri uri, ContentValues values);

    /**
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public abstract HttpUriRequest queryRequest(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder);

    /**
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public abstract HttpUriRequest updateRequest(Uri uri, ContentValues values, String selection,
            String[] selectionArgs);

    public abstract ResponseHandler<? extends AbstractCursor> getQueryHandler(Uri uri);

    public abstract ResponseHandler<? extends Uri> getInsertHandler(Uri uri);

    public abstract ResponseHandler<? extends Integer> getUpdateHandler(Uri uri);

    public abstract ResponseHandler<? extends Integer> getDeleteHandler(Uri uri);

    public void preProcess(HttpUriRequest request) {
    }

    public void postProcess(HttpResponse response) {
    }

    protected HttpRequestInterceptorList getHttpRequestInterceptorList() {
        return null;
    }

    protected HttpResponseInterceptorList getHttpResponseInterceptorList() {
        return null;
    }

    // Different request type
    static final public int QUERY = 0;

    static final public int INSERT = 1;

    static final public int UPDATE = 2;

    static final public int DELETE = 3;

    public static boolean DEBUG = true;

    private OAuthInterceptor interceptor;

    private static void setupHttpClient() {
        BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, CONNECTION_TIMEOUT);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(
                MAX_CONNECTIONS));
        ConnManagerParams.setMaxTotalConnections(httpParams, MAX_CONNECTIONS);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(httpParams, HTTP_USER_AGENT);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        //schemeRegistry.register(new Scheme("https", new EasySSLProtocolSocketFactory(), 443));
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
        httpClient = new DefaultHttpClient(cm, httpParams);
    }

    protected void registerMappedCursor(Cursor cursor, Uri uri) {
        if (cursor instanceof One2ManyMapping) {
            UriCache cache = UriCache.getInstance();

            String[] foreignFields = ((One2ManyMapping)cursor).getForeignFields();
            if (foreignFields == null)
                return;

            String[] ids = cursor.getExtras().getStringArray("ids");
            
            Log.i(TAG, Arrays.toString(ids));
            for (int j = 0; j < cursor.getCount(); j++) {
                String idField = ids[j];
                Uri returi = uri;

                if (uri.getLastPathSegment() != null && uri.getLastPathSegment().equals(idField))
                    returi = Uri
                            .parse(uri.toString().subSequence(0,
                                    uri.toString().length() - uri.getLastPathSegment().length())
                                    .toString());

                returi = Uri.withAppendedPath(returi, ids[j]);

                for (int i = 0; i < foreignFields.length; i++) {
                    Uri ruri = Uri.withAppendedPath(returi, foreignFields[i]);
                    Cursor n = ((One2ManyMapping)cursor).getForeignCursor(j, foreignFields[i]);
                    if (DEBUG)
                        Log.d(TAG, "putting " + ruri.toString() + " into cache.");
                    cache.put(ruri, n);
                }
            }
        }
    }
}
