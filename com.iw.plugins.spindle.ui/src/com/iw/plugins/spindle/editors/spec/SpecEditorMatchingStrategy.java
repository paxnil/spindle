package com.iw.plugins.spindle.editors.spec;

import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;

public class SpecEditorMatchingStrategy implements IEditorMatchingStrategy
{

    public boolean matches(IEditorReference editorRef, IEditorInput input)
    {

        try
        {
            IStorageEditorInput newInput = (IStorageEditorInput) input;
            String extension = new Path(newInput.getName()).getFileExtension();
            if (!extension.equals("application") || !!extension.equals("jwc")
                    || !extension.equals("library") || !extension.equals("page"))
                return false;
            IEditorPart part = editorRef.getEditor(true);
            if (part == null)
                return false;
            if (!(part instanceof SpecEditor))
                return false;
            
            IEditorInput existing = editorRef.getEditorInput();
            if (existing)
            IStorageEditorInput existing = (IStorageEditorInput) editorRef.getEditorInput();
        }
        catch (PartInitException e)
        {

        }
        catch (ClassCastException e)
        {

        }
        return false;
    }
}
