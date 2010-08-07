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
package de.novanic.eventservice.service.connection.strategy.connector.streaming;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.SerializationException;
import de.novanic.eventservice.client.event.DomainEvent;
import de.novanic.eventservice.client.event.Event;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * @author sstrohschein
 *         <br>Date: 26.04.2010
 *         <br>Time: 20:00:07
 */
public class EventSerializationPolicyTest extends TestCase
{
    public void testIsValid() {
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(Event.class));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(Event.class));
    }

    public void testIsValid_2() {
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(DomainEvent.class));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(DomainEvent.class));
    }

    public void testIsValid_3() {
        DomainEvent[] theArray = new DomainEvent[0];

        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(theArray.getClass()));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(theArray.getClass()));
    }

    public void testIsValid_Serializable() {
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(SerializableClass.class));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(SerializableClass.class));
    }

    public void testIsValid_Serializable_2() {
        SerializableClass[] theArray = new SerializableClass[0];

        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(theArray.getClass()));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(theArray.getClass()));
    }

    public void testIsValid_IsSerializable() {
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(IsSerializableClass.class));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(IsSerializableClass.class));
    }

    public void testIsValid_IsSerializable_2() {
        IsSerializableClass[] theArray = new IsSerializableClass[0];

        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertTrue(theEventSerializationPolicy.shouldSerializeFields(theArray.getClass()));
        assertTrue(theEventSerializationPolicy.shouldDeserializeFields(theArray.getClass()));
    }

    public void testIsValid_NotSerializable() {
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertFalse(theEventSerializationPolicy.shouldSerializeFields(NotSerializableClass.class));
        assertFalse(theEventSerializationPolicy.shouldDeserializeFields(NotSerializableClass.class));
    }

    public void testIsValid_NotSerializable_2() {
        NotSerializableClass[] theArray = new NotSerializableClass[0];
        
        final EventSerializationPolicy theEventSerializationPolicy = new EventSerializationPolicy();
        assertFalse(theEventSerializationPolicy.shouldSerializeFields(theArray.getClass()));
        assertFalse(theEventSerializationPolicy.shouldDeserializeFields(theArray.getClass()));
    }

    public void testValidateSerialize() {
        try {
            new EventSerializationPolicy().validateSerialize(Event.class);
            new EventSerializationPolicy().validateSerialize(DomainEvent.class);
        } catch(SerializationException e) {
            fail("No exception was expected! " + e.getMessage());
        }
    }

    public void testValidateDeserialize() {
        try {
            new EventSerializationPolicy().validateDeserialize(Event.class);
            new EventSerializationPolicy().validateDeserialize(DomainEvent.class);
        } catch(SerializationException e) {
            fail("No exception was expected! " + e.getMessage());
        }
    }

    private class SerializableClass implements Serializable {}

    private class IsSerializableClass implements IsSerializable {}

    private class NotSerializableClass {}
}