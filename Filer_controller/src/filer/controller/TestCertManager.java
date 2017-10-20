package filer.controller;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import javax.naming.InvalidNameException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;



public class TestCertManager {
	final static char[] controllerKeystorePassword = "7S+*96D+v{xTT*2g".toCharArray();
	final static char[] agentKeystorePassword = "test".toCharArray();
	final static String controllerKeystoreFilePath = "C:\\temp\\Filer_Controller\\cert\\filer.keystore";
	final static String controllerCAalias = "Filer-Internal-CA-01";
	//final static String agentSSalias = "Filer-default-internal";
	//final static String agentCASalias = "Filer-internal-signed";
	final static String signatureAlgorithm = "SHA256WithRSA";
	public static byte[] initAgentKeystore(String agentURL) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, OperatorCreationException, UnrecoverableKeyException, CMSException, InvalidNameException {
		
		KeyStore controllerKeystore = loadKeyStore(controllerKeystoreFilePath, controllerKeystorePassword);
		
		KeyStore agentKeystore = KeyStore.getInstance("JKS");
		agentKeystore.load( null, agentKeystorePassword );
		
		KeyPair agentKeypair = createKeypair();
		KeyPair controllerKeypair = getkeyPair(controllerCAalias, controllerKeystore, controllerKeystorePassword);
		
		//X509Certificate agentCert = selfSign(agentKeypair, "CN=questo.test");
		
		String agentSubjectDN = "CN=" + agentURL + ",OU=ATMR Applications,O=Axa Technology Mediterranean Region,L=Milan,ST=Italy,C=IT,E=andrea.fracasso@axa-tech.com";
		String agentIssuerDN = agentSubjectDN;
		
		X509Certificate agentCert = selfSign(agentKeypair.getPrivate(), agentKeypair.getPublic(), agentSubjectDN, agentIssuerDN, 4);
		
		java.security.cert.Certificate[] outChain = { agentCert };
		
		agentKeystore.setKeyEntry(agentURL + " (local)", agentKeypair.getPrivate() , agentKeystorePassword, outChain);
		
		agentKeystore.setCertificateEntry(controllerCAalias, controllerKeystore.getCertificate(controllerCAalias));
		
		X509Certificate cacert = (X509Certificate)controllerKeystore.getCertificate(controllerCAalias);
		
		String controllerIssuerDN = cacert.getSubjectX500Principal().getName();
		
		agentCert = selfSign(controllerKeypair.getPrivate(), agentKeypair.getPublic(), agentSubjectDN, controllerIssuerDN, 4);
		
		java.security.cert.Certificate[] outChain2 = { agentCert };
		
		agentKeystore.setCertificateEntry(agentURL + " (" + controllerCAalias + ")", outChain2[0]);
		
//		URL url = new URL("http://axatech-443159.axa-assicurazioni-it.intraxa:9999/ws/get?wsdl");
//		QName qname = new QName("http://agent.filer/", "InitFilerAgentImplService");
//		Service service = Service.create(url, qname);
//		InitFilerAgent remotesvc = service.getPort(InitFilerAgent.class);
//		//remotesvc.putKeyStore(keystore);
		
		
	    //String storeName= "C:\\temp\\Filer_Controller\\cert\\test_keystore.jks";
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    agentKeystore.store(outputStream, agentKeystorePassword);
	    outputStream.flush();
	    outputStream.close();
	    
	    //remotesvc.putKeyStore(outputStream.toByteArray());
		return outputStream.toByteArray();
		
	}
	
	private static KeyPair createKeypair() throws NoSuchAlgorithmException {
		// initialize key generator
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		keyGen.initialize(2048, random);
		
		// generate a keypair
		KeyPair pair = keyGen.generateKeyPair();
	
		return pair;
		
	}
	
	public static X509Certificate selfSign(PrivateKey privateKey, PublicKey publicKey, String subjectDN, String issuerDN, int validity) throws OperatorCreationException, CertificateException, IOException
	{
	    Provider bcProvider = new BouncyCastleProvider();
	    Security.addProvider(bcProvider);

	    long now = System.currentTimeMillis();
	    Date startDate = new Date(now);

	    X500Name issuer = new X500Name(issuerDN);
	    X500Name subject = new X500Name(subjectDN);
	    BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(startDate);
	    calendar.add(Calendar.YEAR, validity); // <-- 1 Yr validity

	    Date endDate = calendar.getTime();

	    ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(privateKey);

	    JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(issuer, certSerialNumber, startDate, endDate, subject, publicKey);
	    
	    // X509v3CertificateBuilder certGen = new X509v3CertificateBuilder(issuer, serial, from, to, agentCsr.getSubject(), agentCsr.getSubjectPublicKeyInfo());

	    // Extensions --------------------------

	    // Basic Constraints
	    BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

	    certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

	    // -------------------------------------

	    return new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
	}
	
	private static KeyStore loadKeyStore(String keystoreFilePath, char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
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
}
