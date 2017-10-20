package filer.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsServer;


public class FilerAgent {
	
	final static Properties prop = new Properties(FilerConfig.getConfig());
	final static Logger logger = Logger.getLogger(FilerAgent.class);
	

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, NoSuchProviderException, SignatureException, IOException, UnrecoverableKeyException, KeyManagementException {
		boolean forceInit = false;
		if (args.length > 0) {
			if(args[0].equalsIgnoreCase("forceinit")) {
				forceInit = true;
			}
		}
		
		//boolean keystoreCheck = new CertManagerAgent().isKeyStoreAvailable()
		if(CertManagerAgent.isKeyStoreAvailable() && ! forceInit) {
			
			
			Endpoint endpoint = Endpoint.create(new SvcFilerAgentImpl());
			HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(prop.getProperty("filer.ws.bindAddress"), 
				      Integer.parseInt(prop.getProperty("filer.ws.bindPort"))), 
				      Integer.parseInt(prop.getProperty("filer.ws.bindPort")));


			
			httpsServer.setHttpsConfigurator(CertManagerAgent.getHttpsConfigurator());
			 

			HttpContext httpContext = httpsServer.createContext("/ws/get");
			httpsServer.start();
			
			endpoint.publish(httpContext);

		} else {
			logger.warn("Filer started in init mode, waiting to be contacted by the controller");
			Endpoint endpoint = Endpoint.create(new InitFilerAgentImpl());
			endpoint.publish("http://"+prop.getProperty("filer.ws.bindAddress")+":"+prop.getProperty("filer.ws.bindPort")+"/ws/get");
		}

		

	}

}
