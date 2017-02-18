package ${package};

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import ${package}.js.PlatformServices;

public class AndroidMain extends Activity {
    private static AndroidServices services;

    public AndroidMain() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(AndroidMain.class.getPackage().getName(), 0);
        services = new AndroidServices(prefs);

        // delegate to original activity
        startActivity(new Intent(getApplicationContext(), com.dukescript.presenters.Android.class)));
        
        finish();
    }

    public static void main(String... args) throws Exception {
        DataModel.onPageLoad(services);
    }

    private static final class AndroidServices extends PlatformServices {
        private final SharedPreferences prefs;

        AndroidServices(SharedPreferences prefs) {
            this.prefs = prefs;
        }
#if ($example.equals("true"))
        @Override
        public String getPreferences(String key) {
            return prefs.getString(key, null);
        }

        @Override
        public void setPreferences(String key, String value) {
            prefs.edit().putString(key, value).apply();
        }
#end
    }
}
