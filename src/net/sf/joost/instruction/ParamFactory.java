/*
 * $Id: ParamFactory.java,v 2.1 2003/04/27 15:34:45 obecker Exp $
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

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;
import net.sf.joost.stx.Value;


/** 
 * Factory for <code>params</code> elements, which are represented by
 * the inner Instance class. 
 * @version $Revision: 2.1 $ $Date: 2003/04/27 15:34:45 $
 * @author Oliver Becker
 */

final public class ParamFactory extends FactoryBase
{
   // Log4J initialization
   private static org.apache.log4j.Logger log4j =
      org.apache.log4j.Logger.getLogger(ParamFactory.class);


   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public ParamFactory()
   {
      attrNames = new HashSet();
      attrNames.add("name");
      attrNames.add("required");
      attrNames.add("select");
   }

   /** @return <code>"param"</code> */
   public String getName()
   {
      return "param";
   }

   public NodeBase createNode(NodeBase parent, String uri, String lName, 
                              String qName, Attributes attrs, 
                              Hashtable nsSet, Locator locator)
      throws SAXParseException
   {
      if (parent == null || 
          !(parent instanceof TransformFactory.Instance ||
            parent instanceof TemplateBase)) // template, procedure
         throw new SAXParseException(
            "`" + qName + "' must be a top level element " +
            "or a child of stx:template or stx:procedure",
            locator);

      if(parent instanceof TemplateBase &&
         parent != parent.lastChild && // not the first
         !(parent.lastChild.getNode() instanceof Instance)) // not a stx:param
         throw new SAXParseException(
            "`" + qName + "' instructions must always occur as first " +
            "children of `" + parent.qName + "'",
            locator);

      String nameAtt = getAttribute(qName, attrs, "name", locator);
      String parName = getExpandedName(nameAtt, nsSet, locator);

      // default is false
      boolean required = getEnumAttValue("required", attrs, YESNO_VALUES, 
                                         locator) == YES_VALUE;

      String selectAtt = attrs.getValue("select");
      Tree selectExpr;
      if (selectAtt != null) {
         if (required)
            throw new SAXParseException(
               "`" + qName + "' must not have a `select' attribute if it " +
               "declares the parameter as required", locator);
         selectExpr = parseExpr(selectAtt, nsSet, locator);
      }
      else
         selectExpr = null;

      checkAttributes(qName, attrs, attrNames, locator);
      return new Instance(qName, parent, locator, nameAtt, parName,
                          selectExpr, required);
   }


   /** Represents an instance of the <code>param</code> element. */
   public class Instance extends VariableBase
   {
      private String varName;
      private Tree select;
      private boolean required;
      private AbstractInstruction contents, successor;

      protected Instance(String qName, NodeBase parent, Locator locator, 
                         String varName, String expName, Tree select,
                         boolean required)
      {
         super(qName, parent, locator, expName, 
               false, // keep-value has no meaning here
               // this element may have children if there is no select
               // attribute and the parameter is not required
               select == null && !required);
         this.varName = varName;
         this.select = select;
         this.required = required;
      }


      public boolean compile(int pass)
      {
         if (pass == 0)
            return true; // nodeEnd not available yet

         contents = next;
         successor = nodeEnd != null ? nodeEnd.next : next;
         return false;
      }


      public short process(Context context)
         throws SAXException
      {
         Value v;
         if (parent instanceof TransformFactory.Instance) {
            // passed value from the outside
            v = (Value)
               ((TransformFactory.Instance)parent).globalParams.get(expName);
         }
         else {
            // passed value from another template via stx:with-param
            v = (Value)context.passedParameters.get(expName);
         }
         if (v == null) {
            // no parameter passed
            if (required) {
               context.errorHandler.error(
                  "Missing value for required parameter `" + varName + "'",
                  publicId, systemId, lineNo, colNo);
               return PR_CONTINUE; // if the errorHandler returns
            }
            else if (select != null) {
               // select attribute present
               v = select.evaluate(context, this);
            }
            else { 
               // use contents
               next = contents;
               super.process(context);
               context.emitter.pushEmitter(
                  new StringEmitter(new StringBuffer(),
                                    "(`" + qName + "' started in line " +
                                    lineNo + ")"));
               return PR_CONTINUE;
            }
         }
         processParam(v, context);
         if (nodeEnd != null) {
            // skip contents, the parameter value is already available
            next = successor;
         }
         return PR_CONTINUE;
      }


      public short processEnd(Context context)
         throws SAXException
      {
         processParam(new Value(((StringEmitter)context.emitter.popEmitter())
                                                       .getBuffer()
                                                       .toString()),
                      context);
         return super.processEnd(context);
      }


      /** Declare a parameter */
      public void processParam(Value v, Context context)
         throws SAXException
      {
         // determine scope
         Hashtable varTable;
         if (parent instanceof TransformFactory.Instance) // global para
            varTable = (Hashtable)((GroupBase)parent).groupVars.peek();
         else
            varTable = context.localVars;

         if (varTable.get(expName) != null) {
            context.errorHandler.error(
               "Param `" + varName + "' already declared",
               publicId, systemId, lineNo, colNo);
            return; // if the errorHandler returns
         }

         varTable.put(expName, v);

         if (varTable == context.localVars)
            parent.declareVariable(expName);
      }
   }
}
