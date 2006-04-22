package net.sf.spindle.core.builder;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.build.AbstractBuild;
import net.sf.spindle.core.build.WebXMLScanner;
import net.sf.spindle.core.eclipse.EclipseMessages;
import net.sf.spindle.core.scanning.ScannerException;
import net.sf.spindle.core.source.IProblem;
import net.sf.spindle.core.types.IJavaType;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author gwl
 */
public class EclipseWebXMLScanner extends WebXMLScanner
{

    public EclipseWebXMLScanner(AbstractBuild fullBuilder)
    {
        super(fullBuilder);
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.builder.WebXMLScanner#getApplicationPathFromServlet(core.IJavaType)
     */
    protected String getApplicationPathFromServletSubclassOverride(IJavaType servletType)
            throws ScannerException
    {
        String result = null;

        IType eServletType = (IType) servletType.getUnderlier();

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
                        throw new ScannerException(EclipseMessages
                                .servletSubclassIsBinaryAttachSource(servletType
                                        .getFullyQualifiedName()), false,
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
            String message = EclipseMessages.errorOccuredAccessingStructureOfServlet(servletType
                    .getFullyQualifiedName());
            TapestryCore.log(message, e);
            throw new ScannerException(message, false, IProblem.NOT_QUICK_FIXABLE);

        }

        return result;
    }
}
