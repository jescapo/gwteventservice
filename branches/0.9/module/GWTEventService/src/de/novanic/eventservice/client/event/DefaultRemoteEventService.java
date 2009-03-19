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
package de.novanic.eventservice.client.event;

import de.novanic.eventservice.client.event.listener.RemoteEventListener;
import de.novanic.eventservice.client.event.filter.EventFilter;
import de.novanic.eventservice.client.event.domain.Domain;

import java.util.*;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The RemoteEventService supports listening to the server via RemoteEventListeners ({@link de.novanic.eventservice.client.event.listener.RemoteEventListener}).
 * It keeps a connection to the server. When an event occurred at the server, the RemoteEventService informs the RemoteEventListeners
 * about the event and starts listening at the server again. When no RemoteEventListeners registered anymore, the
 * RemoteEventService stops listening till new RemoteEventListeners are registered.
 * The listening works with a domain/context scope. See the documentation/manual to get more information about the
 * listening concept.
 *
 * @author sstrohschein
 * <br>Date: 06.06.2008
 * <br>Time: 18:56:46
 */
public final class DefaultRemoteEventService implements RemoteEventService
{
    private RemoteEventConnector myRemoteEventConnector;
    private Map<Domain, List<RemoteEventListener>> myDomainListenerMapping;

    /**
     * Creates a new RemoteEventService.
     */
    DefaultRemoteEventService(RemoteEventConnector aRemoteEventConnector) {
        myRemoteEventConnector = aRemoteEventConnector;
        myDomainListenerMapping = new HashMap<Domain, List<RemoteEventListener>>();
    }

