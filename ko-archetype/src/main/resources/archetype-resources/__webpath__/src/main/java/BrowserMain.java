package ${package};

#if ($example.equals("true"))
import ${package}.js.PlatformServices;
#end

public class BrowserMain {
    private BrowserMain() {
    }

#if ($example.equals("true"))
    public static void main(String... args) throws Exception {
        Main.onPageLoad(new HTML5Services());
    }

    private static final class HTML5Services extends PlatformServices {
        // default behavior is enough for now
    }
#else
    public static void main(String... args) throws Exception {
        Main.onPageLoad();
    }
#end
}
