/*
 * $Id: ModTree.java,v 1.1 2004/09/29 05:59:51 obecker Exp $
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
 * Objects of ModTree represent arithmetic mod nodes in the syntax tree of
 * a pattern or an STXPath expression.
 * @version $Revision: 1.1 $ $Date: 2004/09/29 05:59:51 $
 * @author Oliver Becker
 */
final public class ModTree extends Tree
{
   public ModTree(Tree left, Tree right)
   {
      super(MOD, left, right);
   }

   public Value evaluate(Context context, int top)
      throws SAXException
   {
      Value v1 = left.evaluate(context, top);
      if (v1.type == Value.EMPTY) 
         return v1;

      Value v2 = right.evaluate(context, top);
      if (v2.type == Value.EMPTY) 
         return v2;

      // none of the operands is empty, ok: perform the operation
      return new Value(v1.getNumberValue() % v2.getNumberValue());
   }
}
