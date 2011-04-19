package de.akquinet.chameria.webview.test;

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import de.akquinet.chameria.launcher.ChameRIA;
import de.akquinet.chameria.launcher.Launcher;
import de.akquinet.chameria.services.BrowserService;


public class BrowserServiceTest {

    @Test
    public void launchChameRIA() throws Exception {

        Runnable test = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) { }

                ChameRIA chameleon = Launcher.getChameleon();
                BundleContext context = chameleon.getFramework().getBundleContext();

                ServiceReference ref = context.getServiceReference(BrowserService.class.getName());
                Assert.assertNotNull(ref);

                BrowserService svc = (BrowserService) context.getService(ref);
                System.out.println("Change URL");
                svc.setURL("http://perdu.com");

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("Switch to fullscreen");
                svc.setFullScreen(true);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("Switch to normal");
                svc.setFullScreen(false);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("Switch to 800 x 600");
                svc.setSize(800, 600);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("No resizable");
                svc.setResizable(false);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("Resizable");
                svc.setResizable(true);
                svc.setSize(1024, 600);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }

                System.out.println("Block context menu");
                svc.setContextMenu(false);

                try {
                    Thread.sleep(5000);
                } catch (Exception e) { }

                System.out.println("Renabled context menu");
                svc.setContextMenu(true);

                try {
                    Thread.sleep(2000);
                } catch (Exception e) { }
                System.out.println("Remove menu bar");
                svc.setMenuBar(false);


            }
        };

        new Thread(test).start();

        Launcher.main(new String[] {
           "--core=target/test-configuration/core",
           "--runtime=target/test-configuration/runtime",
           "--app=src/test/resources/app",
           "--debug"
        });



    }



}
