package net.sf.spindle.core.util;

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
