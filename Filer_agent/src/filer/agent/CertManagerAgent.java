package filer.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpsConfigurator;

public class CertManagerAgent {
	final static Properties prop = new Properties(FilerConfig.getConfig()); 
	final static char[] password = "7S+*96D+v{xTT*2g".toCharArray();
	final static Logger logger = Logger.getLogger(CertManagerAgent.class);
	final static String keystoreFilePath = 
			System.getProperty("filer.installationPath") + 
			System.getProperty("file.separator") + "cert" + 
			System.getProperty("file.separator") + 
			prop.getProperty("filer.certManager.keystoreFilename"); 

	public void initKeyStore(byte[] keystoreBA) throws IOException {
		if(logger.isInfoEnabled()){
			logger.warn("Initializing KeyStore from controller");
		}
		
		FileOutputStream keystoreFile = new FileOutputStream(keystoreFilePath);
		keystoreFile.write(keystoreBA);
		keystoreFile.flush();
		keystoreFile.close();
		
	}
	
	public static boolean isKeyStoreAvailable() {
		File keystore = new File(keystoreFilePath);
		
		if (keystore.exists()) {
			return true;
		}
		
		return false;
		
	}
	
	public static HttpsConfigurator getHttpsConfigurator() throws NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyStoreException, KeyManagementException, IOException {
		if(logger.isDebugEnabled()){
			logger.debug("Load KeyStore " + keystoreFilePath);
		}
		FileInputStream fIn = new FileInputStream(keystoreFilePath);
		KeyStore keystore = KeyStore.getInstance("JKS");
		try {
			keystore.load(fIn, password);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.error("Load Keystore failed: ", e);
		}
		fIn.close();
		
		SSLContext ssl = SSLContext.getInstance("TLSv1.2");
		KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		
		
		keyFactory.init(keystore, password);
		TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustFactory.init(keystore);
		ssl.init(keyFactory.getKeyManagers(),trustFactory.getTrustManagers(), new SecureRandom());
		HttpsConfigurator configurator = new HttpsConfigurator(ssl);
		return configurator;
			
	}
}
