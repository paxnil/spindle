package core.parser.dom;

import org.apache.hivemind.Resource;

public interface IDOMModelSource
{
    IDOMModel parseDocument(Resource resource, boolean validate, Object requestor);

    IDOMModel parseDocument(Resource resource, String encoding, boolean validate, Object consumer);

    void release(IDOMModel model, Object requestor);
}
