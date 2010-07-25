
package novoda.rest.cursors;

import java.io.InputStream;
import java.util.List;

import android.database.Cursor;

public abstract class CursorFactory {
    Cursor getRootCursor() {
        return null;
    }

    List<Cursor> getChildCursor() {
        return null;
    }

    public abstract void parse(InputStream in, Mapper mapper, Features features);
}
