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
import java.util.HashMap;
import java.util.Map;

import net.sf.tapestry.PageLoaderException;
import net.sf.tapestry.parse.ITemplateParserDelegate;
import net.sf.tapestry.parse.TemplateParseException;
import net.sf.tapestry.parse.TemplateParser;
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.internal.core.IModelProviderEvent;
import org.eclipse.pde.internal.core.IModelProviderListener;
import org.eclipse.pde.internal.ui.editor.IPDEEditorPage;
import org.eclipse.pde.internal.ui.editor.SystemFileDocumentProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.MessageUtil;
import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.editorjwc.JWCMultipageEditor;
import com.iw.plugins.spindle.editorjwc.components.ComponentsFormPage;
import com.iw.plugins.spindle.editors.SpindleMultipageEditor;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.ModelUtils;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.ui.ToolTipHandler;
import com.iw.plugins.spindle.ui.text.ColorManager;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;
import com.iw.plugins.spindle.util.Utils;
import com.iw.plugins.spindle.wizards.NewTapComponentWizardPage;

public class TapestryHTMLEditor extends TextEditor implements IAdaptable, IModelProviderListener {
	
  static public final String MARKER_ID = TapestryPlugin.MARKER_ID;

  private ISpindleColorManager colorManager = new ColorManager();
  private HTMLContentOutlinePage outline = null;
  private Shell shell;
  private IEditorInput input;
  private StyledText stext;

  private boolean duringInit = false;

  /**
   * Constructor for TapestryHTMLEdiitor
   */
  public TapestryHTMLEditor() {
    super();
    setSourceViewerConfiguration(new TapestryHTMLSourceConfiguration(colorManager));
  }

