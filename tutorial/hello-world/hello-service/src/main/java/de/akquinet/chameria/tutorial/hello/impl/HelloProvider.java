package de.akquinet.chameria.tutorial.hello.impl;

import de.akquinet.chameria.tutorial.hello.Hello;
import org.apache.felix.ipojo.annotations.*;

/**
 * Simple implementation of the Hello service.
 *
 * This class is an iPOJO component (http://ipojo.org). So, we can simply register the OSGi service
 * without touching the OSGi API or being bothered by the OSGi complexity.
 *
 * This service will be exposed to the web view using a simple servlet.
 *
 */
@Component
@Provides
@Instantiate
public class HelloProvider implements Hello {
    
    public String hello(String name) {
        return "Hello " + name;
    }
}
