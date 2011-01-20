package de.akquinet.gomobile.chameria;

import javax.servlet.ServletException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;


@Component(immediate = true)
public class ChameriaWebRegistrator
{
    @Requires
    private HttpService http;

    @Validate
    public void start() throws ServletException, NamespaceException {
        http.registerResources("/chameria-it", "web", null);
    }

    @Invalidate
    public void stop() {
        if (http != null) {
            http.unregister("/chameria-it");
        }
    }
}
