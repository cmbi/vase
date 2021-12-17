/**
 * Copyright 2014 CMBI (contact: <Coos.Baakman@radboudumc.nl>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.ru.cmbi.vase.web;

import java.awt.Desktop;
import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Separate startup class for people that want to run the examples directly. Use parameter
 * -Dcom.sun.management.jmxremote to startup JMX (and e.g. connect with jconsole).
 */
public class Start
{
    public static void main(String[] args) throws Exception {
		// Mock database and contents
		// XXX DBMock.main(new String[0]);

		// Setup server to host web app
		int port = 8081;
		Server server = new Server(port);

		WebAppContext bb = new WebAppContext();
		bb.setServer(server);
		bb.setContextPath("/");
		bb.setWar("src/main/webapp");
		server.setHandler(bb);

		System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
		server.start();

		// Launch browser
		Desktop.getDesktop().browse(new URI("http://localhost:" + port + bb.getContextPath()));

		System.in.read();
		System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
		server.stop();
		server.join();
    }
}
