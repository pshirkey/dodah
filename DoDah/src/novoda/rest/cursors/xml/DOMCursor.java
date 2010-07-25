
package novoda.rest.cursors.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import novoda.rest.cursors.json.JsonCursor;
import novoda.rest.handlers.QueryHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.database.AbstractCursor;
import android.util.Log;

public class DOMCursor extends AbstractCursor implements QueryHandler<DOMCursor> {

    private static final String TAG = DOMCursor.class.getSimpleName();

    private String root;

    NodeList array;

    Node current;

    private String[] columnNames;

    public DOMCursor(String root) {
        this.root = root;
    }

    public DOMCursor() {
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public int getCount() {
        return array.getLength();
    }

    @Override
    public double getDouble(int arg0) {
        return 0;
    }

    @Override
    public float getFloat(int arg0) {
        return 0;
    }

    @Override
    public int getInt(int arg0) {
        return 0;
    }

    @Override
    public long getLong(int arg0) {
        return 0;
    }

    @Override
    public short getShort(int arg0) {
        return 0;
    }

    @Override
    public String getString(int arg0) {
        return null;
    }

    @Override
    public boolean isNull(int arg0) {
        return false;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        current = array.item(newPosition);
        return super.onMove(oldPosition, newPosition);
    }

    public DOMCursor handleResponse(HttpResponse response) throws ClientProtocolException,
            IOException {
        return init(response);
    }

    private DOMCursor init(HttpResponse response) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        factory.setNamespaceAware(false);
        factory.setValidating(false);

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();

            Document dom = builder.parse(response.getEntity().getContent());

            System.out.println(dom.getDoctype());

            Element rootElement = dom.getDocumentElement();
            array = rootElement.getElementsByTagName(root);

            System.out.println(array.getLength());

            NodeList names = array.item(0).getChildNodes();
            columnNames = new String[names.getLength()];
            for (int i = 0; i < names.getLength(); i++) {
                columnNames[i] = names.item(i).getNodeName();
            }

        } catch (ParserConfigurationException e) {
            Log.e(TAG, "an error occured in init", e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "an error occured in init", e);
        } catch (SAXException e) {
            Log.e(TAG, "an error occured in init", e);
        } catch (IOException e) {
            Log.e(TAG, "an error occured in init", e);
        }
        return this;
    }

    public static class Builder {
        private String root = null;

        private String[] columnNames;

        private boolean withId;

        private String idNode = null;

        private String[] foreignKeys;

        public Builder withRootField(String root) {
            this.root = root;
            return this;
        }

        public Builder withIDField(String field) {
            idNode = field;
            return this;
        }

        public Builder addIDField(boolean add) {
            withId = add;
            return this;
        }

        public Builder addOneToMany(JsonCursor.Builder... fields) {
            throw new UnsupportedOperationException("not implemented");
        }

        public Builder removeFields(String... fields) {
            throw new UnsupportedOperationException("not implemented");
        }

        public Builder withArraysAsForeignKeys(boolean auto) {
            throw new UnsupportedOperationException("use addOneToMany instead");
        }

        public DOMCursor create() {
            DOMCursor cursor = new DOMCursor();
            cursor.root = root;
            cursor.columnNames = columnNames;
            return cursor;
        }
    }
}
