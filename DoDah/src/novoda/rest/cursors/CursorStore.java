
package novoda.rest.cursors;

import java.io.InputStream;
import java.util.Map;

import com.google.common.collect.MapMaker;

import android.database.Cursor;
import android.net.Uri;

public abstract class CursorStore<T extends Cursor> {

    private Map<Uri, T> cache;

    public CursorStore() {
        this.cache = new MapMaker().weakValues().makeMap();
    }

    public CursorStore generate(InputStream in, Mapper mapper, Features features) {
        
        getCursorFactory().parse(in, mapper, features);
        
//        for (each cursor) do
//            insert into cache;
//        end
        
        return null;
    }

    protected abstract T getCursor(Uri uri);

    protected abstract CursorFactory getCursorFactory();
}
