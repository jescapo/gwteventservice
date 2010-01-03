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

/**
 * An EventServiceConfiguration holds the configuration for {@link de.novanic.eventservice.client.event.service.EventService}.
 * The time for a timeout and the min- and max-waiting-time can be configured.
 * <br>
 * <br>- Min waiting time - Listening should hold at least for min waiting time.
 * <br>- Max waiting time - Listening shouldn't hold longer than max waiting time.
 * <br>- Timeout time - Max time for a listen cycle. If the timeout time is overlapsed, the client will be deregistered.
 *
 * @author sstrohschein
 * <br>Date: 09.08.2008
 * <br>Time: 23:17:38
 */
public class RemoteEventServiceConfiguration implements EventServiceConfiguration
{
    private final int myMinWaitingTime;
    private final int myMaxWaitingTime;
    private final int myTimeoutTime;

    /**
     * Creates a new RemoteEventServiceConfiguration.
     * @param aMinWaitingTime min waiting time before listen returns (in milliseconds)
     * @param aMaxWaitingTime max waiting time before listen returns, when no events recognized (in milliseconds)
     * @param aTimeoutTime timeout time for a listen cycle (in milliseconds)
     */
    public RemoteEventServiceConfiguration(int aMinWaitingTime, int aMaxWaitingTime, int aTimeoutTime) {
        myMinWaitingTime = aMinWaitingTime;
        myMaxWaitingTime = aMaxWaitingTime;
        myTimeoutTime = aTimeoutTime;
    }

    /**
     * Returns the min waiting time. Listening should hold at least for min waiting time.
     * @return min waiting time
     */
    public int getMinWaitingTime() {
        return myMinWaitingTime;
    }

    /**
     * Returns the max waiting time. Listening shouldn't hold longer than max waiting time.
     * @return max waiting time
     */
    public int getMaxWaitingTime() {
        return myMaxWaitingTime;
    }

    /**
     * Returns the timeout time (max time for a listen cycle).
     * @return timeout time
     */
    public int getTimeoutTime() {
        return myTimeoutTime;
    }

    public boolean equals(Object anObject) {
        if(this == anObject) {
            return true;
        }
        if(anObject == null || getClass() != anObject.getClass()) {
            return false;
        }

        EventServiceConfiguration theConfiguration = (EventServiceConfiguration)anObject;
        return (myMaxWaitingTime == theConfiguration.getMaxWaitingTime()
                && myMinWaitingTime == theConfiguration.getMinWaitingTime()
                && myTimeoutTime == theConfiguration.getTimeoutTime());
    }

    public int hashCode() {
        int theResult = myMinWaitingTime;
        theResult = 31 * theResult + myMaxWaitingTime;
        theResult = 31 * theResult + myTimeoutTime;
        return theResult;
    }

    public String toString() {
        StringBuffer theConfigStringBuffer = new StringBuffer(50);
        theConfigStringBuffer.append("EventServiceConfiguration. Min.: ");
        theConfigStringBuffer.append(getMinWaitingTime());
        theConfigStringBuffer.append("ms; Max.: ");
        theConfigStringBuffer.append(getMaxWaitingTime());
        theConfigStringBuffer.append("ms; Timeout: ");
        theConfigStringBuffer.append(getTimeoutTime());
        theConfigStringBuffer.append("ms");
        return theConfigStringBuffer.toString();
    }
}