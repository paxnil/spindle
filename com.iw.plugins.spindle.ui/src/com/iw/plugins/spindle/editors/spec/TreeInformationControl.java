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

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.spec.IIdentifiable;

/**
 * TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: TreeInformationControl.java,v 1.2 2004/05/05 19:29:32 glongman
 *          Exp $
 */
public abstract class TreeInformationControl
    implements
      IInformationControl,
      IInformationControlExtension,
      IInformationControlExtension2
{

  /**
   * The NamePatternFilter selects the elements which match the given string
   * patterns.
   * <p>
   * The following characters have special meaning: ? => any character * => any
   * string
   * </p>
   * 
   * @since 2.0
   */
  protected static class NamePatternFilter extends ViewerFilter
  {
    private String fPattern;
    private StringMatcher fMatcher;
    private ILabelProvider fLabelProvider;
    private ITreeContentProvider fContentProvider;
    protected Viewer fViewer;

    private StringMatcher getMatcher()
    {
      return fMatcher;
    }

    /*
     * (non-Javadoc) Method declared on ViewerFilter.
     */
    public boolean select(Viewer viewer, Object parentElement, Object element)
    {
      ILabelProvider labelProvider = getLabelProvider(viewer);

      String matchName = null;
      if (labelProvider != null)
        matchName = ((ILabelProvider) labelProvider).getText(element);
      else if (element instanceof IIdentifiable)
        matchName = ((IIdentifiable) element).getIdentifier();

      if (match(matchName))
        return true;

      return hasUnfilteredChild(viewer, element);
    }

    protected final boolean match(String matchName)
    {
      if (fMatcher == null || matchName == null)
        return true;

      return fMatcher.match(matchName);
    }

    private ITreeContentProvider getContentProvider(Viewer viewer)
    {
      if (fViewer == viewer)
        return fContentProvider;

      fContentProvider = null;
      IContentProvider contentProvider = null;
      if (viewer instanceof StructuredViewer)
        contentProvider = ((StructuredViewer) viewer).getContentProvider();

      if (contentProvider != null && contentProvider instanceof ITreeContentProvider)
        fContentProvider = (ITreeContentProvider) contentProvider;

      return fContentProvider;
    }

    private ILabelProvider getLabelProvider(Viewer viewer)
    {
      if (fViewer == viewer)
        return fLabelProvider;

      fLabelProvider = null;
      IBaseLabelProvider baseLabelProvider = null;
      if (viewer instanceof StructuredViewer)
        baseLabelProvider = ((StructuredViewer) viewer).getLabelProvider();

      if (baseLabelProvider != null && baseLabelProvider instanceof ILabelProvider)
        fLabelProvider = (ILabelProvider) baseLabelProvider;

      return fLabelProvider;
    }

    protected final boolean hasUnfilteredChild(Viewer viewer, Object element)
    {
      ITreeContentProvider provider = getContentProvider(viewer);
      if (provider == null)
        return false;

      Object[] children = provider.getChildren(element);
      if (children.length == 0)
        return false;
      for (int i = 0; i < children.length; i++)
        if (select(viewer, element, children[i]))
          return true;
      return false;
    }

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning: ? => any character * =>
     * any string
     * </p>
     */
    public void setPattern(String pattern)
    {
      fPattern = pattern;
      if (fPattern == null)
      {
        fMatcher = null;
        return;
      }
      boolean ignoreCase = pattern.toLowerCase().equals(pattern);
      fMatcher = new StringMatcher(pattern, ignoreCase, false);
    }
  }

  private static class BorderFillLayout extends Layout
  {

    /** The border widths. */
    final int fBorderSize;

    /**
     * Creates a fill layout with a border.
     */
    public BorderFillLayout(int borderSize)
    {
      if (borderSize < 0)
        throw new IllegalArgumentException();
      fBorderSize = borderSize;
    }

    /**
     * Returns the border size.
     */
    public int getBorderSize()
    {
      return fBorderSize;
    }

    /*
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
     *      int, int, boolean)
     */
    protected Point computeSize(
        Composite composite,
        int wHint,
        int hHint,
        boolean flushCache)
    {

      Control[] children = composite.getChildren();
      Point minSize = new Point(0, 0);

      if (children != null)
      {
        for (int i = 0; i < children.length; i++)
        {
          Point size = children[i].computeSize(wHint, hHint, flushCache);
          minSize.x = Math.max(minSize.x, size.x);
          minSize.y = Math.max(minSize.y, size.y);
        }
      }

      minSize.x += fBorderSize * 2 + RIGHT_MARGIN;
      minSize.y += fBorderSize * 2;

      return minSize;
    }
    /*
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
     *      boolean)
     */
    protected void layout(Composite composite, boolean flushCache)
    {

      Control[] children = composite.getChildren();
      Point minSize = new Point(composite.getClientArea().width, composite
          .getClientArea().height);

      if (children != null)
      {
        for (int i = 0; i < children.length; i++)
        {
          Control child = children[i];
          child.setSize(minSize.x - fBorderSize * 2, minSize.y - fBorderSize * 2);
          child.setLocation(fBorderSize, fBorderSize);
        }
      }
    }
  }

  /** Border thickness in pixels. */
  private static final int BORDER = 1;
  /** Right margin in pixels. */
  private static final int RIGHT_MARGIN = 3;

  /** The control's shell */
  private Shell fShell;
  /** The composite */
  Composite fComposite;
  /** The control's text widget */
  private Text fFilterText;
  /** The control's tree widget */
  protected TreeViewer fTreeViewer;
  /** The control width constraint */
  private int fMaxWidth = -1;
  /** The control height constraint */
  private int fMaxHeight = -1;

  /** the content provider for the TreeViewer- set by subclass constructor * */
  protected ITreeContentProvider fContentProvider;
  /** the label provider for the TreeViewer - set by subclass constructor * */
  protected ILabelProvider fLabelProvider;
  /** the optional sorter for the TreeViewer - set by subclass constructor * */
  private ViewerSorter fSorter;

  private StringMatcher fStringMatcher;

  /**
   * Creates a tree information control with the given shell as parent. The
   * given style is applied to the tree widget.
   * 
   * @param parent the parent shell
   * @param style the additional styles for the tree widget
   */
  public TreeInformationControl(Shell parent, int style)
  {
    this(parent, SWT.RESIZE, style);
  }

  /**
   * Creates a tree information control with the given shell as parent. No
   * additional styles are applied.
   * 
   * @param parent the parent shell
   */
  public TreeInformationControl(Shell parent)
  {
    this(parent, SWT.NONE);
  }

  /**
   * Creates a tree information control with the given shell as parent. The
   * given styles are applied to the shell and the tree widget.
   * 
   * @param parent the parent shell
   * @param shellStyle the additional styles for the shell
   * @param treeStyle the additional styles for the tree widget
   */
  public TreeInformationControl(Shell parent, int shellStyle, int treeStyle)
  {
    fShell = new Shell(parent, shellStyle);
    Display display = fShell.getDisplay();
    fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

    // Composite for filter text and tree
    fComposite = new Composite(fShell, SWT.RESIZE);
    GridLayout layout = new GridLayout(1, false);
    fComposite.setLayout(layout);
    fComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    createFilterText(fComposite);
    createTreeViewer(fComposite, treeStyle);

    int border = ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
    fShell.setLayout(new BorderFillLayout(border));

    setInfoSystemColor();
    installFilter(createFilter());
  }

  protected void setLabelProvider(ILabelProvider provider)
  {
    fLabelProvider = provider;
    fTreeViewer.setLabelProvider(fLabelProvider);
  }

  protected void setContentProvider(ITreeContentProvider provider)
  {
    fContentProvider = provider;
    fTreeViewer.setContentProvider(fContentProvider);
  }

  protected void setSorter(ViewerSorter sorter)
  {
    fSorter = sorter;
    fTreeViewer.setSorter(fSorter);
  }

  protected NamePatternFilter createFilter()
  {
    return new NamePatternFilter();
  }

  private void createTreeViewer(Composite parent, int style)
  {
    Tree tree = new Tree(parent, SWT.SINGLE | (style & ~SWT.MULTI));
    GridData data = new GridData(GridData.FILL_BOTH);
    tree.setLayoutData(data);

    fTreeViewer = new TreeViewer(tree);

    // Hide import declartions but show the container
    fTreeViewer.addFilter(new ViewerFilter()
    {
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        return !(element instanceof IImportDeclaration);
      }
    });

    fTreeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);

    fTreeViewer.getTree().addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.character == 0x1B) // ESC
          dispose();
      }
      public void keyReleased(KeyEvent e)
      {
        // do nothing
      }
    });

    fTreeViewer.getTree().addSelectionListener(new SelectionListener()
    {
      public void widgetSelected(SelectionEvent e)
      {
        // do nothing
      }
      public void widgetDefaultSelected(SelectionEvent e)
      {
        handleSelectedElement();
      }
    });
  }

  private Text createFilterText(Composite parent)
  {
    fFilterText = new Text(parent, SWT.FLAT);

    GridData data = new GridData();
    GC gc = new GC(parent);
    gc.setFont(parent.getFont());
    FontMetrics fontMetrics = gc.getFontMetrics();
    gc.dispose();

    data.heightHint = org.eclipse.jface.dialogs.Dialog.convertHeightInCharsToPixels(
        fontMetrics,
        1);
    data.horizontalAlignment = GridData.FILL;
    data.verticalAlignment = GridData.BEGINNING;
    fFilterText.setLayoutData(data);

    fFilterText.addKeyListener(new KeyListener()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.keyCode == 0x0D) // return
          handleSelectedElement();
        if (e.keyCode == SWT.ARROW_DOWN)
          fTreeViewer.getTree().setFocus();
        if (e.keyCode == SWT.ARROW_UP)
          fTreeViewer.getTree().setFocus();
        if (e.character == 0x1B) // ESC
          dispose();
      }
      public void keyReleased(KeyEvent e)
      {
        // do nothing
      }
    });

    // Horizonral separator line
    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    return fFilterText;
  }

  private void setInfoSystemColor()
  {
    Display display = fShell.getDisplay();
    setForegroundColor(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    setBackgroundColor(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
  }

  private void installFilter(NamePatternFilter filter)
  {
    final NamePatternFilter viewerFilter = filter;
    fTreeViewer.addFilter(viewerFilter);
    fFilterText.setText(""); //$NON-NLS-1$

    fFilterText.addModifyListener(new ModifyListener()
    {
      public void modifyText(ModifyEvent e)
      {
        String pattern = fFilterText.getText();
        if (pattern != null)
        {
          int length = pattern.length();
          if (length == 0)
            pattern = null;
          else if (pattern.charAt(length - 1) != '*')
            pattern = pattern + '*';
        } else
          pattern = null;
        viewerFilter.setPattern(pattern);
        fStringMatcher = viewerFilter.getMatcher();
        fTreeViewer.getControl().setRedraw(false);
        fTreeViewer.refresh();
        fTreeViewer.expandAll();
        selectFirstMatch();
        fTreeViewer.getControl().setRedraw(true);
      }
    });
  }

  protected void handleSelectedElement()
  {
    Object selectedElement = ((IStructuredSelection) fTreeViewer.getSelection())
        .getFirstElement();
    if (selectedElement != null)
    {
      try
      {

        if (doHandleSelectedElement(selectedElement))
          dispose();

      } catch (Exception ex)
      {
        UIPlugin.log(ex);
      }
    }
  }

  /**
   * 
   * @param selected the object selected by the user
   * @return true iff the selection is valid and the control should be hidden
   */
  protected abstract boolean doHandleSelectedElement(Object selected);

  /**
   * Selects the first element in the tree which matches the current filter
   * pattern.
   */
  private void selectFirstMatch()
  {
    Tree tree = fTreeViewer.getTree();
    Object element = findElement(tree.getItems());
    if (element != null)
    {
      Object parent = fContentProvider.getParent(element);
      if (parent != null)
        fTreeViewer.setSelection(new StructuredSelection(), true);
      fTreeViewer.setSelection(new StructuredSelection(element), true);
    } else
    {
      fTreeViewer.setSelection(StructuredSelection.EMPTY);
    }
  }

  private Object findElement(TreeItem[] items)
  {
    ILabelProvider labelProvider = (ILabelProvider) fTreeViewer.getLabelProvider();
    for (int i = 0; i < items.length; i++)
    {
      Object element = items[i].getData();
      if (fStringMatcher == null)
        return element;

      if (element != null)
      {
        String label = labelProvider.getText(element);
        if (fStringMatcher.match(label))
          return element;
      }

      element = findElement(items[i].getItems());
      if (element != null)
        return element;
    }
    return null;
  }

  /*
   * @see IInformationControl#setInformation(String)
   */
  public void setInformation(String information)
  {
    // this method is ignored, see IInformationControlExtension2
  }

  /*
   * @see IInformationControlExtension2#setInput(Object)
   */
  public void setInput(Object information)
  {
    fFilterText.setText("");
    if (information == null || information instanceof String)
    {
      return;
    }
    doSetInput(information);
    //        fTreeViewer.setInput(sel);
    //        fTreeViewer.setSelection(new StructuredSelection(information));
  }

  protected abstract void doSetInput(Object information);

  /*
   * @see IInformationControl#setVisible(boolean)
   */
  public void setVisible(boolean visible)
  {
    fShell.setVisible(visible);
  }

  /*
   * @see IInformationControl#dispose()
   */
  public void dispose()
  {
    if (fShell != null)
    {
      if (!fShell.isDisposed())
        fShell.dispose();
      fShell = null;
      fTreeViewer = null;
      fComposite = null;
      fFilterText = null;
    }
  }

  /*
   * @see org.eclipse.jface.text.IInformationControlExtension#hasContents()
   */
  public boolean hasContents()
  {
    return fTreeViewer != null && fTreeViewer.getInput() != null;
  }

  /*
   * @see org.eclipse.jface.text.IInformationControl#setSizeConstraints(int,
   *      int)
   */
  public void setSizeConstraints(int maxWidth, int maxHeight)
  {
    fMaxWidth = maxWidth;
    fMaxHeight = maxHeight;
  }

  /*
   * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
   */
  public Point computeSizeHint()
  {
    return fShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
  }

  /*
   * @see IInformationControl#setLocation(Point)
   */
  public void setLocation(Point location)
  {
    Rectangle trim = fShell.computeTrim(0, 0, 0, 0);
    Point textLocation = fComposite.getLocation();
    location.x += trim.x - textLocation.x;
    location.y += trim.y - textLocation.y;
    fShell.setLocation(location);
  }

  /*
   * @see IInformationControl#setSize(int, int)
   */
  public void setSize(int width, int height)
  {
    fShell.setSize(width, height);
  }

  /*
   * @see IInformationControl#addDisposeListener(DisposeListener)
   */
  public void addDisposeListener(DisposeListener listener)
  {
    fShell.addDisposeListener(listener);
  }

  /*
   * @see IInformationControl#removeDisposeListener(DisposeListener)
   */
  public void removeDisposeListener(DisposeListener listener)
  {
    fShell.removeDisposeListener(listener);
  }

  /*
   * @see IInformationControl#setForegroundColor(Color)
   */
  public void setForegroundColor(Color foreground)
  {
    fTreeViewer.getTree().setForeground(foreground);
    fFilterText.setForeground(foreground);
    fComposite.setForeground(foreground);
  }

  /*
   * @see IInformationControl#setBackgroundColor(Color)
   */
  public void setBackgroundColor(Color background)
  {
    fTreeViewer.getTree().setBackground(background);
    fFilterText.setBackground(background);
    fComposite.setBackground(background);
  }

  /*
   * @see IInformationControl#isFocusControl()
   */
  public boolean isFocusControl()
  {
    return fTreeViewer.getControl().isFocusControl() || fFilterText.isFocusControl();
  }

  /*
   * @see IInformationControl#setFocus()
   */
  public void setFocus()
  {
    fShell.forceFocus();
    fFilterText.setFocus();
  }

  /*
   * @see IInformationControl#addFocusListener(FocusListener)
   */
  public void addFocusListener(FocusListener listener)
  {
    fShell.addFocusListener(listener);
  }

  /*
   * @see IInformationControl#removeFocusListener(FocusListener)
   */
  public void removeFocusListener(FocusListener listener)
  {
    fShell.removeFocusListener(listener);
  }
}