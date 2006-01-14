package com.iw.plugins.spindle.core.eclipse.lang;


/*package*/class TypeElementInfo
{
    protected String label;
    protected TypeElement parent;
    protected TypeElement[] children;
    
    protected TypeElement getParent() {
        return parent;
    }
    
    protected void setParent(TypeElement parent) {
        this.parent = parent;
    }

    protected TypeElementInfo(String label)
    {
        this.label = label;
        this.children = TypeElement.EMPTY_TYPE_ELEMENT_ARRAY;        
    }

    public void addChild(TypeElement child)
    {
        if (this.children == TypeElement.EMPTY_TYPE_ELEMENT_ARRAY)
        {
            setChildren(new TypeElement[]
            { child });
        }
        else
        {
            if (!includesChild(child))
                setChildren(growAndAddToArray(this.children, child));
        }
    }

    public Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error();
        }
    }

    public TypeElement[] getChildren()
    {
        return this.children;
    }

    /**
     * Adds the new element to a new array that contains all of the elements of the old array.
     * Returns the new array.
     */
    protected TypeElement[] growAndAddToArray(TypeElement[] array, TypeElement addition)
    {
        TypeElement[] old = array;
        array = new TypeElement[old.length + 1];
        System.arraycopy(old, 0, array, 0, old.length);
        array[old.length] = addition;
        return array;
    }

    /**
     * Returns <code>true</code> if this child is in my children collection
     */
    protected boolean includesChild(TypeElement child)
    {

        for (int i = 0; i < this.children.length; i++)
        {
            if (this.children[i].equals(child))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an array with all the same elements as the specified array except for the element to
     * remove. Assumes that the deletion is contained in the array.
     */
    protected TypeElement[] removeAndShrinkArray(TypeElement[] array, TypeElement deletion)
    {
        TypeElement[] old = array;
        array = new TypeElement[old.length - 1];
        int j = 0;
        for (int i = 0; i < old.length; i++)
        {
            if (!old[i].equals(deletion))
            {
                array[j] = old[i];
            }
            else
            {
                System.arraycopy(old, i + 1, array, j, old.length - (i + 1));
                return array;
            }
            j++;
        }
        return array;
    }

    public void removeChild(TypeElement child)
    {
        if (includesChild(child))
        {
            setChildren(removeAndShrinkArray(this.children, child));
        }
    }

    public void setChildren(TypeElement[] children)
    {
        this.children = children;
    }

    public String toString()
    {
       return label;
    }
    
    
}
