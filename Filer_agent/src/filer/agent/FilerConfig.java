package filer.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public class FilerConfig {
	public static Properties getConfig() {
		String installationPath = System.getProperty("filer.installationPath");
		Properties prop = new Properties();
		InputStream input = null;

		try {
			
			input = new FileInputStream(installationPath + File.separator + "conf" + File.separator + "config.properties");

			// load a properties file
			prop.load(input);
			input.close();
			prop.setProperty("log4j.appender.file.File", System.getProperty("filer.installationPath") + File.separator + "logs" + File.separator + "filer.log");
			prop.setProperty("log4j.rootLogger", prop.getProperty("log4j.rootLogger") + ", file" );
			PropertyConfigurator.configure(prop);
			return prop;


		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;

	}
	
	
}
