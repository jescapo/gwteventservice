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
package de.novanic.eventservice.client.connection.strategy.connector.streaming;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.*;
import de.novanic.eventservice.client.connection.strategy.connector.ConnectionStrategyClientConnector;
import de.novanic.eventservice.client.event.DomainEvent;
import de.novanic.eventservice.client.event.RemoteEventServiceRuntimeException;
import de.novanic.eventservice.client.event.listener.EventNotification;
import de.novanic.eventservice.client.event.service.EventService;
import de.novanic.eventservice.client.event.service.EventServiceAsync;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sstrohschein
 *         <br>Date: 30.04.2010
 *         <br>Time: 18:21:17
 */
public class GwtTestGWTStreamingClientConnector extends GWTTestCase
{
    private EventServiceAsync myEventService;

    public void gwtSetUp() {
        myEventService = (EventServiceAsync)getService(GWT.create(EventService.class), "gwteventservice");
    }

    public String getModuleName() {
        return "de.novanic.eventservice.GWTEventService";
    }

    public void testInit() {
        ConnectionStrategyClientConnector theGWTStreamingClientConnector = new GWTStreamingClientConnector();

        assertFalse(theGWTStreamingClientConnector.isInitialized());
        theGWTStreamingClientConnector.init(myEventService);
        assertTrue(theGWTStreamingClientConnector.isInitialized());
    }

    public void testListen() {
        ConnectionStrategyClientConnector theGWTStreamingClientConnector = new GWTStreamingClientConnector();
        theGWTStreamingClientConnector.init(myEventService);
        theGWTStreamingClientConnector.listen(new DummyEventNotification(), new AsyncCallback<List<DomainEvent>>() {
            public void onSuccess(List<DomainEvent> aDomainEvents) {}

            public void onFailure(Throwable aThrowable) {}
        });
    }

    public void testListen_2() throws Exception {
        DummyEventNotification theDummyEventNotification = new DummyEventNotification();

        assertEquals(0, theDummyEventNotification.getOccurredDomainEvents().size());

        GWTStreamingClientConnector theGWTStreamingClientConnector = new GWTStreamingClientConnector();
        theGWTStreamingClientConnector.init(myEventService);
        theGWTStreamingClientConnector.listen(theDummyEventNotification, new DummyListenAsyncCallback());

        assertEquals(0, theDummyEventNotification.getOccurredDomainEvents().size());

        theGWTStreamingClientConnector.receiveEvent("[4,3,2,1,[\"de.novanic.eventservice.client.event.DefaultDomainEvent/3924906731\",\"de.novanic.eventservice.client.event.domain.DefaultDomain/240262385\",\"test_domain\",null],0,5]");
        theGWTStreamingClientConnector.listen(theDummyEventNotification, new DummyListenAsyncCallback());

        assertEquals(1, theDummyEventNotification.getOccurredDomainEvents().size());
        assertNotNull(theDummyEventNotification.getOccurredDomainEvents().get(0));
        assertNotNull(theDummyEventNotification.getOccurredDomainEvents().get(0).getDomain());
        assertNull(theDummyEventNotification.getOccurredDomainEvents().get(0).getEvent());
        assertEquals(1, theDummyEventNotification.getOccurredDomainEvents().size());
    }

    public void testListen_Error() throws Exception {
        DummyEventNotification theDummyEventNotification = new DummyEventNotification();

        assertEquals(0, theDummyEventNotification.getOccurredDomainEvents().size());

        GWTStreamingClientConnector theGWTStreamingClientConnector = new GWTStreamingClientConnector();
        theGWTStreamingClientConnector.init(myEventService);
        theGWTStreamingClientConnector.listen(theDummyEventNotification, new DummyListenAsyncCallback());

        assertEquals(0, theDummyEventNotification.getOccurredDomainEvents().size());

        try {
            theGWTStreamingClientConnector.receiveEvent("[4,3,2,1,[\"de.novanic.eventservice.client.event.DefaultDomainEvent/3924906731\",\"de.novanic.eventservice.client.event.domain.DefaultDomain/240262385\",\"test_domain\",\"does.not.exist.DummyEventWithoutPackage\"],0,5]");
            fail("Exception expected, because the event can not be de-serialized / instantiated!");
        } catch(RemoteEventServiceRuntimeException e) {
            assertTrue(e.getCause() instanceof SerializationException);
        }
    }

    private ServiceDefTarget getService(Object aService, String aServiceMappingName) {
        String theServiceURL = GWT.getModuleBaseURL() + aServiceMappingName;
        ServiceDefTarget theServiceEndPoint = (ServiceDefTarget)aService;
        theServiceEndPoint.setServiceEntryPoint(theServiceURL);
        return theServiceEndPoint;
    }

    private class DummyEventNotification implements EventNotification
    {
        private List<DomainEvent> myOccurredDomainEvents = new ArrayList<DomainEvent>();

        public void onNotify(DomainEvent aDomainEvent) {
            myOccurredDomainEvents.add(aDomainEvent);
        }

        public void onAbort() {}

        public List<DomainEvent> getOccurredDomainEvents() {
            return myOccurredDomainEvents;
        }
    }

    private class DummyListenAsyncCallback implements AsyncCallback<List<DomainEvent>>
    {
        public void onSuccess(List<DomainEvent> aDomainEvents) {}

        public void onFailure(Throwable aThrowable) {
            fail("onFailure wasn't expected!");
        }
    }
}