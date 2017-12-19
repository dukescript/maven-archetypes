package ${package};

import net.java.html.boot.BrowserBuilder;
#if ($example.equals("true"))
import ${package}.js.PlatformServices;
import apple.foundation.NSUserDefaults;
#end

public final class MoeMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadFinished(MoeMain::onPageLoad).
            showAndWait();
        System.exit(0);
    }

#if ($example.equals("true"))
    public static void onPageLoad() {
        DataModel.onPageLoad(new MoeServices());
    }

    private static final class MoeServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return NSUserDefaults.standardUserDefaults().stringForKey(key);
        }

        @Override
        public void setPreferences(String key, String value) {
            NSUserDefaults.standardUserDefaults().setValueForKey(key, value);
        }
    }
#else
    public static void onPageLoad() {
        DataModel.onPageLoad();
    }
#end
}


