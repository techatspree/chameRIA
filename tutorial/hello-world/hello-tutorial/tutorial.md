ChameRIA tutorial
=================

This tutorial allows you to quickly get started with ChameRIA. It shows:

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
also provides very powerful service-orientation. So applications developed with ChameRIA are modular, and can be
 dynamic. ChameRIA is using [Apache Felix](http://felix.apache.org)].

However, as the OSGi API is kind of tricky, ChameRIA relies on [iPOJO](http://ipojo.org) so as to simplify all OSGi-related
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
     * without touching the OSGi API or being bothered by the complexity of OSGi.
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

_Ignore the instance name set in the code, we will come back to this later._

Finally, a Servlet is used to expose the service to the web view. We're using the HTTP Server contained in ChameRIA and
accessible using the OSGi _HttpService_.


    /**
     * A simple servlet invoking the Hello service, and returning the response.
     *
     * This component requires the Hello Service as well as the HttpService (provided by ChameRIA) to
     * register the servlet.
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
the response. Here, for simplicity, we're using plain servlets, but you could also use JSON-RPC or OW2 Chameleon Rose.

To package our bundle, just use Maven:

    mvn clean install

The ChameRIA view and distribution
----------------------------------

The second project (_hello-distribution_), is a Maven project assembling all the components. The Maven build
downloads all the required components, and places them in the right folders. It builds the distribution for Linux, MacOSX,
 and Windows.

In _src/main/resources/web_, you will find the UI code. ChameRIA promotes a full web approach for the UI. So, HTML,
JavaScript and CSS are used to implement the UI. The communication with the server is done using Ajax (or WebSockets,
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

In _src/main/resources/deploy/chameria.webview-example.cfg_, you can configure the behavior of ChameRIA (size, settings...).

To build the distribution, just run _mvn clean install_. Then, you can launch the application using:

    cd target/linux or target\win or target/mac
    sh launch-linux.sh or launch-win.bat or sh launch-mac.sh


Accessing services using JSON-RPC
---------------------------------

Obviously creating an endpoint for every service you want to access in the UI is not what you want to do. ChameRIA comes
with OW2 Chameleon Rose, a mechanism to expose OSGi services remotely. Especially, Rose supports JSON-RPC, which is
really easy to consume in JavaScript.

In the _deploy_ folder of the distribution, look at the _rose-conf.json_ file:

    {
    	"machine" : {
    		"id" : "chameRIA",
    		"host" : "localhost",

    		"connection" : [
    				{
    				"out" : {
    					"service_filter" : "(objectClass=de.akquinet.chameria.tutorial.hello.Hello)",
    					"protocol" : [ "jsonrpc" ]
    					}
    				}
    		],

    		"component" : [
    			{
    			  "factory" : "RoSe_exporter.jabsorb",
    			  "properties" : { "jsonrpc.servlet.name" : "/JSONRPC" }
    			}
    		]
    	}
    }

This file instructs Rose to expose the Hello Service as JSON-RPC.

Using JSON-RPC makes the client code simpler:

    $(function(){
        var jsonrpc = new JSONRpcClient("/JSONRPC");

        // Intercept button click to call the service.
        $("#callJsonRpc").click(function() {

           jsonrpc.helloService1.hello(function(result, exception){

        	 //Handle the exception
        	 if (exception) {
                console.log('An error occured while trying to call helloService1.hello');
             }

             // Display a success message.
             $("#result").empty();
             var message = $("<div></div>").html(result).addClass("alert-message").addClass("success (JsonRpc)");
             $("#result").append(message);

           }, $("#nameJsonRpc").val()); //args

        });

    });

First, we initiate the JSON-RPC support, and intercept the click action on a button. We can invoke the hello service with:
_jsonrpc.helloService1.hello_, where helloService1 is the instance name (set in the provider @Instantiate annotation).

With JSON-RPC you can easily support:

* exception
* synchronous and asynchronous call
* complex objects


That's it !
-----------
This tutorial has shown how to create a simple ChameRIA application, focusing in particular on how OSGi services can be used in
the front-end.

