package filer.agent;

import java.util.Properties;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

import org.apache.log4j.Logger;


//Service Implementation
@MTOM
@WebService(endpointInterface = "filer.agent.SvcFilerAgent")
public class SvcFilerAgentImpl implements SvcFilerAgent {

	final static Logger logger = Logger.getLogger(SvcFilerAgentImpl.class);
	Properties prop = FilerConfig.getConfig();
	
	
	@Override
	public String getVersionInfo(){
		if(logger.isDebugEnabled()){
			logger.debug("uploading KeyStore");
		}
		return "version 1";
		
		
		
		//return CertManager.initFiler(prop);
		
	}
}
