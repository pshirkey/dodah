
package novoda.rest.providers;

import novoda.rest.cursors.EmptyCursor;
import novoda.rest.database.ModularSQLiteOpenHelper;
import novoda.rest.services.RESTCallService;
import novoda.rest.utils.DatabaseUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public abstract class ModularProvider extends ContentProvider {

    private static final String TAG = ModularProvider.class.getSimpleName();

    protected SQLiteOpenHelper dbHelper;

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long ret = -1;
        try {
            ret = dbHelper.getWritableDatabase()
                    .insertOrThrow(uri.getLastPathSegment(), "", values);
        } catch (SQLiteException e) {
            if (e.getMessage().contains("no such table")) {
                Log.v(TAG, "creating table: " + uri.getLastPathSegment());
                dbHelper.getWritableDatabase().execSQL(
                        DatabaseUtils.contentValuestoTableCreate(values, getTableName(uri)));
                return insert(uri, values);
            }
        }
        if (ret != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
            return uri.buildUpon().appendEncodedPath("" + ret).build();
        }
        throw new SQLiteException("Can not insert");
    }

    @Override
    public boolean onCreate() {
        dbHelper = getSQLiteOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setTables(uri.getLastPathSegment());

        // Make the query.
        Cursor c = null;
        try {
            c = qBuilder.query(dbHelper.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, sortOrder);
        } catch (SQLiteException e) {
            Log.i(TAG, "no table created yet");
            if (e.getMessage().contains("no such table")) {
                c = new EmptyCursor();
            }
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);

        Intent intent = new Intent(getContext(), getService().getClass());
        intent.setAction(RESTCallService.ACTION_QUERY);
        intent.setData(uri);
        Log.i(TAG, intent.toString());
        intent.putExtra(RESTCallService.BUNDLE_PROJECTION, projection);
        intent.putExtra(RESTCallService.BUNDLE_SELECTION, selection);
        intent.putExtra(RESTCallService.BUNDLE_SELECTION_ARG, selectionArgs);
        intent.putExtra(RESTCallService.BUNDLE_SORT_ORDER, sortOrder);
        
        getContext().startService(intent);
        
        return c;
    }

   protected abstract RESTCallService getService();

    protected SQLiteOpenHelper getSQLiteOpenHelper(Context context) {
        return new ModularSQLiteOpenHelper(context);
    }

    public String getTableName(Uri uri) {
        return uri.getLastPathSegment();
    }
    

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        return 0;
    }
}
