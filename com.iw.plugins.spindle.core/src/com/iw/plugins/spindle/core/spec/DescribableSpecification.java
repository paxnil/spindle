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

package com.iw.plugins.spindle.core.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DescribableSpecification extends BaseSpecification implements IPluginDescribable
{

    /**
       *  The locations and values of all description declarations in a spec.
       *  Immutable after a parse/scan episode.
       */
    private List fDescriptionDeclarations;

    private String fDescription;

    /**
     * @param type
     */
    public DescribableSpecification(int type)
    {
        super(type);
    }

    public String getDescription()
    {
        return fDescription;
    }

    public void setDescription(String description)
    {
        String old = fDescription;
        fDescription = description;
        firePropertyChange("description", old, fDescription);
    }

    public void addDescriptionDeclaration(PluginDescriptionDeclaration decl)
    {
        if (fDescriptionDeclarations == null)
            fDescriptionDeclarations = new ArrayList();

        fDescriptionDeclarations.add(decl);
    }

    public List getDescriptionDeclarations()
    {
        if (fDescriptionDeclarations == null)
            return Collections.EMPTY_LIST;

        return fDescriptionDeclarations;
    }

}
