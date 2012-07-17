/*
 * GWTEventService
 * Copyright (c) 2011 and beyond, strawbill UG (haftungsbeschränkt)
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 * Other licensing for GWTEventService may also be possible on request.
 * Please view the license.txt of the project for more information.
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
package de.novanic.eventservice.clientmock;

import com.google.gwt.user.client.rpc.StatusCodeException;
import de.novanic.eventservice.client.event.filter.EventFilter;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.*;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;
import de.novanic.eventservice.client.event.listener.unlisten.UnlistenEvent;
import de.novanic.eventservice.client.event.listener.unlisten.DefaultUnlistenEvent;
import de.novanic.eventservice.client.event.listener.unlisten.UnlistenEventListener;
import de.novanic.eventservice.test.testhelper.DefaultRemoteEventServiceFactoryTestMode;
import de.novanic.eventservice.test.testhelper.DummyDomainEvent;
import de.novanic.eventservice.test.testhelper.EventListenerTestMode;
import de.novanic.eventservice.test.testhelper.UnlistenEventListenerTestMode;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author sstrohschein
 * Date: 03.08.2008
 * Time: 22:55:08
 */
@RunWith(JUnit4.class)
public class RemoteEventServiceMockTest extends AbstractRemoteEventServiceMockTest
{
    private static final Domain TEST_DOMAIN = DomainFactory.getDomain("test_domain");
    private static final Domain TEST_DOMAIN_2 = DomainFactory.getDomain("test_domain_2");

    private RemoteEventService myRemoteEventService;

    @Before
    public void setUp() {
        super.setUp();
        myRemoteEventService = DefaultRemoteEventServiceFactoryTestMode.getInstance().getDefaultRemoteEventService(myEventServiceAsyncMock);
    }

    @Test
    public void testInit_Error() {
        try {
            DefaultRemoteEventServiceFactoryTestMode.getInstance().getDefaultRemoteEventService();
            fail("Exception expected, because the GWTService is instantiated in a non GWT context!");
        } catch(Throwable e) {}
    }

