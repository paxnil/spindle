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

package tests.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import junit.framework.TestCase;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
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

    protected class TestListener implements PropertyChangeListener
    {
        String owner;

        public TestListener()
        {}

        public TestListener(String owner)
        {
            this.owner = owner;
        }

        protected Object source;
        protected Object oldValue;
        protected Object newValue;
        protected String propertyName;

        public void propertyChange(PropertyChangeEvent evt)
        {
            source = evt.getSource();
            oldValue = evt.getOldValue();
            newValue = evt.getNewValue();
            propertyName = evt.getPropertyName();
            if (owner != null)
            {
                // all events should be fired on behalf of the owner
                assertTrue(source == owner);
            }
        }
    };

    protected class OneShotListener extends TestListener
    {

        private Object expectedSource;
        private Object expectedOldValue;
        private Object expectedNewValue;
        private String expectedPropertyName;

        public OneShotListener(
            Object expectedSource,
            Object expectedOldValue,
            Object expectedNewValue,
            String expectedPropertyName)
        {
            super();
            this.expectedSource = expectedSource;
            this.expectedOldValue = expectedOldValue;
            this.expectedNewValue = expectedNewValue;
            this.expectedPropertyName = expectedPropertyName;
        }

        public void propertyChange(PropertyChangeEvent evt)
        {
            super.propertyChange(evt);
            assertEquals("wrong source", expectedSource, source);
            assertEquals("wrong old", expectedOldValue, oldValue);
            assertEquals("wrong new", expectedNewValue, newValue);
            assertEquals("wrong propertyName", expectedPropertyName, propertyName);
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
