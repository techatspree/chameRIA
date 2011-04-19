package de.akquinet.chameria.webview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.ContextMenuPolicy;
import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.network.QNetworkProxy;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.webkit.QWebView;

import de.akquinet.chameria.services.BrowserService;


/**
 * Main component of the web view factory.
 * This component receives the configuration and creates
 * the browser windows and the web view.
 * It also has a couple of up-calls to handle the
 * configuration properly.
 */
@Component(name="chameria.webview")
@Provides
public class WebViewFactory implements BrowserService {

    /**
     * Chameria Logger.
     */
    final Logger m_logger = LoggerFactory.getLogger("Chameria-WebViewFactory");

    /**
     * The Browser Window.
     */
    private WebWindow m_browser;

    /**
     * The initial URL.
     */
    private String m_url;

    /**
     * Configuration Property : enables/disabled
     * the full screen mode.
     * Default : false.
     */
    @Property(name="fullscreen", value="false")
    private boolean m_fullscreen;

    /**
     * Configuration Property : the window height.
     * This property is ignored if the full screen mode
     * is enabled.
     * Default: 600 px.
     */
    @Property(name="height", value="600")
    private int m_height;

    /**
     * Configuration Property : the window width.
     * This property is ignored if the full screen mode
     * is enabled.
     * Default : 1024 px.
     */
    @Property(name="width", value="1024")
    private int m_width;

    /**
     * Configuration Property : enables / disables the
     * window 'resizability'.
     * This property is ignored if the full screen mode
     * is enabled.
     * Default : true.
     */
    @Property(name="resizable", value="true")
    private boolean m_resizable;

    /**
     * Configuration Property : enables / disables
     * the menu bar (top bar)
     * Default : true (enabled).
     */
    @Property(name="menu.bar", value="true")
    private boolean m_bar;

    /**
     * Configuration Property : enables / disables
     * the context menu
     * Default : false (disabled).
     */
    @Property(name="context.menu", value="false")
    private boolean m_contextMenu;

    /**
     * Configuration Property: sets the path
     * of the application icon. If not set, no
     * icon will be used.
     * The path is relative to the execution
     * directory.
     */
    @Property(name="application.icon")
    private String m_icon;

    /**
     * Configuration Property: sets the application
     * name used as window title.
     */
    @Property(name="application.name")
    private String m_appName;

    /**
     * Configuration Property: sets the application
     * version.
     */
    @Property(name="application.version")
    private String m_appVersion;

    /**
     * Configuration Property: enables / disables the
     * print support (<code>window.print</code>)
     * Default: false (disabled)
     */
    @Property(name="print", value="false")
    private boolean m_print;

    /**
     * Configuration Property: enables / disables the
     * download of unsupported content.
     * Default: false (disabled)
     */
    @Property(name="download", value="false")
    private boolean m_download;

    /**
     * Configuration Property: enables / disables the
     * opening of new windows (<code>window.open</code>).
     * The opened window have the same property as the parent
     * (current) one.
     * Default: false (disabled)
     */
    @Property(name="window.open", value="false")
    private boolean m_window_open;

    /**
     * Configuration Property: enables / disables the
     * inspector. The context menu must also be enabled.
     * Default: false (disabled)
     */
    @Property(name="inspector", value="false")
    private boolean m_inspector;

    /**
     * Configuration Property: enables / disables the
     * local storage (session storage, local storage,
     * client database, offline mode...)
     * Default: true (enabled)
     */
    @Property(name="storage", value="true")
    private boolean m_localStorage;

    /**
     * Configuration Property: sets the local storage
     * location (path relative to the launching directory.
     * Default: ./offline-storage
     */
    @Property(name="storage.location", value="offline-storage")
    private String m_localStorageLocation;


    /**
     * Horizontal Scrollbar policy.
     * Supported values are:
     * <ul>
     * <li>ScrollBarAsNeeded (default)</li>
     * <li>ScrollBarAlwaysOn</li>
     * <li>ScrollBarAlwaysOff</li>
     * </ul>
     */
    @Property(name="scrollbar.horizontal", value="ScrollBarAsNeeded")
    private String m_scrollbar_horizontal;

    /**
     * Horizontal Scrollbar policy.
     * Supported values are:
     * <ul>
     * <li>ScrollBarAsNeeded (default)</li>
     * <li>ScrollBarAlwaysOn</li>
     * <li>ScrollBarAlwaysOff</li>
     * </ul>
     */
    @Property(name="scrollbar.vertical", value="ScrollBarAsNeeded")
    private String m_scrollbar_vertical;

    /**
     * Sets the default fixed font size.
     */
    @Property(name="font.size.fixed", value="-1")
    private int m_fixed_fontsize;

    /**
     * Sets the default font size.
     */
    @Property(name="font.size", value="-1")
    private int m_fontsize;

