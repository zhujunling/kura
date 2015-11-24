package org.eclipse.kura.services.test;

import java.io.IOException;
import java.util.Date;
import java.util.Dictionary;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.clock.ClockService;
import org.eclipse.kura.test.annotation.TestTarget;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class ClockServiceTest extends TestCase {
	private static final Logger s_logger = LoggerFactory.getLogger(ClockServiceTest.class);
	
	private static CountDownLatch dependencyLatch = new CountDownLatch(2);	// initialize with number of dependencies
	
	private static ConfigurationAdmin s_configAdmin;
	private static ClockService s_clockService;
	
	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		s_logger.info("Setting ConfigurationAdmin");
		s_configAdmin = configAdmin;
		dependencyLatch.countDown();
	}
	
	public void unsetConfigAdmin(ConfigurationAdmin configAdmin) {
		s_logger.info("Unsetting ConfigurationAdmin");
		s_configAdmin = null;
	}
	
	public void setClockService(ClockService clockService) {
		s_clockService = clockService;
		dependencyLatch.countDown();
	}
	
	public void unsetClockService(ClockService clockService) {
		s_clockService = null;
	}
	
	@BeforeClass
	public void setUp() {
		
		try {
			s_logger.info("Wait for latch...");
			// Wait for OSGi dependencies
			dependencyLatch.await(5, TimeUnit.SECONDS);
			s_logger.info("Done waiting for latch.");
			// Update ClockService
			s_logger.info("Getting ClockService config");
			Configuration clockConfig = s_configAdmin.getConfiguration("org.eclipse.kura.clock.ClockService");
			s_logger.info("Getting ClockService properties");
			Dictionary<String, Object> clockProps = clockConfig.getProperties();
			clockProps.put("enabled", true);
			s_logger.info("Updating properties");
			clockConfig.update(clockProps);
			s_logger.info("Done");

		} catch (InterruptedException e) {
			s_logger.info("Fail: " + e.getMessage());
			fail("OSGi dependencies unfulfilled");
		} catch (IOException e) {
			s_logger.info("Fail: " + e.getMessage());
			fail("Could not get ClockService configuration");
		} catch (Exception e) {
			s_logger.info("Fail, what: " + e.getMessage(), e);
		}
	}
	
	@TestTarget(targetPlatforms={TestTarget.PLATFORM_ALL})
	@Test
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
