package org.eclipse.kura.services.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.clock.ClockService;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.util.ProcessUtil;
import org.eclipse.kura.core.util.SafeProcess;
import org.eclipse.kura.test.annotation.TestTarget;
import org.eclipse.kura.test.utility.ControlFileUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClockServiceTest {
	private static final Logger s_logger = LoggerFactory.getLogger(ClockServiceTest.class);
	
	private static final String CLOCK_SERVICE_PID = "org.eclipse.kura.clock.ClockService";
	private static final String MODIFIED_DATE = "20171230";
	private static final String EXPECTED_HW_DATE = "2017Dec30";
	private static final String DEFAULT_CONTROL_LINE = "ClockServiceTest:false:1";
	
	private static CountDownLatch dependencyLatch = new CountDownLatch(2);	// initialize with number of dependencies
	
	private static ConfigurationService s_configService;
	private static ClockService 		s_clockService;
	
	private static Map<String, Object> s_testProps = new HashMap<String, Object>();
	private static ControlFileUtil s_controlFileUtil;
	
	
	// ----------------------------------------------------------------
	//
	// Dependencies
	//
	// ----------------------------------------------------------------
	
	public void setConfigurationService(ConfigurationService configService) {
		s_configService = configService;
		dependencyLatch.countDown();
	}
	
	public void unsetConfigurationService(ConfigurationService configService) {
		s_configService = null;
	}
	
	public void setClockService(ClockService clockService) {
		s_clockService = clockService;
		dependencyLatch.countDown();
	}
	
	public void unsetClockService(ClockService clockService) {
		s_clockService = null;
	}
	
	// ----------------------------------------------------------------
	//
	// Activation APIs
	//
	// ----------------------------------------------------------------
	
	protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
		s_controlFileUtil = ControlFileUtil.getInstance("/opt/eurotech/esf/tests/control_file");
		String[] props = getControlProps();
		if ((props == null) || !(props[0].startsWith("ClockServiceTest"))) {
			s_logger.info("Updating control file for new test.");
			s_controlFileUtil.writeControlLine(DEFAULT_CONTROL_LINE);
		}
		for (String s : s_testProps.keySet()) {
			s_logger.info("Property => " + s + ":" + s_testProps.get(s));
		}
		
	}
	
	// ----------------------------------------------------------------
	//
	// Private Methods
	//
	// ----------------------------------------------------------------
	
	private String[] getControlProps() {
		String controlLine = s_controlFileUtil.getControlLine();
		if ((controlLine == null) || !(controlLine.length() >1)) {
			return null;
		}
		return controlLine.split(":");
	}
	// ----------------------------------------------------------------
	//
	// JUnit
	//
	// ----------------------------------------------------------------
	
	@BeforeClass
	public static void setUp() {
		
		try {
			s_logger.info("Wait for latch...");
			// Wait for OSGi dependencies
			dependencyLatch.await(5, TimeUnit.SECONDS);
			s_logger.info("Done waiting for latch.");
			

		} catch (InterruptedException e) {
			s_logger.info("Fail: " + e.getMessage());
			fail("OSGi dependencies unfulfilled");
		} catch (Exception e) {
			s_logger.info("Fail, what: " + e.getMessage(), e);
		}
	}
	
	@Test
	public void DEN_CLOCK_001() {
		// Read properties from control file
		String[] testProps = getControlProps();
		int testNumber = Integer.parseInt(testProps[2]);
		boolean afterReboot = Boolean.parseBoolean(testProps[1]);
		
		assumeTrue(testNumber == 1);
		
		SafeProcess proc = null;
		BufferedReader br = null;
		
		// Start test from beginning if system has not rebooted.
		if (!afterReboot) {
			s_logger.info("Starting ClockServiceTest DEN_CLOCK_001");
			try {
				// Update ClockService
				s_logger.info("Disabling ClockService.");
				ComponentConfiguration clockConfig = s_configService.getComponentConfiguration(CLOCK_SERVICE_PID);
				Map<String, Object> clockProps = clockConfig.getConfigurationProperties();
				clockProps.put("enabled", false);
				s_configService.updateConfiguration(CLOCK_SERVICE_PID, clockProps);
				Thread.sleep(5000);
				
				// Set system time
				s_logger.info("Manually setting system clock.");
				proc = ProcessUtil.exec("date +%Y%m%d -s " + MODIFIED_DATE);
				proc.waitFor();
				s_controlFileUtil.writeControlLine("ClockServiceTest:true:1");
				Thread.sleep(5000);
				
				// Reboot
				s_logger.info("Rebooting");
				ProcessUtil.exec("reboot");
				Thread.sleep(50000);  // Long wait so results aren't logged before reboot

			} catch (KuraException e) {
				s_logger.error("Fail: " + e.getMessage());
				fail("KuraException");
			} catch (InterruptedException e) {
				s_logger.error("Fail: " + e.getMessage());
				fail("Interrupt Exception");
			} catch (IOException e) {
				s_logger.error("Fail: " + e.getMessage());
				fail("Error running Linux process");
			} finally {
				if (proc != null) ProcessUtil.destroy(proc);
			}
		}
		// Ensure date is still modified after reboot.
		else {
			s_logger.info("Continuing ClockServiceTest DEN_CLOCK_001 after reboot.");
			try {
				// Reset reboot flag and up tick test number
				s_controlFileUtil.writeControlLine("ClockServiceTest:false:2");
				Thread.sleep(1000);
				
				// Check the system clock
				s_logger.info("Checking that the system clock matches the manually set date.");
				proc = ProcessUtil.exec("date +%Y%m%d");
				proc.waitFor();
				br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String testSysClock = br.readLine();
				assertTrue(MODIFIED_DATE.equals(testSysClock));
				
				// Check the hardware clock
				// Formate of results should be: Day Month Day(numeric) Time Year 
				s_logger.info("Checking that the hardware clock matches the manually set date.");
				proc = ProcessUtil.exec("hwclock -r");
				proc.waitFor();
				br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String testHwClock = br.readLine();
				String[] testHwClockArray = testHwClock.split("\\s+");
				testHwClock = testHwClockArray[4] + testHwClockArray[1] + testHwClockArray[2];
				assertTrue(EXPECTED_HW_DATE.equals(testHwClock));
				
			} catch (InterruptedException e) {
				s_logger.error("Fail: " + e.getMessage());
				fail("Interrupt Exception");
			} catch (IOException e) {
				s_logger.error("Fail: " + e.getMessage());
				fail("Error running Linux process");
			} finally {
				if (proc != null) ProcessUtil.destroy(proc);
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						s_logger.error("Fail: " + e.getMessage());
						fail("Failed to close BufferedReader");
					}
				}
			}
			
		}
	}
	
	@TestTarget(targetPlatforms={TestTarget.PLATFORM_ALL})
	public void testIsSynced() {		
		Date lastSync;
		try {
			
			lastSync = s_clockService.getLastSync();
			assertTrue(lastSync instanceof Date);
		} catch (KuraException e) {
			fail("Clock is not synced");
		}
		
	}
	
	

}
