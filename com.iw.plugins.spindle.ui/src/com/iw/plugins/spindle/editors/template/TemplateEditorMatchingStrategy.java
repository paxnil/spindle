package com.iw.plugins.spindle.editors.template;

import com.iw.plugins.spindle.editors.EditorMatchingStrategy;

public class TemplateEditorMatchingStrategy extends EditorMatchingStrategy
{
    protected Class getEditorClass()
    {
        return TemplateEditor.class;
    }
}
