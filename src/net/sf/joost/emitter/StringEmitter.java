/*
 * $Id: StringEmitter.java,v 1.3 2002/11/22 16:31:14 obecker Exp $
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

package net.sf.joost.emitter;

//SAX2
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

//Joost
import net.sf.joost.stx.SAXEvent;


/**
 * This class implements an emitter that collects characters events
 * @version $Revision: 1.3 $ $Date: 2002/11/22 16:31:14 $
 * @author Oliver Becker
 */

final public class StringEmitter implements StxEmitter 
{
   /** the string buffer */
   private StringBuffer buffer;

   /** 
    * additional info for error messages, <code>null</code> means:
    * don't report errors 
    */
   private String errorInfo;


   //
   // Constructor
   //
   public StringEmitter(StringBuffer buffer, String errorInfo)
   {
      this.buffer = buffer;
      this.errorInfo = errorInfo;
   }


   //
   // SAX ContentHandler interface
   //

   /** not used */
   public void setDocumentLocator(Locator locator)
   { }

   /** do nothing */
   public void startDocument()
      throws SAXException
   { }

   /** do nothing */
   public void endDocument()
      throws SAXException
   { }

   /** do nothing */
   public void startPrefixMapping(String prefix, String uri)
      throws SAXException
   { }

   /** do nothing */
   public void endPrefixMapping(String prefix)
      throws SAXException
   { }

   /** not allowed */
   public void startElement(String namespaceURI, String localName,
                            String qName, Attributes atts)
      throws SAXException
   {
      if (errorInfo != null)
         throw new SAXException("Can't create element `" + qName + "' here " +
                                errorInfo);
   }

   /** not allowed */
   public void endElement(String namespaceURI, String localName,
                          String qName)
      throws SAXException
   {
      // no test necessary here, because there must have been be a
      // startElement event before
   }

   /** Add the characters to the internal buffer */
   public void characters(char[] ch, int start, int length)
      throws SAXException
   {
      buffer.append(ch, start, length);
   }

   /** not used */
   public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException
   {
      characters(ch, start, length); // just to be sure ...
   }

   /** not allowed */
   public void processingInstruction(String target, String data)
      throws SAXException
   {
      if (errorInfo != null)
         throw new SAXException("Can't create processing instruction `" + 
                                target + "' here " + errorInfo);
   }

   /** not used */
   public void skippedEntity(String name)
      throws SAXException
   { }


   //
   // SAX LexicalHandler interface
   //

   /** not used */
   public void startDTD(String name, String publicId, String systemId)
      throws SAXException
   { }

   /** not used */
   public void endDTD()
      throws SAXException
   { }

   /** not used */
   public void startEntity(String name)
      throws SAXException
   { }

   /** not used */
   public void endEntity(String name)
      throws SAXException
   { }

   /** ignored */
   public void startCDATA()
      throws SAXException
   { }

   /** ignored */
   public void endCDATA()
      throws SAXException
   { }

   /** not allowed */
   public void comment(char[] ch, int start, int length)
      throws SAXException
   {
      if (errorInfo != null)
         throw new SAXException("Can't create comment here " + errorInfo);
   }
}