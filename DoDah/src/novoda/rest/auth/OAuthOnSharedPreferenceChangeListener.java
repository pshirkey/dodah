
package novoda.rest.auth;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;

/**
 * As the provider is stared with the application, there is an uncertainty if
 * the consumer will be allocated with the token. We need to ensure the token is
 * taken into account in 2 cases:
 * <p>
 * 1. User is logged in and logs out via the preference activity
 * </p>
 * <p>
 * 2. User logs in via the activity
 * </p>
 * This listener should take care of the 2 instances.
 */
public class OAuthOnSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {
    private static final String TAG = OAuthOnSharedPreferenceChangeListener.class.getSimpleName();

    private String tokenKey;

    private String secretKey;

    private CommonsHttpOAuthConsumer consumer;

    public OAuthOnSharedPreferenceChangeListener(String tokenKey, String secretKey,
            CommonsHttpOAuthConsumer consumer) {
        this.tokenKey = tokenKey;
        this.secretKey = secretKey;
        this.consumer = consumer;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i("RESTProvider", key);
        if (key.equalsIgnoreCase(tokenKey)) {
            Log.i(TAG, "Changing token value");
            consumer.setTokenWithSecret(sharedPreferences.getString(tokenKey, ""),
                    sharedPreferences.getString(secretKey, ""));
        }
    }

}
