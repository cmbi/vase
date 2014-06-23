package nl.ru.cmbi.hssp.web;

import java.awt.Desktop;
import java.net.URI;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

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
		Server server = new Server();
		SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(8081);
		server.setConnectors(new Connector[] { connector });

		WebAppContext bb = new WebAppContext();
		bb.setServer(server);
		bb.setContextPath("/");
		bb.setWar("src/main/webapp");
		server.setHandler(bb);

		System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
		server.start();

		// Launch browser
		Desktop.getDesktop().browse(new URI("http://localhost:" + connector.getPort() + bb.getContextPath()));

		System.in.read();
		System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
		server.stop();
		server.join();
    }
}
