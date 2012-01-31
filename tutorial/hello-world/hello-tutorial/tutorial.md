ChameRIA tutorial
=================

This tutorial allows you to quickly start with ChameRIA. It shows:

 * how to create Java services (OSGi)
 * how they can be published inside the UI
 * how the UI can consume those services
 * how to build a distribution

First of all, you should have the following structure:

 * hello-service : contains the _Hello_ service, and implementation and the endpoint
 * hello-distribution : contains the _UI_ and the way to build the ChameRIA distribution

OSGi services
-------------

ChameRIA relies on [OSGi](http://en.wikipedia.org/wiki/Osgi) for its Java part. OSGi is a module layer for Java, but
also provide a pretty powerful service-orientation. So applications developped with ChameRIA are modular, and can be
 dynamic. ChameRIA is using [Apache Felix](http://felix.apache.org)].

However, as OSGi API is kind of tricky, ChameRIA is relying on [iPOJO](http://ipojo.org), so simplify all OSGi related
tasks.

In this tutorial, we will just define, implement and publish a _Hello_ service. It will be packaged into an OSGi _bundle_,
built using [Maven](http://maven.apache.org).

In _hello-service_, you should find:

* a _pom_ file, instructing Maven to build the bundle.
* _src/main/java/de/akquinet/chameria/tutorial/hello/Hello.java_ our _Hello_ service
* _src/main/java/de/akquinet/chameria/tutorial/hello/impl/HelloProvider.java_ the _Hello_ service implementation
* _src/main/java/de/akquinet/chameria/tutorial/hello/impl/HelloEndpoint.java_ the component responsible to publish the
service on HTTP (using a Servlet).

The Hello service is really simple:

    /**
     * A pretty simple service
     */
    public interface Hello {

        public String hello(String name);

    }

The provider is using iPOJO to be published as an OSGi service. So we don't have to worry about the OSGi API.

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

Finally, a Servlet is used to expose the service to the web view. We're using the HTTP Server contained in ChameRIA and
accessible using the OSGi _HttpService_.

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

The _start()_ and _stop()_ methods register and unregister the servlet. The _HttpService_ is injected using the iPOJO
_@Requires_ annotation as well as our _Hello_ service. Finally, the _doGet()_ method just invokes our service and writes
the response. Here, for simplicity, we're using plain servlet, but you can use JSON-RPC or OW2 Chameleon Rose.

To package our bundle, just use Maven:

    mvn clean install

The ChameRIA view and distribution
----------------------------------

The second project (_hello-distribution_), is a Maven project assembling all the components together. The Maven build
downloads all the required components, and places them in the right folders. It builds the distribution for Linux, MacOSX,
 and Windows.

In _src/main/resources/web_, you will find the UI code. ChameRIA promotes a full web approach for the UI. So, HTML,
Javascript and CSS are used to implement the UI. The communication with the server is done using Ajax (or WebSockets,
  not included in this tutorial). For example, in this tutorial, the Hello service is invoked using:

    // Intercept button click to call the service.
      $("#call").click(function() {
         $.get("http://localhost:8080/hello?name=" + $("#name").val())
              .success(function(result) {
                  // Display a success message.
                  $("#result").empty();
                 var message = $("<div></div>").html(result).addClass("alert-message").addClass("success");
                 $("#result").append(message);
             });
      });


The _index.html_ and the _js/script.js_ just implements this [UI](tutorial.png).

In _src/main/resources/deploy/chameria.webview-example.cfg_, you cna configure the ChameRIA behavior (size, settings...).

To build the distribution, just run _mvn clean install_. Then, you can launch the application using:

    cd target/linux or target\win or target/mac
    sh launch-linux.sh or launch-win.bat or sh launch-mac.sh

That's it !

This tutorial has simply shown how to create a simple ChameRIA application, especially how OSGi services can be used in
the front-end.

