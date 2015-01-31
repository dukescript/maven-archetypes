package ${package}.server;

import ${package}.shared.Address;
import ${package}.shared.Contact;
import ${package}.shared.Phone;
import ${package}.shared.PhoneType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

@Path("/contacts/") @Singleton
public final class ContactsResource {
    private final List<Contact> contacts = new CopyOnWriteArrayList<>();
    private int counter;
    
    public ContactsResource() {
        contacts.add(new Contact(
            "000",    
            "Anton", "Epple", 
            new Address("Bergmannstrasse 66", "80339 München"), 
            new Phone("+49 89 54043186", PhoneType.WORK)
        ));
    }
    
    @GET @Produces(MediaType.APPLICATION_JSON)
    public List<Contact> allContacts() {
        return contacts;
    }
    
    @GET @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Contact getContact(@PathParam("id") String id) {
        for (Contact contact : contacts) {
            if (id.equals(contact.getId())) {
                return contact;
            }
        }
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    @POST @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Contact> addContact(Contact c) {
        validate(c);
        contacts.add(c);
        c.setId("X" + ++counter);
        return contacts;
    }
    
    @DELETE @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public List<Contact> removeContact(@PathParam("id") String id) {
        for (Contact c : contacts) {
            if (id.equals(c.getId())) {
                contacts.remove(c);
                break;
            }
        }
        return contacts;
    }
    
    @PUT @Consumes(MediaType.APPLICATION_JSON) @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public List<Contact> updateContact(@PathParam("id") String id, Contact newContact) {
        for (int i = 0; i < contacts.size(); i++) {
            Contact c = contacts.get(i);
            if (id.equals(c.getId())) {
                contacts.set(i, newContact);
                newContact.setId(id);
                return contacts;
            }
            
        }
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    private static void validate(Contact c) {
        if (c.getValidate() != null) {
            throw new NotAcceptableException(c.getValidate());
        } 
        if (c.getAddress().getValidate() != null) {
            throw new NotAcceptableException(c.getAddress().getValidate());
        } 
        for (Phone phone : c.getPhones()) {
            String err = phone.getValidate();
            if (err != null) {
                throw new javax.ws.rs.NotAcceptableException(err);
            }
        }
    }
}
