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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.html;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.internal.ui.editor.SystemFileDocumentProvider;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ToolTipHandler;
import com.iw.plugins.spindle.ui.text.*;
import com.iw.plugins.spindle.wizards.NewTapComponentWizardPage;
import net.sf.tapestry.parse.ITemplateParserDelegate;
import net.sf.tapestry.parse.TemplateParseException;
import net.sf.tapestry.parse.TemplateParser;

public class TapestryHTMLEditor extends TextEditor implements IAdaptable {

	private ISpindleColorManager colorManager = new ColorManager();
	private HTMLContentOutlinePage outline = null;
	private Shell shell;
	private DebugToolTipHandler handler;
	private IEditorInput input;
	private StyledText text;

	/**
	 * Constructor for TapestryHTMLEdiitor
	 */
	public TapestryHTMLEditor() {
		super();
		setSourceViewerConfiguration(
			new TapestrySourceConfiguration(colorManager));
	}

	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		setDocumentProvider(
			createDocumentProvider(input.getAdapter(IResource.class)));
		super.init(site, input);
		parseForProblems();
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		shell = parent.getShell();
		text = (StyledText) getSourceViewer().getTextWidget();

		text.setKeyBinding(262144, ST.COPY);
		//text.setKeyBinding(131072, ST.CUT);
		text.setKeyBinding(131072, ST.COPY);
// for debugging the partitioning only		
//		handler = new DebugToolTipHandler(shell, getDocumentProvider().getDocument(input));
//		handler.activateHoverHelp(text);
	}

	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		this.input = input;
		outline = createContentOutlinePage(input);
	}

	/*
	* @see IEditorPart#doSave(IProgressMonitor)
	*/
	public void doSave(IProgressMonitor monitor) {
		parseForProblems();
		super.doSave(monitor);

	}

	/*
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		super.doSaveAs();
		parseForProblems();
	}

	public Object getAdapter(Class clazz) {
		Object result = super.getAdapter(clazz);
		if (result == null && IContentOutlinePage.class.equals(clazz)) {
			result = outline;
		}
		return result;
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

  public HTMLContentOutlinePage createContentOutlinePage(IEditorInput input) {
    HTMLContentOutlinePage result = new HTMLContentOutlinePage(this);
    IDocument document = getDocumentProvider().getDocument(input);
    result.setDocument(document);

    result.addSelectionChangedListener(new OutlineSelectionListener());
    IFile documentFile = (IFile) input.getAdapter(IFile.class);
    if (documentFile != null) {
      result.setDocumentFile(documentFile);
    }
    return result;
  }

  protected void parseForProblems() {

    try {
      TapestryPlugin.getDefault().getWorkspace().run(new IWorkspaceRunnable() {
        public void run(IProgressMonitor monitor) {
          removeAllProblemMarkers();
          IEditorInput input = getEditorInput();
          char[] content = getDocumentProvider().getDocument(input).get().toCharArray();
          TemplateParser parser = new TemplateParser();
          IFile file = (IFile) input.getAdapter(IFile.class);
          ITemplateParserDelegate delegate = new TemplateParseDelegate(file);
          try {
            parser.parse(content, delegate, file.getLocation().toString());
          } catch (TemplateParseException e) {
            addProblemMarker(e.getMessage(), e.getLine(), 1, IMarker.SEVERITY_ERROR);
          }
        }
      }, null);
    } catch (CoreException e) {
      TapestryPlugin.getDefault().logException(e);
    }

  }

  protected void addProblemMarker(String message, int line, int column, int severity) {

    IStorage storage = (IStorage) getEditorInput().getAdapter(IStorage.class);
    if (storage instanceof IResource) {
      try {
        IMarker marker = ((IResource) storage).createMarker("com.iw.plugins.spindle.tapestryproblem");
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.LINE_NUMBER, new Integer(line));
        marker.setAttribute(IMarker.CHAR_START, -1);
        marker.setAttribute(IMarker.CHAR_END, -1);
        //IDocument document = getDocumentProvider().getDocument(getEditorInput());
        //ResourceMarkerAnnotationModel annotater = (ResourceMarkerAnnotationModel)getDocumentProvider().getAnnotationModel(getEditorInput());
        //annotater.updateMarkers(document);

      } catch (CoreException corex) {
        corex.printStackTrace();
      }
    }

  }

  protected void removeAllProblemMarkers() {

    IMarker[] found = findProblemMarkers();
    for (int i = 0; i < found.length; i++) {
      try {
        found[i].delete();
      } catch (CoreException corex) {
      }
    }

  }

  public IMarker[] findProblemMarkers() {
    IStorage storage = (IStorage) getEditorInput().getAdapter(IStorage.class);
    if (storage instanceof IResource) {
      String type = "com.iw.plugins.spindle.tapestryproblem";
      try {
        return ((IResource) storage).findMarkers(type, true, 0);
      } catch (CoreException corex) {
      }
    }
    return new IMarker[0];
  }

  protected IDocumentPartitioner createDocumentPartitioner() {
    RuleBasedPartitioner partitioner =
      new RuleBasedPartitioner(
        new TapestryPartitionScanner(),
        new String[] {
          TapestryPartitionScanner.JWC_TAG,
          TapestryPartitionScanner.JWCID_TAG,
          TapestryPartitionScanner.HTML_TAG,
          TapestryPartitionScanner.HTML_COMMENT });
    return partitioner;
  }
  protected IDocumentProvider createDocumentProvider(Object input) {
    IDocumentProvider documentProvider = null;
    if (input instanceof IFile)
      documentProvider = new UTF8FileDocumentProvider();
    else if (input instanceof File)
      documentProvider = new SystemFileDocumentProvider(createDocumentPartitioner(), "UTF8");
    return documentProvider;
  }
  class UTF8FileDocumentProvider extends FileDocumentProvider {
    public IDocument createDocument(Object element) throws CoreException {
      IDocument document = super.createDocument(element);
      if (document != null) {
        IDocumentPartitioner partitioner = createDocumentPartitioner();
        if (partitioner != null) {
          partitioner.connect(document);
          document.setDocumentPartitioner(partitioner);
        }
      }
      return document;
    }
    protected void setDocumentContent(IDocument document, InputStream contentStream) throws CoreException {

      Reader in = null;

      try {

        in = new InputStreamReader(new BufferedInputStream(contentStream), "UTF8");
        StringBuffer buffer = new StringBuffer();
        char[] readBuffer = new char[2048];
        int n = in.read(readBuffer);
        while (n > 0) {
          buffer.append(readBuffer, 0, n);
          n = in.read(readBuffer);
        }

        document.set(buffer.toString());

      } catch (IOException x) {
        IStatus s = new Status(IStatus.ERROR, null, IStatus.OK, x.getMessage(), x);
        throw new CoreException(s);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException x) {
          }
        }
      }
    }
    protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
      throws CoreException {
      if (element instanceof IFileEditorInput) {

        IFileEditorInput input = (IFileEditorInput) element;
        InputStream stream = null;
        try {
          stream = new ByteArrayInputStream(document.get().getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
        }

        IFile file = input.getFile();

        if (file.exists()) {

          FileInfo info = (FileInfo) getElementInfo(element);

          if (info != null && !overwrite)
            checkSynchronizationState(info.fModificationStamp, file);

          file.setContents(stream, overwrite, true, monitor);

          if (info != null) {

            ResourceMarkerAnnotationModel model = (ResourceMarkerAnnotationModel) info.fModel;
            model.updateMarkers(info.fDocument);

            info.fModificationStamp = computeModificationStamp(file);
          }

        } else {
          try {
            //monitor.beginTask(TextEditorMessages.getString("FileDocumentProvider.task.saving"), 2000); //$NON-NLS-1$
            monitor.beginTask("Saving", 2000);
            ContainerGenerator generator = new ContainerGenerator(file.getParent().getFullPath());
            generator.generateContainer(new SubProgressMonitor(monitor, 1000));
            file.create(stream, false, new SubProgressMonitor(monitor, 1000));
          } finally {
            monitor.done();
          }
        }
      } else {
        super.doSaveDocument(monitor, element, document, overwrite);
      }
    }
  }

  protected class OutlineSelectionListener implements ISelectionChangedListener {
    /**
     * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
      IStructuredSelection selection = (IStructuredSelection) event.getSelection();
      Position position = (Position) selection.getFirstElement();
      selectAndReveal(position.getOffset(), position.getLength());
    }
  }

  /**
   * @version 	1.0
   * @author
   */
  public class TemplateParseDelegate implements ITemplateParserDelegate {

    PluginComponentSpecification spec = null;

    public TemplateParseDelegate(IStorage element) {
      TapestryComponentModel model = TapestryPlugin.getTapestryModelManager().findComponentWithHTML(element);
      if (model != null) {
        spec = model.getComponentSpecification();
      }
    }

    /*
    * @see ITemplateParserDelegate#getAllowBody(String)
    */
    public boolean getAllowBody(String componentId) {
      return true;
    }

    /*
     * @see ITemplateParserDelegate#getKnownComponent(String)
     */
    public boolean getKnownComponent(String componentId) {
      if (spec != null) {
        return spec.getComponent(componentId) != null;
      }
      return true;
    }

  }

  public class DebugToolTipHandler extends ToolTipHandler {

    IDocument document;

    /**
     * Constructor for DebugToolTipHandler.
     * @param parent
     */
    public DebugToolTipHandler(Shell parent, IDocument document) {
      super(parent);
      this.document = document;
    }

    /**
     * @see ToolTipHandler#getToolTipHelp(Object)
     */
    protected Object getToolTipHelp(Object object) {
      return null;
    }

    /**
     * @see ToolTipHandler#getToolTipImage(Object)
     */
    protected Image getToolTipImage(Object object) {
      return null;
    }

    /**
     * @see ToolTipHandler#getToolTipText(Object)
     */
    protected String getToolTipText(Object object) {
      StyledText widget = (StyledText) object;
      int currentOffset = widget.getOffsetAtLocation(widgetPosition);
      ITypedRegion region;
      try {
        region = document.getPartition(currentOffset);
      } catch (BadLocationException e) {
        return "bad location";
      }
      return region.getType();
    }

  }

  static public final String SAVE_HTML_TEMPLATE = "com.iw.plugins.spindle.html.saveTemplateAction";
  static public final String REVERT_HTML_TEMPLATE = "com.iw.plugins.spindle.html.revertTemplateAction";

  /**
   * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
   */
  protected void createActions() {
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
  protected void editorContextMenuAboutToShow(IMenuManager menu) {
    super.editorContextMenuAboutToShow(menu);
    addAction(menu, SAVE_HTML_TEMPLATE);
    addAction(menu, REVERT_HTML_TEMPLATE);
  }

  public class SaveHTMLTemplateAction extends Action {

    /**
     * Constructor for SaveHTMLTemplateAction.
     * @param text
     */
    public SaveHTMLTemplateAction(String text) {
      super(text);
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      if (MessageDialog
        .openConfirm(
          getEditorSite().getShell(),
          "Confirm",
          "WARNING: all new components/pages created with the wizard will use this file as template.\n\nProceed?")) {
        IEditorInput input = getEditorInput();
        String contents = getDocumentProvider().getDocument(input).get();
        String comment = MessageUtil.getString("TAPESTRY.xmlComment");
        if (!contents.trim().startsWith(comment)) {
        	contents = comment+contents;
        }
        IPreferenceStore pstore = TapestryPlugin.getDefault().getPreferenceStore();
        pstore.setValue(NewTapComponentWizardPage.P_HTML_TO_GENERATE, contents);
      }
    }

  }

  public class RevertTemplateAction extends Action {

    /**
     * Constructor for SaveHTMLTemplateAction.
     * @param text
     */
    public RevertTemplateAction(String text) {
      super(text);
    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      if (MessageDialog
        .openConfirm(
          getEditorSite().getShell(),
          "Confirm revert to Default",
          "All new components/pages created with the wizard will use the default template.\n\nProceed?")) {
        IEditorInput input = getEditorInput();
        IPreferenceStore pstore = TapestryPlugin.getDefault().getPreferenceStore();
        pstore.setValue(NewTapComponentWizardPage.P_HTML_TO_GENERATE, null);
      }
    }

  }

}