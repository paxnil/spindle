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

package com.iw.plugins.spindle.editors.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PartInitException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.editors.Editor;

/**
 * Base class for editor actions
 * 
 * TODO fix.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class BaseEditorAction extends Action
{

    protected Editor fEditor;
    protected int fDocumentOffset;

    /**
     * 
     */
    public BaseEditorAction()
    {
        super();
    }

    /**
     * @param text
     */
    public BaseEditorAction(String text)
    {
        super(text);
    }

    /**
     * @param text
     * @param image
     */
    public BaseEditorAction(String text, ImageDescriptor image)
    {
        super(text, image);
    }

    /**
     * @param text
     * @param style
     */
    public BaseEditorAction(String text, int style)
    {
        super(text, style);
    }

    public void setActiveEditor(Editor editor)
    {
        fEditor = editor;
    }

    public void run()
    {
        fDocumentOffset = fEditor.getCaretOffset();
    }

    protected IType resolveType(String typeName)
    {
        IJavaProject jproject = TapestryCore.getDefault().getJavaProjectFor(fEditor.getStorage());
        if (jproject == null || typeName == null)
            return null;

        try
        {
            return jproject.findType(typeName);
        } catch (JavaModelException e)
        {
            //do nothing
        }
        return null;
    }

    protected IType resolveType(IFile file)
    {
        ICompilationUnit unit = (ICompilationUnit) JavaCore.create(file);
        try
        {
            IType[] types = unit.getAllTypes();
            if (types.length > 0)
                return types[0];
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
        return null;
    }

    protected IType resolveType(IClassFile file)
    {
        try
        {
            return (IType) file.getType();
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
        return null;
    }

    /**
         * @param resolvedType
         */
    protected void reveal(IType resolvedType)
    {
        try
        {
            JavaUI.openInEditor(resolvedType);
        } catch (PartInitException e)
        {
            UIPlugin.log(e);
        } catch (JavaModelException e)
        {
            UIPlugin.log(e);
        }
    }
    /**
     * contribute to a menu. Override in subclasses.
     * @param menu
     */
    public void editorContextMenuAboutToShow(IMenuManager menu)
    {}

    protected abstract class ChooseLocationPopup
    {

        String commandForward = null;
        String commandBackward = null;
        boolean forward;
        private Object selection;
        private List fTemplateLocations;

        protected ChooseLocationPopup(List templateLocations, boolean forward)
        {
            this.forward = forward;
            fTemplateLocations = templateLocations;
        }

        /** 
         * Dispose the resources cached by this action.
         */
        protected void dispose()
        {}

        /**
         * @see Action#run()
         */
        public void run()
        {
            openDialog();
            activate(selection);
        }

        /**
         * Activate the selected item.
         */
        public void activate(Object selection)
        {
            if (selection != null)
            {
                IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) selection;
                reveal(location);
            }
        }

        /*
         * Open a dialog showing all views in the activation order
         */
        private void openDialog()
        {
            final int MAX_ITEMS = 22;

            selection = null;
            final Shell dialog = new Shell(UIPlugin.getDefault().getActiveWorkbenchShell(), SWT.MODELESS);
            Display display = dialog.getDisplay();
            dialog.setLayout(new FillLayout());

            final Table table = new Table(dialog, SWT.SINGLE | SWT.FULL_SELECTION);
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            TableColumn tc = new TableColumn(table, SWT.NONE);
            tc.setResizable(false);
            tc.setText(getTableHeader());
            addItems(table);
            int tableItemCount = table.getItemCount();

            switch (tableItemCount)
            {
                case 0 :
                    // do nothing;
                    break;
                case 1 :
                    table.setSelection(0);
                    break;
                default :
                    table.setSelection(forward ? 1 : table.getItemCount() - 1);
            }

            tc.pack();
            table.pack();
            Rectangle tableBounds = table.getBounds();
            tableBounds.height = Math.min(tableBounds.height, table.getItemHeight() * MAX_ITEMS);
            table.setBounds(tableBounds);
            dialog.pack();

            tc.setWidth(table.getClientArea().width);
            table.setFocus();
            table.addFocusListener(new FocusListener()
            {
                public void focusGained(FocusEvent e)
                {}

                public void focusLost(FocusEvent e)
                {
                    cancel(dialog);
                }
            });

            Rectangle dialogBounds = dialog.getBounds();
            Rectangle displayBounds = display.getClientArea();
            Rectangle parentBounds = dialog.getParent().getBounds();

            //Place it in the center of its parent;
            dialogBounds.x = parentBounds.x + ((parentBounds.width - dialogBounds.width) / 2);
            dialogBounds.y = parentBounds.y + ((parentBounds.height - dialogBounds.height) / 2);
            if (!displayBounds.contains(dialogBounds.x, dialogBounds.y)
                || !displayBounds.contains(dialogBounds.x + dialogBounds.width, dialogBounds.y + dialogBounds.height))
            {
                //Place it in the center of the display if it is not visible
                //when placed in the center of its parent;
                dialogBounds.x = (displayBounds.width - dialogBounds.width) / 2;
                dialogBounds.y = (displayBounds.height - dialogBounds.height) / 2;
            }
            dialogBounds.height = dialogBounds.height + 3 - table.getHorizontalBar().getSize().y;

            dialog.setBounds(dialogBounds);

            table.addHelpListener(new HelpListener()
            {
                public void helpRequested(HelpEvent event)
                {}
            });

            try
            {
                dialog.open();
                addMouseListener(table, dialog);
                addKeyListener(table, dialog);

                while (!dialog.isDisposed())
                    if (!display.readAndDispatch())
                        display.sleep();
            } finally
            {
                if (!dialog.isDisposed())
                    cancel(dialog);
            }
        }

        /**
         * Returns the string which will be shown in the table header.
         */
        protected String getTableHeader()
        {
            return UIPlugin.getString("choose-template-table-header");
        }

        /**
         * Add all views to the dialog in the activation order
         */
        protected void addItems(Table table)
        {
            for (Iterator iter = fTemplateLocations.iterator(); iter.hasNext();)
            {
                IResourceWorkspaceLocation element = (IResourceWorkspaceLocation) iter.next();
                if (element.getStorage() == null)
                    continue;
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(element.getName());
                item.setImage(getImage(element));
                item.setData(element);
            }
        }

        protected abstract Image getImage(IResourceWorkspaceLocation location);

        private void addKeyListener(final Table table, final Shell dialog)
        {
            table.addKeyListener(new KeyListener()
            {
                private boolean firstKey = true;
                private boolean quickReleaseMode = false;

                public void keyPressed(KeyEvent e)
                {
                    int keyCode = e.keyCode;
                    int stateMask = e.stateMask;
                    char character = e.character;
                    int accelerator = stateMask | (keyCode != 0 ? keyCode : convertCharacter(character));

                    //                    System.out.println("\nPRESSED");
                    //                    printKeyEvent(e);
                    //                    System.out.println(
                    //                        "accelerat:\t"
                    //                            + accelerator
                    //                            + "\t ("
                    //                            + KeySupport.formatStroke(Stroke.create(accelerator), true)
                    //                            + ")");

                    boolean acceleratorForward = false;
                    boolean acceleratorBackward = false;
//TODO revisit
//                    if (commandForward != null)
//                    {
//                        Map commandMap = Manager.getInstance().getKeyMachine().getCommandMap();
//                        SortedSet sequenceSet = (SortedSet) commandMap.get(commandForward);
//
//                        if (sequenceSet != null)
//                        {
//                            Iterator iterator = sequenceSet.iterator();
//
//                            while (iterator.hasNext())
//                            {
//                                Sequence sequence = (Sequence) iterator.next();
//                                List strokes = sequence.getStrokes();
//                                int size = strokes.size();
//
//                                if (size > 0 && accelerator == ((Stroke) strokes.get(size - 1)).getValue())
//                                {
//                                    acceleratorForward = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }
//
//                    if (commandBackward != null)
//                    {
//                        Map commandMap = Manager.getInstance().getKeyMachine().getCommandMap();
//                        SortedSet sequenceSet = (SortedSet) commandMap.get(commandBackward);
//
//                        if (sequenceSet != null)
//                        {
//                            Iterator iterator = sequenceSet.iterator();
//
//                            while (iterator.hasNext())
//                            {
//                                Sequence sequence = (Sequence) iterator.next();
//                                List strokes = sequence.getStrokes();
//                                int size = strokes.size();
//
//                                if (size > 0 && accelerator == ((Stroke) strokes.get(size - 1)).getValue())
//                                {
//                                    acceleratorBackward = true;
//                                    break;
//                                }
//                            }
//                        }
//                    }

                    if (character == SWT.CR || character == SWT.LF)
                        ok(dialog, table);
                    else if (acceleratorForward)
                    {
                        if (firstKey && e.stateMask != 0)
                            quickReleaseMode = true;

                        int index = table.getSelectionIndex();
                        table.setSelection((index + 1) % table.getItemCount());
                    } else if (acceleratorBackward)
                    {
                        if (firstKey && e.stateMask != 0)
                            quickReleaseMode = true;

                        int index = table.getSelectionIndex();
                        table.setSelection(index >= 1 ? index - 1 : table.getItemCount() - 1);
                    } else if (
                        keyCode != SWT.ALT
                            && keyCode != SWT.COMMAND
                            && keyCode != SWT.CTRL
                            && keyCode != SWT.SHIFT
                            && keyCode != SWT.ARROW_DOWN
                            && keyCode != SWT.ARROW_UP
                            && keyCode != SWT.ARROW_LEFT
                            && keyCode != SWT.ARROW_RIGHT)
                        cancel(dialog);

                    firstKey = false;
                }

                public void keyReleased(KeyEvent e)
                {
                    int keyCode = e.keyCode;
                    int stateMask = e.stateMask;
                    char character = e.character;
                    int accelerator = stateMask | (keyCode != 0 ? keyCode : convertCharacter(character));

                    //                    System.out.println("\nRELEASED");
                    //                    printKeyEvent(e);
                    //                    System.out.println(
                    //                        "accelerat:\t"
                    //                            + accelerator
                    //                            + "\t ("
                    //                            + KeySupport.formatStroke(Stroke.create(accelerator), true)
                    //                            + ")");

                    if ((firstKey || quickReleaseMode) && keyCode == stateMask && keyCode != SWT.ALT)
                        ok(dialog, table);
                }
            });
        }

        private char convertCharacter(char c)
        {
            return c >= 0 && c <= 31 ? (char) (c + '@') : Character.toUpperCase(c);
        }
        //    
        //        private void printKeyEvent(KeyEvent keyEvent)
        //        {
        //            System.out.println(
        //                "keyCode:\t"
        //                    + keyEvent.keyCode
        //                    + "\t ("
        //                    + KeySupport.formatStroke(Stroke.create(keyEvent.keyCode), true)
        //                    + ")");
        //            System.out.println(
        //                "stateMask:\t"
        //                    + keyEvent.stateMask
        //                    + "\t ("
        //                    + KeySupport.formatStroke(Stroke.create(keyEvent.stateMask), true)
        //                    + ")");
        //            System.out.println("character:\t" + (int) keyEvent.character + "\t (" + keyEvent.character + ")");
        //        }

        /*
         * Close the dialog saving the selection
         */
        private void ok(Shell dialog, final Table table)
        {
            TableItem[] items = table.getSelection();

            if (items != null && items.length == 1)
                selection = items[0].getData();

            dialog.close();
            dispose();
        }

        /*
         * Close the dialog and set selection to null.
         */
        private void cancel(Shell dialog)
        {
            selection = null;
            dialog.close();
            dispose();
        }

        /*
         * Add mouse listener to the table closing it when
         * the mouse is pressed.
         */
        private void addMouseListener(final Table table, final Shell dialog)
        {
            table.addMouseListener(new MouseListener()
            {
                public void mouseDoubleClick(MouseEvent e)
                {
                    ok(dialog, table);
                }

                public void mouseDown(MouseEvent e)
                {
                    ok(dialog, table);
                }

                public void mouseUp(MouseEvent e)
                {
                    ok(dialog, table);
                }
            });
        }

    }
    /**
            * @param location
            */
    protected void reveal(IResourceWorkspaceLocation location)
    {
        if (location == null)
            return;
        reveal(location.getStorage());
    }

    protected void reveal(IStorage storage)
    {
        if (storage != null)
            UIPlugin.openTapestryEditor((IStorage) storage);
    }

    protected TapestryProject findTapestryProject(IClassFile file)
    {
        TapestryProject tproject = TapestryCore.getDefault().getTapestryProjectFor(file);
        return tproject;
    }
    
    protected TapestryProject findTapestryProject(IFile file)
       {
           TapestryProject tproject = TapestryCore.getDefault().getTapestryProjectFor(file);
           return tproject;
       }

}
