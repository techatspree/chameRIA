/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.chameria.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.ow2.chameleon.core.Chameleon;

import de.akquinet.chameria.services.ActivationService;

public class ChameRIA extends Chameleon {

    public final static String CHAMERIA_INSTALL_LOCATION_PROP = "chameria.install.location";

    public final static String QT_PACKAGES = "" +
            "com.trolltech.extensions.signalhandler, " +
            "com.trolltech.qt, " +
            "com.trolltech.qt.core, " +
            "com.trolltech.qt.designer, " +
            "com.trolltech.qt.gui, " +
            "com.trolltech.qt.network, " +
            "com.trolltech.qt.opengl, " +
            "com.trolltech.qt.phonon, " +
            "com.trolltech.qt.sql, " +
            "com.trolltech.qt.svg, " +
            "com.trolltech.qt.webkit, " +
            "com.trolltech.qt.xml, " +
            "com.trolltech.qt.xmlpatterns";

    public final static String SERVICE_PACKAGES = "de.akquinet.chameria.services";

    private Framework m_framework;

    private String[] m_args;

    private File cache;

    private List<Object> activations = new ArrayList<Object>();

    public ChameRIA(String core, boolean debug, String app, String runtime,
            String fileinstall, String config) throws Exception {
        super(core, debug, runtime, app, fileinstall, config, null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map<String, String> getProperties(Map map) throws Exception {
        Map<String, String> conf = new HashMap<String, String>(super.getProperties(map));
        // Conf already contains chameleon specific packages.
        // Add QT and services.
        String pcks = (String) conf.get(
                "org.osgi.framework.system.packages.extra");
        conf.put("org.osgi.framework.system.packages.extra",
                    QT_PACKAGES + "," + SERVICE_PACKAGES + ","
                            + pcks);

        // Move the chameleon-cache if the install location is defined
        if (System.getProperty(CHAMERIA_INSTALL_LOCATION_PROP) != null) {
            File install = detectLocation();
            if (install.exists()) {
                cache = new File(install, "chameleon-cache");
                conf.put("org.osgi.framework.storage", cache.getAbsolutePath());
            } else {
                // The installation dir does not exist...
                String storage = conf.get("org.osgi.framework.storage");
                cache = new File(storage);
            }
        } else {
            // the org.osgi.framework.storage is already set.
            String storage = conf.get("org.osgi.framework.storage");
            cache = new File(storage);
        }

        return conf;
    }


    public static File detectLocation() {
        String location = System.getProperty(CHAMERIA_INSTALL_LOCATION_PROP);

        location =
                SubstVars.substVars(location, CHAMERIA_INSTALL_LOCATION_PROP, null, System
                        .getProperties());

        return new File(location);
    }

    public Framework start(String[] args) throws BundleException {
        File lock = new File(cache, ".lock");
        if (lock.exists()) {
            return null;
        }

        m_framework =  start();

        // To avoid race condition for the activation service
        // this must be synchronized
        synchronized (this) {
            try {
                m_framework.getBundleContext().addServiceListener(new ActivationListener(),
                        "(" + Constants.OBJECTCLASS + "=" + ActivationService.class.getName() + ")");
            } catch (InvalidSyntaxException e) {
                // Cannot happen
            }
            // As we're synchronized we're sure that events will be delivered once the lock
            // is released.
            activate(args, m_framework.getBundleContext());
        }

        return m_framework;
    }

    public void activate(String[] args, BundleContext context) {
        m_args = args;

        ServiceReference[] refs = null;
        try {
            refs = context.getServiceReferences(ActivationService.class.getName(), null);
        }
        catch (InvalidSyntaxException e) { }

        if (refs != null) {
            for (ServiceReference ref : refs) {
                ActivationService svc = (ActivationService) context.getService(ref);
                if (! activations.contains(svc)) {
                    svc.activation(args);
                    activations.add(svc);
                }
            }
        } else {
            System.out.println("No Activation service detected");
        }
    }

    @Override
    public Framework start() throws BundleException {
        createLockFile();
        return super.start();
    }

    @Override
    public void stop() throws BundleException, InterruptedException {
        if (m_framework != null  && m_framework.getBundleContext() != null) {
            deactivate(m_framework.getBundleContext());
        }
        deleteLock();
        super.stop();
    }

    public Framework getFramework() {
        return m_framework;
    }

    public void deactivate(BundleContext context) {
        ServiceReference[] refs = null;
        try {
            refs = context.getServiceReferences(ActivationService.class.getName(), null);
        }
        catch (InvalidSyntaxException e) { }

        if (refs != null) {
            for (ServiceReference ref : refs) {
                ((ActivationService) context.getService(ref)).deactivation();
            }
        }
    }

    private class ActivationListener implements ServiceListener {
        public synchronized void serviceChanged(ServiceEvent ev) {
            // We need to synchronize to avoid concurrent activation.
            if (m_framework != null  && ev.getType() == ServiceEvent.REGISTERED) {
                ActivationService svc = ((ActivationService) m_framework.getBundleContext()
                        .getService(ev.getServiceReference()));
                if (! activations.contains(svc)) {
                    svc.activation(m_args);
                    activations.add(svc);
                }
                return;
            }

            if (m_framework != null  && ev.getType() == ServiceEvent.UNREGISTERING) {
                activations.remove(m_framework.getBundleContext()
                        .getService(ev.getServiceReference()));
            }
        }
    }

    private void createLockFile() {
        File lock = new File(cache, ".lock");
        if (! cache.exists()) {
            cache.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(lock);
            writer.write("1");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteLock() {
        File lock = new File(cache, ".lock");
        try {
            FileWriter writer = new FileWriter(lock);
            writer.write("0");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        lock.delete();
    }


}
