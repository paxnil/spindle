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
package com.iw.plugins.spindle.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.INamespace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.util.SWTUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryProject;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.util.CoreUtils;
import com.iw.plugins.spindle.core.util.eclipse.SpindleStatus;
import com.iw.plugins.spindle.ui.dialogfields.CheckBoxField;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.wizards.fields.ContainerDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.NamespaceDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.PackageDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.RawTypeDialogField;
import com.iw.plugins.spindle.ui.wizards.fields.TapestryProjectDialogField;

/**
 * TypeChooseWizardPage wizard page for choosing or creating new Tapestry
 * component classes!
 * 
 * @author glongman@gmail.com
 */
public class TypeChooseWizardPage extends NewTypeWizardPage
{

  class RadioField extends CheckBoxField
  {

    public RadioField(String label)
    {
      super(label);
    }

    public Button getCheckBoxControl(Composite parent, int modifier)
    {
      return super.getCheckBoxControl(parent, SWT.RADIO);
    }
  }

  class FieldListener implements IDialogFieldChangedListener
  {
    public void dialogFieldButtonPressed(DialogField field)
    {
      spindleFieldButtonPressed(field);

    }

    public void dialogFieldChanged(DialogField field)
    {
      spindleFieldChanged(field);

    }

    public void dialogFieldStatusChanged(IStatus status, DialogField field)
    {
      spindleFieldStatusChanged(field);

    }

  }

  protected static final IStatus OK_STATUS = new SpindleStatus();

  private static int LABEL_WIDTH = 64;

  String PAGE_NAME;

  private RadioField fChooseClass;
  private RawTypeDialogField fChooseSpecClassDialogField;
  private RadioField fNewClass;
  private IDialogFieldChangedListener fListener = new FieldListener();
  private boolean fUpdatingRadios = false;

  private IType fFinalSpecClass;

  private Composite fJavaControlsComposite;

  private String fDefaultSpecClass;
  private String fBaseSpecClass;
  private String fDefaultInterface;

  private NewTapComponentWizardPage fFirstWizardPage;
  private DialogField fFirstPageNameField;
  private TapestryProjectDialogField fFirstPageProjectField;
  private NamespaceDialogField fFirstPageNamespaceField;
  private ContainerDialogField fFirstPageContainerField;
  private PackageDialogField fFirstPagePackageField;

