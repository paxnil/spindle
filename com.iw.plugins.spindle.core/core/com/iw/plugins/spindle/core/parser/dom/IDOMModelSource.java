package com.iw.plugins.spindle.core.parser.dom;

import com.iw.plugins.spindle.core.resources.ICoreResource;

public interface IDOMModelSource
{
    IDOMModel parseDocument(ICoreResource resource, boolean validate, Object requestor);

    IDOMModel parseDocument(ICoreResource resource, String encoding, boolean validate, Object consumer);

    void release(IDOMModel model, Object requestor);
}
