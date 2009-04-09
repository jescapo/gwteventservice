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
package de.novanic.gwteventservice.demo.conversationapp.client.conversation.ui;

import com.google.gwt.user.client.ui.ClickListener;

/**
 * @author sstrohschein
 *         <br>Date: 16.09.2008
 *         <br>Time: 23:03:11
 */
public interface ConversationMessagePanel
{
    String getMessageText();

    void resetMessageText();

    void reset();

    void enable(boolean isEnable);

    void setFocus(boolean isFocus);

    void addSendButtonListener(ClickListener aClickListener);

    void removeSendButtonListener(ClickListener aClickListener);
}