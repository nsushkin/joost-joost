/*
 * $Id: LitElementFactory.java,v 2.4 2003/08/31 19:38:52 obecker Exp $
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
import org.xml.sax.helpers.AttributesImpl;

import java.util.Enumeration;
import java.util.Hashtable;

import net.sf.joost.Constants;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;
import net.sf.joost.grammar.Tree;


/** 
 * Factory for literal result elements, which are represented by the
 * inner Instance class. 
 * @version $Revision: 2.4 $ $Date: 2003/08/31 19:38:52 $
 * @author Oliver Becker
*/

final public class LitElementFactory
{
   public NodeBase createNode(NodeBase parent, String uri, String lName, 
                              String qName, Attributes attrs, 
                              ParseContext context)
      throws SAXParseException
   {
      if (parent == null) {
         if (lName.equals("transform"))
            throw new SAXParseException(
               "File is not an STX transformation sheet, need namespace `" +
               Constants.STX_NS + "' for the `transform' element",
               context.locator);
         else
            throw new SAXParseException(
               "File is not an STX transformation sheet, found " + qName, 
               context.locator);
      }

      if (parent instanceof TransformFactory.Instance)
         throw new SAXParseException("Literal result element `" + qName + 
                                     "' may occur only within templates",
                                     context.locator);

      Tree[] avtList = new Tree[attrs.getLength()];
      for (int i=0; i<avtList.length; i++) 
         avtList[i] = FactoryBase.parseAVT(attrs.getValue(i), context);

      return new Instance(uri, lName, qName, attrs, avtList, parent, context);
   }


   /** Represents a literal result element. */

   final public class Instance extends NodeBase
   {
      private String uri;
      private String lName;
      private AttributesImpl attrs;
      private Tree[] avtList;
      private Hashtable namespaces;
      private Hashtable namespaceAliases;
      
      protected Instance(String uri, String lName, String qName,
                         Attributes attrs, Tree[] avtList, 
                         NodeBase parent, ParseContext context)
      {
         super(qName, parent, context, true);
         this.uri = uri;
         this.lName = lName;
         this.attrs = new AttributesImpl(attrs);
         this.avtList = avtList;

         // store namespaces without those from exclude-result-prefixes
         namespaces = (Hashtable)context.nsSet.clone();
         for (Enumeration keys = namespaces.keys();
              keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            if (context.transformNode.excludedNamespaces
                                     .contains(namespaces.get(key)))
               namespaces.remove(key);
         }

         this.namespaceAliases = context.transformNode.namespaceAliases;
      }
      

      /**
       * Apply all declared namespaces aliases 
       * (<code>stx:namespace-alias</code>)
       */
      public boolean compile(int pass)
         throws SAXException
      {
         if (pass == 0)
            // have to wait until the whole STX sheet has been parsed
            return true;

         // Change namespace URI of this element
         String toNS = (String)namespaceAliases.get(uri);
         if (toNS != null)
            uri = toNS;

         // Change namespace URI of the attributes
         int aLen = attrs.getLength();
         for (int i=0; i<aLen; i++) {
            String aURI = attrs.getURI(i);
            toNS = (String)namespaceAliases.get(aURI);
            if (toNS != null)
               attrs.setURI(i, toNS);
         }

         // Change namespace URIs of in-scope namespaces
         for (Enumeration keys = namespaces.keys();
              keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = namespaces.get(key);
            Object alias = namespaceAliases.get(value);
            if(alias != null)
               namespaces.put(key,alias);
         }

         return false;
      }


      /**
       * Emits the start tag of this literal element to the emitter
       */
      public short process(Context context)
         throws SAXException
      {
         super.process(context);
         for (int i=0; i<avtList.length; i++)
            attrs.setValue(i, avtList[i].evaluate(context, this).string);
         context.emitter.startElement(uri, lName, qName, attrs, namespaces,
                                      publicId, systemId, lineNo, colNo);
         return PR_CONTINUE;
      }


      /**
       * Emits the end tag of this literal element to the emitter
       */
      public short processEnd(Context context)
         throws SAXException
      {
         context.emitter.endElement(uri, lName, qName,
                                    publicId, systemId, lineNo, colNo);
         return super.processEnd(context);
      }


      //
      // for debugging
      //
      public String toString()
      {
         return "LitElement <" + qName + ">";
      }
   }
}
