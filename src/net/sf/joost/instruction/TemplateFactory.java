/*
 * $Id: TemplateFactory.java,v 1.1 2002/08/27 09:40:51 obecker Exp $
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

import java.io.StringReader;
import java.util.Stack;
import java.util.Hashtable;
import java.util.HashSet;

import net.sf.joost.grammar.Yylex;
import net.sf.joost.grammar.PatternParser;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Emitter;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.SAXEvent;


/**
 * Factory for <code>template</code> elements, which are represented by
 * the inner Instance class.
 * @version $Revision: 1.1 $ $Date: 2002/08/27 09:40:51 $
 * @author Oliver Becker
 */

public final class TemplateFactory extends FactoryBase
{
   /** The local element name. */
   private static final String name = "template";

   /** allowed attributes for this element. */
   private HashSet attrNames;

   /** Visibility values */
   public static final int
      PRIVATE_VISIBLE = 0,
      PUBLIC_VISIBLE = 1,
      GLOBAL_VISIBLE = 2;

   // Log4J initialization
   private static org.apache.log4j.Logger log4j = 
      org.apache.log4j.Logger.getLogger(TemplateFactory.class);


   // Constructor
   public TemplateFactory()
   {
      attrNames = new HashSet();
      attrNames.add("match");
      attrNames.add("priority");
      attrNames.add("visibility");
      attrNames.add("recursion-entry-point");
   }

   public String getName()
   {
      return name;
   }

   public NodeBase createNode(NodeBase parent, String uri, String lName, 
                              String qName, Attributes attrs, 
                              Hashtable nsSet, Locator locator)
      throws SAXParseException
   {
      if (parent == null || !(parent instanceof GroupBase))
         throw new SAXParseException("`" + qName + "' must be a top level " +
                                     "element or a child of stx:group",
                                     locator);

      GroupBase parentGroup = (GroupBase)parent;

      String matchAtt = getAttribute(qName, attrs, "match", locator);

      // parse the attribute
      StringReader sr = new StringReader(matchAtt);
      Yylex lexer = new Yylex(sr);
      PatternParser parser = new PatternParser(lexer, nsSet, locator);
      Tree matchPattern;
      try {
         matchPattern = (Tree)parser.parse().value;
      }
      catch (SAXParseException e) {
         throw e;
      }
      catch (Exception e) {
         throw new SAXParseException(e.getMessage() + 
                                     "Found `" + lexer.last.value + "'",
                                     locator);
      }

      String priorityAtt = attrs.getValue("priority");
      double priority;
      if (priorityAtt != null) {
         try {
            priority = Double.parseDouble(priorityAtt);
         }
         catch (NumberFormatException ex) {
            throw new SAXParseException("The priority value `" + 
                                        priorityAtt + "' is not a number",
                                        locator);
         }
      }
      else {
         priority = computePriority(matchPattern);
      }

      int visibility = PRIVATE_VISIBLE; // default value
      String visibilityAtt = attrs.getValue("visibility");
      if (visibilityAtt != null) {
         if ("public".equals(visibilityAtt))
            visibility = PUBLIC_VISIBLE;
         else if ("global".equals(visibilityAtt))
            visibility = GLOBAL_VISIBLE;
         else if ("private".equals(visibilityAtt))
            visibility = PRIVATE_VISIBLE;
         else 
            throw new SAXParseException("Value of attribute `visibility' " +
                                        "must be one of `private', " + 
                                        "`public', or `global' (found `" + 
                                        visibilityAtt + "')",
                                        locator);
      }

      String recursionEntryAtt = attrs.getValue("recursion-entry-point");
      boolean recursionEntry = false;
      if (recursionEntryAtt != null) {
         if ("yes".equals(recursionEntryAtt))
            recursionEntry = true;
         else if ("no".equals(recursionEntryAtt))
            recursionEntry = false;
         else
            throw new SAXParseException("Value of attribute " + 
                                        "`recursion-entry-point' must " +
                                        "be either `yes' or `no' (found `"+ 
                                        recursionEntryAtt + "')", locator);
      }

      checkAttributes(qName, attrs, attrNames, locator);

      return new Instance(qName, locator, parentGroup,
                          matchPattern, priority, visibility, recursionEntry);
   }


