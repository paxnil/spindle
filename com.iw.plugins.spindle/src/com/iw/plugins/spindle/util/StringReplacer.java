/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.util;

public class StringReplacer {

  private String template;

  public StringReplacer(String template) {
    if (template == null) {
      throw new IllegalArgumentException("template for replacer can't be null!");
    }
    this.template = template;
  }

  public void replace(String token, String replaceWith) {
    String useToken = '$' + token + '$';
    if (useToken.equals(replaceWith)) {
      return;
    }
    int length = useToken.length();
    int start = template.indexOf(useToken);
    while (start >= 0) {
      if (template.equals(useToken)) {

        template = replaceWith;

      } else if (start == 0) {

        template = replaceWith + template.substring(start + length);

      } else if (template.length() - replaceWith.length() == start) {

        template = template.substring(0, start) + replaceWith;

      } else {

        template = template.substring(0, start) + replaceWith + template.substring(start + length);
      }
      start = template.indexOf(useToken);
    }
  }

  public String toString() {
    return template;
  }

  static public void main(String[] args) {
    StringReplacer replacer1 = new StringReplacer("$POO$");
    replacer1.replace("POO", "moo");
    System.out.println(replacer1.toString());
    StringReplacer replacer2 = new StringReplacer("$POO$1234567890");
    replacer2.replace("POO", "moo");
    System.out.println(replacer2.toString());
    StringReplacer replacer3 = new StringReplacer("1234567890$POO$");
    replacer3.replace("POO", "moo");
    System.out.println(replacer3.toString());
    StringReplacer replacer4 = new StringReplacer("$POO$1234567890$POO$");
    replacer4.replace("POO", "moo");
    System.out.println(replacer4.toString());
    StringReplacer replacer5 = new StringReplacer("1234567890$POO$1234567890");
    replacer5.replace("POO", "moo");
    System.out.println(replacer5.toString());
    StringReplacer replacer6 = new StringReplacer("$POO$1234567890$POO$1234567890$POO$1234567890$POO$");
    replacer6.replace("POO", "moo");
    System.out.println(replacer6.toString());

    /*
    
    */
  }
}