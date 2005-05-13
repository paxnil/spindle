package com.iw.plugins.spindle.core.builder;

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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.AbstractScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;

/**
 * A Processor class used by FullBuild that extracts Tapestry information from the file web.xml
 * 
 * @author glongman@gmail.com
 */
public class WebXMLScanner extends AbstractScanner
{

    protected FullBuild fBuilder;

    protected IJavaProject fJavaProject;

    protected ArrayList fServletNames;

    protected ArrayList fSeenServletNames;

    /**
     * Constructor for WebXMLProcessor.
     */
    public WebXMLScanner(FullBuild fullBuilder)
    {
        super();
        this.fBuilder = fullBuilder;
        this.fJavaProject = fBuilder.fJavaProject;
    }

    public WebAppDescriptor scanWebAppDescriptor(Object source) throws ScannerException
    {
        Node webxml = ((Document) source).getDocumentElement();
        return (WebAppDescriptor) scan(webxml, null);
    }

    public Object beforeScan(Object source)
    {
        fSeenServletNames = new ArrayList();
        return new WebAppDescriptor();
    }

    protected void checkApplicationLocation(IResourceWorkspaceLocation location)
            throws ScannerException
    {
        if (location == null)
            return;

        if (location.getStorage() == null)
            throw new ScannerException(TapestryCore.getString(
                    "web-xml-ignore-application-path-not-found",
                    location == null ? "no location found" : location.toString()), false,
                    IProblem.NOT_QUICK_FIXABLE);

        IPath ws_path = new Path(location.getName());
        String extension = ws_path.getFileExtension();
        if (extension == null || !extension.equals(TapestryBuilder.APPLICATION_EXTENSION))
            throw new ScannerException(TapestryCore.getString(
                    "web-xml-wrong-file-extension",
                    location.toString()), false, IProblem.NOT_QUICK_FIXABLE);

    }

