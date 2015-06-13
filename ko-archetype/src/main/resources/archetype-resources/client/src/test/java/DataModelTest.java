package ${package};

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class DataModelTest {
    @Test public void testUIModelWithoutUI() {
        Data model = new Data();
#if ($example.equals("true"))
        model.setMessage("Hello World!");
        
        java.util.List<String> arr = model.getWords();
        assertEquals(arr.size(), 6, "Six words always");
        assertEquals("Hello", arr.get(0), "Hello is the first word");
        assertEquals("World!", arr.get(1), "World is the second word");
#end        
    }
}
