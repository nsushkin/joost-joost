/*
 * $Id: ResultDocumentFactory.java,v 2.16 2004/11/06 13:03:05 obecker Exp $
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

import java.io.Writer;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import net.sf.joost.emitter.StreamEmitter;
import net.sf.joost.grammar.Tree;
import net.sf.joost.stx.Context;
import net.sf.joost.stx.ParseContext;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** 
 * Factory for <code>result-document</code> elements, which are represented by
 * the inner Instance class. 
 * @version $Revision: 2.16 $ $Date: 2004/11/06 13:03:05 $
 * @author Oliver Becker
 */

final public class ResultDocumentFactory extends FactoryBase
{
   /** allowed attributes for this element */
   private HashSet attrNames;

   // Constructor
   public ResultDocumentFactory()
   {
      attrNames = new HashSet();
      attrNames.add("href");
      attrNames.add("output-encoding");
      attrNames.add("output-method");
   }


   /** @return <code>"result-document"</code> */
   public String getName()
   {
      return "result-document";
   }

   public NodeBase createNode(NodeBase parent, String qName, 
                              Attributes attrs, ParseContext context)
      throws SAXParseException
   {
      Tree href = parseAVT(getAttribute(qName, attrs, "href", context), 
                           context);

      String encodingAtt = attrs.getValue("output-encoding");

      String methodAtt = attrs.getValue("output-method");
      if (methodAtt != null) {
         if (methodAtt.indexOf(':') != -1)
            methodAtt = getExpandedName(methodAtt, context);
         else if (!methodAtt.equals("text") && !methodAtt.equals("xml"))
            throw new SAXParseException(
               "Value of attribute `output-method' must be `xml', `text', " + 
               "or a qualified name. Found `" + methodAtt + "'",
               context.locator);
      }

      checkAttributes(qName, attrs, attrNames, context);
      return new Instance(qName, parent, context, href, encodingAtt, 
                          methodAtt);
   }


   /** Represents an instance of the <code>result-document</code> element. */
   final public class Instance extends NodeBase
   {
      private Tree href;
      private String encoding, method;

      protected Instance(String qName, NodeBase parent, ParseContext context,
                         Tree href, String encoding, String method)
      {
         super(qName, parent, context, true);
         this.href = href;
         this.encoding = encoding;
         this.method = method;
      }
      

      /**
       * Redirects the result stream to the specified URI
       */
      public short process(Context context)
         throws SAXException
      {
         super.process(context);
         if (encoding == null) // no encoding attribute specified
            // use global encoding att
            encoding = context.currentProcessor.getOutputEncoding();

         String filename = href.evaluate(context, this).getString();
            
         StreamEmitter se = null;
         try {
            // TO DO: introduce OutputURIResolver and request
            // a Result object
            Writer osw = context.emitter.getResultWriter(
                            filename, encoding, 
                            publicId, systemId, lineNo, colNo);

            Properties props = (Properties)context.currentProcessor
                                                  .outputProperties.clone();
            props.setProperty(OutputKeys.ENCODING, encoding);
            if (method != null)
               props.setProperty(OutputKeys.METHOD, method);
            se = StreamEmitter.newEmitter(osw, encoding, props);
            localFieldStack.push(osw);
         }
         catch (java.io.IOException ex) {
            context.errorHandler.error(ex.toString(), 
                                       publicId, systemId, lineNo, colNo);
            return PR_CONTINUE; // if the errorHandler returns
         }

         context.pushEmitter(se);
         context.emitter.startDocument();
         return PR_CONTINUE;
      }


      /** Close the current result stream */
      public short processEnd(Context context)
         throws SAXException
      {
         context.emitter.endDocument(nodeEnd);
         context.popEmitter();
         try {
            ((Writer)localFieldStack.pop()).close();
         }
         catch (java.io.IOException ex) {
            context.errorHandler.error(ex.toString(), 
                                       publicId, systemId, 
                                       nodeEnd.lineNo, nodeEnd.colNo);
            return PR_CONTINUE; // if the errorHandler returns
         }

         return super.processEnd(context);
      }
   }
}
