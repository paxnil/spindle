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

package com.iw.plugins.spindle.editors;

import java.util.Map;

import net.sf.solareclipse.xml.ui.XMLPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.editors.actions.BaseEditorAction;
import com.iw.plugins.spindle.editors.actions.JumpToJavaAction;
import com.iw.plugins.spindle.editors.actions.JumpToNextAttributeAction;
import com.iw.plugins.spindle.editors.actions.JumpToNextTagAction;
import com.iw.plugins.spindle.editors.actions.JumpToSpecAction;
import com.iw.plugins.spindle.editors.actions.JumpToTemplateAction;
import com.iw.plugins.spindle.ui.util.PreferenceStoreWrapper;

/**
 * Abstract base class for Editors.
 * 
 * @author glongman@gmail.com
 *  
 */
public abstract class Editor extends TextEditor implements IAdaptable, IReconcileWorker
{

  class PreferenceListener implements IPropertyChangeListener
  {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
      IPreferenceStore store = (IPreferenceStore) event.getSource();
      if (PreferenceConstants.AUTO_ACTIVATE_CONTENT_ASSIST.equals(event.getProperty())
          && fContentAssistant != null)
      {
        fContentAssistant.enableAutoInsert(store
            .getBoolean(PreferenceConstants.AUTO_ACTIVATE_CONTENT_ASSIST));
      }
    }
  }

  /** contect menu groups for additions */
  protected final static String NAV_GROUP = "navigation";
  protected final static String SHOW_GROUP = "show";
  protected final static String SOURCE_GROUP = "source";

  /** jump action ids */
  protected final static String JUMP_JAVA_ACTION_ID = UIPlugin.PLUGIN_ID
      + ".editor.commands.jump.java";
  protected final static String JUMP_SPEC_ACTION_ID = UIPlugin.PLUGIN_ID
      + ".editor.commands.jump.spec";
  protected final static String JUMP_TEMPLATE_ACTION_ID = UIPlugin.PLUGIN_ID
      + ".editor.commands.jump.template";

  protected boolean fReadyToReconcile = false;

  protected IEditorInput fInput;

  protected IContentOutlinePage fOutline = null;

  protected ContentAssistant fContentAssistant;

  protected IPreferenceStore fPreferenceStore;

  protected BaseEditorAction[] fJumpActions;

  private IPropertyChangeListener fPreferenceListener;

  public Editor()
  {
    super();
    setSourceViewerConfiguration(createSourceViewerConfiguration());
    //    fPreferenceStore = new PreferenceStoreWrapper(UIPlugin
    //        .getDefault()
    //        .getPreferenceStore(), XMLPlugin.getDefault().getPreferenceStore());
    //
    //    setPreferenceStore(fPreferenceStore);
    setRangeIndicator(new DefaultRangeIndicator());
    setKeyBindingScopes(new String[]{"com.iw.plugins.spindle.ui.editor.commands"});

  }

  protected void initializeEditor()
  {
    super.initializeEditor();
    IPreferenceStore store = createCombinedPreferenceStore();
    fPreferenceListener = new PreferenceListener();
    store.addPropertyChangeListener(fPreferenceListener);
    setPreferenceStore(store);
    setCompatibilityMode(false);
  }

  /**
   * Creates a combined preference store, this store is read-only.
   * 
   * @return the combined preference store
   * @since 3.0
   */
  private IPreferenceStore createCombinedPreferenceStore()
  {
    IPreferenceStore store = new PreferenceStoreWrapper(UIPlugin
        .getDefault()
        .getPreferenceStore(), XMLPlugin.getDefault().getPreferenceStore());
    IPreferenceStore generalTextStore = EditorsUI.getPreferenceStore();
    return new ChainedPreferenceStore(new IPreferenceStore[]{store, generalTextStore});
  }

  public void init(IEditorSite site, IEditorInput input) throws PartInitException
  {
    setDocumentProvider(createDocumentProvider(input));
    super.init(site, input);
  }

  /**
   * Creates the annotation access for this editor.
   * 
   * @return the created annotation access
   */
  protected IAnnotationAccess createAnnotationAccess()
  {
    return new ProblemAnnotationAccess();
  }

  protected void createActions()
  {
    super.createActions();

    Action action = new TextOperationAction(
        UIPlugin.getDefault().getResourceBundle(),
        "Format.",
        this,
        ISourceViewer.FORMAT);
    //Hook the action to the format command (plugin.xml)
    action.setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.format");
    setAction("Format", action);
    // should be updated as the editor state changes
    markAsStateDependentAction("Format", true);
    // action depends on the state of editor selection
    // in this case the format command is not called if there is
    // a text selection in the editor
    markAsSelectionDependentAction("Format", true);

    JumpToNextAttributeAction jumpNavNext = new JumpToNextAttributeAction(true);
    jumpNavNext.setActiveEditor(null, this);
    jumpNavNext
        .setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.navigate.attributeRight");
    setAction(
        "com.iw.plugins.spindle.ui.editor.commands.navigate.attributeRight",
        jumpNavNext);

    JumpToNextAttributeAction jumpNavPrevious = new JumpToNextAttributeAction(false);
    jumpNavPrevious.setActiveEditor(null, this);
    jumpNavPrevious
        .setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.navigate.attributeLeft");
    setAction(
        "com.iw.plugins.spindle.ui.editor.commands.navigate.attributeLeft",
        jumpNavPrevious);

    JumpToNextTagAction jumpNextTag = new JumpToNextTagAction(true);
    jumpNextTag.setActiveEditor(null, this);
    jumpNextTag
        .setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.navigate.attributeDown");
    setAction(
        "com.iw.plugins.spindle.ui.editor.commands.navigate.attributeDown",
        jumpNextTag);

    JumpToNextTagAction jumpPreviousTag = new JumpToNextTagAction(false);
    jumpPreviousTag.setActiveEditor(null, this);
    jumpPreviousTag
        .setActionDefinitionId("com.iw.plugins.spindle.ui.editor.commands.navigate.attributeUp");
    setAction(
        "com.iw.plugins.spindle.ui.editor.commands.navigate.attributeUp",
        jumpPreviousTag);

    BaseEditorAction jumpToJava = new JumpToJavaAction();
    jumpToJava.setActiveEditor(null, this);
    jumpToJava.setActionDefinitionId(JUMP_JAVA_ACTION_ID);
    setAction(JUMP_JAVA_ACTION_ID, jumpToJava);

    BaseEditorAction jumpToSpec = new JumpToSpecAction();
    jumpToSpec.setActiveEditor(null, this);
    jumpToSpec.setActionDefinitionId(JUMP_SPEC_ACTION_ID);
    setAction(JUMP_SPEC_ACTION_ID, jumpToSpec);

    BaseEditorAction jumpToTemplate = new JumpToTemplateAction();
    jumpToTemplate.setActiveEditor(null, this);
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
  
  

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#editorContextMenuAboutToShow(org.eclipse.jface.action.IMenuManager)
     */
    protected void editorContextMenuAboutToShow(IMenuManager menu)
    {        
        super.editorContextMenuAboutToShow(menu);
        menu.appendToGroup(ITextEditorActionConstants.GROUP_UNDO, new Separator(NAV_GROUP));	
		menu.insertAfter(NAV_GROUP, new GroupMarker(SHOW_GROUP));
		menu.insertAfter(ITextEditorActionConstants.GROUP_COPY, new Separator(SOURCE_GROUP));
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
    IProject project = (IProject)storage.getAdapter(IProject.class);    
    TapestryArtifactManager manager = TapestryArtifactManager
        .getTapestryArtifactManager();
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

  public abstract IContentOutlinePage createContentOutlinePage(IEditorInput input);

  public ContentAssistant getContentAssistant()
  {
    if (fContentAssistant == null)
    {
      fContentAssistant = new ContentAssistant();
      IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
      fContentAssistant.enableAutoInsert(store
          .getBoolean(PreferenceConstants.AUTO_ACTIVATE_CONTENT_ASSIST));
    }
    return fContentAssistant;
  }

  public Object getAdapter(Class clazz)
  {
    Object result = super.getAdapter(clazz);
    if (result == null && IContentOutlinePage.class.equals(clazz))
    {
      return fOutline;
    }
    return super.getAdapter(clazz);
  }

  protected abstract IDocumentProvider createDocumentProvider(IEditorInput input);

  protected abstract SourceViewerConfiguration createSourceViewerConfiguration();

  /*
   * (non-Javadoc)
   * 
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
    if (sourceViewer instanceof ITextViewerExtension5)
    {
      ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
      caret = extension.widgetOffset2ModelOffset(styledText.getCaretOffset());
    } else
    {
      int offset = sourceViewer.getVisibleRegion().getOffset();
      caret = offset + styledText.getCaretOffset();
    }
    return caret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {

    super.dispose();
    if (fPreferenceListener != null)
    {
      UIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(
          fPreferenceListener);
      fPreferenceListener = null;
    }
  }

  public int getCursorOffset(int x, int y)
  {
    ISourceViewer viewer = getSourceViewer();
    StyledText styledText = viewer.getTextWidget();
    int caret = -1;
    try
    {
      caret = styledText.getOffsetAtLocation(styledText.toControl(x, y));
    } catch (Exception ex)
    {
      //ex.printStackTrace();
    }

    return caret;
  }

  public Object highlightRange(StyleRange range)
  {
    ISourceViewer viewer = getSourceViewer();
    StyledText styledText = viewer.getTextWidget();

    StyleRange[] styles = styledText.getStyleRanges(0, styledText.getText().length());
    styledText.setStyleRange(range);

    return styles;

  }
  
  public Object getPlugin()
  {
      return UIPlugin.getDefault();
   }

  public void restoreStyles(Object object)
  {
    StyleRange[] styles = (StyleRange[]) object;
    System.out.println("styles.length = " + styles.length);

    ISourceViewer viewer = getSourceViewer();
    StyledText styledText = viewer.getTextWidget();
    styledText.setStyleRanges(styles);
  }

  public void addReconcileListener(IReconcileListener listener)
  {
    // TODO Auto-generated method stub

  }
  public void reconcile(IProblemCollector collector, IProgressMonitor fProgressMonitor)
  {
    // TODO Auto-generated method stub

  }
  public void removeReconcileListener(IReconcileListener listener)
  {
    // TODO Auto-generated method stub

  }


  public final ISourceViewer getViewer() 
  {
    return getSourceViewer();
  }

}