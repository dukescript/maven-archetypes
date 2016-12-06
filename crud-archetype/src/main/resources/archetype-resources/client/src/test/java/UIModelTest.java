package ${package};

import ${package}.shared.Contact;
import ${package}.shared.PhoneType;
import net.java.html.junit.BrowserRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for behavior of your application in isolation. Verify
 * behavior of your MVVC code in a unit test.
  */
@RunWith(BrowserRunner.class)
public class UIModelTest {
    @Test public void addNewSetsEdited() {
        UI model = new UI();
        Contact c = new Contact();
        UIModel.edit(model, c);
        assertEquals("c is now edited", model.getEdited(), c);

        assertTrue("No phone yet", model.getEdited().getPhones().isEmpty());
        UIModel.addPhoneEdited(model);
        assertEquals("One phone added", model.getEdited().getPhones().size(), 1);
        assertEquals("First is home phone", model.getEdited().getPhones().get(0).getType(), PhoneType.HOME);

        UIModel.addPhoneEdited(model);
        assertEquals("2nd phone added", model.getEdited().getPhones().size(), 2);
        assertEquals("2nd is work phone", model.getEdited().getPhones().get(1).getType(), PhoneType.WORK);
    }
}
