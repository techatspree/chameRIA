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
package de.akquinet.chameria.web.exposer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Unbind;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component(immediate=true)
@Instantiate
public class WebExposer {

    public class ExposerHttpContext implements HttpContext {

        private String m_prefix;

        public ExposerHttpContext() {
            if (m_alias.startsWith("/")) {
                m_prefix = m_alias.substring(1) + "/";
            } else {
                m_prefix = m_alias + "/";
            }
        }

        public boolean handleSecurity(HttpServletRequest request,
                HttpServletResponse response) throws IOException {
            return true;
        }

        public URL getResource(String name) {

            if (name.startsWith(m_prefix)) {
                name = name.substring(m_prefix.length());
            }

            File file = new File(m_root, name);

            if (file.isDirectory()) {
                File index = new File(file, "index.html");
                if (index.exists()) {
                    file = index;
                }
            }

            m_logger.info("Looking for " + file.getAbsolutePath());
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                // Cannot happen.
            }

            return null;
        }

        public String getMimeType(String name) {
            return null;
        }

    }

    @Property(value="web", name="exposed.directory")
    private String m_exposedDirectoryName;

    @Property(value="/web", name="alias")
    private String m_alias;

    private Logger m_logger = LoggerFactory.getLogger(getClass());

    private File m_root;

    @Bind
    public void bindHTTP(HttpService http) throws NamespaceException {
        if (! m_alias.startsWith("/")) {
            m_alias = "/" + m_alias;
        }

        m_root = new File(m_exposedDirectoryName);
        http.registerResources(m_alias, m_exposedDirectoryName, new ExposerHttpContext());
        m_logger.info("Exposing resources from " + m_root.getAbsolutePath() + " on " + m_alias);

        if (! m_root.exists()) {
            m_logger.warn("The " + m_root.getAbsolutePath() + " directory does not exist");
        }

        if (! m_root.isDirectory()) {
            m_logger.warn("The " + m_root.getAbsolutePath() + " directory is not a directory");
        }
    }

    @Unbind
    public void unbindHTTP(HttpService http) {
        m_logger.info("Unregister resources from " + m_alias);
        http.unregister(m_alias);
    }

}