    /**
     * Sets the proxy type. To avoid proxy, don't assign a value to
     * this property.
     * Supported values are:
     * <ul>
     * <li>Socks5Proxy</li>
     * <li>HttpProxy</li>
     * <li>HttpCachingProxy</li>
     * </ul>
     */
    @Property(name="proxy.type")
    private QNetworkProxy.ProxyType m_proxyType;

    /**
     * Sets the proxy host name.
     * Cannot be null, if proxy is enabled.
     */
    @Property(name="proxy.hostname")
    private String m_proxyHostName;

    /**
     * Sets the proxy port.
     * Cannot be 0, if proxy is enabled.
     */
    @Property(name="proxy.port")
    private int m_proxyPort;

    /**
     * Bundle Context.
     */
    private BundleContext m_context;

    /**
     * Creates a WebViewFactory.
     * @param ctxt the bundle context.
     */
    public WebViewFactory(BundleContext ctxt) {
        m_context = ctxt;
    }

    /**
     * Configuration Property: set the initial URL.
     * This property is <b>mandatory</b>.
     * If the browser is already created, this web view loads
     * this new url.
     * @param url the url
     */
    @Property(name="url")
    public synchronized void setURL(final String url) {
        this.m_url = url;
        if (m_browser != null) {
            QApplication.invokeLater(new Runnable() {
                public void run() {
                    m_browser.open(url);
                }
            });
        }
    }

    /**
     * Deletes the create browser.
     */
    @Invalidate
    public synchronized void stop() {
        if (m_browser != null) {
            m_browser.disposeLater();
        }
    }

