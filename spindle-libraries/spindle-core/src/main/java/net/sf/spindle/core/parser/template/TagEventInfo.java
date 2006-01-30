package net.sf.spindle.core.parser.template;
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
import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.core.source.ISourceLocation;
import net.sf.spindle.core.source.SourceLocation;

public class TagEventInfo {

	private Map<String, ISourceLocation> fAttributeMap;

	SourceLocation fStartTagLocation;

	public Map<String, ISourceLocation> getAttributeMap() {
		if (fAttributeMap == null)
			fAttributeMap = new HashMap<String, ISourceLocation>();

		return fAttributeMap;
	}

	public ISourceLocation getStartTagLocation() {
		return fStartTagLocation;
	}

	/**
	 * @param cursor
	 * @return
	 */
	public ISourceLocation findLocation(int cursorPosition) {

		if (fAttributeMap != null) {
			for (String attributeName : fAttributeMap.keySet()) {
				ISourceLocation attrLoc = (ISourceLocation) fAttributeMap
						.get(attributeName);
				if (attrLoc.contains(cursorPosition))
					return attrLoc;
			}
		}
		if (fStartTagLocation != null)
			return fStartTagLocation;

		return null;
	}

}