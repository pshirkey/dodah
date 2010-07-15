package novoda.rest.cursors.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import android.database.AbstractCursor;

public class CursorDeserializer extends JsonDeserializer<AbstractCursor> {

    @Override
    public AbstractCursor deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        return null;
    }
}
