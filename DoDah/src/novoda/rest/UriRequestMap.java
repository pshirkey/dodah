
package novoda.rest;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.ContentValues;
import android.database.AbstractCursor;
import android.net.Uri;

public interface UriRequestMap {

    static final public int QUERY = 0;

    static final public int INSERT = 1;

    static final public int UPDATE = 2;

    static final public int DELETE = 3;

    /**
     * @param uri
     * @param type
     * @param params
     * @return
     */
    public abstract HttpUriRequest getRequest(Uri uri, int type, List<NameValuePair> params);

    public abstract ResponseHandler<? extends AbstractCursor> getQueryHandler(Uri uri);

    public abstract ResponseHandler<? extends Uri> getInsertHandler(Uri uri);

    public abstract ResponseHandler<? extends Integer> getUpdateHandler(Uri uri);

    public abstract ResponseHandler<? extends Integer> getDeleteHandler(Uri uri);

    public abstract List<NameValuePair> getQueryParams(Uri uri, String[] projection,
            String selection, String[] selectionArg, String sortOrder);

    public abstract List<NameValuePair> getUpdateParams(Uri uri, ContentValues values,
            String selection, String[] selectionArg);

    public abstract List<NameValuePair> getInsertParams(Uri uri, ContentValues values);

    public abstract List<NameValuePair> getDeleteParams(Uri uri, String selection,
            String[] selectionArg);
}
