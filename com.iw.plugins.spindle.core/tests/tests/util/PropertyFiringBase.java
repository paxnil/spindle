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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package tests.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

/**
 *  Base class for OrderPreserving Collection classes
 * 
 *  Constructor is protected as this is a base class and should not
 *  be invoked by Junit.
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public abstract class PropertyFiringBase extends TestCase
{

    protected class ErrorListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent arg0)
        {
           fail("property event was fired but should not have been"); 
        }
    }


    /**
     *  A Listener that sublcasses of PropertyFiringBase can use.
     * 
     *  By Default the listener is passed an object that it will verify
     *  as the source field of any PropertyChangeEvents it recieves.
     * 
     *  Users can subclass this to add more checks!
     * 
     * @author glongman@gmail.com
     */
    protected class TestListener implements PropertyChangeListener
    {
        Object owner;

        public TestListener()
        {}

        public TestListener(Object owner)
        {
            this.owner = owner;
        }

        protected Object eventSource;
        protected Object eventOldValue;
        protected Object eventNewValue;
        protected String eventPropertyName;

        public void propertyChange(PropertyChangeEvent evt)
        {
            eventSource = evt.getSource();
            eventOldValue = evt.getOldValue();
            eventNewValue = evt.getNewValue();
            eventPropertyName = evt.getPropertyName();
            if (owner != null)
            {
                // all events should be fired on behalf of the owner
                assertSame(owner,eventSource);
            }
        }
    };

    /**
     *  A premade Listener that takes as parameters all the values
     *  it expects to see in any PropertyChangeEvents it recieves.
     * 
     *  If the recieved events's values do not match the OneShot's exactly,
     *  this class will force the test to fail.
     * 
     *  Only useful for one shot as the expected values are set in the
     *  constructor and cannot be changed.
     * 
     * @author glongman@gmail.com
     * @version $Id$
     */
    protected class OneShotListener extends TestListener
    {

        protected Object expectedOldValue;
        protected Object expectedNewValue;
        protected String expectedPropertyName;
        private boolean fired = false;

        public OneShotListener(
            Object owner,
            Object expectedOldValue,
            Object expectedNewValue,
            String expectedPropertyName)
        {
            super(owner);
            this.expectedOldValue = expectedOldValue;
            this.expectedNewValue = expectedNewValue;
            this.expectedPropertyName = expectedPropertyName;
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            super.propertyChange(evt);
            if(fired)
            {
                fail("this OneShot has already been fired");
            }
            assertEquals("wrong source", owner, eventSource);
            assertEquals("wrong old", expectedOldValue, eventOldValue);
            assertEquals("wrong new", expectedNewValue, eventNewValue);
            assertEquals("wrong propertyName", expectedPropertyName, eventPropertyName);
            fired = true;
        }
    }

    /**
     * @param arg0
     */
    protected PropertyFiringBase(String arg0)
    {
        super(arg0);
    }

}
