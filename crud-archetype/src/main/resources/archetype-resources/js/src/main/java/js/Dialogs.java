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
}
