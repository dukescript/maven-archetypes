package ${package};

import net.java.html.boot.BrowserBuilder;

public final class MoeMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadFinished(MoeMain::onPageLoad).
            showAndWait();
        System.exit(0);
    }

    public static void onPageLoad() {
        DataModel.onPageLoad();
    }
}


