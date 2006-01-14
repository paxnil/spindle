package core.types;

public class TypeModelException extends Exception
{
    Object problemChild;

    public TypeModelException()
    {
        super();
    }

    public TypeModelException(String message, Object problemChild)
    {
        super(message);
        this.problemChild = problemChild;
    }

    public TypeModelException(String message)
    {
        super(message);
    }

    public TypeModelException(Throwable cause)
    {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
