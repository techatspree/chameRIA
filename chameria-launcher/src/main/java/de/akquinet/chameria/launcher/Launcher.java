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

import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.ow2.chameleon.core.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QIcon;

/**
 * Main class starting a ChameRIA application.
 */
public class Launcher {

    private static ChameRIA m_chameleon;

    /**
     * Constructor to avoid creating a new Launcher object.
     */
    private Launcher() { }

    public static void main(String[] args) throws Exception {

        String loc = System.getProperty("chameria.install.location");
        File root = null;
        if (loc != null) {
            root = new File(loc);
        } else {
            root = new File(System.getProperty("user.dir"));
        }

        printWelcomeBanner();


        m_chameleon = null;
        try {
            m_chameleon = createChameleon(args);
        } catch (Exception e) {
            System.err
                    .println("Cannot initalize Chameleon : " + e.getMessage());
            e.printStackTrace();
        }
        if (m_chameleon == null) {
            return;
        }

        final Logger logger = LoggerFactory.getLogger("Chameria-Launcher");
        registerShutdownHook(m_chameleon);
        try {
            logger.info("Initiliazing QT");
            // In order to enable the anti-aliasing of rounded corner, we set the
            // graphic system to 'raster'. opengl is currently not supported on mac.
            QApplication.setGraphicsSystem("raster");
            QApplication.initialize(new String[0]);

            // Add plugins.
            File qt = new File(root, "qt");
            File plugins = new File(qt, "plugins");
            if (plugins.exists()) {
                QCoreApplication.addLibraryPath(plugins.getAbsolutePath());
            }

            logger.info("QT initialized");

            configureApplication();
            logger.info("Application configured");

            Framework fmwk = m_chameleon.start(args);
            if (fmwk != null) {
                // The framework was created,
                // If a splash screen is here, removed it
                SplashScreen splash = SplashScreen.getSplashScreen();
                if (splash != null) {
                    splash.close();
                }

                QApplication.exec();
            }
            // Else we just exit immediately.
            // Reason might be the .lock file...

            m_chameleon.stop();
            logger.info("Chameleon Stopped");
        } catch (BundleException e) {
            System.err.println("Cannot start Chameleon : " + e.getMessage());
        }

    }

    public static ChameRIA getChameleon() {
        return m_chameleon;
    }

    /**
     * Configures the application.
     */
    private static void configureApplication() {
        // Try to load the file.
        File file = new File("chameria.props");
        if (file.exists()) {
            Properties props = new Properties();
            InputStream is = null;
            try {
                is = new FileInputStream(file);
                props.load(is);

                String n = props.getProperty("application.name");
                if (n != null) {
                    QApplication.setApplicationName(n);
                } else {
                    QApplication.setApplicationName("akquinet ChameRIA");
                }

                n = props.getProperty("application.version");
                if (n != null) {
                    QApplication.setApplicationVersion(n);
                }

                n = props.getProperty("application.icon");
                if (n != null) {
                    QIcon icon = new QIcon(n);
                    QApplication.setWindowIcon(icon);
                }

            } catch (Exception e) {
                System.err.println("Cannot read the application configuration "
                        + e.getMessage());
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // Ignored
                    }
                }
            }
        }

        QApplication.setOrganizationName("akquinet A.G.");
    }

    /**
     * Prints Welcome Banner.
     */
    private static void printWelcomeBanner() {
        StringBuffer banner = new StringBuffer();
        banner.append("\n");
        banner.append("\t============================\n");
        banner.append("\t|                          |\n");
        banner.append("\t|   Welcome to ChameRIA    |\n");
        banner.append("\t|                          |\n");
        banner.append("\t============================\n");
        banner.append("\n");
        System.out.println(banner);
    }

    /**
     * Prints Stopped Banner.
     */
    private static void printStoppedBanner() {
        System.out.println("\n");
        System.out.println("\t=========================");
        System.out.println("\t|   ChameRIA  stopped   |");
        System.out.println("\t=========================");
        System.out.println("\n");
    }

    /**
     * Creates the Chameleon instance.The instance is not started.
     * @param args the command line parameters.
     * @return the Chameleon instance
     * @throws Exception if the chameleon instance cannot be created correctly.
     */
    public static ChameRIA createChameleon(String[] args) throws Exception {
        boolean debug = isDebugModeEnabled(args);
        String core = getCore(args);
        String app = getApp(args);
        String runtime = getRuntime(args);
        String fileinstall = getDeployDirectory(args);
        String config = getProps(args);
        if (config == null  || ! new File(config).exists()) {
            return new ChameRIA(core, debug, app, runtime, fileinstall, null);
        } else {
            return new ChameRIA(core, debug, app, runtime, fileinstall, config);
        }

    }

    /**
     * Parses the --deploy parameter.
     * @param args the parameters.
     * @return the deploy folder or <code>null</code> if not found.
     */
    private static String getDeployDirectory(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (StringUtils.contains(arg, "--deploy=")) {
                return arg.substring("--deploy=".length());
            }
        }
        return null;
    }

    /**
     * Registers a shutdown hook to stop nicely the embedded framework.
     * @param chameleon the stopped chameleon
     */
    private static void registerShutdownHook(final ChameRIA chameleon) {
        Runtime runtime = Runtime.getRuntime();
        Runnable hook = new Runnable() {

            public void run() {
                try {
                    if (chameleon != null) {
                        chameleon.stop();
                        printStoppedBanner();
                    }
                } catch (BundleException e) {
                    System.err.println("Cannot stop Chameleon correctly : "
                            + e.getMessage());
                } catch (InterruptedException e) {
                    System.err.println("Unexpected Exception : "
                            + e.getMessage());
                    // nothing to do
                }
            }
        };
        runtime.addShutdownHook(new Thread(hook));

    }

    /**
     * Parses the --debug parameter.
     * @param args the parameters.
     * @return true if the debug mode is enabled.
     */
    private static boolean isDebugModeEnabled(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("--debug")) {
                return true;
            }
        }
        return false;

    }

    /**
     * Parses the --core parameter.
     * @param args the parameters.
     * @return the core folder or <code>null</code> if not found.
     */
    private static String getCore(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (StringUtils.contains(arg, "--core=")) {
                return arg.substring("--core=".length());
            }
        }
        return null;
    }

    /**
     * Parses the --app parameter.
     * @param args the parameters.
     * @return the application folder or <code>null</code> if not found.
     */
    private static String getApp(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (StringUtils.contains(arg, "--app=")) {
                return arg.substring("--app=".length());
            }
        }
        return null;
    }

    /**
     * Parses the --config parameter.
     * @param args the parameters.
     * @return the configuration file path or <code>null</code> if not found.
     */
    private static String getProps(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (StringUtils.contains(arg, "--config=")) {
                return arg.substring("--config=".length());
            }
        }
        return null;
    }

    /**
     * Parses the --runtime parameter.
     * @param args the parameters.
     * @return the runtime folder or <code>null</code> if not found.
     */
    private static String getRuntime(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (StringUtils.contains(arg, "--runtime=")) {
                return arg.substring("--runtime=".length());
            }
        }
        return null;
    }

}
