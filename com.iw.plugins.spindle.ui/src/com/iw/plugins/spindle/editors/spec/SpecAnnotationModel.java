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

package com.iw.plugins.spindle.editors.spec;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.editors.ProblemAnnotationModel;

/**
 *  Model for Spec annotations - of course only files and not jar entries will
 *  have an annotation model.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecAnnotationModel extends ProblemAnnotationModel
{
    private static final int STAGE_INACTIVE = -1;
    /** accept parser problems**/
    private static final int STAGE_1 = 0;
    /** accept scanner problems**/
    private static final int STAGE_2 = 1;

    private int fStage = STAGE_INACTIVE;

    public SpecAnnotationModel(IFileEditorInput input)
    {
        super(input);
    }

    // Must be a resource
    public void beginCollecting()
    {
        int stage = fStage;
        switch (stage)
        {
            case STAGE_INACTIVE :
                fStage = STAGE_1;
                Object spec = getSpecification();
                if (spec != null)
                    setIsActive(true);
                else
                    setIsActive(false);
                break;
            case STAGE_1 :
                fStage = STAGE_2;
                break;
            default :
                throw new Error("invalid stage");
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.parser.IProblemCollector#endCollecting()
     */
    public void endCollecting()
    {
        int stage = fStage;
        switch (stage)
        {
            case STAGE_1 :
                // do nothing - wait for stage 2
                break;

            case STAGE_2 :
                fStage = STAGE_INACTIVE;
                super.endCollecting();
                break;

            default :
                throw new Error("invalid stage");
        }
    }

    private Object getSpecification()
    {
        IFile file = fInput.getFile();

        IProject project = file.getProject();
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
        {
            return specs.get(file);
        }
        return null;
    }

}
