package org.kuali.rice.core.cxf.interceptors

import org.junit.Test
import org.kuali.rice.core.api.config.property.Config
import org.junit.Before
import org.kuali.rice.core.api.config.property.ConfigContext
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import org.kuali.rice.core.framework.config.property.SimpleConfig

/**
 * Ensures ServiceCallVersioningHelper is populating headers correctly
 */
class ServiceCallVersioningHelperTest {
    static final String FAKE_RICE_VERSION = "11.11-SNAPSHOT"
    static final String FAKE_ENV = "fake-env"
    static final String FAKE_APP_NAME = "fake-app"
    static final String FAKE_APP_VERSION = "99.99-SNAPSHOT"

    def config

    @Before
    void initConfig() {
        System.err.println(Thread.currentThread().getContextClassLoader())
        config = new SimpleConfig()
        config.putProperty(Config.RICE_VERSION, FAKE_RICE_VERSION)
        config.putProperty(Config.ENVIRONMENT, FAKE_ENV)
        ConfigContext.init(config)
    }

    @Test void testIAmSettingUpTheConfigCorrectly() {
        Config cfg = ConfigContext.getCurrentContextConfig()
        assertNotNull(cfg)
        assertEquals(FAKE_ENV, cfg.getEnvironment())
    }

    @Test void testOmitApplicationInfo() {
        System.err.println(Thread.currentThread().getContextClassLoader())
        HashMap<String, List<String>> headers = new HashMap<String, List<String>>()
        ServiceCallVersioningHelper.populateVersionHeaders(headers)

        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_VERSION_HEADER).contains(FAKE_RICE_VERSION))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_ENVIRONMENT_HEADER).contains(FAKE_ENV))
        assertEquals(null, headers.get(ServiceCallVersioningHelper.KUALI_APP_NAME_HEADER))
        assertEquals(null, headers.get(ServiceCallVersioningHelper.KUALI_APP_VERSION_HEADER))
    }

    @Test void testFallbackParams() {
        config.putProperty(Config.MODULE_NAME, FAKE_APP_NAME)
        config.putProperty(Config.VERSION, FAKE_APP_VERSION)

        HashMap<String, List<String>> headers = new HashMap<String, List<String>>()
        ServiceCallVersioningHelper.populateVersionHeaders(headers)

        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_VERSION_HEADER).contains(FAKE_RICE_VERSION))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_ENVIRONMENT_HEADER).contains(FAKE_ENV))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_APP_NAME_HEADER).contains(FAKE_APP_NAME))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_APP_VERSION_HEADER).contains(FAKE_APP_VERSION))
    }

    /**
     * Tests that the new application.name and application.version parameters take priority
     * I suppose this is really just a test of the Config object
     */
    @Test void testPreferredParams() {
        config.putProperty(Config.MODULE_NAME, "bogus value")
        config.putProperty(Config.VERSION, "bogus value")
        config.putProperty(Config.APPLICATION_NAME, FAKE_APP_NAME)
        config.putProperty(Config.APPLICATION_VERSION, FAKE_APP_VERSION)

        HashMap<String, List<String>> headers = new HashMap<String, List<String>>()
        ServiceCallVersioningHelper.populateVersionHeaders(headers)

        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_VERSION_HEADER).contains(FAKE_RICE_VERSION))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_RICE_ENVIRONMENT_HEADER).contains(FAKE_ENV))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_APP_NAME_HEADER).contains(FAKE_APP_NAME))
        assertTrue(headers.get(ServiceCallVersioningHelper.KUALI_APP_VERSION_HEADER).contains(FAKE_APP_VERSION))
    }
}