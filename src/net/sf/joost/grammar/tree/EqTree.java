/*
 * $Id: EqTree.java,v 1.2 2005/01/23 19:47:27 obecker Exp $
 * 
 * The contents of this file are subject to the Mozilla Public License 
 * Version 1.1 (the "License"); you may not use this file except in 
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the 
 * License.
 *
 * The Original Code is: this file
 *
 * The Initial Developer of the Original Code is Oliver Becker.
 *
 * Portions created by  ______________________ 
 * are Copyright (C) ______ _______________________. 
 * All Rights Reserved.
 *
 * Contributor(s): Thomas Behrends.
 */

package net.sf.joost.grammar.tree;

import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;

import org.xml.sax.SAXException;

/**
 * Objects of EqTree represent comparison nodes (equal, "=") in the syntax 
 * tree of a pattern or an STXPath expression.
 * @version $Revision: 1.2 $ $Date: 2005/01/23 19:47:27 $
 * @author Oliver Becker
 */
final public class EqTree extends Tree
{
   public EqTree(Tree left, Tree right)
   {
      super(EQ, left, right);
   }

   public Value evaluate(Context context, int top)
      throws SAXException
   {
      Value v1 = left.evaluate(context, top);
      Value v2 = right.evaluate(context, top);
      if (v1.type == Value.EMPTY || v2.type == Value.EMPTY)
         return Value.VAL_FALSE;
      
      // sequences: find a pair such that the comparison is true
      for (Value vi = v1; vi != null; vi = vi.next) {
         for (Value vj = v2; vj != null; vj = vj.next) {
            if (vi.type == Value.BOOLEAN || vj.type == Value.BOOLEAN) {
               if (vi.getBooleanValue() == vj.getBooleanValue())
                  return Value.VAL_TRUE;
            }
            else if (vi.type == Value.NUMBER || vj.type == Value.NUMBER) {
               if (vi.getNumberValue() == vj.getNumberValue())
                  return Value.VAL_TRUE;
            }
            else {
               if (vi.getStringValue().equals(vj.getStringValue()))
                  return Value.VAL_TRUE;
            }
         } // for (vj ...
      } // for (vi ...
      // none of the item comparisons evaluated to true
      return Value.VAL_FALSE;
   }
}
