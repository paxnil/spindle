package net.sf.spindle.core.build;

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
/**
 * Common Question asked of all kinds of Incremental Builders
 * 
 * @author glongman@gmail.com
 */
public interface IIncrementalBuild extends IBuild
{
    /**
     * A question asked of Incremental Builds by the TapestryBuilder.
     * <p>
     * Answering false prompts a Full build
     * 
     * @return true if an incremental build is indicated, false otherwise.
     */
    public boolean canIncrementalBuild();

    /**
     * A question asked of Incremental Builds by the TapestryBuilder.
     * <p>
     * Answering false says that yes, an incremental build is indicated but would be fruitless and
     * the build state would not change. In this case no build at all is needed.
     * 
     * @return true if an build is indicated, false otherwise.
     */
    public boolean needsIncrementalBuild();

}