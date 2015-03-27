import org.osgi.framework.launch.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.felix.main.AutoProcessor;
import org.apache.felix.main.Main;


public class FelixLauncher
{
	private static Framework m_fwk = null;
	private static ServiceLoader<FrameworkFactory> fLoader = null;
	private static TCPServer tcpServer= null;

	public static void main(String[] args) throws Exception
	{
		//Load system properties.
		Main.loadSystemProperties();
		
		//Read configuration properties.
		Map<String, String> configProps = Main.loadConfigProperties();
	    if (configProps == null)
	    {
	        System.err.println("No " + "CONFIG_PROPERTIES_FILE_VALUE" + " found.");
	        configProps = new HashMap<String, String>();
	    }
	    
	    //Copy framework properties from the system properties.
	    Main.copySystemProperties(configProps);
	    
	    //Add a shutdown hook to clean stop the framework.
	    String enableHook = configProps.get("SHUTDOWN_HOOK_PROP");
	    if ((enableHook==null)||!enableHook.equalsIgnoreCase("false"))
	    {
	    	Runtime.getRuntime().addShutdownHook(new Thread("Felix shutdown hook"){
	    		public void run() 
	    		{
	    			try 
	    			{
						if (m_fwk!=null) 
						{
							m_fwk.stop();
							m_fwk.waitForStop(0);
						}
					} catch (Exception e) 
	    			{
						System.err.println("Error stopping framework:"+ e);
					}
	    		}
	    	});
	    }
	    
		try {
			//Create an instance and initialize the framework.
			//System.out.println(configProps);
			m_fwk = getFrameworkFactory().newFramework(configProps);
			m_fwk.init();
			AutoProcessor.process(configProps, m_fwk.getBundleContext());
			m_fwk.start();
			//start the TcpServer
			if(tcpServer == null){
				tcpServer = new TCPServer(configProps,m_fwk.getBundleContext());
			}
			m_fwk.waitForStop(0);
			System.exit(0);

		}
		catch (Exception e) {
			// TODO: handle exception
			System.err.println("could not create framework:"+ e);
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	private static FrameworkFactory getFrameworkFactory() throws Exception
	{
		fLoader = ServiceLoader.load(FrameworkFactory.class);
		while (fLoader.iterator().hasNext())
		{
			return fLoader.iterator().next();
		}
		
		throw new Exception("could not find framework factory");
		
	}

}



/**
		
URL url= Main.class.getClassLoader().getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory");
		if(url!=null)
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			try
			{
				for(String s = br.readLine();s!=null;s=br.readLine())
				{
					s=s.trim();
					if (s.length() > 0 && (s.charAt(0)!='#'))
					{
						return (FrameworkFactory) Class.forName(s).newInstance(); 
					}
				}
			} 
			finally
			{
				if (br!=null) br.close();
			}

		}
		
		throw new Exception("could not find framework factory");
*/