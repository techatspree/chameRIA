package de.akquinet.chameria.tutorial.hello.impl;

import de.akquinet.chameria.tutorial.hello.Hello;
import org.apache.felix.ipojo.annotations.*;

/**
 * Simple implementation of the Hello service.
 *
 * This class is an iPOJO component (http://ipojo.org). So, we can simply register the OSGi service
 * without touching the OSGi API or being bothered by the OSGi complexity.
 *
 * Don't be afraid by the @ServiceProperty annotations. There are just indicating that our service will
 * be exposed using JSON-RPC to the <tt>hello</tt> endpoint. To support this publication, ChameRIA is using ROSE
 * (http://wiki.chameleon.ow2.org/xwiki/bin/view/Main/JSON-RPC_Export)
 *
 *
 */
@Component(immediate = true)
@Provides
@Instantiate
public class HelloProvider implements Hello {

    @ServiceProperty(name="service.exported.configs")
    public final String[] ROSE_EXPORTER = new String[] {"json-rpc"};

    @ServiceProperty(name="endpoint.name")
    public final String ENDPOINT = "hello";
    
    @ServiceProperty(name = "service.exported.interfaces")
    public final String INTERFACE = Hello.class.getName();
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
