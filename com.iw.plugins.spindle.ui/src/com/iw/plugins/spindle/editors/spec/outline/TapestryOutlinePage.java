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

package com.iw.plugins.spindle.editors.spec.outline;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.IAssetSpecification;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.BaseSpecification;
import com.iw.plugins.spindle.core.spec.IIdentifiable;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginLibrarySpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.spec.SpecEditor;
import com.iw.plugins.spindle.editors.util.DoubleClickSelection;

/**
 * Outline view for Spec Editor that shows parse results only.
 * 
 * @author glongman@gmail.com
 */
public class TapestryOutlinePage extends ContentOutlinePage
{
  public static final String ALPHA_PREFERENCE = UIPlugin.PLUGIN_ID
      + ".tapestry-outline-sort-alpha";
  public static final String CATEGORY_PREFERENCE = UIPlugin.PLUGIN_ID
      + ".tapestry-outline-sort-category";

  public static void initializeDefaultPreferences(IPreferenceStore store)
  {
    store.setDefault(ALPHA_PREFERENCE, false);
    store.setDefault(CATEGORY_PREFERENCE, true);
  }

  private SpecEditor fEditor;
  private Tree fTree;
  private TreeViewer treeViewer;

  private ViewerSorter fCurrentSorter;
  private DefaultSorter fDefaultSorter = new DefaultSorter();
  private AlphaCategorySorter fAlphaCatSorter = new AlphaCategorySorter();

  private TapestryOutlinePage.ToggleAlphaSortAction fToggleAlphaSort = new TapestryOutlinePage.ToggleAlphaSortAction();
  private TapestryOutlinePage.ToggleCatSortAction fToggleCatSort = new TapestryOutlinePage.ToggleCatSortAction();

  private Object fSavedInput; // the input might have been posted before the
  // control was created!

  public TapestryOutlinePage(SpecEditor editor)
  {
    fEditor = editor;
    if (UIPlugin.getDefault().getPreferenceStore().getBoolean(ALPHA_PREFERENCE))
      fCurrentSorter = fAlphaCatSorter;
    else
      fCurrentSorter = fDefaultSorter;
  }

