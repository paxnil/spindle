package net.sf.spindle.core.scanning;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
import net.sf.spindle.core.messages.ParseMessages;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.spec.PluginApplicationSpecification;

import org.apache.tapestry.spec.IApplicationSpecification;
import org.w3c.dom.Node;

/**
 * Scanner that turns a node tree into a IApplicationSpecification
 * 
 * @author glongman@gmail.com
 */
public class ApplicationScanner extends LibraryScanner
{

    /*
     * Don't need to throw an exception or add a problem here, the Parser will already have caught
     * this
     * 
     * @see core.scanner.AbstractScanner#beforeScan()
     */
    protected Object beforeScan() throws ScannerException
    {
        return super.beforeScan();
    }

    protected Object createResult()
    {
        return new PluginApplicationSpecification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.scanning.AbstractScanner#doScan(org.w3c.dom.Node)
     */
    protected void doScan() throws ScannerException
    {
        validate(fSource);
        IApplicationSpecification specification = (IApplicationSpecification) fResultObject;
        specification.setPublicId(fPublicId);
        specification.setSpecificationLocation(fResourceLocation);
        specification.setLocation(getSourceLocationInfo(fRootNode));
        String rootName = fRootNode.getNodeName();
        if (!rootName.equals("application"))
        {
            addProblem(
                    IProblem.ERROR,
                    getBestGuessSourceLocation(fRootNode, false),
                    ParseMessages.incorrectDocumentType("application", rootName),
                    false,
                    IProblem.SPINDLE_INCORRECT_DOCUMENT_ROOT_EXPECT_APPLICATION);
        }
        else
        {
            scanApplicationSpecification(fRootNode, specification);
        }
    }

    protected void scanApplicationSpecification(Node rootNode,
            IApplicationSpecification specification) throws ScannerException
    {

        specification.setName(getAttribute(rootNode, "name"));

        specification.setEngineClassName(getAttribute(rootNode, "engine-class"));

        ((PluginApplicationSpecification) specification).validateSelf(fValidator);

        scanLibrarySpecification(rootNode, specification);
    }
}