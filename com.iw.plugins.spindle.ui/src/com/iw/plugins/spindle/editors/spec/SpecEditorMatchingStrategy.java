package com.iw.plugins.spindle.editors.spec;

import com.iw.plugins.spindle.editors.EditorMatchingStrategy;

public class SpecEditorMatchingStrategy extends EditorMatchingStrategy
{
    protected Class getEditorClass()
    {
        return SpecEditor.class;
    }
}
