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
package com.iw.plugins.spindle.editors.template;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.TemplateScanner;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.editors.AnnotationAccess;
import com.iw.plugins.spindle.editors.AnnotationType;
import com.iw.plugins.spindle.editors.IReconcilingEditor;
import com.iw.plugins.spindle.ui.text.ColorManager;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;
import com.iw.plugins.spindle.ui.util.ToolTipHandler;

public class TemplateEditor extends TextEditor implements IAdaptable, IReconcilingEditor
{
    /** Preference key for highlighting current line */
    protected final static String CURRENT_LINE= PreferenceConstants.EDITOR_CURRENT_LINE;
    /** Preference key for highlight color of current line */
    protected final static String CURRENT_LINE_COLOR= PreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
    /** Preference key for showing print marging ruler */
    protected final static String PRINT_MARGIN= PreferenceConstants.EDITOR_PRINT_MARGIN;
    /** Preference key for print margin ruler color */
    protected final static String PRINT_MARGIN_COLOR= PreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
    /** Preference key for print margin ruler column */
    protected final static String PRINT_MARGIN_COLUMN= PreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
    /** Preference key for error indication */
    protected final static String ERROR_INDICATION= PreferenceConstants.EDITOR_PROBLEM_INDICATION;
    /** Preference key for error color */
    protected final static String ERROR_INDICATION_COLOR= PreferenceConstants.EDITOR_PROBLEM_INDICATION_COLOR;
    /** Preference key for warning indication */
    protected final static String WARNING_INDICATION= PreferenceConstants.EDITOR_WARNING_INDICATION;
    /** Preference key for warning color */
    protected final static String WARNING_INDICATION_COLOR= PreferenceConstants.EDITOR_WARNING_INDICATION_COLOR;
    /** Preference key for task indication */
    protected final static String TASK_INDICATION= PreferenceConstants.EDITOR_TASK_INDICATION;
    /** Preference key for task color */
    protected final static String TASK_INDICATION_COLOR= PreferenceConstants.EDITOR_TASK_INDICATION_COLOR;
    /** Preference key for bookmark indication */
    protected final static String BOOKMARK_INDICATION= PreferenceConstants.EDITOR_BOOKMARK_INDICATION;
    /** Preference key for bookmark color */
    protected final static String BOOKMARK_INDICATION_COLOR= PreferenceConstants.EDITOR_BOOKMARK_INDICATION_COLOR;
    /** Preference key for search result indication */
    protected final static String SEARCH_RESULT_INDICATION= PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION;
    /** Preference key for search result color */
    protected final static String SEARCH_RESULT_INDICATION_COLOR= PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_COLOR;
    /** Preference key for unknown annotation indication */
    protected final static String UNKNOWN_INDICATION= PreferenceConstants.EDITOR_UNKNOWN_INDICATION;
    /** Preference key for unknown annotation color */
    protected final static String UNKNOWN_INDICATION_COLOR= PreferenceConstants.EDITOR_UNKNOWN_INDICATION_COLOR;
    /** Preference key for error indication in overview ruler */
    protected final static String ERROR_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_ERROR_INDICATION_IN_OVERVIEW_RULER;
    /** Preference key for warning indication in overview ruler */
    protected final static String WARNING_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_WARNING_INDICATION_IN_OVERVIEW_RULER;
    /** Preference key for task indication in overview ruler */
    protected final static String TASK_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_TASK_INDICATION_IN_OVERVIEW_RULER;
    /** Preference key for bookmark indication in overview ruler */
    protected final static String BOOKMARK_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_BOOKMARK_INDICATION_IN_OVERVIEW_RULER;
    /** Preference key for search result indication in overview ruler */
    protected final static String SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER;
    /** Preference key for unknown annotation indication in overview ruler */
    protected final static String UNKNOWN_INDICATION_IN_OVERVIEW_RULER= PreferenceConstants.EDITOR_UNKNOWN_INDICATION_IN_OVERVIEW_RULER;


    /** The annotation access */
    protected IAnnotationAccess fAnnotationAccess = new AnnotationAccess();

    private ISpindleColorManager colorManager = new ColorManager();
    private TemplateContentOutlinePage outline = null;
    private Shell shell;
    private IEditorInput input;
    private StyledText stext;
    private DebugToolTipHandler handler;

    private TemplateScanner scanner = new TemplateScanner();
    private IScannerValidator validator = new BaseValidator();

    private boolean duringInit = false;

