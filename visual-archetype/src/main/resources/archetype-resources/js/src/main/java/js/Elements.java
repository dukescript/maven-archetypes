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

    /** Shows confirmation dialog to the user.
     *
     * @param msg the message
     * @param callback called back when the use accepts (can be null)
     */
    @JavaScriptBody(
        args = { "msg", "callback" },
        javacall = true,
        body = "if (confirm(msg)) {\n"
             + "  callback.@java.lang.Runnable::run()();\n"
             + "}\n"
    )
    public static native void confirmByUser(String msg, Runnable callback);

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

    @JavaScriptBody(args = { "id", "listener" }, javacall = true, body = "\n" +
"    var elem = document.getElementById(id);\n" +
"    elem.addEventListener('click', function(event) {\n" +
"        var x = event.pageX - elem.offsetLeft;\n" +
"        var y = event.pageY - elem.offsetTop;\n" +
"        x = x / elem.clientWidth * elem.width;\n" +
"        y = y / elem.clientHeight * elem.height;\n" +
"        @${package}.js.Elements::dispatch(Ljava/lang/Object;II)(listener, x, y);\n" +
"    }, false);\n" +
"\n"
    )
    public static native void onClick(String id, Listener listener);

    static void dispatch(Object obj, int x, int y) {
        Listener listener = (Listener) obj;
        listener.onEvent(new Event(x, y));
    }

    public interface Listener {
        public void onEvent(Event ev);
    }

    public static final class Event {
        private final int x;
        private final int y;

        Event(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
