
package novoda.rest.test.mock;

import java.io.ByteArrayInputStream;

import novoda.rest.RESTProvider;
import novoda.rest.logging.DebugLogConfig;
import android.test.ProviderTestCase2;

public class RESTProviderTest<T extends RESTProvider> extends ProviderTestCase2<T> {

    private static String debug = "org.apache.http.impl.conn.level = FINEST\n"
            + "org.apache.http.impl.client.level = FINEST\n"
            + "org.apache.http.client.level = FINEST\n" + "org.apache.http.level = FINEST";

    public RESTProviderTest(Class<T> providerClass, String providerAuthority) {
        super(providerClass, providerAuthority);
        new DebugLogConfig(new ByteArrayInputStream(debug.getBytes()));
    }

}
