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

package net.sf.spindle.core.resources.search.eclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.spindle.core.resources.search.AbstractTapestrySearchAcceptor;

import org.eclipse.core.resources.IStorage;

/**
 * Acceptor that will accept/reject things based on the flags set in it.
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractEclipseSearchAcceptor extends AbstractTapestrySearchAcceptor
{

    private List<String> fExcludedFiles;

    private List<String> fExcludedExtensions;

    public AbstractEclipseSearchAcceptor(int acceptFlags, Set<String> allowedTemplateExtensions,
            List<String> exclusions)
    {
        super(acceptFlags, allowedTemplateExtensions);
        setupExclusions(exclusions);
    }

    private void setupExclusions(List exclusions)
    {
        fExcludedFiles = new ArrayList<String>();
        fExcludedExtensions = new ArrayList<String>();
        for (Iterator iter = exclusions.iterator(); iter.hasNext();)
        {
            String element = (String) iter.next();
            if (element.startsWith("*."))
            {
                fExcludedExtensions.add(element.substring(1));
            }
            else
            {
                fExcludedFiles.add(element);
            }
        }
    }

    protected String getFileExtension(Object leaf)
    {
        IStorage storage = (IStorage) leaf;
        return storage.getFullPath().getFileExtension();
    }

    protected boolean isExcluded(Object leaf)
    {
        IStorage storage = (IStorage) leaf;
        String name = storage.getName();

        if (fExcludedFiles.contains(name))
            return true;

        String extension = storage.getFullPath().getFileExtension();

        if (fExcludedExtensions.contains(extension))
            return true;

        return false;
    }
}