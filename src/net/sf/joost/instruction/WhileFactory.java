/*
 * $Id: WhileFactory.java,v 1.1 2003/03/17 15:56:39 obecker Exp $
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

package net.sf.joost.instruction;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Stack;

import net.sf.joost.stx.Context;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.Value;
import net.sf.joost.grammar.Tree;


/** 
 * Factory for <code>while</code> elements, which are represented by
 * the inner Instance class. 
 * @version $Revision: 1.1 $ $Date: 2003/03/17 15:56:39 $
 * @author Oliver Becker
 */

final public class WhileFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public WhileFactory()
   {
      attrNames = new HashSet();
      attrNames.add("test");
   }

   /** @return <code>"while"</code> */
   public String getName()
   {
      return "while";
   }

   public NodeBase createNode(NodeBase parent, String uri, String lName, 
                              String qName, Attributes attrs, 
                              Hashtable nsSet, Locator locator)
      throws SAXParseException
   {
      String testAtt = getAttribute(qName, attrs, "test", locator);
      Tree testExpr = parseExpr(testAtt, nsSet, locator);
      checkAttributes(qName, attrs, attrNames, locator);
      return new Instance(qName, parent, locator, testExpr);
   }


   /** Represents an instance of the <code>while</code> element. */
   final public class Instance extends NodeBase
   {
      private Tree test;

      // Constructor
      protected Instance(String qName, NodeBase parent, Locator locator, 
                         Tree test)
      {
         super(qName, parent, locator, false);
         this.test = test;
      }
      
      /**
       * Evaluates the expression given in the test attribute and
       * processes its children as long as the result is true.
       *
       * @param emitter the Emitter
       * @param eventStack the ancestor event stack
       * @param processStatus the current processing status
       * @return the new processing status, influenced by contained
       *         <code>stx:process-...</code> elements.
       */    
      protected short process(Emitter emitter, Stack eventStack,
                              Context context, short processStatus)
         throws SAXException
      {
         boolean testResult = false;
         if ((processStatus & ST_PROCESSING) != 0) {
            testResult = test.evaluate(context, eventStack, this)
                             .convertToBoolean().bool;
         }
         else {
            // re-entered, we must have been here before ...
            testResult = true;
         }

         while (testResult) {
            processStatus = super.process(emitter, eventStack, context,
                                          processStatus);
            if ((processStatus & ST_PROCESSING) == 0) // suspend processing
               break;
            testResult = test.evaluate(context, eventStack, this)
                             .convertToBoolean().bool;
         }
         return processStatus;
      }
   }
}