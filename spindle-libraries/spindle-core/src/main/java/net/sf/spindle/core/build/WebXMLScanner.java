package net.sf.spindle.core.build;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.core.CoreMessages;
import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.messages.DefaultTapestryMessages;
import net.sf.spindle.core.parser.IDOMModel;
import net.sf.spindle.core.resources.ICoreResource;
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.resources.PathUtils;
import net.sf.spindle.core.scanning.AbstractDOMScanner;
import net.sf.spindle.core.scanning.BaseValidator;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.TypeModelException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A Processor class used by FullBuild that extracts Tapestry information from the file web.xml
 * 
 * @author glongman@gmail.com
 */
public abstract class WebXMLScanner extends AbstractDOMScanner
{

    protected AbstractBuild builder;

    protected ArrayList<String> seenServletNames;

    /**
     * Constructor for WebXMLProcessor.
     */
    public WebXMLScanner(AbstractBuild fullBuilder)
    {
        super();
        this.builder = fullBuilder;
    }

    public WebAppDescriptor scanWebAppDescriptor(IDOMModel source) throws ScannerException
    {
        return (WebAppDescriptor) scan(source, new BaseValidator(builder));
    }

    protected Object beforeScan()
    {
        seenServletNames = new ArrayList<String>();
        return new WebAppDescriptor();
    }

    protected void checkApplicationLocation(ICoreResource location) throws ScannerException
    {
        if (location == null)
            return;

        if (!location.exists())
            throw new ScannerException(CoreMessages.format(
                    "web-xml-ignore-application-path-not-found",
                    location == null ? "no location found" : location.toString()), false,
                    IProblem.NOT_QUICK_FIXABLE);

        PathUtils ws_path = new PathUtils(location.getName());
        String extension = ws_path.getFileExtension();
        if (extension == null
                || !extension.equals(AbstractBuildInfrastructure.APPLICATION_EXTENSION))
            throw new ScannerException(CoreMessages.format("web-xml-wrong-file-extension", location
                    .toString()), false, IProblem.NOT_QUICK_FIXABLE);

    }

