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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package net.sf.spindle.core.extensions.eclipse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.spindle.core.TapestryCore;
import net.sf.spindle.core.extensions.IBeanSpecificationValidator;
import net.sf.spindle.core.extensions.SpindleExtensionException;
import net.sf.spindle.core.util.Assert;

import org.apache.tapestry.spec.IBeanSpecification;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * BeanSpecificationValidators Container for all of the IBeanSpecificationValidator contributed
 * through the extension point:
 * <ul>
 * <li>com.iw.plugins.spindle.beanSpecificationValidators - extension point</li>
 * </ul>
 * <p>
 * if any exception is thrown during a contributions canResolve() or doResolve() execution, an error
 * is logged and the contribution is removed from the list.
 * 
 * @author glongman@gmail.com
 */
public class EclipseBeanSpecificationValidators implements IBeanSpecificationValidator
{
    public static final String EXTENSION_ID = TapestryCore.IDENTIFIER
            + ".beanSpecificationValidators";

    private static List VALIDATORS;

    public static void clearResolvers()
    {
        VALIDATORS.clear();
    }

    private IBeanSpecificationValidator fValidator;

    public EclipseBeanSpecificationValidators()
    {
        init();
    }

    /**
     * Instantiate all of the contributed resource resolvers. Will log an error and ignore the
     * offending contribution if something bad happens.
     */
    private synchronized void init()
    {
        if (VALIDATORS != null)
            return;

        IExtensionRegistry reg = Platform.getExtensionRegistry();
        IExtensionPoint ep = reg.getExtensionPoint(EXTENSION_ID);
        IExtension[] extensions = ep.getExtensions();
        VALIDATORS = new ArrayList();
        for (int i = 0; i < extensions.length; i++)
        {
            IExtension ext = extensions[i];
            IConfigurationElement[] ce = ext.getConfigurationElements();
            for (int j = 0; j < ce.length; j++)
            {
                Object obj = null;
                try
                {
                    obj = ce[j].createExecutableExtension("class");
                    if (!(obj instanceof IBeanSpecificationValidator))
                    {
                        TapestryCore.log("could not create contribution '"
                                + ext.getUniqueIdentifier() + "'. class '"
                                + ce[j].getAttribute("class")
                                + "' does not implement core.IBeanSpecificationValidator");
                        continue;
                    }
                }
                catch (CoreException e)
                {
                    TapestryCore.log("skipped contribution '" + ext.getUniqueIdentifier() + "'", e);
                }
                VALIDATORS.add(obj);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.extensions.IBeanSpecificationValidator#canValidate(org.apache.tapestry.spec.IBeanSpecification)
     */
    public boolean canValidate(IBeanSpecification bean)
    {
        fValidator = null;
        for (Iterator iter = VALIDATORS.iterator(); iter.hasNext();)
        {
            IBeanSpecificationValidator candidate = (IBeanSpecificationValidator) iter.next();
            try
            {
                if (candidate.canValidate(bean))
                {
                    fValidator = candidate;
                    return true;
                }
            }
            catch (Throwable e)
            {
                TapestryCore.log("exception occured calling " + candidate.getClass().getName()
                        + " canValidate(). This validator has been removed from the list", e);
                iter.remove();
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.extensions.IBeanSpecificationValidator#validate(org.apache.tapestry.spec.IBeanSpecification)
     */
    public void validate(IBeanSpecification bean) throws SpindleExtensionException
    {
        Assert.isTrue(fValidator != null, "Error - called validate before canValidate()");
        try
        {
            fValidator.validate(bean);
        }
        catch (SpindleExtensionException e)
        {
            throw (e);
        }
        catch (Throwable e)
        {
            TapestryCore.log("exception occured calling " + fValidator.getClass().getName()
                    + ".validate(). This validator has been removed from the list", e);

            VALIDATORS.remove(fValidator);
            fValidator = null;
            throw new SpindleExtensionException("Validation exception occured, check the log");
        }
    }
}