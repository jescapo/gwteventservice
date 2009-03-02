/*
 * GWTEventService
 * Copyright (c) 2008, GWTEventService Committers
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.novanic.eventservice.config;

import de.novanic.eventservice.config.loader.ConfigurationException;
import de.novanic.eventservice.config.loader.ConfigurationLoader;
import de.novanic.eventservice.EventServiceTestCase;
import de.novanic.eventservice.test.testhelper.factory.FactoryResetService;

/**
 * @author sstrohschein
 *         <br>Date: 23.10.2008
 *         <br>Time: 18:11:43
 */
public class EventServiceConfigurationFactoryTest extends EventServiceTestCase
{
    public void testInit() {
        EventServiceConfigurationFactory theEventServiceConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        assertSame(theEventServiceConfigurationFactory, EventServiceConfigurationFactory.getInstance());
    }

    public void testReset() {
        EventServiceConfigurationFactory theEventServiceConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        assertSame(theEventServiceConfigurationFactory, EventServiceConfigurationFactory.getInstance());

        FactoryResetService.resetFactory(EventServiceConfigurationFactory.class);
        assertNotSame(theEventServiceConfigurationFactory, EventServiceConfigurationFactory.getInstance());

        theEventServiceConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        assertSame(theEventServiceConfigurationFactory, EventServiceConfigurationFactory.getInstance());
    }

    public void testLoadEventServiceConfiguration() {
        //loads eventservice.properties
        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        EventServiceConfiguration theConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theConfiguration.getMinWaitingTime());
        assertEquals(20000, theConfiguration.getMaxWaitingTime());
        assertEquals(90000, theConfiguration.getTimeoutTime());
    }

    public void testLoadEventServiceConfiguration_2() {
        //loads eventservice.properties
        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        EventServiceConfiguration theConfiguration = theConfigurationFactory.loadEventServiceConfiguration(null);
        assertEquals(0, theConfiguration.getMinWaitingTime());
        assertEquals(20000, theConfiguration.getMaxWaitingTime());
        assertEquals(90000, theConfiguration.getTimeoutTime());
    }

    public void testLoadEventServiceConfiguration_3() {
        //loads eventservice.bak.properties
        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        EventServiceConfiguration theConfiguration = theConfigurationFactory.loadEventServiceConfiguration("eventservice.bak.properties");
        assertEquals(2000, theConfiguration.getMinWaitingTime());
        assertEquals(5000, theConfiguration.getMaxWaitingTime());
        assertEquals(50000, theConfiguration.getTimeoutTime());
    }

    public void testLoadEventServiceConfiguration_Default() {
        //uses DefaultConfigurationLoader
        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        EventServiceConfiguration theConfiguration = theConfigurationFactory.loadEventServiceConfiguration("notAnExistingFile");
        assertEquals(0, theConfiguration.getMinWaitingTime());
        assertEquals(20000, theConfiguration.getMaxWaitingTime());
        assertEquals(90000, theConfiguration.getTimeoutTime());
    }

    public void testLoadEventServiceConfiguration_Failure() {
        //loads eventservice_error.properties
        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        try {
            theConfigurationFactory.loadEventServiceConfiguration("eventservice_error.properties");
            fail("Exception expected!");
        } catch(ConfigurationException e) {}
    }

    public void testAddCustomConfigurationLoader() {
        //add custom ConfigurationLoader
        final EventServiceConfiguration theConfiguration = createConfiguration(0, 3000, 70000);

        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        final DummyConfigurationLoader theCustomConfigurationLoader = new DummyConfigurationLoader(theConfiguration);
        theConfigurationFactory.addCustomConfigurationLoader(theCustomConfigurationLoader);

        EventServiceConfiguration theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(3000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(70000, theLoadedConfiguration.getTimeoutTime());

        //remove custom ConfigurationLoader
        theConfigurationFactory.removeCustomConfigurationLoader(theCustomConfigurationLoader);

        theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(20000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(90000, theLoadedConfiguration.getTimeoutTime());
    }

    public void testResetCustomConfigurationLoaders() {
        //add custom ConfigurationLoader
        final EventServiceConfiguration theConfiguration = createConfiguration(0, 3000, 70000);

        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        final DummyConfigurationLoader theCustomConfigurationLoader = new DummyConfigurationLoader(theConfiguration);
        theConfigurationFactory.addCustomConfigurationLoader(theCustomConfigurationLoader);

        EventServiceConfiguration theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(3000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(70000, theLoadedConfiguration.getTimeoutTime());

        //reset
        FactoryResetService.resetFactory(EventServiceConfigurationFactory.class);
        theConfigurationFactory = EventServiceConfigurationFactory.getInstance();

        theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(20000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(90000, theLoadedConfiguration.getTimeoutTime());
    }

    public void testResetCustomConfigurationLoaders_2() {
        //add custom ConfigurationLoader
        final EventServiceConfiguration theConfiguration = createConfiguration(0, 3000, 70000);

        EventServiceConfigurationFactory theConfigurationFactory = EventServiceConfigurationFactory.getInstance();
        final DummyConfigurationLoader theCustomConfigurationLoader = new DummyConfigurationLoader(theConfiguration);
        theConfigurationFactory.addCustomConfigurationLoader(theCustomConfigurationLoader);

        EventServiceConfiguration theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(3000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(70000, theLoadedConfiguration.getTimeoutTime());

        //reset and re-init
        FactoryResetService.resetFactory(EventServiceConfigurationFactory.class);
        theConfigurationFactory = EventServiceConfigurationFactory.getInstance();

        theLoadedConfiguration = theConfigurationFactory.loadEventServiceConfiguration();
        assertEquals(0, theLoadedConfiguration.getMinWaitingTime());
        assertEquals(20000, theLoadedConfiguration.getMaxWaitingTime());
        assertEquals(90000, theLoadedConfiguration.getTimeoutTime());
    }

    private class DummyConfigurationLoader implements ConfigurationLoader
    {
        private EventServiceConfiguration myEventServiceConfiguration;

        private DummyConfigurationLoader(EventServiceConfiguration anEventServiceConfiguration) {
            myEventServiceConfiguration = anEventServiceConfiguration;
        }

        public boolean isAvailable() {
            return true;
        }

        public EventServiceConfiguration load() {
            return myEventServiceConfiguration;
        }
    }
}
