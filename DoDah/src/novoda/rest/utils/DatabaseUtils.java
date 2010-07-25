package novoda.rest.utils;

import java.util.Map.Entry;

import android.content.ContentValues;

public class DatabaseUtils extends android.database.DatabaseUtils {

	public static String contentValuestoTableCreate(ContentValues values,
			String table) {
		StringBuffer buf = new StringBuffer("CREATE TABLE ").append(table)
				.append(" (");
		for (Entry<String, Object> entry : values.valueSet()) {
			buf.append(entry.getKey()).append(" TEXT").append(", ");
		}
		buf.delete(buf.length() - 2, buf.length());
		buf.append(");");
		return buf.toString();
	}
}
