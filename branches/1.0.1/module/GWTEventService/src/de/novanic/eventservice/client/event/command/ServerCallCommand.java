/*
 * GWTEventService
 * Copyright (c) 2009, GWTEventService Committers
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
package de.novanic.eventservice.client.event.command;

import de.novanic.eventservice.client.event.RemoteEventConnector;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Command which is used at the client side to execute and handle server calls.
 *
 * @author sstrohschein
 *         <br>Date: 27.03.2009
 *         <br>Time: 23:34:41
 */
public abstract class ServerCallCommand<R> implements ClientCommand<R>
{
    private RemoteEventConnector myRemoteEventConnector;
    private AsyncCallback<R> myCallback;

    /**
     * Creates an ServerCallCommand to execute and handle server calls.
     * @param aRemoteEventConnector {@link de.novanic.eventservice.client.event.RemoteEventConnector}
     * @param aCallback callback of the command
     */
    public ServerCallCommand(RemoteEventConnector aRemoteEventConnector, AsyncCallback<R> aCallback) {
        myRemoteEventConnector = aRemoteEventConnector;
        myCallback = aCallback;
    }

    /**
     * Returns the registered {@link de.novanic.eventservice.client.event.RemoteEventConnector}.
     * @return {@link de.novanic.eventservice.client.event.RemoteEventConnector}
     */
    protected RemoteEventConnector getRemoteEventConnector() {
        return myRemoteEventConnector;
    }

    /**
     * Returns the callback of the command. That can be used to get the callback when the command executes a server
     * call.
     * @return callback of the command
     */
    public AsyncCallback<R> getCommandCallback() {
        return myCallback;
    }
}