    protected void checkApplicationServletPathParam(String value, ServletInfo currentInfo,
            ISourceLocation location)
    {
        if (currentInfo.isServletSubclass && currentInfo.applicationSpecLocation != null)
        {
            addProblem(IProblem.WARNING, location, CoreMessages.format(
                    "web-xml-application-path-param-but-servlet-defines",
                    currentInfo.classname), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }

        if (!value.endsWith(".application"))
        {
            addProblem(IProblem.ERROR, location, CoreMessages.format(
                    "web-xml-wrong-file-extension",
                    value), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }

        ICoreResource ws_location = getApplicationLocation(currentInfo, value);
        if (ws_location == null)
        {
            addProblem(IProblem.ERROR, location, CoreMessages.format(
                    "web-xml-ignore-application-path-not-found",
                    value), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }
        currentInfo.applicationSpecLocation = ws_location;
    }

    protected boolean checkJavaSubclassOfImplements(IJavaType superclass, IJavaType candidate,
            ISourceLocation location)
    {
        if (superclass.equals(candidate))
            return true;

        if (candidate.isInterface() || candidate.isAnnotation())
        {
            addProblem(
                    IProblem.ERROR,
                    location,
                    "web-xml-must-be-class-not-interface",
                    false,
                    IProblem.WEB_XML_INCORRECT_APPLICATION_SERVLET_CLASS);
            return false;
        }

        return superclass.isSuperTypeOf(candidate);

    }

    protected IJavaType checkJavaType(String className, ISourceLocation location)
    {
        IJavaType found = builder.findType(className);
        builder.typeChecked(className, found);

        if (found == null)
            addProblem(IProblem.ERROR, location, DefaultTapestryMessages.format(
                    "unable-to-resolve-class",
                    className), false, IProblem.NOT_QUICK_FIXABLE);

        return found;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.processing.AbstractProcessor#doProcessing(org.w3c.dom.Node)
     */
    protected void doScan()
    {
        Document d = getDOMModel().getDocument();
        Node rootNode = d.getDocumentElement();
        ArrayList<ServletInfo> infos = new ArrayList<ServletInfo>(11);
        Map<String, String> contextParameters = new HashMap<String, String>();
        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet"))
            {
                ServletInfo info = extractServletInfo(node);
                if (info != null)
                    infos.add(info);
                continue;
            }

            if (isElement(node, "context-param"))
            {
                extractContextParameter(node, contextParameters);
            }
        }
        WebAppDescriptor descriptor = (WebAppDescriptor) fResultObject;
        descriptor.setServletInfos((ServletInfo[]) infos.toArray(new ServletInfo[] {}));
        descriptor.setContextParameters(contextParameters);
    }

    private void extractContextParameter(Node contextParamNode, Map<String, String> target)
    {
        String key = null;
        String value = null;
        ISourceLocation keyLoc = null;
        ISourceLocation valueLoc = null;
        for (Node node = contextParamNode.getFirstChild(); node != null; node = node
                .getNextSibling())
        {
            if (isElement(node, "param-name"))
            {
                key = getValue(node);
                keyLoc = getBestGuessSourceLocation(node, true);
                if (key == null)
                {
                    addProblem(
                            IProblem.ERROR,
                            keyLoc,
                            CoreMessages.format("web-xml-context-param-null-key"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);
                    return;
                }
                else if (target.containsKey(key))
                {
                    addProblem(IProblem.ERROR, keyLoc, CoreMessages.format(
                            "web-xml-context-param-duplicate-key",
                            key), false, IProblem.NOT_QUICK_FIXABLE);
                    return;
                }
            }

            if (isElement(node, "param-value"))
            {
                value = getValue(node);
                valueLoc = getBestGuessSourceLocation(node, true);
                if (value == null)
                    addProblem(
                            IProblem.ERROR,
                            valueLoc,
                            CoreMessages.format("web-xml-context-param-null-value"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);
            }
        }
        if (key != null && value != null)
            target.put(key, value);
    }

    protected void cleanup()
    {
    }

    protected ICoreResource getApplicationLocation(ServletInfo info, String path)
    {
        if (path != null)
        {
            return check(builder.classpathRoot, path);
        }
        else
        {

            IResourceRoot context = builder.contextRoot;
            String servletName = info.name;
            String expectedName = servletName + ".application";

            ICoreResource webInfLocation = (ICoreResource) context.getRelativeResource("/WEB-INF/");
            ICoreResource webInfAppLocation = (ICoreResource) webInfLocation
                    .getRelativeResource(servletName + "/");

            ICoreResource result = check(webInfAppLocation, expectedName);
            if (result != null)
                return result;

            return check(webInfLocation, expectedName);
        }
    }

    private ICoreResource check(ICoreResource location, String name)
    {
        if (location == null)
            return null;

        ICoreResource result = (ICoreResource) location.getRelativeResource(name);

        if (result != null && result.exists())
            return result;

        return null;
    }

    private ICoreResource check(IResourceRoot root, String name)
    {
        if (root == null)
            return null;

        ICoreResource result = (ICoreResource) root.getRelativeResource(name);

        if (result != null && result.exists())
            return result;

        return null;
    }

    /**
     * Legacy (pre Tapestry 3.0) applications could only specify the location of the application
     * specification in one way. By overriding the method getApplicationSpecificationPath() in
     * ApplicationServlet.
     * <p>
     * This is Spindle's attempt to support this legacy method. Subclasses must override.
     * 
     * @param servletType
     *            the subclass of org.apache.tapestry.ApplicationServlet
     * @return the path to the Application specification.
     * @throws ScannerException
     *             if the lookup fails to obtain a path
     */
    protected abstract String getApplicationPathFromServletSubclassOverride(IJavaType servletType)
            throws ScannerException;

    /**
     * @param servletNode
     *            a servlet xml node
     * @return an instance of ServletInfo is the node contains a valid Tapestry servlet
     */
    protected ServletInfo extractServletInfo(Node servletNode)
    {
        ServletInfo newInfo = new ServletInfo();
        for (Node node = servletNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet-name"))
            {
                if (!extractServletName(node, newInfo))
                    return null;
                continue;

            }
            if (isElement(node, "servlet-class"))
            {
                if (!extractServletClassAndApplicationSpecLocation(node, newInfo))
                    return null;
                continue;

            }
            if (isElement(node, "init-param"))
            {
                if (!extractServletInitParameter(node, newInfo))
                    return null;
                continue;
            }
        }
        if (AbstractBuildInfrastructure.DEBUG)
        {
            System.out.println("parsing web.xml found servlet:");
            System.out.println(newInfo.toString());
        }
        return newInfo;
    }

    private boolean isTapestryServletOrSubclass(IJavaType candidate, ISourceLocation location)
    {
        if (candidate.equals(builder.tapestryServletType))
            return true;

        return checkJavaSubclassOfImplements(builder.tapestryServletType, candidate, location);
    }

    protected boolean extractServletInitParameter(Node initParamNode, ServletInfo currentInfo)
    {
        String key = null;
        String value = null;
        ISourceLocation keyLoc = null;
        ISourceLocation valueLoc = null;
        for (Node node = initParamNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "param-name"))
            {
                key = getValue(node);
                keyLoc = getBestGuessSourceLocation(node, true);
                if (key == null)
                {
                    addProblem(
                            IProblem.ERROR,
                            keyLoc,
                            CoreMessages.format("web-xml-init-param-null-key"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);
                    return false;
                }
                else if (currentInfo.parameters.containsKey(key))
                {
                    addProblem(IProblem.ERROR, keyLoc, CoreMessages.format(
                            "web-xml-init-param-duplicate-key",
                            key), false, IProblem.NOT_QUICK_FIXABLE);
                    return false;
                }
            }

            if (isElement(node, "param-value"))
            {
                value = getValue(node);
                valueLoc = getBestGuessSourceLocation(node, true);
                if (value == null)
                    addProblem(
                            IProblem.ERROR,
                            valueLoc,
                            CoreMessages.format("web-xml-init-param-null-value"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);

            }
        }
        if (key != null && value != null)
        {
            if (AbstractBuildInfrastructure.APP_SPEC_PATH_PARAM.equals(key))
                checkApplicationServletPathParam(value, currentInfo, valueLoc);

            currentInfo.parameters.put(key, value);

        }
        return true;
    }

    /**
     * update the servlet info with the servlet class defined in the node and find the location of
     * the application specification; logging any problems encountered
     * 
     * @param node
     *            the servlet-class xml node
     * @param newInfo
     *            the ServletInfo to store extracted info into
     * @return true iff
     */
    protected boolean extractServletClassAndApplicationSpecLocation(Node node, ServletInfo newInfo)
    {
        newInfo.classname = getValue(node);
        ISourceLocation nodeLocation = getBestGuessSourceLocation(node, true);
        ICoreResource applicationSpecLocation = null;

        if (newInfo.classname == null)
        {
            String message = CoreMessages.format("web-xml-servlet-null-classname", newInfo.name);
            addProblem(
                    IProblem.WARNING,
                    nodeLocation,
                    message,
                    false,
                    IProblem.WEB_XML_MISSING_APPLICATION_SERVLET_CLASS);
            return false;
        }
        else
        {
            IJavaType servletType = checkJavaType(newInfo.classname, nodeLocation);
            if (servletType != null && isTapestryServletOrSubclass(servletType, nodeLocation))
            {
                newInfo.isServletSubclass = !builder.tapestryServletType.equals(servletType);

                if (newInfo.isServletSubclass)
                {
                    try
                    {
                        String path = getApplicationPathFromServletSubclassOverride(servletType);
                        try
                        {

                            applicationSpecLocation = getApplicationLocation(newInfo, path);
                            checkApplicationLocation(applicationSpecLocation);
                        }
                        catch (ScannerException e)
                        {
                            addProblem(IProblem.ERROR, nodeLocation, CoreMessages.format(
                                    "web-xml-ignore-invalid-application-path",
                                    servletType.getFullyQualifiedName(),
                                    path.toString()), false, IProblem.NOT_QUICK_FIXABLE);
                        }
                    }
                    catch (ScannerException e)
                    {
                        addProblem(
                                IProblem.ERROR,
                                nodeLocation,
                                e.getMessage(),
                                false,
                                IProblem.NOT_QUICK_FIXABLE);
                    }

                }
                else
                {
                    try
                    {
                        applicationSpecLocation = getApplicationLocation(newInfo, null);
                        checkApplicationLocation(applicationSpecLocation);
                    }
                    catch (ScannerException e)
                    {
                        addProblem(IProblem.ERROR, nodeLocation, CoreMessages.format(
                                "web-xml-ignore-invalid-application-path",
                                servletType.getFullyQualifiedName(),
                                null), false, IProblem.NOT_QUICK_FIXABLE);
                    }
                }
            }
        }
        newInfo.applicationSpecLocation = applicationSpecLocation;
        return applicationSpecLocation != null;
    }

    /**
     * Extract the servlet's name and validate it.
     * <ul>
     * <li>Must not be null or an empty string</li>
     * <li>Must not be a duplicate</li>
     * </ul>
     * 
     * @param node
     *            the servlet-name xml node
     * @param newInfo
     *            the ServletInfo that carries the extracted information about the servlet
     * @return iff the node has a valid servlet name
     */
    protected boolean extractServletName(Node node, ServletInfo newInfo)
    {
        String servletName = getValue(node);

        ISourceLocation bestGuessSourceLocation = getBestGuessSourceLocation(node, true);
        if (servletName == null)
        {
            addProblem(IProblem.WARNING, bestGuessSourceLocation, CoreMessages
                    .format("web-xml-servlet-has-null-name"), false, IProblem.NOT_QUICK_FIXABLE);

        }
        else
        {
            if (seenServletNames.contains(servletName))
            {
                addProblem(IProblem.WARNING, bestGuessSourceLocation, CoreMessages.format(
                        "web-xml-servlet-duplicate-name",
                        newInfo.name), false, IProblem.NOT_QUICK_FIXABLE);
            }
            else
            {
                seenServletNames.add(servletName);
                newInfo.name = servletName;
            }
        }
        return newInfo.name != null;
    }

}