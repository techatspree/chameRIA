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
package de.akquinet.chameria.activation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.swissbox.tinybundles.core.TinyBundles;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

import aQute.lib.osgi.Constants;
import de.akquinet.chameria.activation.bundle.ActivationServiceImpl;
import de.akquinet.chameria.launcher.ChameRIA;
import de.akquinet.chameria.services.ActivationService;

public class ActivationTest {


    private static final String APP_1_DIRECTORY = "target/test-configuration/app-1";
    private static final String APP_2_DIRECTORY = "target/test-configuration/app-2";


    private Field count;
    private Field args;
    private Field open;

    @Before
    public void setUp() throws Exception {
        File app1 = new File(APP_1_DIRECTORY);
        app1.mkdirs();

        File app2 = new File(APP_2_DIRECTORY);
        app2.mkdirs();

        createActivationBundle();
        createTwoActivationBundles();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("log"));
        FileUtils.deleteDirectory(new File("chameleon-cache"));
    }

    @Test
    public void startChameriaWithoutActivation() throws Exception {

        ChameRIA chameleon = new ChameRIA("target/core", true, null, null, null, null);
        Framework framework = chameleon.start(new String[0]);

        ServiceReference ref = framework.getBundleContext().getServiceReference(ActivationService.class.getName());
        Assert.assertNull(ref);

        chameleon.stop();
    }

    @Test
    public void startChameriaWithActivation() throws Exception {

        ChameRIA chameleon = new ChameRIA("target/core", true, null, APP_1_DIRECTORY, null, null);
        Framework framework = chameleon.start(new String[0]);

        ServiceReference ref = framework.getBundleContext().getServiceReference(ActivationService.class.getName());
        Assert.assertNotNull(ref);
        ActivationService svc = (ActivationService) framework.getBundleContext().getService(ref);
        count = svc.getClass().getField("activation_count");
        args = svc.getClass().getField("last_args");

        Assert.assertEquals(1, count.get(svc));
        Assert.assertEquals(0, ((String[]) args.get(svc)).length);

        chameleon.stop();
    }

    @Test
    public void startChameriaWithActivationAndArguments() throws Exception {

        ChameRIA chameleon = new ChameRIA("target/core", true, null, APP_1_DIRECTORY, null, null);
        Framework framework = chameleon.start(new String[] {"-open", "bla" });

        ServiceReference ref = framework.getBundleContext().getServiceReference(ActivationService.class.getName());
        Assert.assertNotNull(ref);
        ActivationService svc = (ActivationService) framework.getBundleContext().getService(ref);
        count = svc.getClass().getField("activation_count");
        args = svc.getClass().getField("last_args");
        open = svc.getClass().getField("open");

        Assert.assertEquals(1, count.get(svc));
        Assert.assertEquals(2, ((String[]) args.get(svc)).length);
        Assert.assertEquals("bla", open.get(svc));

        chameleon.stop();
    }

    @Test
    public void startChameriaWithTwoActivations() throws Exception {

        ChameRIA chameleon = new ChameRIA("target/core", true, null, APP_2_DIRECTORY, null, null);
        Framework framework = chameleon.start(new String[] {"-open", "bla" });

        ServiceReference[] refs = framework.getBundleContext().getServiceReferences(ActivationService.class.getName(), null);
        Assert.assertNotNull(refs);

        for(ServiceReference ref : refs) {
            ActivationService svc = (ActivationService) framework.getBundleContext().getService(ref);
            count = svc.getClass().getField("activation_count");
            args = svc.getClass().getField("last_args");
            open = svc.getClass().getField("open");

            Assert.assertEquals(1, count.get(svc));
            Assert.assertEquals(2, ((String[]) args.get(svc)).length);
            Assert.assertEquals("bla", open.get(svc));
        }

        chameleon.stop();

    }

    private void createActivationBundle() throws NullArgumentException, FileNotFoundException, IOException {
        InputStream is = TinyBundles.newBundle()
            .add(ActivationServiceImpl.class)
            .set(Constants.BUNDLE_ACTIVATOR, ActivationServiceImpl.class.getName())
            .build(TinyBundles.withBnd());
        File bundle = new File(APP_1_DIRECTORY + "/activation.jar");

        StreamUtils.copyStream(is, new FileOutputStream(bundle), true);

    }

    private void createTwoActivationBundles() throws NullArgumentException, FileNotFoundException, IOException {
        InputStream is = TinyBundles.newBundle()
            .add(ActivationServiceImpl.class)
            .set(Constants.BUNDLE_ACTIVATOR, ActivationServiceImpl.class.getName())
            .build(TinyBundles.withBnd());
        File bundle1 = new File(APP_2_DIRECTORY + "/activation1.jar");

        InputStream is2 = TinyBundles.newBundle()
        .add(ActivationServiceImpl.class)
        .set(Constants.BUNDLE_ACTIVATOR, ActivationServiceImpl.class.getName())
        .build(TinyBundles.withBnd());
        File bundle2 = new File(APP_2_DIRECTORY + "/activation2.jar");

        StreamUtils.copyStream(is, new FileOutputStream(bundle1), true);
        StreamUtils.copyStream(is2, new FileOutputStream(bundle2), true);

    }

}
