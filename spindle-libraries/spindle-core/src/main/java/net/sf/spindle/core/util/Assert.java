package net.sf.spindle.core.util;

/*
 The contents of this file are subject to the Mozilla Public License
 Version 1.1 (the "License"); you may not use this file except in
 compliance with the License. You may obtain a copy of the License at
 http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS"
 basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 License for the specific language governing rights and limitations
 under the License.

 The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

 The Initial Developer of the Original Code is _____Geoffrey Longman__.
 Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
 __Geoffrey Longman____. All Rights Reserved.

 Contributor(s): __glongman@gmail.com___.
 */
public final class Assert
{

    /**
     * <code>AssertionFailedException</code> is a runtime exception thrown by some of the methods
     * in <code>Assert</code>.
     */
    public static class AssertionFailedException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public AssertionFailedException()
        {
        }

        public AssertionFailedException(String detail)
        {
            super(detail);
        }
    }

    /* This class is not intended to be instantiated. */
    private Assert()
    {
    }

    /**
     * Asserts that an argument is legal. If the given boolean is not <code>true</code>, an
     * <code>IllegalArgumentException</code> is thrown.
     */
    public static boolean isLegal(boolean expression)
    {
        // succeed as quickly as possible
        if (expression)
            return true;

        return isLegal(expression, "");
    }

    /**
     * Asserts that an argument is legal. If the given boolean is not <code>true</code>, an
     * <code>IllegalArgumentException</code> is thrown.
     */
    public static boolean isLegal(boolean expression, String message)
    {
        if (!expression)
            throw new IllegalArgumentException("assertion failed; " + message);
        return expression;
    }

    /**
     * Asserts that the given object is not <code>null</code>. If this is not the case, some kind
     * of unchecked exception is thrown.
     */
    public static void isNotNull(Object object)
    {
        // succeed as quickly as possible
        if (object != null)
            return;

        isNotNull(object, "");
    }

    /**
     * Asserts that the given object is not <code>null</code>. If this is not the case, a
     * <code>NullPointerException</code> exception is thrown. The given message is included in
     * that exception, to aid debugging.
     */
    public static void isNotNull(Object object, String message)
    {
        if (object == null)
            throw new NullPointerException("null argument;" + message);
    }

    /**
     * Asserts that the given boolean is <code>true</code>. If this is not the case, some kind of
     * unchecked exception is thrown.
     */
    public static boolean isTrue(boolean expression)
    {
        if (expression)
            return true;

        return isTrue(expression, "");
    }

    /**
     * Asserts that the given boolean is <code>true</code>. If this is not the case,
     * <code>AssertionFailedException</code> exception is thrown. The given message is included in
     * that exception.
     */
    public static boolean isTrue(boolean expression, String message)
    {
        if (!expression)
            throw new AssertionFailedException("Assertion failed: " + message);
        return expression;
    }
}