  public void createControl(Composite parent)
  {
    fTree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    treeViewer = new TreeViewer(fTree);
    treeViewer.addSelectionChangedListener(this);
    treeViewer.setContentProvider(createContentProvider());
    treeViewer.setLabelProvider(createLabelProvider());
    treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
    treeViewer.setUseHashlookup(false);
    treeViewer.setSorter(fCurrentSorter);
    treeViewer.addDoubleClickListener(new IDoubleClickListener()
    {
      public void doubleClick(DoubleClickEvent event)
      {
        IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
        if (!selection.isEmpty())
          fireSelectionChanged(new DoubleClickSelection(selection.getFirstElement()));
      }
    });
    UIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
        new IPropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent event)
          {
            adaptToPreferenceChange(event);
          }
        });
    setInput(fSavedInput);
  }

  /**
   * @param event
   */
  protected void adaptToPreferenceChange(PropertyChangeEvent event)
  {
    String property = event.getProperty();
    if (property.equals(ALPHA_PREFERENCE))
    {
      updateSorter(false);
    } else if (property.equals(CATEGORY_PREFERENCE))
    {
      boolean value = ((Boolean) event.getNewValue()).booleanValue();
      fAlphaCatSorter.setUseCategorySort(value);
      fDefaultSorter.setUseCategorySort(value);
      updateSorter(true);
    }
  }

  /**
   * @param fAlphaCatSorter
   */
  private void updateSorter(boolean categoryChanged)
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    ViewerSorter toBeShown;

    boolean showAlpha = store.getBoolean(ALPHA_PREFERENCE);

    if (showAlpha)
      toBeShown = fAlphaCatSorter;
    else
      toBeShown = fDefaultSorter;

    Control control = getControl();
    if (control == null || control.isDisposed())
    {
      fCurrentSorter = toBeShown;
      return;
    }
    treeViewer.setSorter(null);
    treeViewer.setSorter(toBeShown);
  }

  public void dispose()
  {
    super.dispose();
  }

  public void setInput(final Object input)
  {
    if (input == null || !(input instanceof BaseSpecification))
      return;

    if (treeViewer == null)
    {
      fSavedInput = input;
      return;
    }

    Display d = treeViewer.getControl().getDisplay();
    d.asyncExec(new Runnable()
    {
      public void run()
      {
        treeViewer.setInput(input);
      }
    });
  }

  protected ITreeContentProvider createContentProvider()
  {
    return new ContentProvider();
  }

  protected ILabelProvider createLabelProvider()
  {
    return new BasicLabelProvider();
  }

  public Control getControl()
  {
    return treeViewer != null ? treeViewer.getControl() : null;
  }

  public void setFocus()
  {
    if (treeViewer != null)
      treeViewer.getTree().setFocus();
  }

  public ISelection getSelection()
  {
    if (treeViewer == null)
      return StructuredSelection.EMPTY;
    return treeViewer.getSelection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  public void setSelection(ISelection selection)
  {
  }

  public static class ContentProvider implements ITreeContentProvider
  {
    BaseSpecification specification;

    public ContentProvider()
    {
      super();
    }
    public void dispose()
    {
    }
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
      specification = (BaseSpecification) newInput;
    }
    public Object[] getElements(Object obj)
    {
      return getArtifacts();
    }
    public Object[] getChildren(Object obj)
    {
      return new Object[0];
    }
    public boolean hasChildren(Object obj)
    {
      return getChildren(obj).length > 0;
    }
    public Object getParent(Object obj)
    {
      return null;
    }
    private Object[] getArtifacts()
    {
      if (specification == null)
        return new Object[0];

      List results = new ArrayList();
      switch (specification.getSpecificationType())
      {
        case BaseSpecification.APPLICATION_SPEC :
        case BaseSpecification.LIBRARY_SPEC :
          PluginLibrarySpecification lib = (PluginLibrarySpecification) specification;
          results.addAll(lib.getDescriptionDeclarations());
          results.addAll(lib.getPageDeclarations());
          results.addAll(lib.getEngineServiceDeclarations());
          results.addAll(lib.getComponentTypeDeclarations());
          results.addAll(lib.getPropertyDeclarations());
          results.addAll(lib.getLibraryDeclaration());
          for (Iterator iter = lib.getExtensionNames().iterator(); iter.hasNext();)
          {
            String name = (String) iter.next();
            results.add(lib.getExtension(name));
          }
          break;
        case BaseSpecification.COMPONENT_SPEC :
          PluginComponentSpecification component = (PluginComponentSpecification) specification;
          results.addAll(component.getPropertyDeclarations());
          results.addAll(component.getDescriptionDeclarations());
          for (Iterator iter = component.getComponentIds().iterator(); iter.hasNext();)
          {
            String id = (String) iter.next();
            results.add(component.getComponent(id));
          }
          for (Iterator iter = component.getParameterNames().iterator(); iter.hasNext();)
          {
            String pname = (String) iter.next();
            results.add(component.getParameter(pname));
          }
          for (Iterator iter = component.getBeanNames().iterator(); iter.hasNext();)
          {
            String bname = (String) iter.next();
            results.add(component.getBeanSpecification(bname));
          }
          for (Iterator iter = component.getPropertySpecificationNames().iterator(); iter
              .hasNext();)
          {
            String propName = (String) iter.next();
            results.add(component.getPropertySpecification(propName));
          }
          for (Iterator iter = component.getAssetNames().iterator(); iter.hasNext();)
          {
            String assetName = (String) iter.next();
            results.add(component.getAsset(assetName));
          }
          break;
        default :
          Assert.isLegal(false);
      }
      return results.toArray();
    }
  }

  public static class BasicLabelProvider extends LabelProvider
  {
    public String getText(Object obj)
    {

      String identifier = ((IIdentifiable) obj).getIdentifier();
      if (identifier == null || identifier.equals(BaseValidator.DefaultDummyString)
          || identifier.trim().length() == 0)
        return "[no value]";
      return identifier;
    }
    public Image getImage(Object obj)
    {
      BaseSpecification spec = (BaseSpecification) obj;
      switch (spec.getSpecificationType())
      {
        case BaseSpecification.COMPONENT_TYPE_DECLARATION :
        case BaseSpecification.CONTAINED_COMPONENT_SPEC :
          return Images.getSharedImage("component16.gif");

        case BaseSpecification.LIBRARY_DECLARATION :
          return Images.getSharedImage("library16.gif");

        case BaseSpecification.PAGE_DECLARATION :
          return Images.getSharedImage("page16.gif");

        case BaseSpecification.DESCRIPTION_DECLARATION :
          return Images.getSharedImage("description16.gif");

        case BaseSpecification.PROPERTY_DECLARATION :
          return Images.getSharedImage("property16.gif");

        case BaseSpecification.BEAN_SPEC :
          return Images.getSharedImage("bean.gif");

        case BaseSpecification.ASSET_SPEC :
          IAssetSpecification assetSpec = (IAssetSpecification) spec;
          AssetType type = assetSpec.getType();
          if (type == AssetType.CONTEXT)
            return Images.getSharedImage("assetContext16.gif");
          if (type == AssetType.EXTERNAL)
            return Images.getSharedImage("assetExternal.gif");
          if (type == AssetType.PRIVATE)
            return Images.getSharedImage("assetPrivate.gif");

        case BaseSpecification.PROPERTY_SPEC :
          return Images.getSharedImage("propertySpec16.gif");

        case BaseSpecification.PARAMETER_SPEC :
          return Images.getSharedImage("parameter16.gif");

        case BaseSpecification.RESERVED_PARAMETER_DECLARATION :
          return Images.getSharedImage("parameterReserved16.gif");
        default :
          return Images.getSharedImage("missing");
      }
    }
  }

  public static class BaseSorter extends ViewerSorter
  {
    protected boolean useCategorySort = true;

    public BaseSorter()
    {
      super(null);
    }

    // this does not trigger a resort!
    public void setUseCategorySort(boolean flag)
    {
      useCategorySort = flag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ViewerSorter#category(java.lang.Object)
     */
    public int category(Object obj)
    {
      if (!useCategorySort)
        return 0;
      BaseSpecification spec = (BaseSpecification) obj;
      switch (spec.getSpecificationType())
      {
        case BaseSpecification.DESCRIPTION_DECLARATION :
          return 0;

        case BaseSpecification.PARAMETER_SPEC :
          return 1;

        case BaseSpecification.PROPERTY_DECLARATION :
          return 2;

        case BaseSpecification.LIBRARY_DECLARATION :
          return 3;

        case BaseSpecification.ENGINE_SERVICE_DECLARATION :
          return 4;

        case BaseSpecification.PAGE_DECLARATION :
          return 5;

        case BaseSpecification.COMPONENT_TYPE_DECLARATION :
          return 6;

        case BaseSpecification.BEAN_SPEC :
          return 7;

        case BaseSpecification.PROPERTY_SPEC :
          return 8;

        case BaseSpecification.CONTAINED_COMPONENT_SPEC :
          return 9;

        case BaseSpecification.ASSET_SPEC :
          return 10;

        default :
          return 99;
      }
    }
  }

  // Sorter based on document location
  public static class DefaultSorter extends BaseSorter
  {

    public int compare(Viewer viewer, Object e1, Object e2)
    {

      int cat1 = category(e1);
      int cat2 = category(e2);

      if (cat1 != cat2)
        return cat1 - cat2;

      ISourceLocationInfo l1 = (ISourceLocationInfo) ((BaseSpecification) e1)
          .getLocation();
      ISourceLocationInfo l2 = (ISourceLocationInfo) ((BaseSpecification) e2)
          .getLocation();
      int offset1 = l1.getOffset();
      int offset2 = l2.getOffset();
      return (offset1 > offset2) ? 1 : ((offset1 < offset2) ? -1 : 0);
    }
  }

  //Sorts alphabetically.
  //Optionally, category sorting is applied before the alpha sort
  public static class AlphaCategorySorter extends BaseSorter
  {
    public AlphaCategorySorter()
    {
      super();
    }

    public int compare(Viewer viewer, Object e1, Object e2)
    {

      int cat1 = category(e1);
      int cat2 = category(e2);

      if (cat1 != cat2)
        return cat1 - cat2;

      // cat1 == cat2

      String name1;
      String name2;

      if (viewer == null || !(viewer instanceof ContentViewer))
      {
        name1 = e1.toString();
        name2 = e2.toString();
      } else
      {
        IBaseLabelProvider prov = ((ContentViewer) viewer).getLabelProvider();
        if (prov instanceof ILabelProvider)
        {
          ILabelProvider lprov = (ILabelProvider) prov;
          name1 = lprov.getText(e1);
          name2 = lprov.getText(e2);
        } else
        {
          name1 = e1.toString();
          name2 = e2.toString();
        }
      }
      if (name1 == null)
        name1 = ""; //$NON-NLS-1$
      if (name2 == null)
        name2 = ""; //$NON-NLS-1$
      return getCollator().compare(name1, name2);
    }

    public final Collator getCollator()
    {
      if (collator == null)
      {
        collator = Collator.getInstance();
      }
      return collator;
    }
  }
  private static class ToggleAlphaSortAction extends Action
  {
    public ToggleAlphaSortAction()
    {
      super();
      setText("Toggle alphabetical sorting");
      setToolTipText("Toggle alphabetical sorting");
      setImageDescriptor(Images.getImageDescriptor("alphab_sort_co.gif"));

      IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
      boolean checked = store.getBoolean(ALPHA_PREFERENCE);
      valueChanged(checked, false);
    }
    public void run()
    {

      boolean checked = isChecked();
      valueChanged(checked, true);
    }

    public void valueChanged(boolean on, boolean store)
    {
      setChecked(on);
      if (store)
      {
        UIPlugin.getDefault().getPreferenceStore().setValue(ALPHA_PREFERENCE, on);
      }
    }
  }
  private static class ToggleCatSortAction extends Action
  {
    public ToggleCatSortAction()
    {
      super();
      setText("Toggle group sorting");
      setToolTipText("Toggle group sorting");
      setImageDescriptor(Images.getImageDescriptor("group_sort.gif"));

      IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
      boolean checked = store.getBoolean(CATEGORY_PREFERENCE);
      valueChanged(checked, false);
    }
    public void run()
    {

      boolean checked = isChecked();
      valueChanged(checked, true);
    }

    public void valueChanged(boolean on, boolean store)
    {
      setChecked(on);
      if (store)
      {
        UIPlugin.getDefault().getPreferenceStore().setValue(CATEGORY_PREFERENCE, on);
      }
    }
  }
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.part.Page#makeContributions(org.eclipse.jface.action.IMenuManager,
   *      org.eclipse.jface.action.IToolBarManager,
   *      org.eclipse.jface.action.IStatusLineManager)
   */
  public void makeContributions(
      IMenuManager menuManager,
      IToolBarManager toolBarManager,
      IStatusLineManager statusLineManager)
  {
    toolBarManager.add(fToggleAlphaSort);
    toolBarManager.add(fToggleCatSort);
  }

}