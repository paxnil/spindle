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
import net.sf.spindle.core.DTDRegistry;

import org.apache.tapestry.parse.SpecificationParser;

/**
 * @author gwl
 */
public class XMLPublicIDUtil
{

    static public final int DTD_3_0 = 4;

    static public final int DTD_4_0 = 5;

    static public final int DTD_SERVLET_2_2 = 20;

    static public final int DTD_SERVLET_2_3 = 21;

    static public final int UNKNOWN_DTD = 999;

    static public final int[] ALLOWED_SPEC_DTDS = new int[]
    { DTD_3_0, DTD_4_0 };

    static public final String SPEC_DTD_ERROR_KEY = "error-invalid-spec-public-id";

    static public final int[] ALLOWED_SERVLET_DTDS = new int[]
    { DTD_SERVLET_2_2, DTD_SERVLET_2_3 };

    static public final String SERVLET_DTD_ERROR_KEY = "error-invalid-servlet-public-id";

    static public int getDTDVersion(String publicId)
    {

        if (publicId == null)
            return UNKNOWN_DTD;

        if (publicId.equals(SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID))
            return DTD_3_0;

        if (publicId.equals(SpecificationParser.TAPESTRY_DTD_4_0_PUBLIC_ID))
            return DTD_4_0;

        if (publicId.equals(DTDRegistry.SERVLET_2_2_PUBLIC_ID))
            return DTD_SERVLET_2_2;

        if (publicId.equals(DTDRegistry.SERVLET_2_3_PUBLIC_ID))
            return DTD_SERVLET_2_3;

        return UNKNOWN_DTD;
    }

    static public String getPublicId(int DTDVersion)
    {
        switch (DTDVersion)
        {
            case DTD_3_0:
                return SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID;
            case DTD_4_0:
                return SpecificationParser.TAPESTRY_DTD_4_0_PUBLIC_ID;
            case DTD_SERVLET_2_2:
                return DTDRegistry.SERVLET_2_2_PUBLIC_ID;
            case DTD_SERVLET_2_3:
                return DTDRegistry.SERVLET_2_3_PUBLIC_ID;
        }
        return null;
    }

}
