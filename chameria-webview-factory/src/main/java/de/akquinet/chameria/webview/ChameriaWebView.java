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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.ContextMenuPolicy;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.webkit.QWebPage;
import com.trolltech.qt.webkit.QWebPage.WebWindowType;
import com.trolltech.qt.webkit.QWebView;

/**
 * This class represents web views.
 */
public class ChameriaWebView extends QWebView {

    /**
     * The logger.
     */
    private Logger m_logger = LoggerFactory.getLogger(this.getClass());

    /**
     * the logger used for javascript messages.
     */
    private Logger m_javascriptLogger = LoggerFactory.getLogger("chameria.javascript");

    /**
     * The WebView Factory.
     */
    private WebViewFactory m_factory;

    /**
     * Creates a {@link ChameriaWebView}.
     * @param factory the factory
     */
    public ChameriaWebView(WebViewFactory factory) {
        super();
        m_factory = factory;
        loadStarted.connect(this, "loadStarted()");
        loadProgress.connect(this, "loadProgress(int)");
        loadFinished.connect(this, "loadDone()");
        linkClicked.connect(this, "linkClicked(QUrl)");

        setPage(new QWebPage() {
            @Override
            protected void javaScriptConsoleMessage(String message,
                    int lineNumber, String sourceID) {
                m_javascriptLogger.info("Javascript> " + message + " (" + sourceID + ", " + lineNumber + ")");
            }
        });

        page().setForwardUnsupportedContent(true);
        page().unsupportedContent.connect(this, "save(QNetworkReply)");

        page().printRequested.connect(this, "print()");
        page().windowCloseRequested.connect(this, "windowCloseRequested()");

        page().mainFrame().setScrollBarPolicy(Orientation.Horizontal, m_factory.getHorizontalScrollBarQTPolicy());
        page().mainFrame().setScrollBarPolicy(Orientation.Vertical, m_factory.getVerticalScrollBarQTPolicy());

        if (! m_factory.isContextMenuSupported()) {
            setContextMenuPolicy(ContextMenuPolicy.NoContextMenu);
        }


    }

    /**
     * This function is called whenever WebKit wants to create a new window of the given type,
     * for example when a JavaScript program requests to open a document in a new window.
     * @see com.trolltech.qt.webkit.QWebView#createWindow(com.trolltech.qt.webkit.QWebPage.WebWindowType)
     */
    @Override
    protected QWebView createWindow(WebWindowType type) {
        m_logger.info("Open a new window");
        return m_factory.openWindow();
    }

    /**
     * A link was clicked.
     * @param url the url
     */
    public void linkClicked(QUrl url) {
        m_logger.info("Link " + url);
    }

    /**
     * A new url is is loading.
     */
    public void loadStarted() {
        m_logger.info("Starting to load " + url());
    }

    /**
     * Loading done.
     */
    public void loadDone() {
        m_logger.info("Loading done... " + url());
        repaint();
    }

    /**
     * The current window will be closed.
     */
    public void windowCloseRequested() {
        m_logger.info("Close the window");
        this.close();
    }

    /**
     * Print requested (<code>window.print</code>)
     */
    public void print() {
        m_logger.info("Print Requested");
        m_factory.print(this);
    }

    /**
     * The page is loading.
     * @param x the new progress
     */
    public void loadProgress(int x) {
        if (x >= 10  && x % 10 == 0) {
            m_logger.info("Loading: " + x + " %");
        }
    }

    /**
     * Save dialog requested (unsupported content).
     * @param reply the url
     */
    public void save(QNetworkReply reply) {
        m_logger.info("Unsupported Content : " + reply.url().toString()  + " - Download request");
        m_factory.save(reply);
    }
}
