package ${package}.js;

import net.java.html.js.JavaScriptBody;

/** Use {@link JavaScriptBody} annotation on methods to
 * directly interact with JavaScript. See
 * http://bits.netbeans.org/html+java/1.2/net/java/html/js/package-summary.html
 * to understand how.
 */
public final class Elements {
    private Elements() {
    }

    @JavaScriptBody(
        args = {}, body =
        "var w = window,\n" +
        "    d = document,\n" +
        "    e = d.documentElement,\n" +
        "    g = d.getElementsByTagName('body')[0],\n" +
        "    x = w.innerWidth || e.clientWidth || g.clientWidth,\n" +
        "    y = w.innerHeight|| e.clientHeight|| g.clientHeight;\n" +
        "\n" +
        "return [x, y];\n"
    )
    private static native Object[] screenSizeImpl();

    public static int[] screenSize() {
        Object[] size = screenSizeImpl();
        return new int[] {
            ((Number)size[0]).intValue(),
            ((Number)size[1]).intValue()
        };
    }

}