    @Test
    public void testAddListener() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());
            //a second time  
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 2);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testAddListener_Error() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testAddListener_Error_2() {
        //onFailure throws a runtime exception, when the init call to the RemoteEventService fails. No following commands should be executed in that case.
        mockInit(new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertFalse(myRemoteEventService.isActive());
            //a second time
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 2);
    }

    @Test
    public void testAddListener_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by third addListener (another domain)
        mockRegister(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a second time
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            //nothing is called on the callback, because the user is already registered to the domain
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a third time for another domain
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN_2, new EventListenerTestMode(), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, TEST_DOMAIN_2);
        assertContainsListeners(TEST_DOMAIN, 2);
        assertContainsListeners(TEST_DOMAIN_2, 1);
    }

    @Test
    public void testAddListener_Callback_Failure() {
        mockInit();

        //caused by first addListener
        mockRegister(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testAddListener_EventFilter() {
        mockInit();

        final TestEventFilter theEventFilter = new TestEventFilter();
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, theEventFilter);

        //caused by second addListener
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
            //a second time
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 2);
    }

    @Test
    public void testAddListener_EventFilter_Callback() {
        mockInit();

        final TestEventFilter theEventFilter = new TestEventFilter();
        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN, theEventFilter);

        //caused by second addListener
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter);

        //caused by callback of register
        mockListen();

        //caused by third addListener (another domain)
        mockRegister(TEST_DOMAIN_2, theEventFilter);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theEventFilter, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a second time
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theEventFilter);
            assertTrue(myRemoteEventService.isActive());
            //nothing is called on the callback, because the user is already registered to the domain
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            //a third time for another domain
            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN_2, new EventListenerTestMode(), theEventFilter, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, TEST_DOMAIN_2);
        assertContainsListeners(TEST_DOMAIN, 2);
        assertContainsListeners(TEST_DOMAIN_2, 1);
    }

    @Test
    public void testAddListener_EventFilter_Callback_Failure() {
        mockInit();

        //caused by first addListener
        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegister(TEST_DOMAIN, theEventFilter, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode(), theEventFilter, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testAddListener_DomainLess() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(null);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(null, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());
            //a second time
            myRemoteEventService.addListener(null, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(new Domain[]{null});
        assertContainsListeners(null, 2);
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testAddUnlistenListener() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by addUnlistenListener
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(null);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            //add Listener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());

            //add UnlistenListener
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(new UnlistenEventListenerTestMode(), null);
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, DomainFactory.UNLISTEN_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 1);
    }

    @Test
    public void testAddUnlistenListener_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by addUnlistenListener
        final UnlistenEvent theUnlistenEvent = new DefaultUnlistenEvent(new HashSet<Domain>(Arrays.asList(TEST_DOMAIN)), "testUser", true);
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(theUnlistenEvent);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            //add Listener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());

            //add UnlistenListener
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(new UnlistenEventListenerTestMode(), theUnlistenEvent, null);
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, DomainFactory.UNLISTEN_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 1);
    }

    @Test
    public void testAddUnlistenListener_Local() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            //add Listener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());

            //add UnlistenListener
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(UnlistenEventListener.Scope.LOCAL, new UnlistenEventListenerTestMode(), null);
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, DomainFactory.UNLISTEN_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 1);
    }

    @Test
    public void testAddUnlistenListener_Local_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            //add Listener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, new EventListenerTestMode());
            assertTrue(myRemoteEventService.isActive());

            //add UnlistenListener
            assertTrue(myRemoteEventService.isActive());
            final UnlistenEvent theUnlistenEvent = new DefaultUnlistenEvent(new HashSet<Domain>(Arrays.asList(TEST_DOMAIN)), "testUser", true);
            myRemoteEventService.addUnlistenListener(UnlistenEventListener.Scope.LOCAL, new UnlistenEventListenerTestMode(), theUnlistenEvent, null);
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN, DomainFactory.UNLISTEN_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 1);
    }

    @Test
    public void testRemoveListener() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            //more than one call shouldn't affect the mocks, because it is only removed/unlistened on first call
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);

            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListener_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive()); //because there is a listener in TEST_DOMAIN_2
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN_2);
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 1);
    }

    @Test
    public void testRemoveListener_3() {
        mockInit();
        //caused by new activation after complete deactivation
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();
        //caused by second callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN);
        //caused by second removeListener
        mockUnlisten(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());

            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListener_4() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            final EventListenerTestMode theRemoteListener_2 = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener_2);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener_2);
            //still active, because there is still another listener registered to the domain
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListener_Error() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            //No listener will be removed, because it isn't the same instance of the added listener.
            myRemoteEventService.removeListener(TEST_DOMAIN, new EventListenerTestMode());
            myRemoteEventService.removeListener(TEST_DOMAIN, new EventListenerTestMode());
            myRemoteEventService.removeListener(TEST_DOMAIN, null);

            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testRemoveListener_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        //caused by second addListener / reactivate
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN);
        //caused by second removeListener
        mockUnlisten(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive()); //because there is a listener in TEST_DOMAIN_2
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN_2, theRemoteListener, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListener_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by first removeListener
        mockUnlisten(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListener(TEST_DOMAIN, theRemoteListener, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListeners() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners();
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domain() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEqualsActiveDomains(TEST_DOMAIN, TEST_DOMAIN_2);
            assertContainsListeners(TEST_DOMAIN, 1);
            assertContainsListeners(TEST_DOMAIN_2, 1);

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertTrue(myRemoteEventService.isActive());

            assertEqualsActiveDomains(TEST_DOMAIN_2);
            assertContainsListeners(TEST_DOMAIN, 0);
            assertContainsListeners(TEST_DOMAIN_2, 1);

            myRemoteEventService.removeListeners(TEST_DOMAIN_2);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domains() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomains);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domains_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first call to removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);
        //caused by second call to removeListeners
        Set<Domain> theDomainsSecondCall = new HashSet<Domain>();
        theDomainsSecondCall.add(TEST_DOMAIN);
        mockUnlisten(theDomainsSecondCall);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEqualsActiveDomains(TEST_DOMAIN, TEST_DOMAIN_2);
            assertContainsListeners(TEST_DOMAIN, 1);
            assertContainsListeners(TEST_DOMAIN_2, 1);

            myRemoteEventService.removeListeners(theDomains);
            assertTrue(myRemoteEventService.isActive());

            assertEqualsActiveDomains(TEST_DOMAIN);
            assertContainsListeners(TEST_DOMAIN, 1);
            assertContainsListeners(TEST_DOMAIN_2, 0);

            myRemoteEventService.removeListeners(theDomainsSecondCall);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListeners_Domain_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN_2, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domain_Callback_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN);
        //caused by second call to removeListeners
        mockUnlisten(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(DomainFactory.getDomain("unknownDomain"), theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN_2, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domain_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by first call to removeListeners
        mockUnlisten(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(TEST_DOMAIN, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveListeners_Domains_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domains_Callback_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);
        mockUnlisten(theDomains);

        //that shouldn't trigger a server call
        Set<Domain> theUnknownDomains = new HashSet<Domain>(1);
        theUnknownDomains.add(DomainFactory.getDomain("unknownDomain"));

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theUnknownDomains, theRecordedCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());

            theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertTrue(theRecordedCallback.isOnSuccessCalled());
            assertFalse(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRemoveListeners_Domains_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by removeListeners
        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            RecordedCallback theRecordedCallback = new RecordedCallback();
            myRemoteEventService.removeListeners(theDomains, theRecordedCallback);
            assertFalse(myRemoteEventService.isActive());
            assertFalse(theRecordedCallback.isOnSuccessCalled());
            assertTrue(theRecordedCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testRemoveUnlistenListener() {
        mockInit();

        //caused by addUnlistenListener
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(null);

        //caused by callback of register
        mockListen();

        //caused by removeUnlistenListener
        mockUnlisten(DomainFactory.UNLISTEN_DOMAIN);

        final UnlistenEventListenerTestMode theTestUnlistenEventListener = new UnlistenEventListenerTestMode();
        EasyMock.replay(myEventServiceAsyncMock);
            //add UnlistenListener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(theTestUnlistenEventListener, null);
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeUnlistenListener(theTestUnlistenEventListener, new RecordedCallback());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 0);
    }

    @Test
    public void testRemoveUnlistenListener_2() {
        mockInit();

        //caused by addUnlistenListener
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(null);

        //caused by callback of register
        mockListen();

        //caused by the second removeUnlistenListener
        mockUnlisten(DomainFactory.UNLISTEN_DOMAIN);

        final UnlistenEventListenerTestMode theTestUnlistenEventListener = new UnlistenEventListenerTestMode();
        final UnlistenEventListenerTestMode theTestUnlistenEventListener_2 = new UnlistenEventListenerTestMode();
        EasyMock.replay(myEventServiceAsyncMock);
            //add UnlistenListener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(theTestUnlistenEventListener, null);
            assertTrue(myRemoteEventService.isActive());

            //add second UnlistenListener
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(theTestUnlistenEventListener_2, null);
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeUnlistenListener(theTestUnlistenEventListener, new RecordedCallback());
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeUnlistenListener(theTestUnlistenEventListener_2, new RecordedCallback());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 0);
    }

    @Test
    public void testRemoveUnlistenListeners() {
        mockInit();

        //caused by addUnlistenListener
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(null);

        //caused by callback of register
        mockListen();

        //caused by removeUnlistenListeners
        mockUnlisten(DomainFactory.UNLISTEN_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            //add UnlistenListener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(new UnlistenEventListenerTestMode(), null);
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeUnlistenListeners(new RecordedCallback());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 0);
    }

    @Test
    public void testRemoveUnlistenListeners_2() {
        mockInit();

        //caused by addUnlistenListener
        mockRegister(DomainFactory.UNLISTEN_DOMAIN);
        mockRegisterUnlistenEvent(null);

        //caused by callback of register
        mockListen();

        //caused by removeUnlistenListeners
        mockUnlisten(DomainFactory.UNLISTEN_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            //add UnlistenListener
            assertFalse(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(new UnlistenEventListenerTestMode(), null);
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addUnlistenListener(new UnlistenEventListenerTestMode(), null);
            assertTrue(myRemoteEventService.isActive());

            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeUnlistenListeners(new RecordedCallback());
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(DomainFactory.UNLISTEN_DOMAIN, 0);
    }

    @Test
    public void testUnlisten() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        //caused by removeListeners for TEST_DOMAIN
        mockUnlisten(TEST_DOMAIN);
        //caused by removeListeners for TEST_DOMAIN_2
        mockUnlisten(TEST_DOMAIN_2);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.removeListeners(TEST_DOMAIN_2);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testUnlisten_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);
        mockRegister(TEST_DOMAIN_2);

        //caused by callback of register
        mockListen();

        Set<Domain> theDomains = new HashSet<Domain>();
        theDomains.add(TEST_DOMAIN);
        theDomains.add(TEST_DOMAIN_2);

        //caused by removeListeners for domains
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());
            myRemoteEventService.addListener(TEST_DOMAIN_2, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(theDomains);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testUnlisten_Error() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by removeListeners for TEST_DOMAIN
        mockUnlisten(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.removeListeners(TEST_DOMAIN);
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testListen() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 3);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(3, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testListen_UserSpecific() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(null);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(new DummyEvent()));
        mockListen(theEvents, 3);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(null, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(3, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(new Domain[]{null});
        assertContainsListeners(null, 1);
    }

    @Test
    public void testListen_Error() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(new DummyEvent(), TEST_DOMAIN));
        mockListen(theEvents, 2, new TestException());
        //two reconnect attempts and one successful call

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(1, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testListen_Error_2() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen(null, 1);

        Set<Domain> theDomains = new HashSet<Domain>(1);
        theDomains.add(TEST_DOMAIN);
        mockUnlisten(theDomains);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertFalse(myRemoteEventService.isActive());

            assertEquals(0, theRemoteListener.getEventCount(DummyEvent.class));
            assertFalse(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains();
        assertContainsListeners(TEST_DOMAIN, 0);
    }

    @Test
    public void testListen_Error_3() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(new DummyEvent(), TEST_DOMAIN));
        mockListen(theEvents, 0, new StatusCodeException(0, ""));

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(0, theRemoteListener.getEventCount(DummyEvent.class)); //no reconnect will be executed due to status code 0
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testListen_Error_4() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(new DummyEvent(), TEST_DOMAIN));
        mockListen(theEvents, 1, new StatusCodeException(500, ""));
        //one reconnect attempt and one successful call

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(1, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testAddEvent() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by add event from the client side
        mockAddEvent(TEST_DOMAIN);
        mockAddEvent(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theEventListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theEventListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(TEST_DOMAIN, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(TEST_DOMAIN, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testAddEventUserSpecific() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by add event from the client side
        mockAddEvent(DomainFactory.USER_SPECIFIC_DOMAIN);
        mockAddEvent(DomainFactory.USER_SPECIFIC_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theEventListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theEventListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(DomainFactory.USER_SPECIFIC_DOMAIN, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(DomainFactory.USER_SPECIFIC_DOMAIN, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testAddEventUserSpecific_NULL_Domain() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        mockListen();

        //caused by add event from the client side
        mockAddEvent(null);
        mockAddEvent(null);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());
            final EventListenerTestMode theEventListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theEventListener);
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(null, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());

            myRemoteEventService.addEvent(null, new DummyEvent());
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
        assertContainsListeners(TEST_DOMAIN_2, 0);
    }

    @Test
    public void testRegisterEventFilter() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));

        //listen without filter
        mockListen(theEvents, 1);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter);

        mockDeregisterEventFilter(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter);
            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN);

            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testRegisterEventFilter_Callback() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter);

        mockDeregisterEventFilter(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertTrue(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theDeregisterEventFilterCallback.isOnFailureCalled());

            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testRegisterEventFilter_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter, new TestException());

        mockDeregisterEventFilter(TEST_DOMAIN);

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertFalse(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertTrue(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theDeregisterEventFilterCallback.isOnFailureCalled());

            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    @Test
    public void testDeregisterEventFilter_Callback_Failure() {
        mockInit();

        //caused by first addListener / activate
        mockRegister(TEST_DOMAIN);

        //caused by callback of register
        List<DomainEvent> theEvents = new ArrayList<DomainEvent>();
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        theEvents.add(new DummyDomainEvent(TEST_DOMAIN));
        mockListen(theEvents, 1);

        final TestEventFilter theEventFilter = new TestEventFilter();
        mockRegisterEventFilter(TEST_DOMAIN, theEventFilter);

        mockDeregisterEventFilter(TEST_DOMAIN, new TestException());

        EasyMock.replay(myEventServiceAsyncMock);
            assertFalse(myRemoteEventService.isActive());

            RecordedCallback theRegisterEventFilterCallback = new RecordedCallback();
            RecordedCallback theDeregisterEventFilterCallback = new RecordedCallback();

            final EventListenerTestMode theRemoteListener = new EventListenerTestMode();
            myRemoteEventService.addListener(TEST_DOMAIN, theRemoteListener);
            assertTrue(myRemoteEventService.isActive());

            assertEquals(2, theRemoteListener.getEventCount(DummyEvent.class));
            assertTrue(myRemoteEventService.isActive());

            //this can not be tested in this test, because the filter function is implemented in the mocked server side.
            myRemoteEventService.registerEventFilter(TEST_DOMAIN, theEventFilter, theRegisterEventFilterCallback);
            assertTrue(myRemoteEventService.isActive());
            assertTrue(theRegisterEventFilterCallback.isOnSuccessCalled());
            assertFalse(theRegisterEventFilterCallback.isOnFailureCalled());

            myRemoteEventService.deregisterEventFilter(TEST_DOMAIN, theDeregisterEventFilterCallback);
            assertTrue(myRemoteEventService.isActive());
            assertFalse(theDeregisterEventFilterCallback.isOnSuccessCalled());
            assertTrue(theDeregisterEventFilterCallback.isOnFailureCalled());
        EasyMock.verify(myEventServiceAsyncMock);
        EasyMock.reset(myEventServiceAsyncMock);

        assertEqualsActiveDomains(TEST_DOMAIN);
        assertContainsListeners(TEST_DOMAIN, 1);
    }

    private void assertEqualsActiveDomains(Domain... aDomains) {
        final Set<Domain> theActiveDomains = myRemoteEventService.getActiveDomains();
        for(Domain theDomain: aDomains) {
            assertTrue("The domain \"" + theDomain + "\" isn't active!", theActiveDomains.contains(theDomain));
        }
        assertEquals(aDomains.length, theActiveDomains.size());
    }

    private void assertContainsListeners(Domain aDomain, int anExpectedCountOfListeners) {
        List<RemoteEventListener> theRegisteredListeners = myRemoteEventService.getRegisteredListeners(aDomain);
        if(theRegisteredListeners != null) {
            assertEquals(anExpectedCountOfListeners, theRegisteredListeners.size());
        } else if(anExpectedCountOfListeners > 0) {
            fail("There are no listeners registered to the domain \"" + aDomain + "\" and " + anExpectedCountOfListeners + " listeners were expected!");
        }
    }

    private class TestEventFilter implements EventFilter
    {
        public boolean match(Event anEvent) {
            return false;
        }
    }
}