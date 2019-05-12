package ${package}.js;

public final class Dialogs {
    private Dialogs() {
    }
    
    /** Shows confirmation dialog to the user.
     * 
     * @param msg the message
     * @param callback called back when the use accepts (can be null)
     * @return true or false
     */
    @net.java.html.js.JavaScriptBody(
        args = { "msg", "callback" }, 
        javacall = true, 
        body = 
            "var ret = confirm(msg);" +
            "if (ret && callback) callback.@java.lang.Runnable::run()();" +
            "return ret;"
    )
    public static native boolean confirm(String msg, Runnable callback);

    /** Makes sure {@code confirm} function is defined. Used from unit tests.
     */
    @net.java.html.js.JavaScriptBody(args = {},
            body = "\n"
            + "if (typeof confirm === 'undefined') {\n"
            + "  confirm = function(ignore) {\n"
            + "    return true;\n"
            + "  }\n"
            + "}\n"
    )
    static native void installConfirmPolyfill();
}
