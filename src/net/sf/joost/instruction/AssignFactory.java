/*
 * $Id: AssignFactory.java,v 2.3 2003/06/03 14:30:18 obecker Exp $
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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Hashtable;
import java.util.HashSet;

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;


/** 
 * Factory for <code>assign</code> elements, which are represented by
 * the inner Instance class. 
 * @version $Revision: 2.3 $ $Date: 2003/06/03 14:30:18 $
 * @author Oliver Becker
 */

final public class AssignFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public AssignFactory()
   {
      attrNames = new HashSet();
      attrNames.add("name");
      attrNames.add("select");
   }

   /** @return <code>"assign"</code> */
   public String getName()
   {
      return "assign";
   }

   public NodeBase createNode(NodeBase parent, String qName, 
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      String nameAtt = getAttribute(qName, attrs, "name", context);
      String varName = getExpandedName(nameAtt, context);

      String selectAtt = attrs.getValue("select");
      Tree selectExpr;
      if (selectAtt != null) 
         selectExpr = parseExpr(selectAtt, context);
      else
         selectExpr = null;

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, nameAtt, varName,
                          selectExpr);
   }


   /** Represents an instance of the <code>assign</code> element. */
   final public class Instance extends NodeBase
   {
      private String varName, expName;
      private Tree select;

      protected Instance(String qName, NodeBase parent, ParseContext context,
                         String varName, String expName, Tree select)
      {
         super(qName, parent, context,
               // this element must be empty if there is a select attribute
               select == null);
         this.varName = varName;
         this.expName = expName;
         this.select = select;
      }
      

      /**
       * Evaluate the <code>select</code> attribute if present.
       */
      public short process(Context context)
         throws SAXException
      {
         // does this variable have a select attribute?
         if (select != null) {
            Value v = select.evaluate(context, this);
            processVar(v, context);
         }
         else {
            // endInstruction present
            super.process(context);
            // create a new StringEmitter for this instance and put it
            // on the emitter stack
            context.emitter.pushEmitter(
               new StringEmitter(new StringBuffer(),
                                 "(`" + qName + "' started in line " +
                                 lineNo + ")"));
         }
         return PR_CONTINUE;
      }


      /**
       * Called only if this instruction has no <code>select</code>
       * attribute. Evaluates its contents.
       */
      public short processEnd(Context context)
         throws SAXException
      {
         // use contents
         Value v = new Value(((StringEmitter)context.emitter.popEmitter())
                                                 .getBuffer().toString());

         processVar(v, context);

         return super.processEnd(context);
      }


      /**
       * Assigns a value to a variable.
       * @param v the value
       * @param context the current context
       */
      private void processVar(Value v, Context context)
         throws SAXException
      {
         // find variable
         // TODO: this should be a static lookup
         Hashtable vars = null;
         Object obj = context.localVars.get(expName);
         if (obj != null)
            vars = context.localVars;
         else {
            GroupBase group = context.currentGroup;
            while (obj == null && group != null) {
               vars = (Hashtable)group.groupVars.peek();
               obj = vars.get(expName);
               group = group.parentGroup;
            }
         }
         if (vars == null) {
            context.errorHandler.error(
               "Can't assign to undeclared variable `" + varName + "'",
               publicId, systemId, lineNo, colNo);
            return; // if the errorHandler returns
         }

         // release stored SAXEvent objects
         Value val = (Value)obj;
         do {
            if (val.type == Value.NODE)
               val.event.removeRef();
            val = val.next;
         } while (val != null);

         // assign new value
         vars.put(expName, v);
      }
   }
}