    /**
     * Starts the browser and loads the
     * set url in the web view.
     */
    @Validate
    public  void start() {
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureApplication();
                m_logger.info("Creating a web ui...");
                synchronized (WebViewFactory.this) {
                    m_browser = new WebWindow(m_url, WebViewFactory.this);

                    configureWindow(m_browser);
                    m_browser.show();
                }
                m_logger.info("Web UI created.");
            }
        });
    }

    /**
     * Configures the application:
     * <ul>
     * <li>sets the application name (default: 'akquinet ChameRIA')</li>
     * <li>sets the application version (default: current web view factory version)</li>
     * <li>sets the application icon (default : no icon)</li>
     * </ul>
     */
    private void configureApplication() {
        if (m_appName != null) {
            QApplication.setApplicationName(m_appName);
        } else {
            QApplication.setApplicationName("akquinet ChameRIA");
        }
        if (m_appVersion != null) {
            QApplication.setApplicationVersion(m_appVersion);
        } else {
            QApplication.setApplicationVersion(m_context.getBundle().getVersion().toString());
        }

        if (m_icon != null) {
            QFile file = new QFile(m_icon);

            QIcon icon = new QIcon(file.fileName());
            QApplication.setWindowIcon(icon);
        }

        QApplication.setOrganizationName("akquinet A.G.");

        // Configure the proxy
        if (m_proxyType != null) {
            m_logger.warn("Set application proxy : " + m_proxyType);
            if (m_proxyHostName == null || m_proxyPort == 0) {
                m_logger.error("Cannot configure proxy : hostname or port not set : " + m_proxyHostName + ":" + m_proxyPort);
            } else {
                QNetworkProxy proxy = new QNetworkProxy(m_proxyType, m_proxyHostName, m_proxyPort);
                QNetworkProxy.setApplicationProxy(proxy);
                m_logger.warn("Application proxy set " + m_proxyType + " on " + m_proxyHostName + ":" + m_proxyPort);
            }
        }

    }

    /**
     * Configures the browser window.
     * @param web the browser window
     */
    private void configureWindow(WebWindow web) {

        if (m_fullscreen) {
            // We need to store the previous width and height values
            m_width = web.width();
            m_height = web.height();
            web.showFullScreen();
        } else {
            web.showNormal();
            if (! m_resizable) {
               web.setFixedSize(new QSize(m_width, m_height));
            } else {
               web.setBaseSize(new QSize(m_width, m_height));
            }
            web.resize(m_width, m_height);
        }

        if (! m_bar) {
            web.menuBar().setVisible(false);
        } else {
            web.menuBar().setVisible(true);
            if (m_icon != null) {
                QIcon icon = new QIcon(m_icon);
                web.setWindowIcon(icon);
            }
            web.setWindowTitle(m_appName);
        }

        if (! m_contextMenu) {
            web.setContextMenuPolicy(ContextMenuPolicy.PreventContextMenu);
        } else {
            web.setContextMenuPolicy(ContextMenuPolicy.DefaultContextMenu);
        }
    }

    /**
     * Print callback.
     * Checks if the print feature is enabled. If so, launch the system
     * print job.
     * @param view the view to print
     */
    public void print(QWebView view) {
        if (m_print) {
            QPrinter printer = new QPrinter();
            QPrintDialog printDialog = new QPrintDialog(printer, view);
            if (printDialog.exec() == QDialog.DialogCode.Accepted.value()) {
                // print ...
                view.print(printer);
            }
        } else {
            m_logger.warn("Print disabled");
        }
    }

    /**
     * Save callback (for unsupported content).
     * Checks if the 'save' feature is enabled. If so, launch the system
     * save dialog.
     * @param reply the url to save
     */
    public void save(QNetworkReply reply) {
        if (m_download) {
            String fn = QFileDialog.getSaveFileName();
            if (fn != null && fn.length()>0) {
                m_logger.info("File name : " + fn);
                try {
                    URL u = new URL(reply.url().toString());
                    FileOutputStream out = new FileOutputStream(new File(fn));
                    InputStream in = u.openStream();
                    write(in, out);
                } catch (IOException e) {
                    m_logger.error("Cannot download file " + e.getMessage(), e);
                }
            } else {
                m_logger.warn("No File Name - Download request cancelled");
            }
        } else {
            m_logger.warn("Download disabled");
        }

    }

    /**
     * Utility method to copy a stream to another stream.
     * @param in the stream to copy
     * @param out the destination
     * @throws IOException the stream cannot be copied
     */
    public static void write(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.write(b, 0, n);
        }
        in.close();
        out.close();
    }

    /**
     * Open callback (<code>window.open</code>).
     * Checks if the 'open new window' feature is enabled. If so, creates the
     * web view
     */
    public QWebView openWindow() {
        if (m_window_open) {
            // We share the same factory
            QWebView newwindow = new ChameriaWebView(this);
            return newwindow;
        } else {
            m_logger.warn("Open new window disabled");
            return null;
        }
    }

    /**
     * Is the context menu supported ?
     * @return <code>true</code> if the context menu is supported,
     * otherwise <code>false</code>
     */
    public boolean isContextMenuSupported() {
        return m_contextMenu;
    }

    /**
     * Gets the horizontal scroll bar policy.
     * @return the horizontal bar policy
     */
    public ScrollBarPolicy getHorizontalScrollBarQTPolicy() {
        return ScrollBarPolicy.valueOf(m_scrollbar_horizontal);
    }

    /**
     * Gets the vertical scroll bar policy.
     * @return the vertical bar policy
     */
    public ScrollBarPolicy getVerticalScrollBarQTPolicy() {
        return ScrollBarPolicy.valueOf(m_scrollbar_vertical);
    }

    /**
     * Checks if the inspector is enabled.
     * @return <code>true</code> if the inspector
     * is enabled.
     */
    public boolean isInspectorEnabled() {
        return m_inspector;
    }

    /**
     * Checks if the local storage is enabled.
     * @return <code>true</code> if the local storage
     * is enabled.
     */
    public boolean isLocalStorageEnabled() {
        return m_localStorage;
    }

    /**
     * Gets the local storage location.
     * @return the local storage location
     */
    public String getLocalStorageLocation() {
        return m_localStorageLocation;
    }

    public int getHeight() {
        return m_height;
    }

    public String getURL() {
        return m_url;
    }

    public int getWidth() {
        return m_width;
    }

    public int getDefaultFixedFontSize() {
        return m_fixed_fontsize;
    }

    public int getDefaultFontSize() {
        return m_fontsize;
    }

    public boolean isContextMenuEnabled() {
        return m_contextMenu;
    }

    public boolean isDownloadSupported() {
        return m_download;
    }

    public boolean isFullScreen() {
        return m_fullscreen;
    }

    public boolean isMenuBarEnabled() {
        return m_bar;
    }

    public boolean isOpenWindowSupported() {
        return m_window_open;
    }

    public boolean isPrintSupported() {
        return m_print;
    }

    public boolean isResizable() {
        return m_resizable;
    }

    public void setContextMenu(boolean enabled) {
        m_contextMenu = enabled;
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureWindow(m_browser);
            }
        });
    }

    public void setDownloadSupport(boolean enabled) {
        m_download = enabled;
    }

    public void setFullScreen(boolean fullscreen) {
        m_fullscreen = fullscreen;
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureWindow(m_browser);
            }
        });
    }

    public void setHorizontalScrollBarPolicy(String policy) {
        m_scrollbar_horizontal = policy;
    }

    public void setMenuBar(boolean enabled) {
        m_bar = enabled;
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureWindow(m_browser);
            }
        });

    }

    public void setOpenWindowSupport(boolean enabled) {
        m_window_open = enabled;
    }

    public void setPrintSupport(boolean enabled) {
        m_print = enabled;
    }

    public void setResizable(boolean resizable) {
        m_resizable = resizable;
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureWindow(m_browser);
            }
        });
    }

    public void setSize(int width, int height) {
        m_width = width;
        m_height = height;
        QApplication.invokeLater(new Runnable() {
            public void run() {
                configureWindow(m_browser);
            }
        });
    }

    public void setVerticalScrollBarPolicy(String policy) {
        m_scrollbar_vertical = policy;
    }

    public String getHorizontalScrollBarPolicy() {
        return m_scrollbar_horizontal;
    }

    public String getVerticalScrollBarPolicy() {
        return m_scrollbar_vertical;
    }

}
