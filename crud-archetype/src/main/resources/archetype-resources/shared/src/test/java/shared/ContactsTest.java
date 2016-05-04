package ${package}.shared;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ContactsTest {
    
    public ContactsTest() {
    }

    @Test public void noAlphabetInPhoneNumber() {
        Phone p = new Phone("+4AFDc", PhoneType.HOME);
        String err = p.getValidate();
        assertNotNull("Need error message, number is not valid", err);
    }

    @Test public void hasToStartWithPlus() {
        Phone p = new Phone("464254", PhoneType.WORK);
        String err = p.getValidate();
        assertNotNull("Need error message, number is not international", err);
    }

    @Test public void mayContainSpaces() {
        Phone p = new Phone("+1 464 254 542 555", PhoneType.MOBILE);
        String err = p.getValidate();
        assertNull("Spaces are OK",err);
    }
    
}