  public void init(IEditorSite site, IEditorInput input) throws PartInitException {
    duringInit = true;
    setDocumentProvider(createDocumentProvider(input));
    super.init(site, input);
    //    TapestryPlugin.getTapestryModelManager(input.getAdapter(IStorage.class)).addModelProviderListener(this);
    duringInit = false;

  }

  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    shell = parent.getShell();
    stext = (StyledText) getSourceViewer().getTextWidget();

  }

  protected void doSetInput(IEditorInput input) throws CoreException {
    super.doSetInput(input);
    this.input = input;
    outline = createContentOutlinePage(input);
    parseForProblems();
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

  public void openTo(String jwcid) {

    selectAndReveal(0, 0);
    ITypedRegion[] partitions = null;
    IDocument document = getDocumentProvider().getDocument(getEditorInput());
    if (document == null) {

      return;

    }
    try {
      partitions = document.computePartitioning(0, document.getLength() - 1);
      for (int i = 0; i < partitions.length; i++) {

        String type = partitions[i].getType();

        if (type.equals(TapestryHTMLPartitionScanner.JWC_TAG) || type.equals(TapestryHTMLPartitionScanner.JWCID_TAG)) {
          String found = getJWCID(document, partitions[i]);

          if (found != null && jwcid.equals(found)) {

            Position position = findJWCID(document, partitions[i]);
            selectAndReveal(position.getOffset(), position.getLength());

          }
        }
      }
    } catch (BadLocationException e) {
    }
  }

  private String getJWCID(IDocument document, ITypedRegion region) {
    try {
      Position p = findJWCID(document, region);
      if (p == null) {
        return null;
      }
      return document.get(p.getOffset(), p.getLength());
    } catch (BadLocationException blex) {
      return null;
    }
  }

  private Position findJWCID(IDocument document, ITypedRegion region) {
    if (region == null) {
      return null;
    }
    Position result = new Position(region.getOffset(), region.getLength());
    String type = region.getType();
    String start = null;
    if (TapestryHTMLPartitionScanner.JWCID_TAG.equals(type)) {
      start = "jwcid=\"";
    } else if (TapestryHTMLPartitionScanner.JWC_TAG.equals(type)) {
      start = "id=\"";
    }
    if (start != null) {

      try {
        String tag = document.get(region.getOffset(), region.getLength());
        int startIndex = tag.indexOf(start);
        if (startIndex >= 0) {
          startIndex += start.length();
          tag = tag.substring(startIndex);
          int end = tag.indexOf("\"");
          if (end >= 0) {
            result = new Position(region.getOffset() + startIndex, tag.substring(0, end).length());
          }
        } else {
          return null;
        }
      } catch (BadLocationException blex) {
      }
    }
    return result;
  }

  public void dispose() {
    colorManager.dispose();
    //    TapestryPlugin.getTapestryModelManager().removeModelProviderListener(this);
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

  void parseForProblems() {

    try {
      TapestryPlugin.getDefault().getWorkspace().run(new IWorkspaceRunnable() {
        public void run(IProgressMonitor monitor) {
          IEditorInput input = getEditorInput();
          if (input instanceof IStorageEditorInput) {
            return;
          }
          IFile file = (IFile) input.getAdapter(IFile.class);
          removeAllProblemMarkers(file);
          char[] content = getDocumentProvider().getDocument(input).get().toCharArray();

          TemplateParser parser = new TemplateParser();
          TapestryComponentModel model = ModelUtils.findComponentWithHTML(file);
          if (model == null) {
            addProblemMarker("There is no .jwc file for this template.", 0, 0, IMarker.SEVERITY_WARNING);
            return;
          }
          ITemplateParserDelegate delegate = new TemplateParseDelegate(model);
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
        Map map = new HashMap();
        map.put(IMarker.MESSAGE, message);
        map.put(IMarker.SEVERITY, new Integer(severity));
        map.put(IMarker.LINE_NUMBER, new Integer(line));
        map.put(IMarker.CHAR_START, new Integer(column));
        map.put(IMarker.CHAR_END, new Integer(column + 1));
        MarkerUtilities.createMarker((IResource) storage, map, MARKER_ID);
      } catch (CoreException corex) {
      }
    }

  }

  protected void removeAllProblemMarkers(IFile file) {

    int depth = IResource.DEPTH_INFINITE;
    try {
      file.deleteMarkers(IMarker.PROBLEM, true, depth);
    } catch (CoreException e) {
      // something went wrong
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
        new TapestryHTMLPartitionScanner(),
        new String[] {
          TapestryHTMLPartitionScanner.JWC_TAG,
          TapestryHTMLPartitionScanner.JWCID_TAG,
          TapestryHTMLPartitionScanner.HTML_TAG,
          TapestryHTMLPartitionScanner.HTML_COMMENT });
    return partitioner;
  }
  protected IDocumentProvider createDocumentProvider(IEditorInput input) {
    IDocumentProvider documentProvider = null;

    if (input instanceof JarEntryEditorInput) {

      documentProvider = new HTMLStorageDocumentProvider();

    } else {

      Object element = input.getAdapter(IResource.class);

      if (element instanceof IFile) {

        documentProvider = new HTMLFileDocumentProvider();

      } else if (element instanceof File) {

        documentProvider = new SystemFileDocumentProvider(createDocumentPartitioner(), "UTF8");
      }
    }
    return documentProvider;

  }

  class HTMLFileDocumentProvider extends FileDocumentProvider {
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

  }

  class HTMLStorageDocumentProvider extends StorageDocumentProvider {

    /**
    * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createDocument(Object)
    */
    protected IDocument createDocument(Object element) throws CoreException {
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

    public TemplateParseDelegate(TapestryComponentModel model) {

      IStorage underlier = model.getUnderlyingStorage();

      IEditorPart editor = null;

      if (!duringInit) {
        editor = Utils.getEditorFor(underlier);
      }
      if (editor != null && editor instanceof SpindleMultipageEditor) {

        model = (TapestryComponentModel) ((SpindleMultipageEditor) editor).getModel();

      } else {

        try {
          TapestryProjectModelManager mgr = TapestryPlugin.getTapestryModelManager(underlier);
          model = (TapestryComponentModel) mgr.getReadOnlyModel(underlier);
        } catch (CoreException e) {
        }

      }
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

    /**
     * @see net.sf.tapestry.parse.ITemplateParserDelegate#getAllowBody(String, String)
     */
    public boolean getAllowBody(String libraryId, String type) throws PageLoaderException {
      return true;
    }

  }


  static public final String SAVE_HTML_TEMPLATE = "com.iw.plugins.spindle.html.saveTemplateAction";
  static public final String REVERT_HTML_TEMPLATE = "com.iw.plugins.spindle.html.revertTemplateAction";
  static public final String JUMPTO = "com.iw.plugins.spindle.html.jumpToAction";

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
    action = new JumpToAction();
    action.setActionDefinitionId(JUMPTO);
    setAction(JUMPTO, action);

  }

  /**
   * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(IMenuManager)
   */
  protected void editorContextMenuAboutToShow(IMenuManager menu) {
    super.editorContextMenuAboutToShow(menu);
    addAction(menu, SAVE_HTML_TEMPLATE);
    addAction(menu, REVERT_HTML_TEMPLATE);
    JumpToAction jumpTo = (JumpToAction) getAction(JUMPTO);
    jumpTo.configure();
    if (jumpTo.isEnabled()) {
      menu.add(new Separator());
      MenuManager jumpToMenu = new MenuManager("Jump to...");
      addAction(jumpToMenu, JUMPTO);
      menu.add(jumpToMenu);
    }
  }

  /**
   * @see org.eclipse.pde.internal.core.IModelProviderListener#modelsChanged(IModelProviderEvent)
   */
  public void modelsChanged(IModelProviderEvent event) {
    boolean needParse = false;
    IFile documentFile = (IFile) getEditorInput().getAdapter(IFile.class);
    if (documentFile == null) {
      return;
    }

    needParse = checkNeedParse(documentFile, event.getAddedModels());
    if (!needParse) {
      needParse = checkNeedParse(documentFile, event.getChangedModels());
    }
    if (!needParse) {
      needParse = event.getRemovedModels().length > 0;
    }
    if (needParse) {
      parseForProblems();
    }
  }

  /**
   * Method checkNeedParse.
   * @param iModels
   * @return boolean
   */
  private boolean checkNeedParse(IFile documentFile, IModel[] iModels) {
    if (iModels == null) {
      return false;
    }
    for (int i = 0; i < iModels.length; i++) {
      ITapestryModel changedModel = (ITapestryModel) iModels[i];
      try {
        IFile changedFile = (IFile) changedModel.getUnderlyingStorage();
        ITapestryModel model = ModelUtils.findComponentWithHTML(documentFile);
        if (model == null) {
          continue;
        }
        IFile file = (IFile) model.getUnderlyingStorage();

        if (changedFile.equals(file)) {
          return true;
        }
      } catch (ClassCastException e) {
        continue;
      }
    }
    return false;
  }

  private IFile findRelatedComponent() {

    IFile documentFile = (IFile) getEditorInput().getAdapter(IFile.class);
    if (documentFile != null) {

      IContainer parent = documentFile.getParent();
      String name = documentFile.getFullPath().removeFileExtension().lastSegment();

      String fullName = name + ".jwc";
      IFile componentResource = (IFile) parent.findMember(fullName);

      if (componentResource == null) {

        fullName = name + ".page";
        componentResource = (IFile) parent.findMember(fullName);

      }

      if (componentResource != null && componentResource.exists()) {

        return componentResource;

      }
    }
    return null;
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
          contents = comment + contents;
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

  public class JumpToAction extends Action {

    String jwcid = null;
    IStorage relatedComponent = null;

    /**
     * Constructor for SaveHTMLTemplateAction.
     * @param text
     */
    public JumpToAction() {
      super();
    }

    public void configure() {

      setEnabled(false);

      IDocument document = getDocumentProvider().getDocument(getEditorInput());
      if (document == null) {

        return;

      }
      
	  Point p = stext.getSelection();

      int offset = p.x;
      
      if (offset == 0) {
      	
      	offset = stext.getCaretOffset();
      	
      }

      ITypedRegion region = document.getDocumentPartitioner().getPartition(offset);

      jwcid = getJWCID(document, region);

      relatedComponent = findRelatedComponent();

      if (relatedComponent != null) {
      	
      	setText(relatedComponent.getName());

        setEnabled(true);

      }

    }

    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

      IEditorPart editor = Utils.getEditorFor(relatedComponent);

      if (editor != null) {

        TapestryPlugin.getDefault().getActivePage().bringToTop(editor);

      } else {

        TapestryPlugin.openTapestryEditor(relatedComponent);

        editor = Utils.getEditorFor(relatedComponent);

      }

      if (editor != null && editor instanceof JWCMultipageEditor) {

        JWCMultipageEditor jwc = (JWCMultipageEditor) editor;

        ITapestryModel model = (ITapestryModel) jwc.getModel();

        if (!model.isLoaded()) {

          jwc.showPage(jwc.SOURCE_PAGE);

        } else {

          IPDEEditorPage currentPage = (IPDEEditorPage) jwc.getCurrentPage();
          ComponentsFormPage desiredPage = (ComponentsFormPage) jwc.getPage(jwc.COMPONENTS);

          if (currentPage != desiredPage) {
            jwc.showPage(jwc.COMPONENTS);
          }
          desiredPage.openTo(this.jwcid);
        }

      }

    }

  }

}