  /**
   * @param isClass
   * @param pageName
   */
  public TypeChooseWizardPage(String pageName, NewTapComponentWizardPage predecessor)
  {
    super(true, UIPlugin.getString(pageName + ".title"));

    this.PAGE_NAME = pageName;
    String CHOOSECLASS = PAGE_NAME + ".chooseclass";
    String SPEC_CLASS = PAGE_NAME + ".specclass";
    String NEWCLASS = PAGE_NAME + ".newclass";

    fFirstWizardPage = predecessor;
    fFirstPageNameField = fFirstWizardPage.getComponentNameField();
    fFirstPageNameField.addListener(fListener);
    fFirstPageProjectField = fFirstWizardPage.getProjectField();
    fFirstPageProjectField.addListener(fListener);
    fFirstPageNamespaceField = fFirstWizardPage.getNamespaceField();
    fFirstPageContainerField = fFirstWizardPage.getContainerField();
    fFirstPagePackageField = fFirstWizardPage.getPackageField();

    fDefaultSpecClass = UIPlugin.getString(PAGE_NAME + ".defaultSpecClass");
    fBaseSpecClass = UIPlugin.getString(PAGE_NAME + ".baseClass");
    fDefaultInterface = UIPlugin.getString(PAGE_NAME + ".defaultInterface");

    fChooseClass = new RadioField(UIPlugin.getString(CHOOSECLASS));
    //    fChooseClass.addListener(listener);

    fChooseSpecClassDialogField = createChooseSpecClassField(SPEC_CLASS);
    //    fChooseSpecClassDialogField.addListener(listener);

    fNewClass = new RadioField(UIPlugin.getString(NEWCLASS));
    //    fNewClass.addListener(listener);

    setImageDescriptor(ImageDescriptor.createFromURL(Images.getImageURL(UIPlugin
        .getString(PAGE_NAME + ".image"))));

    setDescription(UIPlugin.getString(PAGE_NAME + ".description"));

  }

  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    if (visible)
    {
      boolean updateAdvancedClasspath = fFirstPageContainerField
          .getButtonControl(null)
          .isVisible();
      if (updateAdvancedClasspath)
      {
        setPackageFragmentRoot(fFirstPageContainerField.getPackageFragmentRoot(), true);
        setPackageFragment(fFirstPagePackageField.getPackageFragment(), true);
      } else
      {
        updateDefaultContainerAndPackage();
      }
    }
  }

  private void updateDefaultContainerAndPackage()
  {
    IPackageFragmentRoot root = null;
    IPackageFragment fragment = getPackageFragment();
    INamespace namespace = fFirstPageNamespaceField.getSelectedNamespace();

    if (namespace != null)
    {
      IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) namespace
          .getSpecificationLocation();
      if (location.isClasspathResource())
      {

        IFile file = (IFile) location.getStorage();
        fragment = (IPackageFragment) JavaCore.create(file.getParent());
        if (fragment != null)
          root = CoreUtils.getPackageFragmentRoot(fragment);
      }
    }

    if (root == null)
      root = getFirstRoot(fFirstPageProjectField.getTapestryProject());

    setPackageFragmentRoot(root, true);
    setPackageFragment(fragment, true);

  }

  private IPackageFragmentRoot getFirstRoot(ITapestryProject tproject)
  {
    if (tproject == null)
      return null;
    try
    {
      IJavaProject jproject = tproject.getJavaProject();
      if (jproject == null)
        return null;

      IPackageFragmentRoot[] roots = jproject.getPackageFragmentRoots();
      for (int i = 0; i < roots.length; i++)
      {
        if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
          return roots[i];
      }
    } catch (CoreException e)
    {
      //eat it
    }
    return null;
  }
  /**
   * Find the file generated, if it was
   * 
   * @return the IFile that was generated. If no generation occured, return null
   * @throws JavaModelException if there is a problem getting the file from the
   *                     generated Type.
   */
  public IFile getGeneratedJavaFile()
  {
    IFile result = null;
    try
    {
      IType generated = getCreatedType();
      if (generated != null)
        result = (IFile) generated.getUnderlyingResource();
    } catch (JavaModelException e)
    {
      UIPlugin.log_it(e);
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    initializeDialogUnits(parent);

    Composite composite = new Composite(parent, SWT.NONE);
    int nColumns = 4;

    GridLayout layout = new GridLayout();
    layout.numColumns = nColumns;
    composite.setLayout(layout);

    createClassChoserControls(composite, nColumns);

    createJavaControls(composite, nColumns);

    setControl(composite);

    Dialog.applyDialogFont(composite);

    fChooseClass.setCheckBoxValue(true);
    fNewClass.setCheckBoxValue(false);
    setJavaControlsEnabled(false);

    fChooseClass.addListener(fListener);
    fChooseSpecClassDialogField.addListener(fListener);
    fNewClass.addListener(fListener);

  }

  private void createClassChoserControls(Composite container, int nColumns)
  {
    GridData data;

    createRadioControls(container, fChooseClass, nColumns);

    Label specClassLabel = fChooseSpecClassDialogField.getLabelControl(container);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.horizontalSpan = 1;
    specClassLabel.setLayoutData(data);

    Text specClassText = fChooseSpecClassDialogField.getTextControl(container);
    data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    data.horizontalSpan = nColumns - 2;
    data.widthHint = getMaxFieldWidth();
    specClassText.setLayoutData(data);

    Button browseSpecClassButton = fChooseSpecClassDialogField
        .getButtonControl(container);
    data = new GridData();
    data.horizontalSpan = 1;
    //    data.horizontalAlignment= GridData.FILL;
    data.grabExcessHorizontalSpace = false;
    
    
//    data.heightHint = SWTUtil.getButtonHeightHint(browseSpecClassButton);
    data.widthHint = SWTUtil.getButtonWidthHint(browseSpecClassButton);
    browseSpecClassButton.setLayoutData(data);

    createSeparator(container, nColumns);

    createRadioControls(container, fNewClass, nColumns);

    new Separator(SWT.NONE).doFillIntoGrid(
        container,
        nColumns,
        convertHeightInCharsToPixels(1));

  }

  private void createRadioControls(Composite parent, RadioField field, int nColumns)
  {
    GridData data;

    Composite container = new Composite(parent, SWT.NULL | SWT.NO_RADIO_GROUP);

    GridLayout layout = new GridLayout();
    layout.numColumns = nColumns;
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    container.setLayout(layout);

    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = nColumns;
    data.heightHint = convertHeightInCharsToPixels(1);
    container.setLayoutData(data);

    Button button = field.getCheckBoxControl(container, SWT.RADIO);
    data = new GridData();
    data.horizontalSpan = 1;
    button.setLayoutData(data);

    Label label = field.getLabelControl(container);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 1;
    data.verticalAlignment = GridData.BEGINNING;
    label.setLayoutData(data);

  }

  private void createJavaControls(Composite parent, int nColumns)
  {
    fJavaControlsComposite = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout();
    layout.numColumns = nColumns;
    fJavaControlsComposite.setLayout(layout);

    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = nColumns;
    fJavaControlsComposite.setLayoutData(data);

    // pick & choose the wanted UI components

    createContainerControls(fJavaControlsComposite, nColumns);
    createPackageControls(fJavaControlsComposite, nColumns);
    createEnclosingTypeControls(fJavaControlsComposite, nColumns);

    createSeparator(fJavaControlsComposite, nColumns);

    createTypeNameControls(fJavaControlsComposite, nColumns);
    createModifierControls(fJavaControlsComposite, nColumns);

    createSuperClassControls(fJavaControlsComposite, nColumns);
    createSuperInterfacesControls(fJavaControlsComposite, nColumns);

    setSuperClass(fDefaultSpecClass, true);
  }

  private void setJavaControlsEnabled(boolean flag)
  {
    if (!flag)
      keepDisabled.clear();

    setChildrenEnabled(fJavaControlsComposite, flag);

    if (flag)
      keepDisabled.clear();
  }

  private List keepDisabled = new ArrayList();

  private void setChildrenEnabled(Composite composite, boolean flag)
  {
    Control[] controls = composite.getChildren();
    for (int i = 0; i < controls.length; i++)
    {
      if (!flag && !controls[i].isEnabled())
      {
        keepDisabled.add(controls[i]);
        continue;
      }

      if (flag && keepDisabled.contains(controls[i]))
        continue;
      if (controls[i] instanceof Composite)
      {
        setChildrenEnabled((Composite) controls[i], flag);
      } else
      {
        controls[i].setEnabled(flag);
      }

    }
  }

  /*
   * (non-Javadoc) Callback to handle statii of the java class fields only!
   * 
   * @see org.eclipse.jdt.ui.wizards.NewContainerWizardPage#handleFieldChanged(java.lang.String)
   */
  protected void handleFieldChanged(String fieldName)
  {
    super.handleFieldChanged(fieldName);

    doStatusUpdate();
  }

  //------ validation --------
  // for new java class fields + choose spec class field.
  private void doStatusUpdate()
  {
    IStatus status = null;

    if (fChooseClass.getCheckBoxValue())
    {
      //status of choose type component
      status = fChooseSpecClassDialogField.getStatus();
    } else
    {
      // we want to morph the package status to severe if ! First we must obtain
      // it for later analysis...
      IStatus packageStatus = isEnclosingTypeSelected() ? null : fPackageStatus;
      // status of all new java type components
      status = StatusUtil.getMostSevere(new IStatus[]{fContainerStatus,
          isEnclosingTypeSelected() ? fEnclosingTypeStatus : fPackageStatus,
          fTypeNameStatus, fModifierStatus, fSuperClassStatus, fSuperInterfacesStatus});

      if (status.getSeverity() < IStatus.ERROR || getSuperClass().trim().length() == 0)
      {
        //need to extra checks to see if the new class conforms
        //to Tapestry norms.
        SpindleStatus spindle = new SpindleStatus();
        if (isEnclosingTypeSelected())
          checkEnclosingModifiers(spindle);

        if (!isEnclosingTypeSelected() && getPackageFragment().isDefaultPackage()
            && fContainerStatus.isOK())
        {
          spindle.setError(UIPlugin.getString(PAGE_NAME + ".newclass.package"));
        }
        try
        {
          // common check, did user change the project to a non Tapestry
          // one?
          IJavaProject current = getPackageFragmentRoot().getJavaProject();
          if (current != null)
          {
            if (!current.getProject().hasNature(TapestryCore.NATURE_ID))
            {
              spindle.setWarning(UIPlugin.getString(PAGE_NAME + ".newclass.project"));

            } else if (!current.equals(fFirstPageProjectField
                .getTapestryProject()
                .getJavaProject()))
            {
              spindle.setWarning(UIPlugin.getString(PAGE_NAME + ".newclass.project1"));
            }
          }

          // here we do any other checks to see if the class we are about to
          // create is a valid tapestry type

          boolean hasDefaultSuperclass = fBaseSpecClass.equals(getSuperClass())
              || fDefaultSpecClass.equals(getSuperClass());

          boolean hasDefaultIface = getSuperInterfaces().contains(fDefaultInterface);

          if (!hasDefaultSuperclass && !hasDefaultIface)
          {
            checkNewType(spindle, fFirstPageProjectField
                .getTapestryProject()
                .getJavaProject());
          }

        } catch (CoreException e)
        {
          UIPlugin.log_it(e);
        }

        status = spindle.getSeverity() >= status.getSeverity() ? spindle : status;
      }

    }
    // severe status will affect in the ok button
    // enabled/disabled.
    updateStatus(status);
  }

  private void checkEnclosingModifiers(SpindleStatus status)
  {
    int mods = getModifiers();
    boolean isPublic = (mods & F_PUBLIC) > 0;
    boolean isStatic = (mods & F_STATIC) > 0;
    if (!(isPublic && isStatic) && fEnclosingTypeStatus.getSeverity() < IStatus.ERROR)
      status.setWarning(UIPlugin.getString(
          PAGE_NAME + ".newclass.enclosing",
          getTypeName()));

  }

  /**
   * Allow subclasses to ensure that if the user has requested that a type be
   * created that the new type is a valid Tapestry type (in context).
   * 
   * Things we already know:
   * <ul>
   * <li>the superclass chosen exists and is not the tapestry default one</li>
   * <li>any interfaces chosen already exist and none is IComponent or IPage
   * </li>
   * </ul>
   * 
   * @param status
   * @param fJavaProject
   */
  protected void checkNewType(SpindleStatus status, IJavaProject jproject) throws JavaModelException
  {
    boolean superClassGood = true;
    boolean hasGoodIface = true;
    IType superType = jproject.findType(getSuperClass());
    boolean success = false;
    if (superType != null)
      success = checkTypes(superType, fBaseSpecClass, fDefaultInterface);

    if (!success)
    {
      List interfaces = getSuperInterfaces();
      for (Iterator iter = interfaces.iterator(); iter.hasNext();)
      {
        String ifaceName = (String) iter.next();
        IType ifaceType = jproject.findType(ifaceName);
        if (ifaceType == null)
          continue;
        if (CoreUtils.extendsType(ifaceType, fDefaultInterface))
        {
          success = true;
          break;
        }
      }
    }

    if (!success)
      status.setError(UIPlugin.getString(PAGE_NAME + ".newclass.error", new String[]{
          getTypeName(), fBaseSpecClass, fDefaultInterface}));
  }
  /**
   * 
   * @param baseType the IType we are checking
   * @param tapestryclass the name of a class we would like the IType to extend
   * @param tapestryinterface the name of an interface the IType might implement
   *                     instead of extending the type
   * @return true iff the Itype extends tapestryclass or implements
   *                 tapestryinterface.
   */
  protected final boolean checkTypes(
      IType baseType,
      String tapestryclass,
      String tapestryinterface)
  {
    if (baseType == null)
      return true;
    boolean result = true;
    try
    {
      if (tapestryclass != null)
        result = CoreUtils.extendsType(baseType, tapestryclass);
    } catch (JavaModelException e)
    {
      UIPlugin.log_it(e); // but do nothing else
    }
    try
    {
      if (!result)
        result = CoreUtils.implementsInterface(baseType, tapestryinterface);
    } catch (JavaModelException e1)
    {
      UIPlugin.log_it(e1); // but do nothing else
    }
    return result;
  }

  protected RawTypeDialogField createChooseSpecClassField(String name)
  {
    return new RawTypeDialogField(name, fDefaultInterface, LABEL_WIDTH);
  }

  /**
   * called when a Spindle dialog field changes.
   * 
   * @param field the Spindle field.
   */
  private void spindleFieldChanged(DialogField field)
  {
    if (field == fChooseSpecClassDialogField)
    {
      doStatusUpdate();

    } else if (field == fFirstPageNameField)
    {
      componentNameChanged();

    } else if (field == fFirstPageProjectField)
    {
      tapestryProjectChanged();
    }

  }
  private void componentNameChanged()
  {
    setTypeName(fFirstWizardPage.getChosenComponentName(), true);
    doStatusUpdate();
  }

  private void tapestryProjectChanged()
  {
    ITapestryProject project = fFirstPageProjectField.getTapestryProject();
    if (project == null)
    {
      setPackageFragmentRoot(null, false);
      setPackageFragment(null, false);
    } else
    {

      try
      {
        IJavaProject jproject = project.getJavaProject();
        fChooseSpecClassDialogField.init(jproject, (IRunnableContext) getWizard()
            .getContainer());
        String existingSpecClassname = fChooseSpecClassDialogField.getTextValue();
        if (existingSpecClassname == null || existingSpecClassname.trim().length() == 0)
        {
          fChooseSpecClassDialogField.setTextValue(fDefaultSpecClass);
        } else
        {
          fChooseSpecClassDialogField.setTextValue(existingSpecClassname);
        }
        IPackageFragmentRoot[] roots = jproject.getAllPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++)
        {
          IPackageFragmentRoot previous = getPackageFragmentRoot();
          if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE && roots[i].exists())
          {
            setPackageFragmentRoot(roots[i], true);
            if (previous != null && !previous.getParent().equals(jproject))
              setPackageFragment(null, true);
            break;
          }
        }
      } catch (CoreException e)
      {
        UIPlugin.log_it(e);
      }
    }
  }

  /**
   * called when a Spindle dialog field (that has a button) pressed.
   * 
   * @param field the Spindle field.
   */
  private void spindleFieldButtonPressed(DialogField field)
  {
    boolean flag;

    if (field == fChooseClass)
    {
      flag = fChooseClass.getCheckBoxValue();
      fChooseSpecClassDialogField.setEnabled(flag);
      setJavaControlsEnabled(!flag);
      fNewClass.setCheckBoxValue(!flag, false);
    } else if (field == fNewClass)
    {
      flag = fNewClass.getCheckBoxValue();
      setJavaControlsEnabled(flag);
      fChooseSpecClassDialogField.setEnabled(!flag);
      fChooseClass.setCheckBoxValue(!flag, false);
    }

    doStatusUpdate();
  }
  /**
   * called when the validation status of a Spindle dialog field changes
   * 
   * @param field the Spindle field.
   */
  private void spindleFieldStatusChanged(DialogField field)
  {
    doStatusUpdate();
  }

  public IType getFinalSpecClass()
  {
    return fFinalSpecClass;
  }

  public IRunnableWithProgress getRunnable(Object object)
  {
    return new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        try
        {
          if (monitor == null)
          {
            monitor = new NullProgressMonitor();
          }
          if (fChooseClass.getCheckBoxValue())
          {
            fFinalSpecClass = fChooseSpecClassDialogField.getType();
          } else
          {
            createType(new SubProgressMonitor(monitor, 1));
            fFinalSpecClass = getCreatedType();
          }
          monitor.done();
        } catch (CoreException e)
        {
          throw new InvocationTargetException(e);
        }
      }
    };
  }

  public boolean performFinish()
  {
    return true;
  }
}