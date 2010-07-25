
package novoda.rest.services;

import novoda.rest.UriRequestMap;
import novoda.rest.utils.AndroidHttpClient;
import novoda.rest.utils.DatabaseUtils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.AbstractCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public abstract class RESTCallService extends IntentService implements UriRequestMap {

    public RESTCallService() {
        super("REST");
    }
    
    public RESTCallService(String name) {
        super(name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
    
    private static final String TAG = RESTCallService.class.getSimpleName();

    public static final String BUNDLE_SORT_ORDER = "sortOrder";

    public static final String BUNDLE_SELECTION_ARG = "selectionArg";

    public static final String BUNDLE_SELECTION = "selection";

    public static final String BUNDLE_PROJECTION = "projection";


    public static final String ACTION_QUERY = "novoda.rest.action.ACTION_QUERY";

    public static final String ACTION_UPDATE = "novoda.rest.action.ACTION_UPDATE";

    public static final String ACTION_INSERT = "novoda.rest.action.ACTION_INSERT";

    public static final String ACTION_DELETE = "novoda.rest.action.ACTION_DELETE";

    protected static AndroidHttpClient httpClient;

    static {
        setupHttpClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        Uri uri = intent.getData();
        String action = intent.getAction();

        Log.i(TAG, "ur:" + uri.toString());
        
        if (action.equals(ACTION_QUERY)) {
            final String[] projection = bundle.getStringArray(BUNDLE_PROJECTION);
            final String selection = bundle.getString(BUNDLE_SELECTION);
            final String[] selectionArg = bundle.getStringArray(BUNDLE_SELECTION_ARG);
            final String sortOrder = bundle.getString(BUNDLE_SORT_ORDER);
            try {
                AbstractCursor cursor = httpClient.execute(getRequest(uri, UriRequestMap.QUERY,
                        getQueryParams(uri, projection, selection, selectionArg, sortOrder)),
                        getQueryHandler(uri));
                ContentValues values = new ContentValues(cursor.getColumnCount());
                while (cursor.moveToNext()){
                    DatabaseUtils.cursorRowToContentValues(cursor, values);
                    Log.i(TAG, "urid: " + uri.toString());
                    getContentResolver().insert(uri, values);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void setupHttpClient() {
        httpClient = AndroidHttpClient.newInstance("Android/RESTProvider");
    }
}
