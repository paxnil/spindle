package com.iw.plugins.spindle.editors.scratch;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiEditorInput;

import com.iw.plugins.spindle.UIPlugin;

public class OpenActionDelegate extends ActionDelegate implements IViewActionDelegate
{

    IViewPart view;

    public void run(IAction action)
    {
        String id = action.getId();
        System.err.println("TiledEditorExample - Run: " + id);

        ISelectionProvider serv = view.getSite().getSelectionProvider();
        ISelection sel = serv.getSelection();

        if (sel instanceof IStructuredSelection)
        {
            IStructuredSelection ss = (IStructuredSelection) sel;
            Object el[] = ss.toArray();
            String ids[] = new String[el.length];
            IEditorInput allInput[] = new IEditorInput[el.length];
            for (int i = 0; i < el.length; i++)
            {
                System.out.println("TiledEditorExample - " + el[i]);
                if (el[i] instanceof IFile)
                {
                    ids[i] = "org.eclipse.ui.DefaultTextEditor";
                    allInput[i] = new FileEditorInput((IFile) el[i]);
                    String ext = ((IFile) el[i]).getFileExtension();
                    if ("java".equals(ext))
                        ids[i] = "org.eclipse.jdt.ui.CompilationUnitEditor";

                    if ("page".equals(ext) || "jwc".equals(ext))
                        ids[i] = "com.iw.plugins.spindle.editors.spec";

                    if ("html".equals(ext))
                        ids[i] = "com.iw.plugins.spindle.editors.template";

                } else if (el[i] instanceof ICompilationUnit)
                {
                    IFile file;
                    try
                    {
                        file = (IFile) ((ICompilationUnit) el[i]).getUnderlyingResource();
                        ids[i] = "org.eclipse.jdt.ui.CompilationUnitEditor";
                        allInput[i] = new FileEditorInput(file);

                    } catch (JavaModelException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                } else
                {
                    System.out.println("TiledEditorExample - Select only IFile(s)");
                }
            }
            MultiEditorInput input = new MultiEditorInput(ids, allInput);
            try
            {
                UIPlugin.getDefault().getActivePage().openEditor(input, "org.eclipse.ui.examples.tilededitor.TiledEditor");
            } catch (PartInitException e)
            {
                e.printStackTrace();
            }
        }

    }

    /**
     * @see IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose()
    {}

   

    public void selectionChanged(IAction action, ISelection selection)
    {}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view)
    {
        // TODO Auto-generated method stub

    }

}
