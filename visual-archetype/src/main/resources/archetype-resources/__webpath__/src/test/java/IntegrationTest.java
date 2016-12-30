package ${package};

import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for behavior of your application in real systems. The {@link BrowserRunner}
 * selects all possible presenters from your <code>pom.xml</code> and
 * runs the tests inside of them. By default there are two:
 * <ul>
 *   <li>JavaFX WebView presenter - verifies behavior in HotSpot JVM</li>
 *   <li>Bck2Brwsr presenter - runs the test in a pluginless browser</li>
 * </ul>
 *
 * See your <code>pom.xml</code> dependency section for details.
 */
@RunWith(BrowserRunner.class)
@HTMLContent(
    "<h3>Test in JavaFX WebView and pluginless Browser</h3>\n" +
    "<canvas id='canvas'></canvas>\n" +
    "<canvas id='pieChart'></canvas>\n" +
    "<canvas id='lineChart'></canvas>\n" +
    "<div id='map'></div>\n" +
    "\n"
)
public class IntegrationTest {
    @Test public void testDrawingOfACanvas() {
        Data model = new Data();
        model.applyBindings();
        model.drawCanvas();
    }

    @Test public void testLineChart() {
        Data model = new Data();
        model.applyBindings();
        model.lineChart();
        model.lineChart();
        model.lineChart();
    }

    @Test public void testMap() {
        Data model = new Data();
        model.applyBindings();
        model.map();
    }

    @Test public void testPieChart() {
        Data model = new Data();
        model.applyBindings();
        model.pieChart();
        model.pieChart();
        model.pieChart();
    }
}
