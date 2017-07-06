package ee.ioc.phon.android.speechtrigger;

public final class Log {

    private Log() {
    }

    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static final String LOG_TAG = "speechtrigger";

    public static void i(String msg) {
        if (DEBUG) android.util.Log.i(LOG_TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG) android.util.Log.e(LOG_TAG, msg);
    }
}
