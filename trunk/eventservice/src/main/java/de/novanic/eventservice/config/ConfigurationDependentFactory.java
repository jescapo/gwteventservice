/*
 * GWTEventService
 * Copyright (c) 2010, GWTEventService Committers
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

import de.novanic.eventservice.service.connection.id.ConnectionIdGenerator;
import de.novanic.eventservice.service.connection.strategy.connector.ConnectionStrategyServerConnector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The {@link de.novanic.eventservice.config.ConfigurationDependentFactory} can create instances from a configuration
 * which can be configured with a class name (for example {@link de.novanic.eventservice.config.ConfigParameter#CONNECTION_ID_GENERATOR}.
 * The created instances are hold as a singleton.
 *
 * @author sstrohschein
 *         <br>Date: 05.04.2010
 *         <br>Time: 13:55:51
 */
public final class ConfigurationDependentFactory
{
    private static EventServiceConfiguration myConfiguration;

    private ConnectionIdGenerator myConnectionIdGenerator;
    private ConnectionStrategyServerConnector myConnectionStrategyServerConnector;

    /**
     * Initializes the {@link de.novanic.eventservice.config.ConfigurationDependentFactory}. That constructor is only called one time,
     * because the factory is created as a singleton.
     */
    private ConfigurationDependentFactory() {
        init();
    }

    /**
     * Factory-Holder class to ensure thread-safe lazy-loading with IODH.
     */
    private static class ConfigurationDependentFactoryHolder {
        private static ConfigurationDependentFactory INSTANCE = new ConfigurationDependentFactory();
    }

    /**
     * This method should be used to create an instance of {@link de.novanic.eventservice.config.ConfigurationDependentFactory}.
     * {@link de.novanic.eventservice.config.ConfigurationDependentFactory} is a singleton, so this method returns always the same instance of
     * {@link de.novanic.eventservice.config.ConfigurationDependentFactory}. This method initializes the factory with a configuration and should be
     * called before {@link ConfigurationDependentFactory#getInstance()} is used, because it has to be initialized with
     * with a configuration at first.
     * @return {@link de.novanic.eventservice.config.ConfigurationDependentFactory} (singleton)
     */
    public static ConfigurationDependentFactory getInstance(EventServiceConfiguration aConfiguration) {
        if(myConfiguration == null) {
            myConfiguration = aConfiguration;
        }
        return ConfigurationDependentFactoryHolder.INSTANCE;
    }

    /**
     * This method returns the {@link de.novanic.eventservice.config.ConfigurationDependentFactory} as a singleton. It has to be initialized
     * with a configuration at first. Therefore the method {@link de.novanic.eventservice.config.ConfigurationDependentFactory#getInstance(EventServiceConfiguration)} should
     * be used at first.
     * @return {@link de.novanic.eventservice.config.ConfigurationDependentFactory} (singleton)
     */
    public static ConfigurationDependentFactory getInstance() {
        if(myConfiguration == null) {
            throw new ConfigurationException(ConfigurationDependentFactory.class.getName() + " has to be initialized with a configuration before!");
        }
        return ConfigurationDependentFactoryHolder.INSTANCE;
    }

    /**
     * Creates the configured instances from the configuration.
     */
    private void init() {
        if(myConfiguration != null) {
            //TODO optimize the class cast handling
            try {
                myConnectionIdGenerator = createObject(myConfiguration.getConnectionIdGeneratorClassName());
            } catch(ClassCastException e) {
                throw new ConfigurationException(myConfiguration.getConnectionIdGeneratorClassName() + " should have another type!", e);
            }
            try {
                myConnectionStrategyServerConnector = createObject(myConfiguration.getConnectionStrategyServerConnectorClassName());
            } catch(ClassCastException e) {
                throw new ConfigurationException(myConfiguration.getConnectionStrategyServerConnectorClassName() + " should have another type!", e);
            }
        } else {
            throw new ConfigurationException(ConfigurationDependentFactory.class.getName() + " was initialized without a configuration!");
        }
    }

    /**
     * Returns the configured {@link de.novanic.eventservice.service.connection.id.ConnectionIdGenerator}.
     * @see de.novanic.eventservice.config.ConfigParameter#CONNECTION_ID_GENERATOR
     * @return the configured {@link de.novanic.eventservice.service.connection.id.ConnectionIdGenerator}
     */
    public ConnectionIdGenerator getConnectionIdGenerator() {
        return myConnectionIdGenerator;
    }

    /**
     * Returns the server side part / connector of the configured connection strategy.
     * @see de.novanic.eventservice.config.ConfigParameter#CONNECTION_STRATEGY_SERVER_CONNECTOR
     * @return server side part / connector of the configured connection strategy
     */
    public ConnectionStrategyServerConnector getConnectionStrategyServerConnector() {
        return myConnectionStrategyServerConnector;
    }

    /**
     * Creates and initializes an object of a specific type.
     */
    private static <T> T createObject(String aClassName) {
        //when no class is configured, no object is created
        if(aClassName == null) {
            return null;
        }

        try {
            final Class theConnectionIdGeneratorClass = Class.forName(aClassName);

            Constructor<T> theDefaultConstructor = null;
            for(Constructor<T> theConstructor: theConnectionIdGeneratorClass.getDeclaredConstructors()) {
                Class<?>[] theParameterTypes = theConstructor.getParameterTypes();
                switch(theParameterTypes.length) {
                    case 0:
                        theDefaultConstructor = theConstructor;
                        break;
                    case 1:
                        if(theParameterTypes[0].equals(EventServiceConfiguration.class)) {
                            return theConstructor.newInstance(myConfiguration);
                        }
                }
            }

            if(theDefaultConstructor == null) {
                throw new ConfigurationException("The class \"" + theConnectionIdGeneratorClass + "\" has no default constructor and no constructor which requires a single configuration! At least one of both is needed!");
            }
            return theDefaultConstructor.newInstance();
        } catch(ClassNotFoundException e) {
            throw new ConfigurationException(aClassName + " couldn't be instantiated!", e);
        } catch(InstantiationException e) {
            throw new ConfigurationException(aClassName + " couldn't be instantiated!", e);
        } catch(IllegalAccessException e) {
            throw new ConfigurationException(aClassName + " couldn't be instantiated!", e);
        } catch(InvocationTargetException e) {
            throw new ConfigurationException(aClassName + " couldn't be instantiated!", e);
        }
    }

    /**
     * Returns the configuration which was specified with the initialization of the {@link de.novanic.eventservice.config.ConfigurationDependentFactory}.
     * @see {@link ConfigurationDependentFactory#getInstance(EventServiceConfiguration)}
     * @return configuration which was specified with the initialization of the {@link de.novanic.eventservice.config.ConfigurationDependentFactory}
     */
    public static EventServiceConfiguration getConfiguration() {
        return myConfiguration;
    }

    /**
     * Resets the {@link de.novanic.eventservice.config.ConfigurationDependentFactory} and re-initializes it
     * with a new configuration.
     * @param aConfiguration new configuration
     */
    public void reset(EventServiceConfiguration aConfiguration) {
        reset(aConfiguration, true);
    }

    /**
     * Resets the {@link de.novanic.eventservice.config.ConfigurationDependentFactory} and can automatically re-initializes it
     * with a new configuration.
     * @param aConfiguration new configuration
     * @param isReInit if is the factory should be re-initialized with the new factory
     */
    protected void reset(EventServiceConfiguration aConfiguration, boolean isReInit) {
        myConfiguration = aConfiguration;
        if(isReInit) {
            init();
        }
    }
}