    /**
     * Constructor for TapestryHTMLEdiitor
     */
    public TemplateEditor()
    {
        super();
        setSourceViewerConfiguration(new TemplateSourceConfiguration(colorManager, this));
        setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
        setRangeIndicator(new DefaultRangeIndicator());

    }

    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        duringInit = true;
        setDocumentProvider(createDocumentProvider(input));
        super.init(site, input);
        duringInit = false;
    }

    public void createPartControl(Composite parent)
    {
        super.createPartControl(parent);
        shell = parent.getShell();
        stext = (StyledText) getSourceViewer().getTextWidget();
        handler = new DebugToolTipHandler(shell, getDocumentProvider().getDocument(input));
        handler.activateHoverHelp(stext);
        fSourceViewerDecorationSupport.install(getPreferenceStore());
    }

    protected final ISourceViewer createSourceViewer(Composite parent, IVerticalRuler verticalRuler, int styles)
    {

        fOverviewRuler = new OverviewRuler(fAnnotationAccess, VERTICAL_RULER_WIDTH, colorManager);
        fOverviewRuler.addHeaderAnnotationType(AnnotationType.WARNING);
        fOverviewRuler.addHeaderAnnotationType(AnnotationType.ERROR);

        ISourceViewer viewer =
            new SourceViewer(parent, verticalRuler, fOverviewRuler, true, styles);

        fSourceViewerDecorationSupport =
            new SourceViewerDecorationSupport(viewer, fOverviewRuler, fAnnotationAccess, colorManager);

        configureSourceViewerDecorationSupport();

        return viewer;
    }

    protected void configureSourceViewerDecorationSupport()
    {

        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.UNKNOWN,
            UNKNOWN_INDICATION_COLOR,
            UNKNOWN_INDICATION,
            UNKNOWN_INDICATION_IN_OVERVIEW_RULER,
            0);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.BOOKMARK,
            BOOKMARK_INDICATION_COLOR,
            BOOKMARK_INDICATION,
            BOOKMARK_INDICATION_IN_OVERVIEW_RULER,
            1);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.TASK,
            TASK_INDICATION_COLOR,
            TASK_INDICATION,
            TASK_INDICATION_IN_OVERVIEW_RULER,
            2);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.SEARCH,
            SEARCH_RESULT_INDICATION_COLOR,
            SEARCH_RESULT_INDICATION,
            SEARCH_RESULT_INDICATION_IN_OVERVIEW_RULER,
            3);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.WARNING,
            WARNING_INDICATION_COLOR,
            WARNING_INDICATION,
            WARNING_INDICATION_IN_OVERVIEW_RULER,
            4);
        fSourceViewerDecorationSupport.setAnnotationPainterPreferenceKeys(
            AnnotationType.ERROR,
            ERROR_INDICATION_COLOR,
            ERROR_INDICATION,
            ERROR_INDICATION_IN_OVERVIEW_RULER,
            5);

        fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(CURRENT_LINE, CURRENT_LINE_COLOR);
        fSourceViewerDecorationSupport.setMarginPainterPreferenceKeys(
            PRINT_MARGIN,
            PRINT_MARGIN_COLOR,
            PRINT_MARGIN_COLUMN);

        fSourceViewerDecorationSupport.setSymbolicFontName(getFontPropertyPreferenceKey());
    }

    protected void doSetInput(IEditorInput input) throws CoreException
    {
        super.doSetInput(input);
        this.input = input;
        outline = createContentOutlinePage(input);
    }

    public Object getAdapter(Class clazz)
    {
        Object result = super.getAdapter(clazz);
        if (result == null && IContentOutlinePage.class.equals(clazz))
        {
            result = outline;
        }
        return result;
    }

    public void openTo(String jwcid)
    {
        selectAndReveal(0, 0);
        ITypedRegion[] partitions = null;
        IDocument document = getDocumentProvider().getDocument(getEditorInput());
        if (document == null)
            return;

        try
        {
            partitions = document.computePartitioning(0, document.getLength() - 1);
            for (int i = 0; i < partitions.length; i++)
            {
                String type = partitions[i].getType();

                if (type.equals(TemplatePartitionScanner.JWC_TAG) || type.equals(TemplatePartitionScanner.JWCID_TAG))
                {
                    String found = getJWCID(document, partitions[i]);
                    if (found != null && jwcid.equals(found))
                    {
                        Position position = findJWCID(document, partitions[i]);
                        selectAndReveal(position.getOffset(), position.getLength());
                    }
                }
            }
        } catch (BadLocationException e)
        {}
    }

    private String getJWCID(IDocument document, ITypedRegion region)
    {
        try
        {
            Position p = findJWCID(document, region);
            if (p == null)
            {
                return null;
            }
            return document.get(p.getOffset(), p.getLength());
        } catch (BadLocationException blex)
        {
            return null;
        }
    }

    private Position findJWCID(IDocument document, ITypedRegion region)
    {
        if (region == null)
        {
            return null;
        }
        Position result = new Position(region.getOffset(), region.getLength());
        String type = region.getType();
        String start = null;
        if (TemplatePartitionScanner.JWCID_TAG.equals(type))
        {
            start = "jwcid=\"";
        } else if (TemplatePartitionScanner.JWC_TAG.equals(type))
        {
            start = "id=\"";
        }
        if (start != null)
        {

            try
            {
                String tag = document.get(region.getOffset(), region.getLength());
                int startIndex = tag.indexOf(start);
                if (startIndex >= 0)
                {
                    startIndex += start.length();
                    tag = tag.substring(startIndex);
                    int end = tag.indexOf("\"");
                    if (end >= 0)
                    {
                        result = new Position(region.getOffset() + startIndex, tag.substring(0, end).length());
                    }
                } else
                {
                    return null;
                }
            } catch (BadLocationException blex)
            {}
        }
        return result;
    }

    public void dispose()
    {
        colorManager.dispose();
        super.dispose();
    }

    public TemplateContentOutlinePage createContentOutlinePage(IEditorInput input)
    {
        TemplateContentOutlinePage result = new TemplateContentOutlinePage(this);
        IDocument document = getDocumentProvider().getDocument(input);
        result.setDocument(document);

        result.addSelectionChangedListener(new OutlineSelectionListener());
        IFile documentFile = (IFile) input.getAdapter(IFile.class);
        if (documentFile != null)
        {
            result.setDocumentFile(documentFile);
        }
        return result;
    }

    protected IDocumentPartitioner createDocumentPartitioner()
    {
        RuleBasedPartitioner partitioner =
            new RuleBasedPartitioner(
                new TemplatePartitionScanner(),
                new String[] {
                    TemplatePartitionScanner.JWC_TAG,
                    TemplatePartitionScanner.JWCID_TAG,
                    TemplatePartitionScanner.HTML_TAG,
                    TemplatePartitionScanner.HTML_COMMENT });
        return partitioner;
    }
    protected IDocumentProvider createDocumentProvider(IEditorInput input)
    {
        IDocumentProvider documentProvider = null;

        if (input instanceof JarEntryEditorInput)
        {

            documentProvider = new HTMLStorageDocumentProvider();

        } else
        {

            Object element = input.getAdapter(IResource.class);

            if (element instanceof IFile)
            {

                documentProvider = new HTMLFileDocumentProvider();

            } else if (element instanceof File)
            {

                //        documentProvider = new SystemFileDocumentProvider(createDocumentPartitioner(), "UTF8");
            }
        }
        return documentProvider;

    }

    class HTMLFileDocumentProvider extends FileDocumentProvider
    {
        public IDocument createDocument(Object element) throws CoreException
        {
            IDocument document = super.createDocument(element);
            if (document != null)
            {
                IDocumentPartitioner partitioner = createDocumentPartitioner();
                if (partitioner != null)
                {
                    partitioner.connect(document);
                    document.setDocumentPartitioner(partitioner);
                }
            }
            return document;
        }

        protected IAnnotationModel createAnnotationModel(Object element) throws CoreException
        {
            if (element instanceof IFileEditorInput)
            {
                return new TemplateAnnotationModel((IFileEditorInput) element);
            }

            return super.createAnnotationModel(element);
        }
    }

    class HTMLStorageDocumentProvider extends StorageDocumentProvider
    {
        protected IDocument createDocument(Object element) throws CoreException
        {
            IDocument document = super.createDocument(element);
            if (document != null)
            {
                IDocumentPartitioner partitioner = createDocumentPartitioner();
                if (partitioner != null)
                {
                    partitioner.connect(document);
                    document.setDocumentPartitioner(partitioner);
                }
            }
            return document;
        }

        protected IAnnotationModel createAnnotationModel(Object element) throws CoreException
        {
            if (element instanceof IFileEditorInput)
            {
                return new TemplateAnnotationModel((IFileEditorInput) element);
            }

            return super.createAnnotationModel(element);
        }
    }

    protected class OutlineSelectionListener implements ISelectionChangedListener
    {
        public void selectionChanged(SelectionChangedEvent event)
        {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Position position = (Position) selection.getFirstElement();
            selectAndReveal(position.getOffset(), position.getLength());
        }
    }

    static public final String SAVE_HTML_TEMPLATE = "com.iw.plugins.spindle.html.saveTemplateAction";
    static public final String REVERT_HTML_TEMPLATE = "com.iw.plugins.spindle.html.revertTemplateAction";

    /**
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions()
    {
        super.createActions();

        Action action = new SaveHTMLTemplateAction("Save current as template for New Component Wizards");
        action.setActionDefinitionId(SAVE_HTML_TEMPLATE);
        setAction(SAVE_HTML_TEMPLATE, action);
        action = new RevertTemplateAction("Revert the saved template to the default value");
        action.setActionDefinitionId(REVERT_HTML_TEMPLATE);
        setAction(REVERT_HTML_TEMPLATE, action);

    }

    /**
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
     */
    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {
        super.editorContextMenuAboutToShow(menu);
        addAction(menu, SAVE_HTML_TEMPLATE);
        addAction(menu, REVERT_HTML_TEMPLATE);
    }

    public class SaveHTMLTemplateAction extends Action
    {

        /**
         * Constructor for SaveHTMLTemplateAction.
         * @param text
         */
        public SaveHTMLTemplateAction(String text)
        {
            super(text);
        }

        /**
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run()
        {
            if (MessageDialog
                .openConfirm(
                    getEditorSite().getShell(),
                    "Confirm",
                    "WARNING: all new components/pages created with wizards will use this text as template.\n\nProceed?"))
            {
                IEditorInput input = getEditorInput();
                String contents = getDocumentProvider().getDocument(input).get();
                String comment = TapestryCore.getString("TAPESTRY.xmlComment");
                if (!contents.trim().startsWith(comment))
                {
                    contents = comment + contents;
                }
                IPreferenceStore pstore = getPreferenceStore();
                pstore.setValue(PreferenceConstants.P_HTML_TO_GENERATE, contents);
            }
        }

    }

    public class RevertTemplateAction extends Action
    {
        public RevertTemplateAction(String text)
        {
            super(text);
        }

        public void run()
        {
            if (MessageDialog
                .openConfirm(
                    getEditorSite().getShell(),
                    "Confirm revert to Default",
                    "All new components/pages created with the wizard will use the default template.\n\nProceed?"))
            {
                IEditorInput input = getEditorInput();
                IPreferenceStore pstore = getPreferenceStore();
                pstore.setValue(PreferenceConstants.P_HTML_TO_GENERATE, null);
            }
        }
    }

    /**
     * @param collector
     * @param fProgressMonitor
     */
    public void reconcile(IProblemCollector collector, IProgressMonitor fProgressMonitor)
    {
        boolean didReconcile = false;
        IEditorInput input = getEditorInput();
        if ((input instanceof IFileEditorInput))
        {
            IFile file = ((IFileEditorInput) input).getFile();

            IProject project = file.getProject();
            TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
            Map templates = manager.getTemplateMap(project);
            if (templates != null)
            {
                PluginComponentSpecification component = (PluginComponentSpecification) templates.get(file);
                if (component != null)
                {
                    didReconcile = true;
                    scanner.setExternalProblemCollector(collector);
                    scanner.setPerformDeferredValidations(false);
                    validator.setProblemCollector(scanner);
                    try
                    {
                        scanner.scanTemplate(component, getDocumentProvider().getDocument(input).get(), validator);
                    } catch (ScannerException e)
                    {
                        UIPlugin.log(e);
                    }
                }
            }
        }
        if (!didReconcile)
        {
            collector.beginCollecting();
            collector.endCollecting();
        }
    }

    public class DebugToolTipHandler extends ToolTipHandler
    {

        IDocument document;

        /**
         * Constructor for DebugToolTipHandler.
         * @param parent
         */
        public DebugToolTipHandler(Shell parent, IDocument document)
        {
            super(parent);
            this.document = document;
        }

        /**
         * @see ToolTipHandler#getToolTipHelp(Object)
         */
        protected Object getToolTipHelp(Object object)
        {
            return null;
        }

        /**
         * @see ToolTipHandler#getToolTipImage(Object)
         */
        protected Image getToolTipImage(Object object)
        {
            return null;
        }

        /**
         * @see ToolTipHandler#getToolTipText(Object)
         */
        protected String getToolTipText(Object object, Point widgetPosition)
        {
            StyledText widget = (StyledText) object;
            int currentOffset = widget.getOffsetAtLocation(widgetPosition);
            ITypedRegion region;
            try
            {
                region = document.getPartition(currentOffset);
            } catch (BadLocationException e)
            {
                return "bad location";
            }
            return region.getType();
        }

    }
}