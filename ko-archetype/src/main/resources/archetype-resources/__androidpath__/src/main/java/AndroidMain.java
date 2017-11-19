package ${package};

import android.app.Activity;
#if ($example.equals("true"))
import android.content.SharedPreferences;
import ${package}.js.PlatformServices;
#end

public class AndroidMain extends Activity {
    private AndroidMain() {
    }

#if ($example.equals("true"))
    public static void main(android.content.Context context) throws Exception {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(AndroidMain.class.getPackage().getName(), 0);
        DataModel.onPageLoad(new AndroidServices(prefs));
    }

    private static final class AndroidServices extends PlatformServices {
        private final SharedPreferences prefs;

        AndroidServices(SharedPreferences prefs) {
            this.prefs = prefs;
        }
        @Override
        public String getPreferences(String key) {
            return prefs.getString(key, null);
        }

        @Override
        public void setPreferences(String key, String value) {
            prefs.edit().putString(key, value).apply();
        }
#else
    public static void main(android.content.Context context) throws Exception {
        DataModel.onPageLoad();
#end
    }
}
