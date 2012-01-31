package de.akquinet.chameria.tutorial.hello.impl;

import de.akquinet.chameria.tutorial.hello.Hello;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * A simple servlet invoking the Hello service, and returning the response.
 *
 * This components is requiring the Hello Service as well as the HttpService (provided by ChameRIA), to
 * registers the servlet.
 */
@Component(immediate = true)
@Instantiate
public class HelloEndpoint extends HttpServlet {
    
    
    @Requires
    private HttpService server;

    @Requires
    private Hello hello;
    
    @Validate
    public void start() throws ServletException, NamespaceException {
        server.registerServlet("/hello", this, null, null);
    }
    
    @Invalidate
    public void stop() {
        if (server != null) {
            server.unregister("/hello");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String response =  hello.hello(name);
        Writer writer = resp.getWriter();
        writer.write(response);
        writer.close();
    }
}
