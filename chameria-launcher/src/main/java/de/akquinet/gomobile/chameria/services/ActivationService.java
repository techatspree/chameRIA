package de.akquinet.gomobile.chameria.services;

/**
 * Chameria Activation Service.
 * If present (implemented by an application bundle), this service is called
 * during the application activation / re-activation / shutdown. This allows the application
 * to 
 * <ul>
 * <li>Do some initialization, reconfiguration or cleanup</li>
 * <li>Get access to the launcher arguments</li>
 * </ul> 
 * 
 * For example, implementation can initialize the URL to show up on startup or to change
 * the current url. The service is called at least once on startup, but also when the application 
 * is re-activated (JNLP singleton instance).
 * 
 * Implementation can use {@link ActivationUtils} to parse arguments.
 * 
 * All activation services are called by the launcher, but the order is non-deterministic.
 */
public interface ActivationService {
    
    /**
     * Method called by the launcher every time the application is activate.
     * The given arguments are the launcher argument, and so can contain
     * the <code>-open</code> argument.
     * @param args the arguments
     */
    public void activation(String[] args);
    
    /**
     * Method called by the launcher when the application stops.
     */
    public void deactivation();


}
