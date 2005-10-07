package com.iw.plugins.spindle.core.builder;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;



import core.CoreMessages;
import core.IJavaType;
import core.TapestryCore;
import core.builder.AbstractBuild;
import core.builder.WebXMLScanner;
import core.scanning.ScannerException;
import core.source.IProblem;

/**
 * @author gwl
 * 
 */
public class EclipseWebXMLScanner extends WebXMLScanner
{

    public EclipseWebXMLScanner(AbstractBuild fullBuilder)
    {
        super(fullBuilder);
    }
    
    

    /* (non-Javadoc)
     * @see core.builder.WebXMLScanner#getApplicationPathFromServlet(core.IJavaType)
     */
    protected String getApplicationPathFromServletSubclassOverride(IJavaType servletType) throws ScannerException
    {
       String result = null;
       
       IType eServletType = (IType)servletType.getUnderlier();

        try
        {
            IMethod pathMethod = eServletType.getMethod(
                    "getApplicationSpecificationPath",
                    new String[0]);
            if (pathMethod != null)
            {
                String methodSource = pathMethod.getSource();
                if (methodSource == null)
                {
                    if (servletType.isBinary())
                        throw new ScannerException(CoreMessages.format(
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
}
