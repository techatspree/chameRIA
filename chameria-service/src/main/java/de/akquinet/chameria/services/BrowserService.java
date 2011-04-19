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
package de.akquinet.chameria.services;

/**
 * Service interface provided by the WebViewFactory to control the browser.
 */
public interface BrowserService {

    public static final String SCROLLBAR_AS_NEEDED = "ScrollBarAsNeeded";
    public static final String SCROLLBAR_ALWAYS_ON = "ScrollBarAlwaysOn";
    public static final String SCROLLBAR_ALWAYS_OFF = "ScrollBarAlwaysOff";

    public boolean isFullScreen();

    public void setFullScreen(boolean fullscreen);

    public void setURL(String url);

    public String getURL();

    public void setSize(int width, int height);

    public int getHeight();

    public int getWidth();

    public boolean isResizable();

    public void setResizable(boolean resizable);

    public boolean isMenuBarEnabled();

    public void setMenuBar(boolean enabled);

    public boolean isContextMenuEnabled();

    public void setContextMenu(boolean enabled);

    public boolean isPrintSupported();

    public void setPrintSupport(boolean enabled);

    public boolean isDownloadSupported();

    public void setDownloadSupport(boolean enabled);

    public boolean isOpenWindowSupported();

    public void setOpenWindowSupport(boolean enabled);

    public void setHorizontalScrollBarPolicy(String policy);

    public void setVerticalScrollBarPolicy(String policy);

    public String getVerticalScrollBarPolicy();

    public String getHorizontalScrollBarPolicy();


}
