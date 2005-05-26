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

package com.iw.plugins.spindle.ui.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Wrapper for a store that will consult the wrapped plugins preferences if the normal preferences
 * does not provide a value. This wrapper will fire a property change event if the EditorsPlugin
 * preferences change.
 * 
 * @author glongman@gmail.com
 */
public class PreferenceStoreWrapper implements IPreferenceStore, IPropertyChangeListener
{

    IPreferenceStore fPluginPreferences;

    IPreferenceStore fWrappedPreferences;

    public PreferenceStoreWrapper(IPreferenceStore pluginPreferences, IPreferenceStore wrapped)
    {
        this.fPluginPreferences = pluginPreferences;
        fWrappedPreferences = wrapped;
        fWrappedPreferences.addPropertyChangeListener(this);
    }

    public void dispose()
    {
        fWrappedPreferences.removePropertyChangeListener(this);
    }

    /*
     * The EditorsPlugin preferences changed. We'd better inform the listeners! (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        fPluginPreferences.firePropertyChangeEvent(event.getProperty(), event.getOldValue(), event
                .getNewValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener)
    {
        fPluginPreferences.addPropertyChangeListener(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
     */
    public boolean contains(String name)
    {
        boolean result = fPluginPreferences.contains(name);
        if (!result)
            result = fWrappedPreferences.contains(name);

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    public void firePropertyChangeEvent(String name, Object oldValue, Object newValue)
    {
        fPluginPreferences.firePropertyChangeEvent(name, oldValue, newValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getBoolean(name);
        }
        return fWrappedPreferences.getBoolean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
     */
    public boolean getDefaultBoolean(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultBoolean(name);
        }
        return fWrappedPreferences.getDefaultBoolean(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
     */
    public double getDefaultDouble(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultDouble(name);
        }
        return fWrappedPreferences.getDefaultDouble(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
     */
    public float getDefaultFloat(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultFloat(name);
        }
        return fWrappedPreferences.getDefaultFloat(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
     */
    public int getDefaultInt(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultInt(name);
        }
        return fWrappedPreferences.getDefaultInt(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
     */
    public long getDefaultLong(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultLong(name);
        }
        return fWrappedPreferences.getDefaultLong(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
     */
    public String getDefaultString(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDefaultString(name);
        }
        return fWrappedPreferences.getDefaultString(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
     */
    public double getDouble(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getDouble(name);
        }
        return fWrappedPreferences.getDouble(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
     */
    public float getFloat(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getFloat(name);
        }
        return fWrappedPreferences.getFloat(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
     */
    public int getInt(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getInt(name);
        }
        return fWrappedPreferences.getInt(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
     */
    public long getLong(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getLong(name);
        }
        return fWrappedPreferences.getLong(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
     */
    public String getString(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.getString(name);
        }
        return fWrappedPreferences.getString(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
     */
    public boolean isDefault(String name)
    {
        if (fPluginPreferences.contains(name))
        {
            return fPluginPreferences.isDefault(name);
        }
        return fWrappedPreferences.isDefault(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
     */
    public boolean needsSaving()
    {
        return fPluginPreferences.needsSaving();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String,
     *      java.lang.String)
     */
    public void putValue(String name, String value)
    {
        fPluginPreferences.putValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener)
    {
        fPluginPreferences.removePropertyChangeListener(listener);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
     */
    public void setDefault(String name, boolean value)
    {
        fPluginPreferences.setDefault(name, value);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
     */
    public void setDefault(String name, double value)
    {
        fPluginPreferences.setDefault(name, value);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
     */
    public void setDefault(String name, float value)
    {
        fPluginPreferences.setDefault(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
     */
    public void setDefault(String name, int value)
    {
        fPluginPreferences.setDefault(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
     */
    public void setDefault(String name, long value)
    {
        fPluginPreferences.setDefault(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
     *      java.lang.String)
     */
    public void setDefault(String name, String defaultObject)
    {
        fPluginPreferences.setDefault(name, defaultObject);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
     */
    public void setToDefault(String name)
    {
        fPluginPreferences.setToDefault(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
     */
    public void setValue(String name, boolean value)
    {
        fPluginPreferences.setValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
     */
    public void setValue(String name, double value)
    {
        fPluginPreferences.setValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
     */
    public void setValue(String name, float value)
    {
        fPluginPreferences.setValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
     */
    public void setValue(String name, int value)
    {
        fPluginPreferences.setValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
     */
    public void setValue(String name, long value)
    {
        fPluginPreferences.setValue(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String,
     *      java.lang.String)
     */
    public void setValue(String name, String value)
    {
        fPluginPreferences.setValue(name, value);
    }

}