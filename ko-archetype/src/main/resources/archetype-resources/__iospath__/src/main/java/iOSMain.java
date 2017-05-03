package ${package};

import net.java.html.boot.BrowserBuilder;
#if ($example.equals("true"))
import ${package}.js.PlatformServices;
import org.robovm.apple.foundation.NSUserDefaults;
#end

public final class iOSMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(iOSMain.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.exit(0);
    }

#if ($example.equals("true"))
    public static void onPageLoad() throws Exception {
        DataModel.onPageLoad(new iOSServices());
    }

    private static final class iOSServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return NSUserDefaults.getStandardUserDefaults().getString(key);
        }

        @Override
        public void setPreferences(String key, String value) {
            NSUserDefaults.getStandardUserDefaults().put(key, value);
        }
    }
#else
    public static void onPageLoad() throws Exception {
        DataModel.onPageLoad();
    }
#end
}


