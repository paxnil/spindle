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
package com.iw.plugins.spindle.adapters;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;

/**
 * A factory that adapts IStorages and subtypes into TapestryProjects
 */
public class StorageEditorInputAdapterFactory implements IAdapterFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
     */
    public Object getAdapter(Object adaptableObject, Class adapterType)
    {
        if (!(adaptableObject instanceof JarEntryEditorInput) || adapterType != IStorage.class)
            return null;

        return ((JarEntryEditorInput)adaptableObject).getStorage();        
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
     */
    public Class[] getAdapterList()
    {
        // TODO I think this is ignored when added via extension point
        return new Class[]
        { IStorage.class };
    }
}