/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.parser.xml.pull;

/**
 *  A queue
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
/*package*/
class StateQueue
{
    private State[] store;
    private int tail;
    private int head;
    private TapestryPullParser parser;

    StateQueue(TapestryPullParser parser)
    {
        this.parser = parser;
    }

    public void clear()
    {
        head = tail = 0;
        store = new State[0];
    }

    public boolean isEmpty()
    {
        if (head > tail)
        {
            throw new IllegalStateException();
        }

        return (head == tail);
    }

    public void reset()
    {
        if (head > tail)
        {
            throw new IllegalStateException();
        }
        head = tail = 0;
    }

    public State append(StateType eventType)
    {
        if (tail >= store.length)
        {
            State[] newQueue = new State[tail + 8];
            System.arraycopy(store, 0, newQueue, 0, store.length);
            for (int i = store.length; i < newQueue.length; i++)
            {
                newQueue[i] = new State();
            }
            store = newQueue;
        }
        State state = store[tail++];
        state.repopulate(eventType, parser);
        return state;
    }

    public State peekBottom()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("queue is empty");
        }
        return store[head];
    }

    public State remove()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("queue is empty");
        }
        return store[head++];
    }

    public State top()
    {
        if (isEmpty())
        {
            throw new IllegalStateException("queue is empty");
        }
        return store[tail - 1];
    }
}
