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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.FullBuild.ServletInfo;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.resources.ClasspathResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.ContextResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.scanning.AbstractScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;

/**
 * A Processor class used by FullBuild that extracts Tapestry information from the file web.xml
 * 
 * @version $Id$
 * @author glongman@intelligentworks.com
 */
public class WebXMLScanner extends AbstractScanner
{

    public static final String NULL_NAME = "web-xml-servlet-has-null-name";
    public static final String DUPLICATE_NAME = "web-xml-servlet-duplicate-name";
    public static final String NULL_CLASSNAME = "web-xml-servlet-null-classname";
    public static final String IGNORE_NOT_TAPESTRY = "web-xml-ignore-servlet-not-tapestry";
    public static final String IGNORE_INVALID_APPLICATION_PATH = "web-xml-ignore-invalid-application-path";
    public static final String IGNORE_APPLICATION_PATH_NOT_FOUND = "web-xml-ignore-application-path-not-found";
    public static final String DUPLICATE_APPLICATION_PATH_SUBCLASS = "web-xml-application-path-param-but-servlet-defines";
    public static final String DUPLICATE_APPLICATION_PATH = "web-xml-duplicate-application-path";
    public static final String NULL_APPLICATION_PATH = "web-xml-null-application-path";
    public static final String WRONG_FILE_EXTENSION = "web-xml-wrong-file-extension";

    protected FullBuild builder;
    protected IJavaProject javaProject;
    protected ArrayList servletNames;

    /**
     * Constructor for WebXMLProcessor.
     */
    public WebXMLScanner(FullBuild fullBuilder)
    {
        super();
        this.builder = fullBuilder;
        this.javaProject = builder.javaProject;
    }

    public FullBuild.ServletInfo[] getServletInformation(Node webxml) throws ScannerException
    {
        List result = (List) scan(builder.parser, null, webxml);
        return (FullBuild.ServletInfo[]) result.toArray(new FullBuild.ServletInfo[result.size()]);
    }

    public Object beforeScan(Node node)
    {
        return new ArrayList(11);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.processing.AbstractProcessor#doProcessing(org.w3c.dom.Node)
     */
    protected void doScan(Object resultObject, Node rootNode) throws ScannerException
    {
        ArrayList infos = (ArrayList) resultObject;
        for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet"))
            {
                FullBuild.ServletInfo info = getServletInfo(node);
                if (info != null && info.applicationSpecLocation != null)
                {
                    infos.add(info);
                }
            }
        }
    }

