package ${package};

import ${package}.js.Dialogs;
import ${package}.shared.Contact;
import ${package}.shared.Phone;
import ${package}.shared.PhoneType;
import java.util.List;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/** Generates UI class that provides the application logic model for
 * the HTML page.
 */
@Model(className = "UI", targetId="", properties = {
    @Property(name = "url", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "alert", type = boolean.class),
    @Property(name = "contacts", type = Contact.class, array = true),
    @Property(name = "selected", type = Contact.class),
    @Property(name = "edited", type = Contact.class)
})
final class UIModel {

    //
    // REST API callbacks
    //

    @OnReceive(url = "{url}", onError = "cannotConnect")
    static void loadContacts(UI ui, List<Contact> arr) {
        ui.getContacts().clear();
        ui.getContacts().addAll(arr);
        ui.setMessage("Loaded " + arr.size() + " contact(s).");
    }

    @OnReceive(method = "POST", url = "{url}", data = Contact.class, onError = "cannotConnect")
    static void addContact(UI ui, List<Contact> updatedOnes, Contact newOne) {
        ui.getContacts().clear();
        ui.getContacts().addAll(updatedOnes);
        ui.setMessage("Created " + newOne.getLastName() + ". " + updatedOnes.size() + " contact(s) now.");
        ui.setSelected(null);
        ui.setEdited(null);
    }
    @OnReceive(method = "PUT", url = "{url}/{id}", data = Contact.class, onError = "cannotConnect")
    static void updateContact(UI ui, List<Contact> updatedOnes, Contact original) {
        ui.getContacts().clear();
        ui.getContacts().addAll(updatedOnes);
        ui.setMessage("Updated " + original.getLastName() + ". " + updatedOnes.size() + " contact(s) now.");
        ui.setSelected(null);
        ui.setEdited(null);
    }

    @OnReceive(method = "DELETE", url = "{url}/{id}", onError = "cannotConnect")
    static void deleteContact(UI ui, List<Contact> remainingOnes, Contact original) {
        ui.getContacts().clear();
        ui.getContacts().addAll(remainingOnes);
        ui.setMessage("Deleted " + original.getLastName() + ". " + remainingOnes.size() + " contact(s) now.");
    }

    static void cannotConnect(UI data, Exception ex) {
        data.setMessage("Cannot connect " + ex.getMessage() + ". Should not you start the server project first?");
    }

    //
    // UI callback bindings
    //

    @ModelOperation @Function static void connect(UI data) {
        final String u = data.getUrl();
        if (u.endsWith("/")) {
            data.setUrl(u.substring(0, u.length() - 1));
        }
        data.loadContacts(data.getUrl());
    }

    @Function static void addNew(UI ui) {
        ui.setSelected(null);
        final Contact c = new Contact();
        c.getPhones().add(new Phone("+49 89 0000 0000", PhoneType.HOME));
        ui.setEdited(c);
    }

    @Function static void edit(UI ui, Contact data) {
        ui.setSelected(data);
        ui.setEdited(data.clone());
    }

    @Function static void delete(UI ui, Contact data) {
        ui.deleteContact(ui.getUrl(), data.getId(), data);
    }

    @Function static void cancel(UI ui) {
        ui.setEdited(null);
        ui.setSelected(null);
    }

    @Function static void commit(UI ui) {
        final Contact e = ui.getEdited();
        if (e == null) {
            return;
        }
        String invalid = null;
        if (e.getValidate() != null) {
            invalid = e.getValidate();
        } else if (e.getAddress().getValidate() != null) {
            invalid = e.getAddress().getValidate();
        } else for (Phone p : e.getPhones()) {
            if (p.getValidate() != null) {
                invalid = p.getValidate();
                break;
            }
        }
        if (invalid != null && !Dialogs.confirm("Not all data are valid (" +
                invalid + "). Do you want to proceed?", null
        )) {
            return;
        }

        final Contact s = ui.getSelected();
        if (s != null) {
            ui.updateContact(ui.getUrl(), s.getId(), e, e);
        } else {
            ui.addContact(ui.getUrl(), e, e);
        }
    }

    @Function static void addPhoneEdited(UI ui) {
        final List<Phone> phones = ui.getEdited().getPhones();
        PhoneType t = PhoneType.values()[phones.size() % PhoneType.values().length];
        phones.add(new Phone("", t));
    }

    @Function static void removePhoneEdited(UI ui, Phone data) {
        ui.getEdited().getPhones().remove(data);
    }
    
    @Function static void hideAlert(UI ui) {
        ui.setAlert(false);
    }
    
    @OnPropertyChange(value = "message") static void messageChanged(UI ui) {
        ui.setAlert(true);
    }    

    /**
     * Called when the page is ready.
     */
    static void onPageLoad() {
        UI uiModel = new UI();
        final String baseUrl = "http://localhost:8080/contacts/";
        uiModel.setUrl(baseUrl);
        uiModel.setEdited(null);
        uiModel.setSelected(null);
        uiModel.applyBindings();
        uiModel.connect();
    }

}
