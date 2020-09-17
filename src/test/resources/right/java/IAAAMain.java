package es.indra.iaaa;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdiameter.common.impl.validation.DictionaryImpl;

import es.indra.iaaa.capture.IAAAMessageListener;
import es.indra.iaaa.client.ClientsConfiguration;
import es.indra.iaaa.config.ConfigFile;
import es.indra.iaaa.config.ConfigResolver;
import es.indra.iaaa.config.snmp.TrapsOIDs.NotiFType;
import es.indra.iaaa.diameter.DiameterMainServer;
import es.indra.iaaa.dictionary.DiameterDictionaryUtil;
import es.indra.iaaa.dictionary.RadiusDictionaryUtil;
import es.indra.iaaa.http.HttpMainServer;
import es.indra.iaaa.jmx.AAAJmxServer;
import es.indra.iaaa.jmx.mBeans.call.MbCallPlugin;
import es.indra.iaaa.jmx.mBeans.diameter.MbDiameterStats;
import es.indra.iaaa.jmx.mBeans.logexp.MbLogExpressions;
import es.indra.iaaa.jmx.mBeans.meter.MbRateMeter;
import es.indra.iaaa.jmx.mBeans.policies.MbPluginsCounters;
import es.indra.iaaa.jmx.mBeans.queue.MbQueue;
import es.indra.iaaa.jmx.mBeans.ss.MbSSMainServer;
import es.indra.iaaa.jmx.mBeans.stateServer.MbStateServer;
import es.indra.iaaa.jmx.mBeans.stats.MbUsageStatistics;
import es.indra.iaaa.jmx.systemMonitor.DiskUsageMonitor;
import es.indra.iaaa.jmxcall.JMXCallMainServer;
import es.indra.iaaa.ldap.LdapMainServer;
import es.indra.iaaa.logexp.LogExpressions;
import es.indra.iaaa.policies.EngineMgrBasic;
import es.indra.iaaa.radius.RadiusMainServer;
import es.indra.iaaa.radius.dictionary.DictionaryManager;
import es.indra.iaaa.snmp.AAASnmpServer;
import es.indra.iaaa.ss.SSMainServer;
import es.indra.iaaa.status.IAAAStatusLogger;
import es.indra.iaaa.supervision.SupervisionManager;
import es.indra.iaaa.supervision.notificationSender.NotParametersConstants;
import es.indra.iaaa.syslog.SyslogMainServer;
import es.indra.iaaa.tasks.queues.QueueMgrFactory;
import es.tid.tre.trecommon.TRECommon;
import es.tid.tre.trecommon.TREFatalException;
import es.tid.tre.trecommon.proxy.IAAAProxySelector;

/**
 * 
 * WARNING: THIS CLASS MUST NOT CREATE ANY LOGGER BEFORE THE LOG4J2 HAS BEEN
 * CONFIGURED AS ONCE THE CONFIGURATION HAS BEEN LOADING CHANGING THE
 * CONFIGURATION DOES NOT WORK
 * 
 * @version $Id: IAAAMain.java 96161 2019-05-23 08:01:03Z jaquiles $
 * 
 */
public class IAAAMain {
	
	// The Logger, must not be instantiated until the log configuration has been
	// loaded
	// private static final transient Logger logger = null;

	// This program help file
	private static final String HELP_FILE = "iaaa.help.txt.old";

	// Properties that indicate the systems that should be started, they should
	// be exposed using jmx
	private static boolean radiusserver = false;
	private static boolean diameterserver = false;
	private static boolean sessionserver = false;
	private static boolean syslogserver = false;
	private static boolean httpserver = false;
	private static boolean ldapserver = false;
	private static boolean snmpenabled = false;
	private static boolean supervisionenabled = false;
	
	private static boolean radiusratemeter = false;
	private static boolean diameterratemeter = false;
	private static boolean syslogratemeter = false;
	private static boolean httpratemeter = false;
	private static boolean ldapratemeter = false;