   /**
    * Computes the default priority of a match pattern.
    * @param match the pattern
    * @return the priority as a double value
    */
   private static double computePriority(Tree match)
   {
      if (match.type == Tree.UNION)
         // return NaN, priorities must be computed in the function split
         return Double.NaN; 

      if (match.type == Tree.NAME_TEST ||
              (match.type == Tree.PI_TEST && match.value != ""))
         return 0;
      else if (match.type == Tree.URI_WILDCARD ||
               match.type == Tree.LOCAL_WILDCARD)
         return -0.25;
      else if (match.type == Tree.WILDCARD ||
               match.type == Tree.PI_TEST ||
               match.type == Tree.COMMENT_TEST ||
               match.type == Tree.TEXT_TEST ||
               match.type == Tree.NODE_TEST)
         return -0.5;
      else
         return 0.5;
   }


   // -----------------------------------------------------------------------


   /** The inner Instance class */
   public final class Instance 
      extends NodeBase 
      implements Cloneable, Comparable
   {
      /** The match pattern */
      private Tree match;

      /** The priority of this template */
      private double priority;

      /** The visibility of this template */
      public int visibility;

      /** Is this template a recursion entry point? */
      private boolean recursionEntryPoint;

      /** The parent of this template */
      public GroupBase parent;

      /** stack for local variables */
      private Stack localVarStack = new Stack();


      //
      // Constructor
      //
      
      protected Instance(String qName, Locator locator, GroupBase parent,
                         Tree match, double priority, int visibility,
                         boolean recursionEntryPoint)
         throws SAXParseException
      {
         super(qName, locator, false);
         this.parent = parent;
         this.match = match;
         this.priority = priority;
         this.visibility = visibility;
         this.recursionEntryPoint = recursionEntryPoint;
      }


      /** 
       * @return true if the current event stack matches the pattern of
       *         this template
       * @exception SAXParseException if an error occured while evaluating
       * the match expression
       */
      public boolean matches(Context context, Stack eventStack)
         throws SAXException
      {
         context.stylesheetNode = this;
         return match.matches(context, eventStack, eventStack.size());
      }
      

      /**
       * Splits a match pattern that is a union into several template
       * instances. The match pattern of the object itself loses one
       * union.
       * @return a template Instance object without a union in its
       *         match pattern or <code>null</code>
       */
      public Instance split()
         throws SAXException
      {
         if (match.type != Tree.UNION)
            return null;

         Instance copy = null;
         try {
            copy = (Instance)clone();
         }
         catch (CloneNotSupportedException e) {
            log4j.fatal("Can't split " + this);
            throw new SAXException(e);
         }
         copy.match = match.right; // non-union
         if (Double.isNaN(copy.priority)) // no priority specified
            copy.priority = computePriority(copy.match);
         match = match.left;       // may contain another union
         if (Double.isNaN(priority)) // no priority specified
            priority = computePriority(match);
         return copy;
      }


      /**
       * @return the priority of this template
       */
      public double getPriority()
      {
         return priority;
      }


      /**
       * Compares two templates according to their inverse priorities.
       * This results in a descending natural order with
       * java.util.Arrays.sort()
       */
      public int compareTo(Object o)
      {
         double p = ((Instance)o).priority;
         return (p < priority) ? -1 : ((p > priority) ? 1 : 0);
      }


      public short process(Emitter emitter, Stack eventStack,
                           Context context, short processStatus)
         throws SAXException
      {
//           log4j.debug(this);
//           log4j.debug("size localVarStack: " + localVarStack.size());

         context.currentGroup = parent;
         if ((processStatus & ST_PROCESSING) != 0) {
            // template entered, remove existing local variables
            context.localVars.clear();
            if (recursionEntryPoint) {
               // initialize group variables
               parent.enterRecursionLevel(emitter, eventStack, context);
            }
         }
         else {
            // template re-entered, restore local variables 
            context.localVars = (Hashtable)localVarStack.pop();
         }

         // do processing (implemented in NodeBase)
         short newStatus = super.process(emitter, eventStack, context, 
                                         processStatus);

         if ((newStatus & ST_PROCESSING) == 0) {
            // processing was interrupted, store local variables
            localVarStack.push(context.localVars.clone());
         }
         else {
            // end of template encountered
            if (recursionEntryPoint)
               parent.exitRecursionLevel();
         }

         return newStatus;
      }



      // for debugging
      public String toString()
      {
         return "template:" + lineNo + " " + match + " " + priority;
      }
   }
}
