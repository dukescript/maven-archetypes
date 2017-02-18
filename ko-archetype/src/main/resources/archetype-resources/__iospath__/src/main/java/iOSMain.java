package ${package};

import ${package}.js.PlatformServices;
import net.java.html.boot.BrowserBuilder;
import org.robovm.apple.foundation.NSUserDefaults;

public final class iOSMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(iOSMain.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }

    public static void onPageLoad() throws Exception {
        DataModel.onPageLoad(new iOSServices());
    }

    private static final class iOSServices extends PlatformServices {
#if ($example.equals("true"))
        @Override
        public String getPreferences(String key) {
            return NSUserDefaults.getStandardUserDefaults().getString(key);
        }

        @Override
        public void setPreferences(String key, String value) {
            NSUserDefaults.getStandardUserDefaults().put(key, value);
        }
#end
    }
}


