package filer.agent;


import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;


	//Service Endpoint Interface
	@WebService
	@SOAPBinding(style = Style.RPC)
	//@XmlElementWrapper(name = "SvcFilerAgent")
	public interface SvcFilerAgent{

		@WebMethod String getVersionInfo();
		
	}
