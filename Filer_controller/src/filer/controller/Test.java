package filer.controller;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.naming.InvalidNameException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.operator.OperatorCreationException;
import filer.agent.InitFilerAgent;
import filer.agent.SvcFilerAgent;

//import com.google.gson.Gson;


public class Test {

	final static char[] password = "7S+*96D+v{xTT*2g".toCharArray();
	final static String keystoreFilePath = "C:\\temp\\Filer_Controller\\cert\\filer.keystore";
	final static String CAalias = "testca";
	
	public static void main(String[] args) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, OperatorCreationException, InvalidNameException, IOException, CMSException {
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
		//  -Djavax.net.debug=ssl:handshake:verbose:keymanager:trustmanager -Djava.security.debug=access:stack
		URL url = null;
		
			String agentFQDN = "axatech-443159.axa-assicurazioni-it.intraxa";
//			url = new URL("http://" + agentFQDN + ":9999/ws/get?wsdl");
//			QName qname = new QName("http://agent.filer/", "InitFilerAgentImplService");
//			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
//			Service service = Service.create(url, qname);
//			InitFilerAgent remotesvc = service.getPort(InitFilerAgent.class);
//			remotesvc.putKeyStore(TestCertManager.initAgentKeystore(agentFQDN));
			//QName qname = new QName("http://agent.filer/", "SvcFilerAgentImplService");
			
			javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
				    new javax.net.ssl.HostnameVerifier(){
				 
				        public boolean verify(String hostname,
				                javax.net.ssl.SSLSession sslSession) {
				            
				                return true;
				    
				        }
				    });
			
			
			url = new URL("https://" + agentFQDN + ":9999/ws/get?wsdl");
			QName qname = new QName("http://agent.filer/", "SvcFilerAgentImplService");
			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
			Service service = Service.create(url, qname);
			SvcFilerAgent remotesvc = service.getPort(SvcFilerAgent.class);
			System.out.println(remotesvc.getVersionInfo());
		
			System.out.println("done");
			
	}
}
