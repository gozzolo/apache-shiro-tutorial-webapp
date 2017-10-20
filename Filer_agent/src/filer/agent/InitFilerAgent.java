package filer.agent;

import java.io.IOException;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;


	//Service Endpoint Interface
	@WebService
	@SOAPBinding(style = Style.RPC)
	//@XmlElementWrapper(name = "InitFilerAgent")
	public interface InitFilerAgent{

		@WebMethod void putKeyStore(byte[] keystore) throws IOException;
		
	}
