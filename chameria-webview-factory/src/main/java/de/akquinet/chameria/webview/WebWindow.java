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
package de.akquinet.chameria.webview;

import java.io.File;
import java.util.List;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QPainter.RenderHint;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.webkit.QWebSettings;
import com.trolltech.qt.webkit.QWebSettings.FontSize;
import com.trolltech.qt.webkit.QWebView;

/**
 * This class represents the browser window.
 * It's the main window.
 */
public class WebWindow extends QMainWindow {

    @Override
    protected void dropEvent(QDropEvent arg__1) {
        super.dropEvent(arg__1);

        arg__1.accept();
        List<QUrl> list = arg__1.mimeData().urls();
        for (QUrl url : list) {
            System.out.println(url.toString());
        }
    }

    /**
     * The main web view.
     */
    private QWebView m_view;

    /**
     * The initial url.
     */
    private String m_url;

    /**
     * The webview factory that creates this window
     */
    private WebViewFactory m_factory;

    /**
     * Creates a {@link WebWindow}.
     * @param url the initial url
     * @param factory the factory
     */
    public WebWindow(String url, WebViewFactory factory) {
        this(null, url, factory);
    }

    /**
     * Creates a {@link WebWindow}.
     * This method also loads the given url in the main web view.
     * @param the parent view
     * @param url the initial url
     * @param factory the factory
     */
    public WebWindow(QWidget parent, String url, WebViewFactory factory) {
        super(parent);
        m_factory = factory;
        m_view = new ChameriaWebView(m_factory);


        // TODO Set this as a property CHAMERIA-1
        //QWebSettings.setObjectCacheCapacities(0, 0, 0);


        QWebSettings.globalSettings().setAttribute(QWebSettings.WebAttribute.DeveloperExtrasEnabled, factory.isInspectorEnabled());

        QWebSettings.globalSettings().setAttribute(QWebSettings.WebAttribute.LocalStorageDatabaseEnabled, factory.isLocalStorageEnabled());
        QWebSettings.globalSettings().setAttribute(QWebSettings.WebAttribute.OfflineStorageDatabaseEnabled, factory.isLocalStorageEnabled());

        if (factory.getDefaultFixedFontSize() != -1) {
            QWebSettings.globalSettings().setFontSize(FontSize.DefaultFixedFontSize, factory.getDefaultFixedFontSize());
        }

        if (factory.getDefaultFontSize() != -1) {
            QWebSettings.globalSettings().setFontSize(FontSize.DefaultFontSize, factory.getDefaultFontSize());
        }

        if (factory.isLocalStorageEnabled()) {
            File offline = new File (factory.getLocalStorageLocation());
            if (! offline.exists()) {
                offline.mkdir();
            }
            QWebSettings.setOfflineStoragePath(offline.getAbsolutePath());
        }

        this.m_url = url;
        setCentralWidget(m_view);

        // Set an initial loading page once its up and showing...
        QApplication.invokeLater(new Runnable() {
            public void run() {
                m_view.setRenderHints(
                        RenderHint.Antialiasing,
                        RenderHint.HighQualityAntialiasing,
                        RenderHint.SmoothPixmapTransform,
                        RenderHint.TextAntialiasing,
                        RenderHint.NonCosmeticDefaultPen);
                open();
                repaint();
            }
        });

        // Enable drag and drop
        acceptDrops();

        show();
        raise();
        activateWindow();
    }


    @Override
    protected void closeEvent(QCloseEvent ev) {
        ev.accept();
        System.exit(0);
    }

    /**
     * Loads the given url in the main web view.
     */
    public void open() {
        m_view.load(new QUrl(m_url));
    }

    /**
     * Sets and opens the given url in the main web view.
     * @param url the new url.
     */
    public void open(String url) {
        this.m_url = url;
        open();
    }

}
