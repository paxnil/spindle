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

package com.iw.plugins.spindle.editors;

import java.util.Map;

import net.sf.solareclipse.xml.ui.XMLPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.StatusTextEditor;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.editors.actions.BaseEditorAction;
import com.iw.plugins.spindle.editors.actions.JumpToJavaAction;
import com.iw.plugins.spindle.editors.actions.JumpToSpecAction;
import com.iw.plugins.spindle.editors.actions.JumpToTemplateAction;
import com.iw.plugins.spindle.ui.util.PreferenceStoreWrapper;

/**
 *  Abstract base class for Editors.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class Editor extends StatusTextEditor implements IAdaptable, IReconcileWorker
{

    /** Preference key for highlighting current line */
    protected final static String CURRENT_LINE = PreferenceConstants.EDITOR_CURRENT_LINE;

    /** Preference key for highlight color of current line */
    protected final static String CURRENT_LINE_COLOR = PreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

    /** Preference key for showing print marging ruler */
    protected final static String PRINT_MARGIN = PreferenceConstants.EDITOR_PRINT_MARGIN;

    /** Preference key for print margin ruler color */
    protected final static String PRINT_MARGIN_COLOR = PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;

    /** Preference key for print margin ruler column */
    protected final static String PRINT_MARGIN_COLUMN = PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;

    /** Preference key for error indication */
    protected final static String ERROR_INDICATION = PreferenceConstants.EDITOR_PROBLEM_INDICATION;

    /** Preference key for error color */
    protected final static String ERROR_INDICATION_COLOR = PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;

    /** Preference key for warning indication */
    protected final static String WARNING_INDICATION = PreferenceConstants.EDITOR_WARNING_INDICATION;

    /** Preference key for warning color */
    protected final static String WARNING_INDICATION_COLOR = PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;

    /** Preference key for task indication */
    protected final static String TASK_INDICATION = PreferenceConstants.EDITOR_TASK_INDICATION;

    /** Preference key for task color */
    protected final static String TASK_INDICATION_COLOR = PreferenceConstants.EDITOR_TASK_INDICATION_COLOR;

    /** Preference key for bookmark indication */
    protected final static String BOOKMARK_INDICATION = PreferenceConstants.EDITOR_BOOKMARK_INDICATION;

    /** Preference key for bookmark color */
    protected final static String BOOKMARK_INDICATION_COLOR = PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;

    /** Preference key for search result indication */
    protected final static String SEARCH_RESULT_INDICATION = PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;

    /** Preference key for search result color */
    protected final static String SEARCH_RESULT_INDICATION_COLOR =
        PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;

    /** Preference key for unknown annotation indication */
    protected final static String UNKNOWN_INDICATION = PreferenceConstants.EDITOR_UNKNOWN_INDICATION;

    /** Preference key for unknown annotation color */
    protected final static String UNKNOWN_INDICATION_COLOR = PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;

    /** Preference key for error indication in overview ruler */
    protected final static String ERROR_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key for warning indication in overview ruler */
    protected final static String WARNING_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key for task indication in overview ruler */
    protected final static String TASK_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key for bookmark indication in overview ruler */
    protected final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key for search result indication in overview ruler */
    protected final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key for unknown annotation indication in overview ruler */
    protected final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER =
        PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;

    /** Preference key to show/hide the overview ruler */
    protected final static String OVERVIEW_RULER = PreferenceConstants.EDITOR_OVERVIEW_RULER;

    /** contect menu groups for additions */
    protected final static String NAV_GROUP = UIPlugin.PLUGIN_ID + ".navigationGroup";
    protected final static String SOURCE_GROUP = UIPlugin.PLUGIN_ID + ".sourceGroup";

   /** jump action ids */
    protected final static String JUMP_JAVA_ACTION_ID = UIPlugin.PLUGIN_ID + ".editor.commands.jump.java";
    protected final static String JUMP_SPEC_ACTION_ID = UIPlugin.PLUGIN_ID + ".editor.commands.jump.spec";
    protected final static String JUMP_TEMPLATE_ACTION_ID = UIPlugin.PLUGIN_ID + ".editor.commands.jump.template";

    /** The annotation access */
    protected IAnnotationAccess fAnnotationAccess = new ProblemAnnotationAccess();

    /** The overview ruler */
    protected OverviewRuler fOverviewRuler;

    /** The source viewer decoration support */
    protected SourceViewerDecorationSupport fSourceViewerDecorationSupport;

    protected boolean fReadyToReconcile = false;

    protected IEditorInput fInput;

    protected IContentOutlinePage fOutline = null;

    protected ContentAssistant fContentAssistant = new ContentAssistant();

    protected IPreferenceStore fPreferenceStore;

    protected BaseEditorAction[] fJumpActions;

    public Editor()
    {
        super();
        setSourceViewerConfiguration(createSourceViewerConfiguration());
        fPreferenceStore =
            new PreferenceStoreWrapper(
                UIPlugin.getDefault().getPreferenceStore(),
                XMLPlugin.getDefault().getPreferenceStore());
        setPreferenceStore(fPreferenceStore);
        setRangeIndicator(new DefaultRangeIndicator());
        setKeyBindingScopes(new String[] { "com.iw.plugins.spindle.ui.editor.commands" });
    }

    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        setDocumentProvider(createDocumentProvider(input));
        IProject project = UIPlugin.getDefault().getProjectFor(input);
        if (project != null)
            // force a build if necessary
            TapestryArtifactManager.getTapestryArtifactManager().getLastBuildState(project, true);
        super.init(site, input);
    }

    protected void createActions()
    {
        super.createActions();
        Action action = new TextOperationAction(UIPlugin.getDefault().getResourceBundle(), "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
        //Hook the action to the format command (plugin.xml)
        action.setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.format");
        setAction("Format", action);
        // should be updated as the editor state changes
        markAsStateDependentAction("Format", true);
        // action depends on the state of editor selection
        // in this case the format command is not called if there is
        // a text selection in the editor
        markAsSelectionDependentAction("Format", true);

        BaseEditorAction jumpToJava = new JumpToJavaAction();
        jumpToJava.setActiveEditor(this);
        jumpToJava.setActionDefinitionId(JUMP_JAVA_ACTION_ID);
        setAction(JUMP_JAVA_ACTION_ID, jumpToJava);

        BaseEditorAction jumpToSpec = new JumpToSpecAction();
        jumpToSpec.setActiveEditor(this);
        jumpToSpec.setActionDefinitionId(JUMP_SPEC_ACTION_ID);
        setAction(JUMP_SPEC_ACTION_ID, jumpToSpec);

        BaseEditorAction jumpToTemplate = new JumpToTemplateAction();
        jumpToTemplate.setActiveEditor(this);
        jumpToTemplate.setActionDefinitionId(JUMP_TEMPLATE_ACTION_ID);
        setAction(JUMP_TEMPLATE_ACTION_ID, jumpToTemplate);

        fJumpActions = new BaseEditorAction[3];
        fJumpActions[0] = jumpToJava;
        fJumpActions[1] = jumpToSpec;
        fJumpActions[2] = jumpToTemplate;
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        fSourceViewerDecorationSupport.install(getPreferenceStore());
        fReadyToReconcile = true;
    }

    protected void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);
        fInput = input;
        if (fOutline != null)
            fOutline.dispose();
        fOutline = createContentOutlinePage(input);
    }

    public IStorage getStorage()
    {
        IEditorInput input = getEditorInput();
        if (input instanceof JarEntryEditorInput)
            return ((JarEntryEditorInput) input).getStorage();

        return (IStorage) input.getAdapter(IStorage.class);
    }

    public abstract ICoreNamespace getNamespace();

    public Object getSpecification()
    {
        IStorage storage = getStorage();
        IProject project = null;
        if (storage instanceof IFile)
        {
            project = ((IFile) storage).getProject();
        } else
        {
            project = TapestryCore.getDefault().getProjectFor(storage);
        }
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
            return specs.get(storage);

        return null;
    }

    public IResourceWorkspaceLocation getLocation()
    {
        BaseSpecLocatable spec = (BaseSpecLocatable) getSpecification();
        if (spec != null)
            return (IResourceWorkspaceLocation) spec.getSpecificationLocation();

        return null;
    }

    protected void configureSourceViewerDecorationSupport()
    {

        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.UNKNOWN,
            Editor.UNKNOWN_INDICATION_COLOR,
            Editor.UNKNOWN_INDICATION,
            Editor.UNKNOWN_INDICATION_IN_OVERVIEW_RULER,
            0);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.BOOKMARK,
            Editor.BOOKMARK_INDICATION_COLOR,
            Editor.BOOKMARK_INDICATION,
            Editor.BOOKMARK_INDICATION_IN_OVERVIEW_RULER,
            1);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.TASK,
            Editor.TASK_INDICATION_COLOR,
            Editor.TASK_INDICATION,
            Editor.TASK_INDICATION_IN_OVERVIEW_RULER,
            2);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.SEARCH,
            Editor.SEARCH_RESULT_INDICATION_COLOR,
            Editor.SEARCH_RESULT_INDICATION,
            Editor.SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER,
            3);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.WARNING,
            Editor.WARNING_INDICATION_COLOR,
            Editor.WARNING_INDICATION,
            Editor.WARNING_INDICATION_IN_OVERVIEW_RULER,
            4);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            ProblemAnnotationType.ERROR,
            Editor.ERROR_INDICATION_COLOR,
            Editor.ERROR_INDICATION,
            Editor.ERROR_INDICATION_IN_OVERVIEW_RULER,
            5);

        fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(
            Editor.CURRENT_LINE,
            Editor.CURRENT_LINE_COLOR);
        fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(
            Editor.PRINT_MARGIN,
            Editor.PRINT_MARGIN_COLOR,
            Editor.PRINT_MARGIN_COLUMN);

        fSourceViewerDecorationSupport.setSymbolicFontName(getFontPropertyPreferenceKey());
    }

    protected void showOverviewRuler()
    {
        if (fOverviewRuler != null)
        {
            if (getSourceViewer() instanceof ISourceViewerExtension)
            {
                ((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(true);
                fSourceViewerDecorationSupport.updateOverviewDecorations();
            }
        }
    }

    protected boolean isOverviewRulerVisible()
    {
        IPreferenceStore store = getPreferenceStore();
        return store.getBoolean(Editor.OVERVIEW_RULER);
    }

    protected final ISourceViewer createSourceViewer(Composite parent, IVerticalRuler verticalRuler, int styles)
    {
        ISharedTextColors sharedColors = UIPlugin.getDefault().getSharedTextColors();
        fOverviewRuler = new OverviewRuler(fAnnotationAccess, VERTICAL_RULER_WIDTH, sharedColors);
        fOverviewRuler.addHeaderAnnotationType(ProblemAnnotationType.WARNING);
        fOverviewRuler.addHeaderAnnotationType(ProblemAnnotationType.ERROR);

        ISourceViewer viewer =
            new SourceViewer(parent, verticalRuler, fOverviewRuler, isOverviewRulerVisible(), styles);

        fSourceViewerDecorationSupport =
            new SourceViewerDecorationSupport(viewer, fOverviewRuler, fAnnotationAccess, sharedColors);

        configureSourceViewerDecorationSupport();

        return viewer;
    }

    protected void hideOverviewRuler()
    {
        if (getSourceViewer() instanceof ISourceViewerExtension)
        {
            fSourceViewerDecorationSupport.hideAnnotationOverview();
            ((ISourceViewerExtension) getSourceViewer()).showAnnotationsOverview(false);
        }
    }

    public abstract IContentOutlinePage createContentOutlinePage(IEditorInput input);

    public ContentAssistant getContentAssistant()
    {
        if (fContentAssistant == null)
            fContentAssistant = new ContentAssistant();
        return fContentAssistant;
    }

    public Object getAdapter(Class clazz)
    {
        Object result = super.getAdapter(clazz);
        if (result == null && IContentOutlinePage.class.equals(clazz))
        {
            result = fOutline;
        }
        return result;
    }

    protected abstract IDocumentProvider createDocumentProvider(IEditorInput input);

    protected abstract SourceViewerConfiguration createSourceViewerConfiguration();

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.ISelfReconcilingEditor#isReadyToReconcile()
     */
    public boolean isReadyToReconcile()
    {
        return fReadyToReconcile;
    }

    public int getCaretOffset()
    {
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer == null || fOutline == null)
            return -1;

        StyledText styledText = sourceViewer.getTextWidget();
        if (styledText == null)
            return -1;
        int caret = 0;
        if (sourceViewer instanceof ITextViewerExtension3)
        {
            ITextViewerExtension3 extension = (ITextViewerExtension3) sourceViewer;
            caret = extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
        } else
        {
            int offset = sourceViewer.getVisibleRegion().getOffset();
            caret = offset + styledText.getCaretOffset();
        }
        return caret;
    }

}