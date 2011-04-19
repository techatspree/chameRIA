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