    protected FullBuild.ServletInfo getServletInfo(Node servletNode)
    {
        FullBuild.ServletInfo newInfo = builder.new ServletInfo();
        for (Node node = servletNode.getFirstChild(); node != null; node = node.getNextSibling())
        {
            if (isElement(node, "servlet-name"))
            {
                if (!processServletName(node, newInfo))
                {
                    return null;
                }
            }
            if (isElement(node, "servlet-class"))
            {
                if (!processServletClass(node, newInfo))
                {
                    return null;
                }
            }
            if (isElement(node, "init-param"))
            {
                try
                {
                    getInitParam(node, newInfo);
                } catch (ScannerException e)
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
    protected boolean processServletName(Node node, FullBuild.ServletInfo newInfo)
    {
        newInfo.name = getValue(node);
        if (newInfo.name == null || "".equals(newInfo.name.trim()))
        {
            addProblem(IProblem.WARNING, getBestGuessSourceLocation(node, true), TapestryCore.getString(NULL_NAME));
            return false;

        }
        if (servletNames == null)
        {
            servletNames = new ArrayList(11);
        }
        if (servletNames.contains(newInfo.name))
        {
            String message = TapestryCore.getString(DUPLICATE_NAME, newInfo.name);
            addProblem(IProblem.WARNING, getBestGuessSourceLocation(node, true), message);
            if (TapestryBuilder.DEBUG)
            {
                System.out.println(message);
            }
            return false;
        }
        return true;
    }
    protected boolean processServletClass(Node node, FullBuild.ServletInfo newInfo)
    {
        newInfo.classname = getValue(node);
        ISourceLocation nodeLocation = getBestGuessSourceLocation(node, true);

        if (newInfo.classname == null)
        {
            String message = TapestryCore.getString(NULL_CLASSNAME, newInfo.name);
            addProblem(IMarker.SEVERITY_WARNING, nodeLocation, message);
            return false;
        }

        IType servletType = checkJavaType(newInfo.classname, nodeLocation);
        if (servletType == null)
        {
            return false;
        }
        if (!isTapestryServlet(servletType, nodeLocation))
        {
            return false;

        } else if (!builder.tapestryServletType.equals(servletType))
        {
            newInfo.isServletSubclass = true; // its a subclass
            IPath path = getApplicationPathFromServlet(servletType);
            if (path != null)
            {
                IResourceWorkspaceLocation location = null;
                try
                {
                    location = getApplicationLocation(path.toString());
                    checkApplicationLocation(location);
                } catch (ScannerException e)
                {
                    addProblem(
                        IMarker.SEVERITY_ERROR,
                        nodeLocation,
                        TapestryCore.getString(IGNORE_INVALID_APPLICATION_PATH, servletType.getElementName(), path.toString()));

                    return false;
                }
                newInfo.applicationSpecLocation = location;
            }
        }
        return true;
    }

    protected void checkApplicationServletPathParam(String value, FullBuild.ServletInfo currentInfo, ISourceLocation location)
    {
        if (currentInfo.isServletSubclass && currentInfo.applicationSpecLocation != null)
        {
            addProblem(IProblem.ERROR, location, TapestryCore.getString(DUPLICATE_APPLICATION_PATH_SUBCLASS));
            return;
        }
        IResourceWorkspaceLocation ws_location = getApplicationLocation(value);
        if (ws_location.getStorage() == null)
        {
            addProblem(IProblem.ERROR, location, TapestryCore.getString(IGNORE_APPLICATION_PATH_NOT_FOUND, value));
            return;
        }
        currentInfo.applicationSpecLocation = ws_location;
    }

    protected IType checkJavaType(String className, ISourceLocation location)
    {
        IType found = builder.tapestryBuilder.getType(className);
        if (found == null)
        {
            addProblem(
                IProblem.ERROR,
                location,
                TapestryCore.getTapestryString(TapestryBuilder.TAPESTRY_CLASS_NOT_FOUND, className));
        }
        return found;
    }

    protected boolean checkJavaSubclassOfImplements(IType superclass, IType candidate, ISourceLocation location)
    {
        boolean match = false;
        try
        {
            if (candidate.isInterface())
            {
                addProblem(IProblem.ERROR, location, "web-xml-must-be-class-not-interface");
            }
            ITypeHierarchy hierarchy = candidate.newSupertypeHierarchy(null);
            if (hierarchy.exists())
            {
                IType[] superClasses = hierarchy.getAllSupertypes(candidate);
                for (int i = 0; i < superClasses.length; i++)
                {
                    if (superClasses[i].equals(superclass))
                    {
                        match = true;
                    }
                }
            }
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }
        if (!match)
        {
            addProblem(IProblem.ERROR, location, "web-xml-does-not-subclass");
        }
        return match;
    }

    protected void getInitParam(Node initParamNode, FullBuild.ServletInfo currentInfo) throws ScannerException
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
                    addProblem(IProblem.ERROR, keyLoc, "NULL KEY");
                } else if (currentInfo.parameters.containsKey(key))
                {
                    addProblem(IProblem.ERROR, keyLoc, "DUPLICATE KEY");
                    key = null;
                }
            }

            if (isElement(node, "param-value"))
            {
                value = getValue(node);
                valueLoc = getBestGuessSourceLocation(node, true);
                if (value == null)
                {
                    addProblem(IProblem.ERROR, valueLoc, "NULL VALUE");
                }
            }
        }
        if (key != null && value != null)
        {
            if (TapestryBuilder.APP_SPEC_PATH_PARAM.equals(key))
            {
                checkApplicationServletPathParam(value, currentInfo, valueLoc);
            } else
            {
                currentInfo.parameters.put(key, value);
            }
        }

