/*
 * $Id: Not.java,v 1.2 2006/03/21 19:25:03 obecker Exp $
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
 * Contributor(s): ______________________________________. 
 */

package net.sf.joost.stx.function;

import net.sf.joost.grammar.EvalException;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;
import net.sf.joost.stx.function.FunctionFactory.Instance;

import org.xml.sax.SAXException;

/**
 * The <code>not</code> function.<br>
 * Returns the negation of its parameter.
 * 
 * @see <a target="xq1xp2fo"
 *      href="http://www.w3.org/TR/xpath-functions/#func-not"> fn:not in "XQuery
 *      1.0 and XPath 2.0 Functions and Operators"</a>
 * @version $Revision: 1.2 $ $Date: 2006/03/21 19:25:03 $
 * @author Oliver Becker
 */
final public class Not implements Instance
{
   /** @return 1 **/
   public int getMinParCount() { return 1; }

   /** @return 1 */
   public int getMaxParCount() { return 1; }

   /** @return "not" */
   public String getName() { return FunctionFactory.FNSP + "not"; }

   public Value evaluate(Context context, int top, Tree args)
      throws SAXException, EvalException
   {
      return args.evaluate(context, top).getBooleanValue() ? Value.VAL_FALSE 
                                                           : Value.VAL_TRUE;
   }
}