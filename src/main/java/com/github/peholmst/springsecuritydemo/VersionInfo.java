/*
 * Copyright 2010 Petter Holmström
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.peholmst.springsecuritydemo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class that looks up the application version from a file named
 * <code>version.properties</code> that should be found in the root of the
 * classpath. The file is expected to have a property named
 * <code>app.version</code> that contains the version string. The file itself
 * could be generated during build time.
 * 
 * @author Petter Holmström
 */
public class VersionInfo {

	private static final Log logger = LogFactory.getLog(VersionInfo.class);

	private static String version = null;

	/**
	 * Gets the current version of this application.
	 * 
	 * @return the application version string (never <code>null</code>).
	 */
	public static String getApplicationVersion() {
		if (version == null) {
			readApplicationVersion();
		}
		return version;
	}

	private static void readApplicationVersion() {
		if (logger.isDebugEnabled()) {
			logger
					.debug("Attempting to read application version from 'version.properties'");
		}
		InputStream is = VersionInfo.class
				.getResourceAsStream("/version.properties");
		if (is != null) {
			Properties props = new Properties();
			try {
				props.load(is);
				version = props.getProperty("app.version");
				if (version != null) {
					if (logger.isInfoEnabled()) {
						logger.info("Application version is '" + version + "'");
					}
					return;
				}
			} catch (IOException e) {
				if (logger.isErrorEnabled()) {
					logger.error("Could not load version properties", e);
				}
			}
		}
		if (logger.isWarnEnabled()) {
			logger
					.warn("Could not retrieve a version number, using 'unversioned'");
		}
		version = "unversioned";
	}

}
