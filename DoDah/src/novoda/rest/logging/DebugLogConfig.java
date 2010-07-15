/*
 *   Copyright 2010, Novoda ltd 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package novoda.rest.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import android.util.Log;

/**
 * Debug class which will pipe the output given by HTTP client to logcat. In
 * order for this to work, you need to have a httpclient.logging file in your
 * assets folder. The properties needed are the one specific for apache and none
 * others (i.e. starting with org.apache).
 * 
 * @see <a href="http://hc.apache.org/httpcomponents-client/logging.html">HttpClient common logging practices</a>
 */
public class DebugLogConfig {

    /** The active handler. */
    static DalvikLogHandler activeHandler;

    /**
     * The Class DalvikLogHandler.
     */
    protected static class DalvikLogHandler extends Handler {

        /** The Constant LOG_TAG. */
        private static final String LOG_TAG = "RESTProvider";

        /*
         * (non-Javadoc)
         * @see java.util.logging.Handler#close()
         */
        @Override
        public void close() {
            // do nothing
        }

        /*
         * (non-Javadoc)
         * @see java.util.logging.Handler#flush()
         */
        @Override
        public void flush() {
            // do nothing
        }

        /*
         * (non-Javadoc)
         * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
         */
        @Override
        public void publish(LogRecord record) {
            if (record.getLoggerName().startsWith("org.apache")) {
                Log.d(LOG_TAG, record.getMessage());
            }
        }
    }

    /**
     * Instantiates a new debug log config.
     * 
     * @param config the input stream against the logging properties of http
     *            client
     */
    public DebugLogConfig(InputStream config) {
        try {
            LogManager.getLogManager().readConfiguration(config);
            config.close();
        } catch (IOException e) {
            Log
                    .w(DebugLogConfig.class.getSimpleName(),
                            "Can't read configuration file for logging");
        }
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        activeHandler = new DalvikLogHandler();
        activeHandler.setLevel(Level.ALL);
        rootLogger.addHandler(activeHandler);
    }
}
