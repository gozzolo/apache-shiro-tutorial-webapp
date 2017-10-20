package filer.controller;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

public class CertManagerController2 {
	final static char[] controllerKeystorePassword = "7S+*96D+v{xTT*2g".toCharArray();
	final static char[] agentKeystorePassword = "test".toCharArray();
	final static String controllerKeystoreFilePath = "C:\\temp\\Filer_Controller\\cert\\filer.keystore";
	final static String CAalias = "Filer-Internal-CA-01";
	final static String signatureAlgorithm = "SHA256WithRSA";
	
	public static KeyStore loadKeyStore(String keystoreFilePath, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		FileInputStream fIn = new FileInputStream(keystoreFilePath);
		KeyStore keystore = KeyStore.getInstance("JKS");
		keystore.load(fIn, password);
		fIn.close();
		return keystore;
	}
	
	public static KeyPair getkeyPair(String alias, KeyStore keystore, char[] password ) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		
		Key key = keystore.getKey(alias, password);
	    if (key instanceof PrivateKey) {
	      // Get certificate of public key
	      Certificate cert = keystore.getCertificate(alias);

	      // Get public key
	      PublicKey publicKey = cert.getPublicKey();

	      // Return a key pair
	      return new KeyPair(publicKey, (PrivateKey) key);
	    }
		
		return null;
		
	}
	
	public static X509Certificate selfSign(KeyPair keyPair, String subjectDN) throws OperatorCreationException, CertificateException, IOException
	{
	    Provider bcProvider = new BouncyCastleProvider();
	    Security.addProvider(bcProvider);

	    long now = System.currentTimeMillis();
	    Date startDate = new Date(now);

	    X500Name dnName = new X500Name(subjectDN);
	    BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(startDate);
	    calendar.add(Calendar.YEAR, 1); // <-- 1 Yr validity

	    Date endDate = calendar.getTime();

	    //String signatureAlgorithm = "SHA256WithRSA"; // <-- Use appropriate signature algorithm based on your keyPair algorithm.

	    ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());

	    JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic());

	    // Extensions --------------------------

	    // Basic Constraints
	    BasicConstraints basicConstraints = new BasicConstraints(false); // <-- true for CA, false for EndEntity

	    certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

	    // -------------------------------------

	    return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
	}
	
}
