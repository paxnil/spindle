package core.builder;

import core.resources.ICoreResource;

public class ClashException extends BuilderException
{
    private ICoreResource requestor;

    private ICoreResource owner;

    private ICoreResource claimed;

    public ClashException(ICoreResource requestor, ICoreResource owner, ICoreResource claimed)
    {
        super();
        this.requestor = requestor;
        this.owner = owner;
        this.claimed = claimed;
    }
    
    public ClashException(ICoreResource requestor, ICoreResource owner, ICoreResource claimed, String message)
    {
        super(message);
        this.requestor = requestor;
        this.owner = owner;
        this.claimed = claimed;
    }

    public ICoreResource getClaimed()
    {
        return claimed;
    }

    public ICoreResource getOwner()
    {
        return owner;
    }

    public ICoreResource getRequestor()
    {
        return requestor;
    }

 
}
