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

package com.iw.plugins.spindle.core.scanning;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.ILocation;
import org.apache.tapestry.INamespace;
import org.apache.tapestry.IResourceLocation;
import org.apache.tapestry.parse.ITemplateParserDelegate;
import org.apache.tapestry.parse.TemplateParseException;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TokenType;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.eclipse.core.runtime.CoreException;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.parser.template.CoreOpenToken;
import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;

import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.Files;

/**
 *  Scanner for Tapestry templates. 
 * 
 *  It is assumed that the component we are scanning templates on behalf of
 *  has its namepsace already.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TemplateScanner extends AbstractScanner
{

    private static final int IMPLICIT_ID_PATTERN_ID_GROUP = 1;
    private static final int IMPLICIT_ID_PATTERN_TYPE_GROUP = 2;
    private static final int IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP = 4;
    private static final int IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP = 5;

    private Pattern _simpleIdPattern;
    private Pattern _implicitIdPattern;
    private PatternMatcher _patternMatcher;

    private PluginComponentSpecification fComponentSpec;
    private ICoreNamespace fNamespace;
    private CoreTemplateParser fParser;
    private ITemplateParserDelegate fParserDelegate = new ScannerDelegate();

    public void scanTemplate(
        PluginComponentSpecification spec,
        IResourceLocation templateLocation,
        IScannerValidator validator)
        throws ScannerException
    {
        Assert.isNotNull(spec);
        Assert.isNotNull(spec.getNamespace());
        fComponentSpec = spec;
        fNamespace = (ICoreNamespace) spec.getNamespace();
        fParser = new CoreTemplateParser();
        fParser.setProblemCollector(this);

        scan(templateLocation, validator);

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(java.lang.Object, java.lang.Object)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) source;
        char[] data = null;
        try
        {
            InputStream in = location.getContents();
            data = Files.readFileToString(in, null).toCharArray();
        } catch (CoreException e)
        {
            TapestryCore.log(e);
        } catch (IOException e)
        {
            TapestryCore.log(e);
        }
        if (data == null)
            throw new ScannerException("null data!");

        List result = (List) resultObject;

        TemplateToken[] parseResults = null;

        try
        {
            parseResults = fParser.parse(data, fParserDelegate, location);
        } catch (TemplateParseException e1)
        {
            // should never happen
            TapestryCore.log(e1);
        }

        if (parseResults == null)
            return;

        for (int i = 0; i < parseResults.length; i++)
        {
            if (parseResults[i].getType() == TokenType.OPEN)
            {
                scanOpenToken((CoreOpenToken) parseResults[i], result);
            }
        }
    }

    private void scanOpenToken(CoreOpenToken token, List result) throws ScannerException
    {}

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#beforeScan(java.lang.Object)
     */
    protected Object beforeScan(Object source) throws ScannerException
    {
        return new ArrayList();
    }

    private class ScannerDelegate implements ITemplateParserDelegate
    {

        /* (non-Javadoc)
        * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String, org.apache.tapestry.ILocation)
        */
        public boolean getAllowBody(String componentId, ILocation location)
        {
            IContainedComponent embedded = fComponentSpec.getComponent(componentId);
            if (embedded == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            IComponentSpecification containedSpec = fNamespace.getComponentResolver().resolve(embedded.getType());
            if (containedSpec == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "no-such-component",
                        fComponentSpec.getSpecificationLocation(),
                        componentId));

            return containedSpec.getAllowBody();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getAllowBody(java.lang.String, java.lang.String, org.apache.tapestry.ILocation)
         */
        public boolean getAllowBody(String libraryId, String type, ILocation location)
        {
            if (libraryId != null)
            {
                INamespace namespace = fNamespace.getChildNamespace(libraryId);
                if (namespace == null)
                    throw new ApplicationRuntimeException(
                        "Unable to resolve " + TapestryCore.getTapestryString("Namespace.nested-namespace", libraryId));
            }

            IComponentSpecification spec = fNamespace.getComponentResolver().resolve(libraryId, type);
            if (spec == null)
                throw new ApplicationRuntimeException(
                    TapestryCore.getTapestryString(
                        "Namespace.no-such-component-type",
                        type,
                        libraryId == null ? TapestryCore.getString("project-namespace") : libraryId));

            return spec.getAllowBody();
        }

        /* (non-Javadoc)
         * @see org.apache.tapestry.parse.ITemplateParserDelegate#getKnownComponent(java.lang.String)
         */
        public boolean getKnownComponent(String componentId)
        {
            return fComponentSpec.getComponent(componentId) != null;
        }

    }

}
