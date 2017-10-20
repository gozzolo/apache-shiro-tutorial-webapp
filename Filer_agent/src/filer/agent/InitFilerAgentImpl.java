package filer.agent;

import java.io.IOException;
import java.util.Properties;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.apache.log4j.Logger;


//Service Implementation
@MTOM
@WebService(endpointInterface = "filer.agent.InitFilerAgent")
public class InitFilerAgentImpl implements InitFilerAgent {

	final static Logger logger = Logger.getLogger(InitFilerAgentImpl.class);
	Properties prop = FilerConfig.getConfig();
	
	
	@Override
	public void putKeyStore(byte[] keystore) throws IOException  {
		if(logger.isDebugEnabled()){
			logger.debug("uploading KeyStore");
		}
		
		new CertManagerAgent().initKeyStore(keystore);
		
		//return CertManager.initFiler(prop);
		
	}
}
