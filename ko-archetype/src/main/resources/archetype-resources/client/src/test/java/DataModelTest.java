package ${package};

import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for behavior of your application in real systems. The {@link BrowserRunner}
 * selects all possible presenters from your <code>pom.xml</code> and
 * runs the tests inside of them.
 *
 * See your <code>pom.xml</code> dependency section for details.
 */
@RunWith(BrowserRunner.class)
public class DataModelTest {
    @Test public void testUIModelWithoutUI() {
        Data model = new Data();
#if ($example.equals("true"))
        model.setMessage("Hello World!");
        
        java.util.List<String> arr = model.getWords();
        assertEquals("Six words always", arr.size(), 6);
        assertEquals("Hello is the first word", "Hello", arr.get(0));
        assertEquals("World is the second word", "World!", arr.get(1));
#end        
    }
}