        //        Map map = currentInfo.parameters;
        //        String value = null;
        //        boolean appSpecLocationSpecified = currentInfo.applicationSpecLocation != null;
        //        for (Node node = initParamNode.getFirstChild(); node != null; node = node.getNextSibling())
        //        {
        //            String currentKey = null;
        //            if (isElement(node, "param-name"))
        //            {
        //                currentKey = getValue(node);
        //                if (value == null)
        //                {
        //                    addProblem(IProblem.ERROR, getBestGuessSourceLocation(node, true), "NULL param-name");
        //                    continue;
        //                }
        //                boolean isAppSpecLocation = TapestryBuilder.APP_SPEC_PATH_PARAM.equals(currentKey);
        //                if (isAppSpecLocation)
        //                {
        //
        //                    if (appSpecLocationSpecified)
        //                    {
        //                        addProblem(
        //                            IProblem.WARNING,
        //                            getBestGuessSourceLocation(node, true),
        //                            TapestryCore.getString(DUPLICATE_APPLICATION_PATH_SUBCLASS));
        //                        return;
        //                    } else if (map.containsKey(TapestryBuilder.APP_SPEC_PATH_PARAM))
        //                    {
        //                        addProblem(
        //                            IMarker.SEVERITY_WARNING,
        //                            getBestGuessSourceLocation(node, true),
        //                            TapestryCore.getString(DUPLICATE_APPLICATION_PATH, map.get(TapestryBuilder.APP_SPEC_PATH_PARAM)));
        //                        return;
        //                    }
        //                }
        //            }
        //            if (isElement(node, "param-value"))
        //            {
        //                if (currentKey == null)
        //                {
        //                    continue;
        //                }
        //                value = getValue(node);
        //                if (TapestryBuilder.APP_SPEC_PATH_PARAM.equals(currentKey) && !appSpecLocationSpecified)
        //                {
        //                    if (value == null)
        //                    {
        //                        addProblem(
        //                            IMarker.SEVERITY_ERROR,
        //                            getBestGuessSourceLocation(node, true),
        //                            TapestryCore.getString(NULL_APPLICATION_PATH));
        //                        return;
        //                    } else
        //                    {
        //                        IPath path = new Path(value);
        //                        IResourceWorkspaceLocation location = null;
        //                        try
        //                        {
        //                            location = checkApplicationLocation(path);
        //                        } catch (ScannerException e)
        //                        {
        //                            addProblem(
        //                                IMarker.SEVERITY_ERROR,
        //                                getBestGuessSourceLocation(node, true),
        //                                "invalid value for 'org.apache.tapestry.application-specification': " + e.getMessage());
        //                            throw e;
        //                        }
        //                        currentInfo.applicationSpecLocation = location;
        //
        //                    }
        //                }
        //            }
        //            if (currentKey != null && value != null)
        //            {
        //                map.put(currentKey, value);
        //            }
        //        }
    }

    protected void checkApplicationLocation(IResourceWorkspaceLocation location) throws ScannerException
    {
        IPath ws_path = new Path(location.getPath());
        String extension = ws_path.getFileExtension();
        if (extension == null || !extension.equals(TapestryBuilder.APPLICATION_EXTENSION))
        {
            throw new ScannerException(TapestryCore.getString(WRONG_FILE_EXTENSION, location.toString()));
        }

        if (location.getStorage() == null)
        {
            throw new ScannerException(TapestryCore.getString(IGNORE_APPLICATION_PATH_NOT_FOUND, location.toString()));
        }
    }

    protected IResourceWorkspaceLocation getApplicationLocation(String path)
    {
        if (path == null)
        {
            return null;
        }
        IPath ws_path = new Path(path);
        IResourceWorkspaceLocation location = new ClasspathResourceWorkspaceLocation(builder.javaProject, path.toString());
        if (location.getStorage() == null)
        {
            location = new ContextResourceWorkspaceLocation(builder.tapestryBuilder.contextRoot, path.toString());

        }
        return location;
    }

    private boolean isTapestryServlet(IType candidate, ISourceLocation location)
    {
        if (candidate != null)
        {
            if (!candidate.equals(builder.tapestryServletType))
            {
                return checkJavaSubclassOfImplements(builder.tapestryServletType, candidate, location);
            }

        }
        return false;
    }

    private IPath getApplicationPathFromServlet(IType servletType)
    {
        IPath result = null;
        try
        {
            IMethod pathMethod = servletType.getMethod("getApplicationSpecificationPath", new String[0]);
            if (pathMethod != null)
            {
                String methodSource = pathMethod.getSource();
                if (methodSource != null && !"".equals(methodSource.trim()))
                {
                    int start = methodSource.indexOf("return");
                    methodSource = methodSource.substring(start);
                    int first = methodSource.indexOf("\"");
                    int last = methodSource.lastIndexOf("\"");
                    if (first >= 0 && last > first)
                    {
                        result = new Path(methodSource.substring(first + 1, last));
                    }
                }
            }
        } catch (JavaModelException e)
        {
            TapestryCore.log(e);
        }
        return result;
    }

}