    /**
     * Adds a listener for a domain.
     * It activates the RemoteEventService if it was inactive.
     * @param aDomain domain
     * @param aRemoteListener new listener
     */
    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener) {
        addListener(aDomain, aRemoteListener, (AsyncCallback<Object>)null);
    }

    /**
     * Adds a listener for a domain.
     * It activates the RemoteEventService if it was inactive.
     * @param aDomain domain
     * @param aRemoteListener new listener
     * @param aCallback callback (only called when no listener is registered for the domain)
     */
    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener, AsyncCallback<?> aCallback) {
        List<RemoteEventListener> theListeners = myDomainListenerMapping.get(aDomain);
        if(theListeners == null) {
            theListeners = new ArrayList<RemoteEventListener>();
            myDomainListenerMapping.put(aDomain, theListeners);
            theListeners.add(aRemoteListener);
            activate(aDomain, aCallback);
        } else {
            theListeners.add(aRemoteListener);
        }
    }

    /**
     * Adds a listener for a domain. The EventFilter is applied to the domain to filter events before the
     * RemoteEventListener recognizes the event.
     * It activates the RemoteEventService if it was inactive.
     * @param aDomain domain
     * @param aRemoteListener new listener
     * @param anEventFilter EventFilter to filter the events before RemoteEventListener
     */
    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener, EventFilter anEventFilter) {
        addListener(aDomain, aRemoteListener, anEventFilter, null);
    }

    /**
     * Adds a listener for a domain. The EventFilter is applied to the domain to filter events before the
     * RemoteEventListener recognizes the event.
     * It activates the RemoteEventService if it was inactive.
     * @param aDomain domain
     * @param aRemoteListener new listener
     * @param anEventFilter EventFilter to filter the events before RemoteEventListener
     * @param aCallback callback (only called when no listener is registered for the domain)
     */
    public void addListener(Domain aDomain, RemoteEventListener aRemoteListener, EventFilter anEventFilter, AsyncCallback<?> aCallback) {
        List<RemoteEventListener> theListeners = myDomainListenerMapping.get(aDomain);
        if(theListeners == null) {
            theListeners = new ArrayList<RemoteEventListener>();
            myDomainListenerMapping.put(aDomain, theListeners);
            activate(aDomain, anEventFilter, aCallback);
        } else {
            registerEventFilter(aDomain, anEventFilter);
        }
        theListeners.add(aRemoteListener);
    }

    /**
     * Removes a listener for a domain.
     * The RemoteEventService will get inactive, when no other listeners are registered.
     * @param aDomain domain
     * @param aRemoteListener listener to remove
     */
    public void removeListener(Domain aDomain, RemoteEventListener aRemoteListener) {
        removeListener(aDomain, aRemoteListener, new VoidAsyncCallback());
    }

    /**
     * Removes a listener for a domain.
     * The RemoteEventService will get inactive, when no other listeners are registered.
     * @param aDomain domain
     * @param aRemoteListener listener to remove
     * @param aCallback callback
     */
    public void removeListener(Domain aDomain, RemoteEventListener aRemoteListener, AsyncCallback<?> aCallback) {
        List<RemoteEventListener> theListeners = myDomainListenerMapping.get(aDomain);
        if(theListeners != null) {
            theListeners.remove(aRemoteListener);
            if(removeListenDomain(aDomain)) {
                myRemoteEventConnector.deactivate(aDomain, aCallback);
            }
        }
    }

    /**
     * Registers the domain for listening and activates the RemoteEventService (starts listening) if it is inactive.
     * @param aDomain domain to register/activate
     * @param aCallback callback
     */
    private void activate(Domain aDomain, AsyncCallback<?> aCallback) {
        activate(aDomain, null, aCallback);
    }

    /**
     * Registers the domain with the EventFilter for listening and activates the RemoteEventService (starts listening)
     * if it is inactive.
     * @param aDomain domain to register/activate
     * @param anEventFilter EventFilter to filter the events before RemoteEventListener
     * @param aCallback callback
     */
    private <T> void activate(Domain aDomain, EventFilter anEventFilter, AsyncCallback<T> aCallback) {
        myRemoteEventConnector.activate(aDomain, anEventFilter, new ListenerEventNotification(), aCallback);
    }

    /**
     * Registers an EventFilter for a domain. This can be used when a listener is already added and an EventFilter
     * needed later or isn't available when the listener is added.
     * @param aDomain domain
     * @param anEventFilter EventFilter to filter the events before RemoteEventListener
     */
    public void registerEventFilter(Domain aDomain, EventFilter anEventFilter) {
        registerEventFilter(aDomain, anEventFilter, new VoidAsyncCallback());
    }

    /**
     * Registers an EventFilter for a domain. This can be used when a listener is already added and an EventFilter
     * needed later or isn't available when the listener is added.
     * @param aDomain domain
     * @param anEventFilter EventFilter to filter the events before RemoteEventListener
     * @param aCallback callback
     */
    public void registerEventFilter(Domain aDomain, EventFilter anEventFilter, AsyncCallback<?> aCallback) {
        myRemoteEventConnector.registerEventFilter(aDomain, anEventFilter, aCallback);
    }

    /**
     * Deregisters the EventFilter for a domain.
     * @param aDomain domain to remove the EventFilter from
     */
    public void deregisterEventFilter(Domain aDomain) {
        deregisterEventFilter(aDomain, new VoidAsyncCallback());
    }

    /**
     * Deregisters the EventFilter for a domain.
     * @param aDomain domain to remove the EventFilter from
     * @param aCallback callback
     */
    public void deregisterEventFilter(Domain aDomain, AsyncCallback<?> aCallback) {
        myRemoteEventConnector.deregisterEventFilter(aDomain, aCallback);
    }

    /**
     * Checks if the RemoteEventService is active (listening).
     * @return true when active/listening, otherwise false
     */
    public boolean isActive() {
        return myRemoteEventConnector.isActive();
    }

    /**
     * Removes all RemoteEventListeners and deactivates the RemoteEventService (stop listening).
     */
    public void removeListeners() {
        removeListeners(new VoidAsyncCallback());
    }

    /**
     * Removes all RemoteEventListeners and deactivates the RemoteEventService (stop listening).
     * @param aCallback callback (only called when a listener is registered for the domain)
     */
    public void removeListeners(AsyncCallback<?> aCallback) {
        removeListeners(myDomainListenerMapping.keySet(), aCallback);
    }

    /**
     * Calls unlisten for a set of domains (stop listening for these domains). The RemoteEventListeners for these
     * domains will also be removed.
     * {@link DefaultRemoteEventService#removeListeners()} can be used to call unlisten for all domains.
     * @param aDomains domains to unlisten
     */
    public void removeListeners(Set<Domain> aDomains) {
        removeListeners(aDomains, new VoidAsyncCallback());
    }

    /**
     * Calls unlisten for a set of domains (stop listening for these domains). The RemoteEventListeners for these
     * domains will also be removed.
     * {@link DefaultRemoteEventService#removeListeners()} can be used to call unlisten for all domains.
     * @param aDomains domains to unlisten
     * @param aCallback callback (only called when a listener is registered for the domain)
     */
    public void removeListeners(Set<Domain> aDomains, AsyncCallback<?> aCallback) {
        Set<Domain> theDomains = new HashSet<Domain>(aDomains);
        Iterator<Domain> theDomainIterator = theDomains.iterator();
        while(theDomainIterator.hasNext()) {
            Domain theDomain = theDomainIterator.next();
            if(!(unlisten(theDomain, null))) {
                theDomainIterator.remove();
            }
        }
        if(!theDomains.isEmpty()) {
            //removeListeners is called with a set of domains to reduce remote server calls.
            myRemoteEventConnector.deactivate(theDomains, aCallback);
        }
    }

    /**
     * Stops listening for the corresponding domain. The RemoteEventFilters for the domain will also be removed.
     * {@link DefaultRemoteEventService#removeListeners()} can be used to call unlisten for all domains.
     * @param aDomain domain to unlisten
     */
    public void removeListeners(Domain aDomain) {
        removeListeners(aDomain, new VoidAsyncCallback());
    }

    /**
     * Stops listening for the corresponding domain. The RemoteEventFilters for the domain will also be removed.
     * {@link DefaultRemoteEventService#removeListeners()} can be used to call unlisten for all domains.
     * @param aDomain domain to unlisten
     * @param aCallback callback (only called when a listener is registered for the domain)
     */
    public void removeListeners(Domain aDomain, AsyncCallback<?> aCallback) {
        unlisten(aDomain, aCallback);
    }

    /**
     * Stops listening for the corresponding domain. The RemoteEventFilters for the domain will also be removed.
     * {@link DefaultRemoteEventService#removeListeners()} can be used to call unlisten for all domains.
     * @param aDomain domain to unlisten
     * @param aCallback callback (if it is NULL, no call is executed to the server)
     * @return true when listeners registered (remote call needed), otherwise false
     */
    private boolean unlisten(Domain aDomain, AsyncCallback<?> aCallback) {
        List<RemoteEventListener> theRemoteEventListeners = myDomainListenerMapping.get(aDomain);
        if(theRemoteEventListeners != null) {
            theRemoteEventListeners.clear();
            removeListenDomain(aDomain);
            if(aCallback != null) {
                myRemoteEventConnector.deactivate(aDomain, aCallback);
            }
            return true;
        }
        return false;
    }

    /**
     * Removes the domain if no RemoteEventListeners are registered to the domain.
     * It deactivates the RemoteEventService if no more domains/listeners are registered.
     * @param aDomain domain to remove/check for remove
     * @return true if the domain is removed, false if the domain isn't removed
     */
    private boolean removeListenDomain(Domain aDomain) {
        List<RemoteEventListener> theRemoteEventListeners = myDomainListenerMapping.get(aDomain);
        if(theRemoteEventListeners == null || theRemoteEventListeners.isEmpty()) {
            //remove the domain if the entry exists
            if(theRemoteEventListeners != null) {
                myDomainListenerMapping.remove(aDomain);
            }
            //deactivate the connector if no domain registered
            if(myDomainListenerMapping.isEmpty()) {
                myRemoteEventConnector.deactivate();
            }
            return true;
        }
        return false;
    }

    /**
     * The ListenEventCallback is used to produce the listen cycle. It executes a {@link de.novanic.eventservice.client.command.RemoteListenCommand}
     * and is attached as callback for the listen server call.
     */
    private final class ListenerEventNotification implements EventNotification
    {
        /**
        * All listeners of the according domains will be informed about the incoming events.
        * @param anEvents incoming events
        */
        public void onNotify(List<DomainEvent> anEvents) {
            for(DomainEvent theDomainEvent : anEvents) {
                //all listeners for the domain of the event will be executed
                List<RemoteEventListener> theListeners = myDomainListenerMapping.get(theDomainEvent.getDomain());
                if(theListeners != null) {
                    for(RemoteEventListener theListener: theListeners) {
                        theListener.apply(theDomainEvent.getEvent());
                    }
                }
            }
        }

        public void onAbort() {
            //if the remote doesn't know the client, all listeners will be removed and the connection gets inactive
            removeListeners();
        }
    }

    /**
     * Empty callback
     */
    private static class VoidAsyncCallback implements AsyncCallback<Object>
    {
        public void onFailure(Throwable aThrowable) {}

        public void onSuccess(Object aResult) {}
    }
}