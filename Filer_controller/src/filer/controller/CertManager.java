package filer.controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpsConfigurator;


							


public class CertManager {
	final static Logger logger = Logger.getLogger(CertManager.class);
	
	final static char[] password = "7S+*96D+v{xTT*2g".toCharArray();
	
	final static String keypair_alias = "default_keypair";
	
	final static Properties prop = new Properties(FilerConfig.getConfig());
	
	final static String keystoreFilePath = 
			System.getProperty("filer.installationPath") + 
			System.getProperty("file.separator") + "cert" + 
			System.getProperty("file.separator") + 
			prop.getProperty("filer.certManager.keystoreFilename");
	
	final static String subject = 
			"CN=" + prop.getProperty("filer.certManager.certInfo.CN") + 
        	",OU=" + prop.getProperty("filer.certManager.certInfo.OU") +
        	",O=" + prop.getProperty("filer.certManager.certInfo.O") +
        	",L=" + prop.getProperty("filer.certManager.certInfo.L") +
        	",S=" + prop.getProperty("filer.certManager.certInfo.S") +
        	",C=" + prop.getProperty("filer.certManager.certInfo.C");
	
	final static String issuer = 
			"C=" + prop.getProperty("filer.certManager.certInfo.C") +
			",CN=" + prop.getProperty("filer.certManager.certInfo.CN") +
			",O=" + prop.getProperty("filer.certManager.certInfo.O");
	
	

	public static byte[] initFiler(Properties prop) throws NoSuchAlgorithmException, InvalidKeyException, KeyStoreException, CertificateException, NoSuchProviderException, SignatureException, IOException {
		// create a new keypair 
		
		KeyPair keypair = createKeypair();
				
		
		
		createKeyStore(keystoreFilePath);
		
		KeyStore keystore = loadKeyStore(keystoreFilePath, password);
		
		if(logger.isDebugEnabled()){
			logger.debug("keystoreFilePath: " + keystoreFilePath);
			logger.debug("subject: " + subject);
			logger.debug("issuer: " + issuer);
		}
		
		keystore.setKeyEntry(keypair_alias, keypair.getPrivate(), password, createCert(subject, issuer, keypair));
		
		saveKeyStore(keystore, keystoreFilePath, password);
		
		if(logger.isDebugEnabled()){
			logger.debug("Create CSR resquest for " + System.getProperty("java.vendor"));
		}
		
		byte[] csr = null;
		
		
		
		
		if (System.getProperty("java.vendor").contains("Oracle")) {
			if(logger.isDebugEnabled()){
				logger.debug("Enter CSR resquest for " + System.getProperty("java.vendor"));
			}
        	csr = CertManagerExtOracle.createCSRrequest(subject, keypair);
        } else if (System.getProperty("java.vendor").contains("IBM")) {
        //	csr = CertManagerExtIBM.createCSRrequest(subject, keypair);
        }
		
		return csr;
		
	}
	
	public static HttpsConfigurator getHttpsConfigurator(String keystoreFilePath) throws NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
		FileInputStream fIn = new FileInputStream(keystoreFilePath);
		SSLContext ssl = SSLContext.getInstance("TLS");
		KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(fIn, password);
		keyFactory.init(keystore, password);
		TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustFactory.init(keystore);
		ssl.init(keyFactory.getKeyManagers(),trustFactory.getTrustManagers(), new SecureRandom());
		HttpsConfigurator configurator = new HttpsConfigurator(ssl);
		return configurator;
			
	}
	
	
	
	private static void createKeyStore(String keystoreFilePath) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, NoSuchProviderException, SignatureException {
		if(logger.isInfoEnabled()){
			logger.warn("Creating keystore in: " + keystoreFilePath );
		}
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load( null, password );
    	FileOutputStream fout = new FileOutputStream(keystoreFilePath);
		keystore.store( fout, password );
    	fout.close();
    }
	
	private static KeyStore loadKeyStore(String keystoreFilePath, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		if(logger.isDebugEnabled()) {
			logger.debug("Loading keystore in: " + keystoreFilePath);
		}
		FileInputStream fIn = new FileInputStream(keystoreFilePath);
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(fIn, password);
		fIn.close();
		return keystore;
	}
	
	private static void saveKeyStore(KeyStore keystore, String keystoreFilePath, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		if(logger.isDebugEnabled()) {
			logger.debug("Saving keystore in: " + keystoreFilePath);
		}
		FileOutputStream fout = new FileOutputStream(keystoreFilePath);
    	keystore.store( fout, password );
    	fout.close();
		
	}
	
	private static KeyPair createKeypair() throws NoSuchAlgorithmException {
		// initialize key generator
		if(logger.isInfoEnabled()){
			logger.warn("Creating default keypair: " + keypair_alias );
		}
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		keyGen.initialize(2048, random);
		
		// generate a keypair
		KeyPair pair = keyGen.generateKeyPair();
	
		return pair;
		
	}
	
	private static Certificate[] createCert(String subject, String issuer, KeyPair pair) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, IOException {
		Certificate cert = null;
        
        if (System.getProperty("java.vendor").contains("Oracle")) {
        	cert = CertManagerExtOracle.generateCertificate(subject, issuer, pair, 365, "SHA256WITHRSA");
        } else if (System.getProperty("java.vendor").contains("IBM")) {
        	cert = CertManagerExtIBM.generateCertificate(subject, issuer, pair, 365, "SHA256WITHRSA");
        }
    	
    	List<Certificate> Certificates = new ArrayList<Certificate>();
    	Certificates.add(cert);
    	Certificate[] certs = new Certificate[ Certificates.size()];
    	Certificates.toArray(certs);
		return certs;
		
	}
	

	
//	public static ByteArrayOutputStream createCSRrequest(Properties prop) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, SignatureException {
//		String keystoreFilePath = System.getProperty("filer.installationPath") + System.getProperty("file.separator") + "cert" + System.getProperty("file.separator") + prop.getProperty("filer.certManager.keystoreFilename");
//		String subject= "CN=" + prop.getProperty("filer.certManager.certInfo.CN") + 
//	        	",OU=" + prop.getProperty("filer.certManager.certInfo.OU") +
//	        	",O=" + prop.getProperty("filer.certManager.certInfo.O") +
//	        	",L=" + prop.getProperty("filer.certManager.certInfo.L") +
//	        	",S=" + prop.getProperty("filer.certManager.certInfo.S") +
//	        	",C=" + prop.getProperty("filer.certManager.certInfo.C");
//	   KeyPair keyPair = getKeypair(keystoreFilePath);			
//	   if (System.getProperty("java.vendor").contains("Oracle")) {
//	   		return CertManagerExtOracle.createCSRrequest(subject, keyPair);
//	   	}    	
//	    
//	    return null;
//	    
//	}
	
}
