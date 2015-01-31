package ${package}.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Enumeration;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/** Starts REST server based on Jersey.
 */
final class Main implements ContainerResponseFilter {
    public static void main(String... args) throws Exception {
        ResourceConfig rc = new ResourceConfig(
            ContactsResource.class, Main.class
        );
        URI u = new URI("http://0.0.0.0:8080/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(u, rc);
        System.err.println("Server running on following IP addresses:");
        dumpIPs();
        System.err.println("Press Enter to shutdown the server");
        System.in.read();
        server.stop();
    }

    @Override
    public void filter(
        ContainerRequestContext requestContext, 
        ContainerResponseContext r
    ) throws IOException {
        r.getHeaders().add("Access-Control-Allow-Origin", "*");
        r.getHeaders().add("Access-Control-Allow-Credentials", "true");
        r.getHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        r.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
    }    
    
    private static void dumpIPs() throws Exception {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface n = en.nextElement();
            if (n.isUp()) {
                for (InterfaceAddress i : n.getInterfaceAddresses()) {
                    if (i.getAddress() instanceof Inet4Address) {
                        System.err.println(n.getName() + ": " + i.getAddress());
                    }
                }
            }
        }
    }
}
