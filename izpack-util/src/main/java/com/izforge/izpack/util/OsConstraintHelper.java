package com.izforge.izpack.util;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.binding.OsModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates OS constraints specified on creation time and allows to check them against the
 * current OS.
 * <p/>
 * For example, this is used for &lt;executable&gt;s to check whether the executable is suitable for
 * the current OS.
 *
 * @author Olexij Tkatchenko <ot@parcs.de>
 */
public class OsConstraintHelper
{
    /**
     * Extract a list of OS constraints from given element.
     *
     * @param element parent IXMLElement
     * @return List of OsModel (or empty List if no constraints found)
     */
    public static List<OsModel> getOsList(IXMLElement element)
    {
        // get os info on this executable
        ArrayList<OsModel> osList = new ArrayList<OsModel>();
        for (IXMLElement osElement : element.getChildrenNamed("os"))
        {
            osList.add(
                    new OsModel(
                            osElement.getAttribute("arch",
                                                   null),
                            osElement.getAttribute("family",
                                                   null),
                            osElement.getAttribute("jre",
                                                   null),
                            osElement.getAttribute("name",
                                                   null),
                            osElement.getAttribute("version",
                                                   null))
            );
        }
        // backward compatibility: still support os attribute
        String osattr = element.getAttribute("os");
        if ((osattr != null) && (osattr.length() > 0))
        {
            // add the "os" attribute as a family constraint
            osList.add(
                    new OsModel(null, osattr, null, null, null)
            );
        }

        return osList;
    }
}
