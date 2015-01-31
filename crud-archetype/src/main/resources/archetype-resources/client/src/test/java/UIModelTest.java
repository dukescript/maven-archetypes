package ${package};

import ${package}.shared.Contact;
import ${package}.shared.PhoneType;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class UIModelTest {
    @Test public void addNewSetsEdited() {
        UI model = new UI();
        Contact c = new Contact();
        UIModel.edit(model, c);
        assertEquals(model.getEdited(), c, "c is now edited");

        assertTrue(model.getEdited().getPhones().isEmpty(), "No phone yet");
        UIModel.addPhoneEdited(model);
        assertEquals(model.getEdited().getPhones().size(), 1, "One phone added");
        assertEquals(model.getEdited().getPhones().get(0).getType(), PhoneType.HOME, "First is home phone");

        UIModel.addPhoneEdited(model);
        assertEquals(model.getEdited().getPhones().size(), 2, "2nd phone added");
        assertEquals(model.getEdited().getPhones().get(1).getType(), PhoneType.WORK, "2nd is work phone");    }
}