	public static boolean isRadiusServer() { return radiusserver; }
	public static boolean isDiameterServer() { return diameterserver; }
	public static boolean isSessionServer() { return sessionserver; }
	public static boolean isSyslogServer() { return syslogserver; }
	public static boolean isHttpServer() { return httpserver; }
	public static boolean isLdapServer() { return ldapserver; }
	public static boolean isSnmpEnabled() { return snmpenabled; }
	public static boolean isSupervisionEnabled() { return supervisionenabled; }
	public static boolean isRadiusRateMeter() { return radiusratemeter; }
	public static boolean isDiameterRateMeter() { return diameterratemeter; }
	public static boolean isSyslogRateMeter() { return syslogratemeter; }
	public static boolean isHttpRateMeter() { return httpratemeter; }
	public static boolean isLdapRateMeter() { return ldapratemeter; }


	private static LogExpressions logExpressions;
	public static LogExpressions getLogExpressions() { return logExpressions; }
	
	protected IAAAMain() {
	}
	
	
	public static void main(String[] args) {

		
		// property -Dconsole=false to disable console (System.out)
		if (System.getProperty("console", "true").equalsIgnoreCase("false")) {
			System.setOut(new PrintStream(new OutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					// nothing
				}
			}));
		}
				
		System.out.println("Starting " + IAAA.getProductName() + " " + IAAA.getVersionString() + " (" + IAAA.getFirmwareVersion() + ")");
		
		System.out.println("JAVA HOME:" + System.getProperty("java.home"));
		System.out.println("JAVA VERSION:" + System.getProperty("java.version"));
		System.out.println("JAVA CLASSPATH:" + System.getProperty("java.class.path"));
		System.out.println("USER DIR:" + System.getProperty("user.dir"));
		
		// iAAA proxy
		IAAAProxySelector ps = IAAAProxySelector.getInstance();
		System.out.println("PROXY:\n");
		System.out.println(ps.toString());

		
		try {
			// Parse command line parameters
			final Map<String, String> argsMap = parseCommandLineArguments(args);

			if (argsMap.containsKey("-?") || argsMap.containsKey("/?") || argsMap.containsKey("-help")) {

				// Do not care to use trecommon that will instance a logger even
				// if the logger
				// is not configured because the program will exit
				final String helpFile = TRECommon.getResourceAsFileName(null, HELP_FILE);
				if (helpFile == null) {
					throw new TREFatalException(HELP_FILE + " not found.");
				}
				TRECommon.printTextFile(helpFile);

				return; // Program finish ok to show help
				
			} else {

				// The program will run , initialize and start

			}

			// Create main server instance
			IAAAMain iAAAMain = new IAAAMain();
			
			// Initialize configuration
			IAAAConfiguration.initialize(args);
			
			// Start server, Initialize and start needed management agents
			iAAAMain.start();

			// TODO - check needed daemons and shutdown hooks
			

		} catch (Throwable th) {
			String exceptionMsg = th.getMessage();
			if (exceptionMsg == null && th.getCause() != null) {
				exceptionMsg = th.getCause().getMessage();
			}
			String msg = "FATAL ERROR: " + th.getClass().getSimpleName() + ". " + exceptionMsg;
			LogManager.getLogger(IAAAMain.class).fatal(msg, th);
			LogManager.getLogger(IAAAMain.class).fatal("\n\n"+msg);
			th.printStackTrace();
			System.err.println("\n\n" + new Date() + " - " + msg);
			System.err.println("Exiting...");
			System.exit(-1);
		} 
	}

	
	private void start() throws Exception {

		// logger
		Logger logger = LogManager.getLogger(IAAAMain.class);
		
		logger.info(IAAA.getProductName() + " " + IAAA.getVersionString() + " (" + IAAA.getFirmwareVersion() + ")");
		logger.info("IAAAMain start()" 
					+ "\n    instance=" + IAAAConfiguration.getInstance().getInstanceName() 
					+ "\n    confurl=" + IAAAConfiguration.getInstance().getConfUrl()
					+ "\n    scope=" + IAAAConfiguration.getInstance().getScope()
					+ "\n    group=" + IAAAConfiguration.getInstance().getGroupName()
					+ "\n    environment=" + IAAAConfiguration.getInstance().getEnvironment());
		
		// Check what components should be started and start them
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("radiusserver")) {
			radiusserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusserver"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("diameterserver")) {
			diameterserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterserver"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("sessionserver")) {
			sessionserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("sessionserver"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("syslogserver")) {
			syslogserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogserver"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("httpserver")) {
			httpserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpserver"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("ldapserver")) {
			ldapserver = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapserver"));
		}
		
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("radiusratemeter")) {
			radiusratemeter = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusratemeter"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("diameterratemeter")) {
			diameterratemeter = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterratemeter"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("syslogratemeter")) {
			syslogratemeter = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogratemeter"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("httpratemeter")) {
			httpratemeter = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpratemeter"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("ldapratemeter")) {
			ldapratemeter = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapratemeter"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("snmpenabled")) {
			snmpenabled = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("snmpenabled"));
		}
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("supervisionenabled")) {
			supervisionenabled = Boolean.parseBoolean(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("supervisionenabled"));
		}
		
		if (IAAAConfiguration.getInstance().getIAAAProperties().getProperties().containsKey("statusloggermillis")) {
			IAAAStatusLogger.initialize(Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("statusloggermillis")));
		}
		
		logger.info("iAAA server:\n    radius: {} (rate:{}),\n    diameter: {} (rate:{}),\n    session: {},\n    syslog: {} (rate:{}),\n    http: {} (rate:{}),\n    ldap: {},\n    snmp: {},\n    supervision: {}",
				radiusserver, radiusratemeter?"on":"off",
				diameterserver, diameterratemeter?"on":"off",
				sessionserver, 
				syslogserver, syslogratemeter?"on":"off", 
				httpserver, httpratemeter?"on":"off",
				ldapserver,
				snmpenabled, 
				supervisionenabled);
		
		// Create jmx server instance
		try {
			// Start JMX server
			logger.info("Starting JMX server...");
			AAAJmxServer.createInstance(IAAAConfiguration.getInstance().getIAAAProperties().getProperties());
		} catch (TREFatalException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new TREFatalException("Error initializing jmx agent: " + e.toString());
		}
		
		// read clients configuration
		ClientsConfiguration.getInstance();
		
		// init diameter dictionary (mandatory for javascript use)
		String diameterDictDirectory = "/conf/dictionaries/diameter";
		String diameterDictFile = "diameter.dict";
		LogManager.getLogger(IAAAMain.class).info("Reading diameter dictionary: {}/{}", diameterDictDirectory, diameterDictFile);
		ConfigFile file = ConfigResolver.getInstance().getConfigFile(diameterDictDirectory, diameterDictFile);
		if (file == null) {
			String msg = "Cannot read diameter dictionary: " + diameterDictDirectory + "/" + diameterDictFile;
			logger.error(msg);
			throw new TREFatalException(msg);
		}
		StringBuilder confXml = new StringBuilder(file.readString(false, true));
		LogManager.getLogger(IAAAMain.class).info("Initializing diameter dictionary...");
		DictionaryImpl.getInstance(confXml);
		DiameterDictionaryUtil.getInstance();
		LogManager.getLogger(IAAAMain.class).info("Diameter dictionary loaded");

		// init radius dictionaries (mandatory, for javascript use)
		LogManager.getLogger(IAAAMain.class).info("Reading radius dictionaries...");
		DictionaryManager.getInstance();
		RadiusDictionaryUtil.getInstance();
		LogManager.getLogger(IAAAMain.class).info("Radius dictionaries loaded");

		if (ldapserver) {
			
			LogManager.getLogger(IAAAMain.class).info("Initializing LDAP server...");
			
			LdapMainServer ldapServer = LdapMainServer.getInstance();

			// ldap port
			int ldapPort = 389;
			try {
				ldapPort = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapport", "389"));
			} catch (Exception e) {
				String msg = "Error parsing LDAP port " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				throw new TREFatalException(msg);
			}
			ldapServer.setPort(ldapPort);

			// ldap server threads
			int ldapServerThreads = 10;
			try {
				ldapServerThreads = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapthreads", "10"));
			} catch (Exception e) {
				String msg = "Error parsing LDAP threads " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				throw new TREFatalException(msg);
			}
			ldapServer.setNumThreads(ldapServerThreads);
			
			String ldapbind = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapbind", null);
			if (ldapbind != null) {
				ldapServer.setBindAddress(ldapbind);
			}

			String workingDir = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapworkingdir", ".");
			if (workingDir != null) {
				ldapServer.setWorkingDir(workingDir);
			}
			
			int maxWaitForResponse = 1000;
			try {
				maxWaitForResponse = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ldapmaxwaitforresponse", "1000"));
			} catch (Exception e) {
				String msg = "Error parsing LDAP max wait for response: " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				throw new TREFatalException(msg);
			}
			ldapServer.setMaxWaitForResponse(maxWaitForResponse);

			// Start LDAP server
			LogManager.getLogger(IAAAMain.class).info("Starting LDAP server... port {}", ldapPort);
			System.out.println("STARTING LDAP server at: " + ldapbind + ":" + ldapPort);
			ldapServer.start();
			System.out.println("LDAP server STARTED");

		}

		
		//create and register call plugin mbean instance
		MbCallPlugin.getInstance();
		
		// create JMX queue
		JMXCallMainServer.getInstance();

		// create and register rate meter mbean instance
		MbRateMeter.getInstance();
		
		// create and register log expressions mbean instance
		MbLogExpressions.getInstance();

		// create and register mbean for mbean usage statistics
		MbUsageStatistics.getInstance();
		
		//create diskUsagemonitor instance
		DiskUsageMonitor.getInstance();
		
		// queue statistics and status by protocol
		MbQueue.getInstance();
		
		//create Snmp server instance.
		try {
			if (snmpenabled) {
				System.out.println("STARTING snmp server...");
				AAASnmpServer aaaSnmpServer = AAASnmpServer.createInstance(
						IAAAConfiguration.getInstance().getInstanceName(),
						IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("snmp.agent.address"),
						radiusserver,
						diameterserver);
				if (aaaSnmpServer == null) {
					throw new TREFatalException("Unable to initialize snmp agent properly.");
				}

				// Start SNMP server
				AAASnmpServer.startSnmpAgent();
				System.out.println("SNMP server STARTED");
			}

		} catch (TREFatalException e) {
			throw e;
		} catch (Exception e) {
			throw new TREFatalException("Error initializing snmp server. System will exit. Error detail: " + e.toString(), e);
		}
		
		// Initialize supervision
		try {
			if (supervisionenabled) {
				System.out.println("STARTING supervision manager...");
				SupervisionManager supervisionManager = SupervisionManager.getInstance();
				supervisionManager.start();
				System.out.println("Supervision manager STARTED");
			}
		} catch (TREFatalException e) {
			throw e;
		} catch (Exception e) {
			throw new TREFatalException("Error initializing supervision agent. System will exit." + " Error detail: " + e.toString(), e);
		}
		
		
		// LOG EXPRESSIONS
		logExpressions = new LogExpressions();
		System.out.println("Initial log expressions:");
		System.out.println(logExpressions);

		
		// initialize radius server
		if (radiusserver) {
			// Recover listen interface
			InetAddress radiusbind = null;
			
			String vRadiusbind = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusbind", null);
			if (vRadiusbind != null) {
				try {
					radiusbind = InetAddress.getByName(vRadiusbind);
				} catch (UnknownHostException e) {
					LogManager.getLogger(IAAAMain.class).error("Unknown Radius Bind Address: {}", vRadiusbind);
					throw new TREFatalException("Unknown Radius Bind Address: " + vRadiusbind);
				}
			}
			
			// Recover auth ant acct ports and buffer sizes
			int radiusauthport=1812;
			int radiusacctport=1813;
			int radiusdynport=3799;
			int radiusauthbuffer=1000000;
			int radiusacctbuffer=1000000;
			int radiusdynbuffer=1000000;
			try {
				radiusauthport = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusauthport", "1812"));
				radiusacctport = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusacctport", "1813"));
				radiusdynport = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusdynport", "3799"));
				radiusauthbuffer = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusauthbuffer", "1000000"));
				radiusacctbuffer = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusacctbuffer", "1000000"));
				radiusdynbuffer = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("radiusdynbuffer", "1000000"));
			} catch (Exception e) {
				LogManager.getLogger(IAAAMain.class).warn("Error parsing radius auth and acct ports and buffers, assign defaults. " + e.toString());
				radiusbind = null;
				radiusauthport = 1812;
				radiusacctport=1813;
				radiusdynport=3799;
				radiusauthbuffer=1000000;
				radiusacctbuffer=1000000;
				radiusdynbuffer=1000000;
			}
						
			LogManager.getLogger(IAAAMain.class).info("Starting radius server...");
			System.out.println("STARTING radius server at " + (radiusbind!=null?radiusbind:"ALL") + ":" + radiusauthport + ":" + radiusacctport + ":" + radiusdynport);
			RadiusMainServer radiusServer = RadiusMainServer.getInstance();		
			if (radiusServer == null) {
				throw new TREFatalException("Error initializing iAAA. System will exit.");
			}
			if (radiusbind != null) {
				radiusServer.setListenAddress(radiusbind);
				LogManager.getLogger(IAAAMain.class).info("Radius started on address: {}",radiusbind);
			} else {
				LogManager.getLogger(IAAAMain.class).info("Radius started on ALL addresses.");
			}
			
			radiusServer.setAuthPort(radiusauthport);
			radiusServer.setAcctPort(radiusacctport);
			radiusServer.setDynPort(radiusdynport);
			radiusServer.setAuthBufferSize(radiusauthbuffer);
			radiusServer.setAcctBufferSize(radiusacctbuffer);
			radiusServer.setDynBufferSize(radiusdynbuffer);
			radiusServer.start(true, true, true);

			// registro los MBeans en el servidor
			//MbRadiusServerAcct mbRadiusServerAcct = new MbRadiusServerAcct(radiusAccClientTableMap, countersArray);
			//AAAJmxServer.getInstance().registerMbean(mbRadiusServerAcct);

			
			LogManager.getLogger(IAAAMain.class).info("Radius started on ports (auth, acct, dyn): ({}, {}, {} )", radiusauthport, radiusacctport, radiusdynport);
			System.out.println("Radius server STARTED");
		}

		if (diameterserver) {
			// init the diameter dictionary
			//LogManager.getLogger(IAAAMain.class).info("Reading diameter dictionary...");
			//ConfigFile file = ConfigResolver.getInstance().getConfigFile("conf/dictionaries/diameter", "diameter.dict");
			//StringBuilder confXml = new StringBuilder(file.readString(true));
			//LogManager.getLogger(IAAAMain.class).info("Initializing diameter dictionary...");
			//DictionaryImpl.getInstance(confXml);
			//LogManager.getLogger(IAAAMain.class).info("Diameter dictionary initialized");

			String fqdn = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterfqdn", "");
			String realm = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterrealm", "");
			String port = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterport", "");
			String bind = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("diameterbind", "");

			LogManager.getLogger(IAAAMain.class).info("Starting diameter server... FQDN: {}, Realm: {}, Port: {}, Bind address: {}", fqdn, realm, port, bind!=null && bind.length()>0 ? bind : "ALL");
			System.out.println("STARTING diameter server... FQDN:" + fqdn + ", Realm:" + realm + ", Port:" + port + ", Bind address:" + bind);
			DiameterMainServer diameterServer = DiameterMainServer.getInstance();
			diameterServer.start();
			LogManager.getLogger(IAAAMain.class).info("Diameter server started");
			System.out.println("Diameter server started");
			
			//create and register diameter statistics mbean instance
			MbDiameterStats.getInstance();

		}
		
		if (sessionserver) {
			
			String redisManagement = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("redismanagement", "false");
			String ssConfigFile = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ssconfigfile", "/methods/conf/iaaa.ss.properties");
			String redisDirectory = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("redisdirectory", ".");
			String redisExe = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("redisexe", "redis-server");
			String redisConf = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("redisconf", "redis.conf");
			String ssGroup = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ssgroup", "none");
			int ssExpireTimeout = 2000;
			try {
				ssExpireTimeout = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ssexpiretimeout", "2000"));				
			} catch (Exception e) {
				LogManager.getLogger(IAAAMain.class).warn("Error parsing ssexpiretimeout, assign default. " + e.toString());
				ssExpireTimeout = 2000;
			}
			int ssMaxExpireSessions = 1000;
			try {
				ssMaxExpireSessions = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("ssexpiretimeout", "2000"));				
			} catch (Exception e) {
				LogManager.getLogger(IAAAMain.class).warn("Error parsing ssexpiremaxsessions, assign default. " + e.toString());
				ssMaxExpireSessions = 1000;
			}
						
			// Start SS server
			LogManager.getLogger(IAAAMain.class).info("Starting SS server... Group: {}, Redis configuration file: {}, SS configuration file: {}", ssGroup, redisConf, ssConfigFile);
			System.out.println("STARTING session server... Group: " + ssGroup + ", Redis configuration file: " + redisConf + ", SS configuration file: " + ssConfigFile);
			SSMainServer ssServer = SSMainServer.getInstance();
			
			if (ssServer == null) {
				throw new TREFatalException("Error initializing SS server. System will exit.");
			}
			ssServer.setExpireMaxSessions(ssMaxExpireSessions);
			ssServer.setExpireTimeout(ssExpireTimeout);
			ssServer.setRedisDirectory(redisDirectory);
			ssServer.setRedisConfig(redisConf);
			ssServer.setRedisExe(redisExe);
			ssServer.setGroup(ssGroup);
			ssServer.setSSConfigFile(ssConfigFile);
			ssServer.setRedisManagement("true".equals(redisManagement));
			
			// start session server
			ssServer.start();
			
			LogManager.getLogger(IAAAMain.class).info("SS server started");
			System.out.println("Session server STARTED");
			
			try {
				System.out.println("Registering " + MbSSMainServer.JMX_OBJECT_NAME);
				// create mbean instance.
				MbSSMainServer mbean = new MbSSMainServer(ssServer);
				// register mbean
				AAAJmxServer.getInstance().registerMbean(mbean);				
			} catch (Exception e) {
				System.err.println("Cannot register mbean: " + MbSSMainServer.JMX_OBJECT_NAME);
			}			

			try {
				System.out.println("Registering " + MbStateServer.JMX_OBJECT_NAME);
				// create mbean instance.
				MbStateServer mbean = new MbStateServer();
				// register mbean
				AAAJmxServer.getInstance().registerMbean(mbean);				
			} catch (Exception e) {
				System.err.println("Cannot register mbean: " + MbStateServer.JMX_OBJECT_NAME);
			}			

			// Initialize iAAA message listener
			IAAAMessageListener.getInstance();
			
			LogManager.getLogger(IAAAMain.class).info("SS server started: ({}, {}, {} )", redisDirectory, redisExe, redisConf);					

		}
		
		
		if (syslogserver) {

			// Syslog udp buffer size
			int syslogbuffer = 0;
			try {
				syslogbuffer = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogbuffer", null));
			} catch (Exception e) {
				String msg = "Error parsing Syslog UDP buffer " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				syslogbuffer = 0;
			}

			// Syslog udp max packet size
			int syslogmaxpacketsize = 10000;
			try {
				syslogmaxpacketsize = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogmaxpacketsize", null));
			} catch (Exception e) {
				String msg = "Error parsing Syslog UDP max packet size " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				syslogmaxpacketsize = 0;
			}

			// Syslog port list
			String str = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogport", null);
			if (str == null || str.length() == 0) {
				String msg = "Syslog port undefined";
				LogManager.getLogger(IAAAMain.class).error(msg);
				throw new TREFatalException(msg);
			}
			

			StringTokenizer st = new StringTokenizer(str, ",");
			while (st.hasMoreElements()) {

				int syslogport = 0;
				String p = st.nextToken();
				try {
					syslogport = Integer.parseInt(p);
				} catch (Exception e) {
					String msg = "Error parsing Syslog port " + p +". " + e.toString();
					LogManager.getLogger(IAAAMain.class).error(msg);
					throw new TREFatalException(msg);
				}

				// listen interface
				InetSocketAddress syslogbind = null;
				
				String syslogbindStr = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("syslogbind", null);
				if (syslogbindStr != null) {
					syslogbind = new InetSocketAddress(syslogbindStr, syslogport);
					if (syslogbind.isUnresolved()) {
						String msg = "Unresolved Syslog bind address: " + syslogbindStr;
						LogManager.getLogger(IAAAMain.class).error(msg);
						throw new TREFatalException(msg);					
					}
				}

				// Start Syslog server
				LogManager.getLogger(IAAAMain.class).info("Starting Syslog server... Port: {}, Address: {}", syslogport, (syslogbind!=null?syslogbindStr:"ALL"));
				System.out.println("STARTING Syslog server at port: " + syslogport + ", bind address: " + (syslogbind!=null?syslogbindStr:"ALL"));
				SyslogMainServer syslogServer = SyslogMainServer.getInstance(syslogport);
				if (syslogServer == null) {
					throw new TREFatalException("Error initializing Syslog server. System will exit.");
				}
				
				if (syslogbind != null) {
					LogManager.getLogger(IAAAMain.class).info("Syslog listens on address: {}", syslogbind);
					syslogServer.setListenAddress(syslogbind);
				} else {
					LogManager.getLogger(IAAAMain.class).info("Syslog listens on ALL addresses, port {}", syslogport);
				}
				syslogServer.setListenPort(syslogport);
				
				if (syslogbuffer > 0) {
					syslogServer.setBufferSize(syslogbuffer);
				}

				if (syslogmaxpacketsize > 0) {
					syslogServer.setMaxPacketSize(syslogmaxpacketsize);
				}

				LogManager.getLogger(IAAAMain.class).info("Starting Syslog server...");
				syslogServer.start();
				if (syslogbind != null) {
					LogManager.getLogger(IAAAMain.class).info("Syslog server started. Listening on address {}", syslogbind);
				} else {
					LogManager.getLogger(IAAAMain.class).info("Syslog server started. Listening on ALL addresses, port {}", syslogport);				
				}
				System.out.println("Syslog server STARTED");
				
			}

		}

		if (httpserver) {
			
			LogManager.getLogger(IAAAMain.class).info("Initializing HTTP server...");
			
			// HTTP port
			int httpport = 0;
			try {
				httpport = Integer.parseInt(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpport", null));
			} catch (Exception e) {
				String msg = "Error parsing HTTP port " + e.toString();
				LogManager.getLogger(IAAAMain.class).warn(msg);
				httpport = 8080;
			}
			
			InetSocketAddress httpbind = null;
			String httpbindStr = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpbind", null);
			if (httpbindStr != null) {
				httpbind = new InetSocketAddress(httpbindStr, httpport);
				if (httpbind.isUnresolved()) {
					String msg = "Unresolved HTTP Bind Address: " + httpbindStr;
					LogManager.getLogger(IAAAMain.class).error(msg);
					throw new TREFatalException(msg);					
				}
			}
			
			// Start HTTP server
			LogManager.getLogger(IAAAMain.class).info("Initializing HTTP server... port {}", httpport);
			System.out.println("STARTING HTTP server at port: " + httpport);
			HttpMainServer httpServer = HttpMainServer.getInstance();
			try {
				// listen port
				logger.info("HTTP server listen port {}", httpport);
				httpServer.setListenPort(httpport);
			} catch (Exception e) {
				String msg = "Cannot set http address: " + httpbind + ", port: " + httpport;
				logger.error(msg);
				throw new TREFatalException(msg);
			}
			if (httpbind != null) {
				httpServer.setBindAddress(httpbindStr);
			}
			
			// HTTPS
			if ("true".equalsIgnoreCase(IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpsenabled", "false"))) {
				String keystore = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpskeystore", null);
				String keyalias = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpskeyalias", null);
				String keypwd = IAAAConfiguration.getInstance().getIAAAProperties().getProperties().getProperty("httpskeypassword", null);
				httpServer.setHttps(keystore, keyalias, keypwd);
			};
			
			
			LogManager.getLogger(IAAAMain.class).info("Starting HTTP server...");
			httpServer.start();
			if (httpbind != null) {
				LogManager.getLogger(IAAAMain.class).info("HTTP server started. Listening on address {}, port {}", httpbind, httpport);
			} else {
				LogManager.getLogger(IAAAMain.class).info("HTTP server started. Listening on ALL addresses, port {}", httpport);				
			}

			System.out.println("HTTP server STARTED");

		} else {
			
			// If http queue is not created -> create it
			if (QueueMgrFactory.getQueue(QueueMgrFactory.HTTP_QUEUE) == null) {
				logger.info("Creating queue manager for HTTP messages...");
				QueueMgrFactory.createQueue(QueueMgrFactory.HTTP_QUEUE, EngineMgrBasic.getInstance(), HttpMainServer.CONFIG_DIR, HttpMainServer.CONFIG_FILENAME);
				logger.info("Queue manager for HTTP created OK");			
			}
			
		}

		
		
		//start notification
		try {
			String message = "Server instance "+IAAAConfiguration.getInstance().getInstanceName()+" started";			
			SupervisionManager.getInstance().sendNotification(NotiFType.INFO, NotParametersConstants.NOTIF_ID_START_AAA, message);
		} catch (TREFatalException e) {
			LogManager.getLogger(IAAAMain.class).error("Error sending start notification",e);
		}
		
		//stop notification
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	try {
		    		
		    		String message = "Server instance "+IAAAConfiguration.getInstance().getInstanceName()+" stoped";
		    		SupervisionManager.getInstance().sendNotification(NotiFType.INFO,NotParametersConstants.NOTIF_ID_STOP_AAA,message);
		    		
				} catch (TREFatalException e) {
					System.out.println("Error sending stop notification");
					e.printStackTrace();
				}
		    }
		});
		
		// register mbean for plugin stats
		LogManager.getLogger(IAAAMain.class).info("Registering MbPluginsCounters...");
		AAAJmxServer.getInstance().registerMbean(new MbPluginsCounters());
		LogManager.getLogger(IAAAMain.class).info("MbPluginsCounters registered.");
		
		System.out.println("iAAA started");

	}

	
	/**
	 * Generic method to parse command line arguments with - to map. TRECommon
	 * class not used to avoid the log problem
	 */
	public static Map<String, String> parseCommandLineArguments(
			final String[] args) {

		final Map<String, String> arguments = new HashMap<String, String>(
				args.length);
		int argModifierIdx = 0;
		int argValueIdx = argModifierIdx + 1;

		while (argModifierIdx < args.length) {

			if (args[argModifierIdx].charAt(0) == '-') {

				if (args.length > argValueIdx
						&& args[argValueIdx].charAt(0) != '-') {
					arguments.put(args[argModifierIdx], args[argValueIdx]);
					argModifierIdx++;
				} else { // Parameter is a flag
					arguments.put(args[argModifierIdx], "true");
				}

			} // if not ignore parameter

			argModifierIdx++;
			argValueIdx = argModifierIdx + 1;

		}

		return arguments;

	}

}
