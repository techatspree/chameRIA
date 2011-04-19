package de.akquinet.chameria.activation.bundle;

import java.util.Arrays;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.akquinet.chameria.services.ActivationService;
import de.akquinet.chameria.services.ActivationUtils;


public class ActivationServiceImpl implements ActivationService,
        BundleActivator {

    private ServiceRegistration reg;

    public int activation_count = 0;
    public String[] last_args = null;
    public String open = null;

    public void activation(String[] args) {
        System.out.println("Activation " + Arrays.asList(args));
        last_args = args;
        activation_count ++;
        open = ActivationUtils.getOpenArgument(args);

    }

    public void deactivation() {
        System.out.println("Deactivation");

    }

    public void start(BundleContext arg0) throws Exception {
        reg = arg0.registerService(ActivationService.class.getName(), this, null);
    }

    public void stop(BundleContext arg0) throws Exception {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }

}