    protected void checkApplicationServletPathParam(String value, ServletInfo currentInfo,
            ISourceLocation location)
    {
        if (currentInfo.isServletSubclass && currentInfo.applicationSpecLocation != null)
        {
            addProblem(IProblem.WARNING, location, TapestryCore.getString(
                    "web-xml-application-path-param-but-servlet-defines",
                    currentInfo.classname), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }

        if (!value.endsWith(".application"))
        {
            addProblem(IProblem.ERROR, location, TapestryCore.getString(
                    "web-xml-wrong-file-extension",
                    value), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }

        IResourceWorkspaceLocation ws_location = getApplicationLocation(currentInfo, value);
        if (ws_location == null)
        {
            addProblem(IProblem.ERROR, location, TapestryCore.getString(
                    "web-xml-ignore-application-path-not-found",
                    value), false, IProblem.NOT_QUICK_FIXABLE);
            return;
        }
        currentInfo.applicationSpecLocation = ws_location;
    }

    protected boolean checkJavaSubclassOfImplements(IType superclass, IType candidate,
            ISourceLocation location)
    {
        boolean match = false;
        if (superclass.equals(candidate))
            return match;

        try
        {
            if (candidate.isInterface())
                addProblem(
                        IProblem.ERROR,
                        location,
                        "web-xml-must-be-class-not-interface",
                        false,
                        IProblem.WEB_XML_INCORRECT_APPLICATION_SERVLET_CLASS);

            ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
            if (hierarchy.exists())
            {
                IType[] superClasses = hierarchy.getAllSupertypes(candidate);
                for (int i = 0; i < superClasses.length; i++)
                {
                    match = superClasses[i].equals(superclass);
                    if (match)
                        break;

                }
            }
        }
        catch (JavaModelException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
        //        if (!match)
        //            addProblem(IProblem.ERROR, location, "web-xml-does-not-subclass");

        return match;
    }

    protected IType checkJavaType(String className, ISourceLocation location)
    {
        IType found = fBuilder.fTapestryBuilder.getType(className);
        fBuilder.typeChecked(className, found);

        if (found == null)
            addProblem(IProblem.ERROR, location, TapestryCore.getTapestryString(
                    "unable-to-resolve-class",
                    className), false, IProblem.NOT_QUICK_FIXABLE);

        return found;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.iw.plugins.spindle.core.processing.AbstractProcessor#doProcessing(org.w3c.dom.Node)
     */
    protected void doScan(Object source, Object resultObject) throws ScannerException
    {
        Node rootNode = (Node) source;
        WebAppDescriptor descriptor = (WebAppDescriptor) resultObject;
        ArrayList infos = new ArrayList(11);
        Map contextParameters = new HashMap();
        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet"))
            {
                ServletInfo info = getServletInfo(node);
                if (info != null)
                    infos.add(info);
                continue;
            }

            if (isElement(node, "context-param"))
            {
                scanContextParameter(node, contextParameters);
            }
        }
        descriptor.setServletInfos((ServletInfo[]) infos.toArray(new ServletInfo[infos.size()]));
        descriptor.setContextParameters(contextParameters);
    }

    private void scanContextParameter(Node contextParamNode, Map target)
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
                            TapestryCore.getString("web-xml-context-param-null-key"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);
                    return;
                }
                else if (target.containsKey(key))
                {
                    addProblem(IProblem.ERROR, keyLoc, TapestryCore.getString(
                            "web-xml-context-param-duplicate-key",
                            key), false, IProblem.NOT_QUICK_FIXABLE);
                    return;
                }
            }

            if (isElement(node, "param-value"))
            {
                value = getValue(node);
                valueLoc = getBestGuessSourceLocation(node, true);
            }
        }
        if (key != null && value != null)
            target.put(key, value);
    }

    protected void cleanup()
    {
    }

    protected IResourceWorkspaceLocation getApplicationLocation(ServletInfo info, String path)
    {
        if (path != null)
        {
            return check(fBuilder.fTapestryBuilder.fClasspathRoot, path);
        }
        else
        {

            IResourceWorkspaceLocation context = fBuilder.fTapestryBuilder.fContextRoot;
            String servletName = info.name;
            String expectedName = servletName + ".application";

            IResourceWorkspaceLocation webInfLocation = (IResourceWorkspaceLocation) context
                    .getRelativeResource("/WEB-INF/");
            IResourceWorkspaceLocation webInfAppLocation = (IResourceWorkspaceLocation) webInfLocation
                    .getRelativeResource(servletName + "/");

            IResourceWorkspaceLocation result = check(webInfAppLocation, expectedName);
            if (result != null)
                return result;

            return check(webInfLocation, expectedName);
        }
    }

    private IResourceWorkspaceLocation check(IResourceWorkspaceLocation location, String name)
    {
        if (location == null)
            return null;

        IResourceWorkspaceLocation result = (IResourceWorkspaceLocation) location
                .getRelativeResource(name);

        if (result != null && result.getStorage() != null)
            return result;

        return null;
    }

    private String getApplicationPathFromServlet(IType servletType) throws ScannerException
    {
        String result = null;

        try
        {
            IMethod pathMethod = servletType.getMethod(
                    "getApplicationSpecificationPath",
                    new String[0]);
            if (pathMethod != null)
            {
                String methodSource = pathMethod.getSource();
                if (methodSource == null)
                {
                    if (servletType.isBinary())
                        throw new ScannerException(TapestryCore.getString(
                                "builder-error-servlet-subclass-is-binary-attach-source",
                                servletType.getFullyQualifiedName()), false,
                                IProblem.NOT_QUICK_FIXABLE);

                }
                else if (methodSource.trim().length() > 0)
                {
                    if (servletType.isBinary())
                    {
                        int signatureIndex = methodSource
                                .indexOf("public String getApplicationSpecificationPath");
                        if (signatureIndex > 0)
                        {
                            int start = methodSource.indexOf('{', signatureIndex);
                            int end = methodSource.indexOf('}', start);
                            methodSource = methodSource.substring(start, end);
                        }
                        else
                        {
                            return null;
                        }
                    }

                    int start = methodSource.indexOf("return");

                    methodSource = methodSource.substring(start);
                    int first = methodSource.indexOf("\"");
                    int last = methodSource.lastIndexOf("\"");
                    if (first >= 0 && last > first)
                        result = methodSource.substring(first + 1, last);

                }
            }
        }
        catch (JavaModelException e)
        {
            TapestryCore.log("Not a valid Tapestry ApplicationServlet subclass", e);
        }

        return result;
    }

    protected ServletInfo getServletInfo(Node servletNode)
    {
        ServletInfo newInfo = new ServletInfo();
        for (Node node = servletNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet-name"))
            {
                if (!scanServletName(node, newInfo))
                    return null;

            }
            if (isElement(node, "servlet-class"))
            {
                if (!scanServletClass(node, newInfo))
                    return null;

            }
            if (isElement(node, "init-param"))
            {
                try
                {
                    if (!scanInitParam(node, newInfo))
                        return null;

                }
                catch (ScannerException e)
                {
                    TapestryCore.log(e.getMessage());
                    return null;
                }
            }
        }
        if (TapestryBuilder.DEBUG)
        {
            System.out.println("parsing web.xml found servlet:");
            System.out.println(newInfo.toString());
        }
        return newInfo;
    }

    private boolean isTapestryServlet(IType candidate, ISourceLocation location)
    {
        if (candidate == null)
            return false;

        if (candidate.equals(fBuilder.fTapestryServletType))
            return true;

        return checkJavaSubclassOfImplements(fBuilder.fTapestryServletType, candidate, location);

    }

    protected boolean scanInitParam(Node initParamNode, ServletInfo currentInfo)
            throws ScannerException
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
                            TapestryCore.getString("web-xml-init-param-null-key"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);
                    return false;
                }
                else if (currentInfo.parameters.containsKey(key))
                {
                    addProblem(IProblem.ERROR, keyLoc, TapestryCore.getString(
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
                            TapestryCore.getString("web-xml-init-param-null-value"),
                            false,
                            IProblem.NOT_QUICK_FIXABLE);

            }
        }
        if (key != null && value != null)
        {
            if (TapestryBuilder.APP_SPEC_PATH_PARAM.equals(key))
                checkApplicationServletPathParam(value, currentInfo, valueLoc);

            currentInfo.parameters.put(key, value);

        }
        return true;
    }

    protected boolean scanServletClass(Node node, ServletInfo newInfo)
    {
        newInfo.classname = getValue(node);
        ISourceLocation nodeLocation = getBestGuessSourceLocation(node, true);

        if (newInfo.classname == null)
        {
            String message = TapestryCore.getString("web-xml-servlet-null-classname", newInfo.name);
            addProblem(
                    IMarker.SEVERITY_WARNING,
                    nodeLocation,
                    message,
                    false,
                    IProblem.WEB_XML_MISSING_APPLICATION_SERVLET_CLASS);
            return false;
        }

        IType servletType = checkJavaType(newInfo.classname, nodeLocation);
        if (servletType == null)
            return false;

        IResourceWorkspaceLocation location = null;
        if (!isTapestryServlet(servletType, nodeLocation))
        {
            return false;

        }

        if (!fBuilder.fTapestryServletType.equals(servletType))
        {
            newInfo.isServletSubclass = true; // its a subclass
            String path = null;
            try
            {
                path = getApplicationPathFromServlet(servletType);
            }
            catch (ScannerException e1)
            {
                addProblem(IMarker.SEVERITY_ERROR, nodeLocation, e1.getMessage(), false, e1
                        .getCode());
                return false;
            }

            try
            {
                location = getApplicationLocation(newInfo, path);
                checkApplicationLocation(location);
            }
            catch (ScannerException e)
            {
                addProblem(IMarker.SEVERITY_ERROR, nodeLocation, TapestryCore.getString(
                        "web-xml-ignore-invalid-application-path",
                        servletType.getElementName(),
                        path.toString()), false, IProblem.NOT_QUICK_FIXABLE);

                return false;
            }

        }
        else
        {
            try
            {
                location = getApplicationLocation(newInfo, null);
                checkApplicationLocation(location);
            }
            catch (ScannerException e)
            {
                addProblem(IMarker.SEVERITY_ERROR, nodeLocation, TapestryCore.getString(
                        "web-xml-ignore-invalid-application-path",
                        servletType.getElementName(),
                        null), false, IProblem.NOT_QUICK_FIXABLE);

                return false;
            }
        }

        newInfo.applicationSpecLocation = location;
        return true;
    }

    protected boolean scanServletName(Node node, ServletInfo newInfo)
    {
        newInfo.name = getValue(node);
        ISourceLocation bestGuessSourceLocation = getBestGuessSourceLocation(node, true);
        if (newInfo.name == null || "".equals(newInfo.name.trim()))
        {
            addProblem(IProblem.WARNING, bestGuessSourceLocation, TapestryCore
                    .getString("web-xml-servlet-has-null-name"), false, IProblem.NOT_QUICK_FIXABLE);
            return false;

        }
        if (fSeenServletNames.contains(newInfo.name))
        {
            addProblem(IProblem.WARNING, bestGuessSourceLocation, TapestryCore.getString(
                    "web-xml-servlet-duplicate-name",
                    newInfo.name), false, IProblem.NOT_QUICK_FIXABLE);
            return false;
        }
        else
        {
            fSeenServletNames.add(newInfo.name);
        }

        if (fServletNames == null)
            fServletNames = new ArrayList(11);

        if (fServletNames.contains(newInfo.name))
        {
            String message = TapestryCore.getString("web-xml-servlet-duplicate-name", newInfo.name);
            addProblem(
                    IProblem.WARNING,
                    bestGuessSourceLocation,
                    message,
                    false,
                    IProblem.NOT_QUICK_FIXABLE);
            if (TapestryBuilder.DEBUG)
            {
                System.out.println(message);
            }
            return false;
        }
        return true;
    }

}