package net.sf.spindle.core.spec;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specification types that can have description tags
 * 
 * @author glongman@gmail.com
 */
public class DescribableSpecification extends BaseSpecification
    implements
      IPluginDescribable
{

  /**
   * The locations and values of all description declarations in a spec.
   * Immutable after a parse/scan episode.
   */
  private List<PluginDescriptionDeclaration> fDescriptionDeclarations;

  private String fDescription;

  /**
   * @param type
   */
  public DescribableSpecification(int type)
  {
    super(type);
  }

  public String getDescription()
  {
    return fDescription;
  }

  public void setDescription(String description)
  {
    fDescription = description;
  }

  public void addDescriptionDeclaration(PluginDescriptionDeclaration decl)
  {
    if (fDescriptionDeclarations == null)
      fDescriptionDeclarations = new ArrayList<PluginDescriptionDeclaration>();

    fDescriptionDeclarations.add(decl);
  }

  public List<PluginDescriptionDeclaration> getDescriptionDeclarations()
  {
    if (fDescriptionDeclarations == null)
      return Collections.emptyList();

    return fDescriptionDeclarations;
  }

}