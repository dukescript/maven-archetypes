package ${package};

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import ${package}.js.Dialogs;

#if ($example.equals("true"))
/** Model annotation generates class Data with
 * one message property, boolean property and read only words property
 */
#end
@Model(className = "Data", targetId="", properties = {
#if ($example.equals("true"))
    @Property(name = "message", type = String.class),
    @Property(name = "rotating", type = boolean.class)
#end
})
final class DataModel {
#if ($example.equals("true"))
    @ComputedProperty static java.util.List<String> words(String message) {
        String[] arr = new String[6];
        String[] words = message == null ? new String[0] : message.split(" ", 6);
        for (int i = 0; i < 6; i++) {
            arr[i] = words.length > i ? words[i] : "!";
        }
        return java.util.Arrays.asList(arr);
    }

    @Function static void turnAnimationOn(Data model) {
        model.setRotating(true);
    }

    @Function static void turnAnimationOff(final Data model) {
        Dialogs.confirmByUser("Really turn off?", new Runnable() {
            @Override
            public void run() {
                model.setRotating(false);
            }
        });
    }

    @Function static void rotate5s(final Data model) {
        model.setRotating(true);
        java.util.Timer timer = new java.util.Timer("Rotates a while");
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                model.setRotating(false);
            }
        }, 5000);
    }

    @Function static void showScreenSize(Data model) {
        model.setMessage(Dialogs.screenSize());
    }
#end
    private static Data ui;
    /**
     * Called when the page is ready.
     */
    static void onPageLoad() throws Exception {
        ui = new Data();
#if ($example.equals("true"))
        ui.setMessage("Hello World from HTML and Java!");
#end        
        ui.applyBindings();
    }
}
