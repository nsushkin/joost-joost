/*
 * $Id: PIFactory.java,v 2.1 2003/04/30 15:08:16 obecker Exp $
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

import java.util.Hashtable;
import java.util.HashSet;

import net.sf.joost.emitter.StringEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.Value;


/** 
 * Factory for <code>processing-instruction</code> elements, which are 
 * represented by the inner Instance class. 
 * @version $Revision: 2.1 $ $Date: 2003/04/30 15:08:16 $
 * @author Oliver Becker
 */

final public class PIFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public PIFactory()
   {
      attrNames = new HashSet();
      attrNames.add("name");
   }

   /* @return <code>"processing-instruction"</code> */
   public String getName()
   {
      return "processing-instruction";
   }

   public NodeBase createNode(NodeBase parent, String uri, String lName, 
                              String qName, Attributes attrs, 
                              Hashtable nsSet, Locator locator)
      throws SAXParseException
   {
      String nameAtt = getAttribute(qName, attrs, "name", locator);
      Tree nameAVT = parseAVT(nameAtt, nsSet, parent, locator);

      checkAttributes(qName, attrs, attrNames, locator);

      return new Instance(qName, parent, locator, nameAVT);
   }


   /** 
    * Represents an instance of the <code>processing-instruction</code> 
    * element.
    */
   final public class Instance extends NodeBase
   {
      private Tree name;
      private StringEmitter strEmitter;
      private StringBuffer buffer;
      private String piName;

      protected Instance(String qName, NodeBase parent, Locator locator, 
                         Tree name)
      {
         super(qName, parent, locator, true);
         this.name = name;
         buffer = new StringBuffer();
         strEmitter = new StringEmitter(buffer, 
                                        "(`" + qName + "' started in line " +
                                        locator.getLineNumber() + ")");
      }


      /**
       * Activate a StringEmitter for collecting the data of the new PI
       */
      public short process(Context context)
         throws SAXException
      {
         super.process(context);
         // check for nesting of this stx:processing-instruction
         if (context.emitter.isEmitterActive(strEmitter)) {
            context.errorHandler.error(
               "Can't create nested processing instruction here",
               publicId, systemId, lineNo, colNo);
            return PR_CONTINUE; // if the errorHandler returns
         }
         buffer.setLength(0);
         context.emitter.pushEmitter(strEmitter);

         piName = name.evaluate(context, this).string;

         // TO DO: is this piName valid?
         return PR_CONTINUE;
      }


      /**
       * Emits a processing-instruction to the result stream
       */
      public short processEnd(Context context)
         throws SAXException
      {
         context.emitter.popEmitter();
         int index = buffer.length();
         if (index != 0) {
            // are there any "?>" in the pi data?
            String str = buffer.toString();
            while ((index = str.lastIndexOf("?>", --index)) != -1) 
               buffer.insert(index+1, ' ');
         }
         context.emitter.processingInstruction(piName, buffer.toString(),
                                               publicId, systemId, 
                                               lineNo, colNo);
         return super.processEnd(context);
      }
   }
}
