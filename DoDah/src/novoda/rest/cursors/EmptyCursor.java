/**
 * 
 */

package novoda.rest.cursors;

import android.content.ContentResolver;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.CursorWindow;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

/**
 * This is an empty cursor which will return -1 for ints/longs etc... and an
 * empty string for getString. This is useful for when we don't get any cursor
 * back from a request or in a one to many relationship in order to ensure no
 * exception is thrown on the client side.
 * 
 * @author Carl-Gustaf Harroch
 */
public class EmptyCursor extends AbstractCursor {

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getColumnNames()
     */
    @Override
    public String[] getColumnNames() {
        return new String[] {
            "_id"
        };
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getCount()
     */
    @Override
    public int getCount() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getDouble(int)
     */
    @Override
    public double getDouble(int column) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getFloat(int)
     */
    @Override
    public float getFloat(int column) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getInt(int)
     */
    @Override
    public int getInt(int column) {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getLong(int)
     */
    @Override
    public long getLong(int column) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getShort(int)
     */
    @Override
    public short getShort(int column) {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#getString(int)
     */
    @Override
    public String getString(int column) {
        return "";
    }

    /*
     * (non-Javadoc)
     * @see android.database.AbstractCursor#isNull(int)
     */
    @Override
    public boolean isNull(int column) {
        return true;
    }

    @Override
    public int getColumnIndex(String columnName) {
        return 0;
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) {
        return 0;
    }

    @Override
    protected void checkPosition() {
        super.checkPosition();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        super.copyStringToBuffer(columnIndex, buffer);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

//    @Override
//    public void deactivateInternal() {
//        super.deactivateInternal();
//    }

    @Override
    public void fillWindow(int position, CursorWindow window) {
        super.fillWindow(position, window);
    }

    @Override
    protected void finalize() {
        super.finalize();
    }

    @Override
    public byte[] getBlob(int column) {
        return super.getBlob(column);
    }

    @Override
    public int getColumnCount() {
        return super.getColumnCount();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return super.getColumnName(columnIndex);
    }

//    @Override
//    protected DataSetObservable getDataSetObservable() {
//        return super.getDataSetObservable();
//    }

    @Override
    public Bundle getExtras() {
        return super.getExtras();
    }

    @Override
    protected Object getUpdatedField(int columnIndex) {
        return super.getUpdatedField(columnIndex);
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return super.getWantsAllOnMoveCalls();
    }

    @Override
    public CursorWindow getWindow() {
        return super.getWindow();
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    protected boolean isFieldUpdated(int columnIndex) {
        return super.isFieldUpdated(columnIndex);
    }

    //@Override
    protected void notifyDataSetChange() {
        // TODO Auto-generated method stub
        //super.notifyDataSetChange();
    }

    @Override
    protected void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        return super.onMove(oldPosition, newPosition);
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        super.registerContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
    }

    @Override
    public boolean requery() {
        return super.requery();
    }

    @Override
    public Bundle respond(Bundle extras) {
        return super.respond(extras);
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri notifyUri) {
        super.setNotificationUri(cr, notifyUri);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        super.unregisterContentObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
    }

}
