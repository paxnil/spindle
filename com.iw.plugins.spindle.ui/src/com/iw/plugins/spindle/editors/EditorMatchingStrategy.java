package com.iw.plugins.spindle.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;

import com.iw.plugins.spindle.core.util.JarEntryFileUtil;

public abstract class EditorMatchingStrategy implements IEditorMatchingStrategy
{

    protected abstract Class getEditorClass();
    
    public boolean matches(IEditorReference editorRef, IEditorInput input)
    {
        try
        {
            IStorageEditorInput newInput = (IStorageEditorInput) input;
            IEditorPart existing = editorRef.getEditor(true);
            if (existing == null)
                return false;
            if (!getEditorClass().isAssignableFrom(existing.getClass()))
                return false;

            IStorageEditorInput existingInput = (IStorageEditorInput) existing.getEditorInput();

            if (existingInput instanceof IFileEditorInput)
                return input.equals(existingInput);
            else
                return JarEntryFileUtil.inputsEqual(newInput, existingInput);
        }
        catch (ClassCastException e)
        {

        }
        catch (CoreException e)
        {

        }
        return false;
    }

}
