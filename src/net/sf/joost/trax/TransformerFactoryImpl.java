/*
 * $Id: TransformerFactoryImpl.java,v 1.3 2002/10/15 19:02:13 zubow Exp $
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
 * The Initial Developer of the Original Code is Anatolij Zubow.
 *
 * Portions created by  ______________________
 * are Copyright (C) ______ _______________________.
 * All Rights Reserved.
 *
 * Contributor(s): ______________________________________.
 */


package net.sf.joost.trax;

//JAXP
import net.sf.joost.stx.Processor;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.Hashtable;


/**
 * This class implements the TransformerFactory-Interface for TraX.
 * With the help of this factory you can get a templates-object or
 * directly a tranformer-object for transformation process. If you
 * use a SAXResult you can simply downcast to SAXTransformerFactory
 * and use it like a Sax-Parser.
 * @author Zubow
 */
public class TransformerFactoryImpl extends SAXTransformerFactory
    implements TrAXConstants{

    // Define a static logger variable so that it references the
    // Logger instance named "TransformerFactoryImpl".
    static Logger log = Logger.getLogger(TransformerFactoryImpl.class);

    //Member
    private URIResolver resolver        = null;
    private ErrorListener errorListener = null;

    //Hashtable for storing attributes
    private Hashtable attHash           = new Hashtable();

    // Synch object to gaurd against setting values from the TrAX interface
    // or reentry while the transform is going on.
    private Boolean reentryGuard = new Boolean(true);

    /**
     * defaultconstructor
     */
    public TransformerFactoryImpl() {}


    //*************************************************************************
    // IMPLEMENTATION OF TransformerFactory
    //*************************************************************************

    /**
     * Returns the <code>Source</code> of the stylesheet associated with
     *  the xml-document.
     * Feature is not supported.
     * @param source The <code>Source</code> of the xml-document.
     * @param media Matching media-type.
     * @param title Matching title-type.
     * @param charset Matching charset-type.
     * @return A <code>Source</code> of the stylesheet.
     * @throws TransformerConfigurationException
     */
    public Source getAssociatedStylesheet(Source source, String media,
                                            String title, String charset)
        throws TransformerConfigurationException {

        TransformerConfigurationException tE =
                new TransformerConfigurationException("Feature not supported");
        // user ErrorListener if available
        if(errorListener != null) {
            try {
                errorListener.fatalError(tE);
                return null;
            } catch( TransformerException e2) {
                return null;
            }
        } else {
            // Feature is not supported by Joost.
            throw tE;
        }
    }


    /**
     * Allows the user to retrieve specific attributes of the underlying
     * implementation.
     * Feature not supported.
     * @param name The attribute name.
     * @return An object according to the attribute-name
     * @throws IllegalArgumentException When such a attribute does not exists.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException {

        throw new IllegalArgumentException("Feature not supported");
    }

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation. An attribute in this context is defined to
     * be an option that the implementation provides.
     * Feature not supported.
     * @param name Name of the attribute (key)
     * @param value Value of the attribute.
     * @throws IllegalArgumentException
     */
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException {

        throw new IllegalArgumentException("Feature not supported");
    }

    /**
     * Getter for {@link #errorListener}
     * @return The registered <code>ErrorListener</code>
     */
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * Setter for {@link #errorListener}
     * @param errorListener The <code>ErrorListener</code> object.
     * @throws IllegalArgumentException
     */
    public void setErrorListener(ErrorListener errorListener)
        throws IllegalArgumentException {

        synchronized (reentryGuard) {
            log.debug("setting ErrorListener");
            if (errorListener == null) {
                throw new IllegalArgumentException("ErrorListener is null");
            }
            this.errorListener = errorListener;
        }
    }

    /**
     * Getter for {@link #resolver}
     * @return The registered <code>URIResolver</code>
     */
    public URIResolver getURIResolver() {
        return resolver;
    }

    /**
     * Setter for {@link #resolver}
     * @param resolver The <code>URIResolver</code> object.
     */
    public void setURIResolver(URIResolver resolver) {

        synchronized (reentryGuard) {
            this.resolver = resolver;
        }
    }

    /**
     * Supplied features.
     * @param name Name of the feature.
     * @return true if feature is supported.
     */
    public boolean getFeature(String name) {

    	if (name.equals(SAXSource.FEATURE)) {
            return true;
        }
    	if (name.equals(SAXResult.FEATURE)) {
            return true;
        }
    	if (name.equals(DOMSource.FEATURE)) {
            return true;
        }
    	if (name.equals(DOMResult.FEATURE)) {
            return true;
        }
    	if (name.equals(StreamSource.FEATURE)) {
            return true;
        }
    	if (name.equals(StreamResult.FEATURE)) {
            return true;
        }
        if (name.equals(SAXTransformerFactory.FEATURE)) {
            return true;
        }
        if (name.equals(SAXTransformerFactory.FEATURE_XMLFILTER)) {
            return true;
        }
        // user ErrorListener if available
        if(errorListener != null) {
            try {
                errorListener.error(new TransformerConfigurationException("Unknown feature " + name));
                return false;
            } catch( TransformerException e2) {
                return false;
            }
        } else {
            throw new IllegalArgumentException("Unknown feature " + name);
        }
    }


    /**
     * Creates a new Templates for Transformations.
     * @param source The <code>Source</code> of the stylesheet.
     * @return A <code>Templates</code> object or <code>null</code>
     * @throws TransformerConfigurationException
     */
    public Templates newTemplates(Source source)
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.debug("get a Templates-instance from Source " +
                source.getSystemId());
            try {
                SAXSource saxSource = getSAXSource(source, true);
                InputSource isource = saxSource.getInputSource();
                Templates template = new TemplatesImpl(isource, this);
                return template;
            } catch (TransformerConfigurationException tE) {
                // user ErrorListener if available
                if(errorListener != null) {
                    try {
                        errorListener.fatalError(new TransformerConfigurationException(tE));
                        return null;
                    } catch( TransformerException e2) {
                        new TransformerConfigurationException(e2);
                        return null;
                    }
                } else {
                    log.fatal(tE);
                    throw tE;
                }
            }
        }
    }


    /**
     * Creates a new Transformer object that performs a copy of the source to
     * the result.
     * @return A <code>Transformer</code> object for an identical
     *  transformation.
     * @throws TransformerConfigurationException
     */
    public Transformer newTransformer()
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            StreamSource streamSrc =
                    new StreamSource(new StringReader(IDENTITY_TRANSFORM));
            return newTransformer(streamSrc);
        }
    }


    /**
     * Gets a new Transformer object for transformation.
     * @param source The <code>Source</code> of the stylesheet.
     * @return A <code>Transformer</code> object according to the
     *  <code>Templates</code> object.
     * @throws TransformerConfigurationException
     */
    public Transformer newTransformer(Source source)
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.debug("get a Transformer-instance");
            Templates templates     = newTemplates(source);
            Transformer transformer = templates.newTransformer();
            //set the URI-Resolver
            if (resolver != null) {
                transformer.setURIResolver(resolver);
            }
            return(transformer);
        }
    }


    //*************************************************************************
    // Helper methods
    //*************************************************************************

    /**
    * Converts a supplied Source to a SAXSource, DOMSource or StreamSource.
    * @param source The supplied input source
    * @param isStyleSheet true if the source is a stylesheet
    * @return A <code>SAXSource</code> object.
    */
    private SAXSource getSAXSource(Source source, boolean isStyleSheet)
        throws TransformerConfigurationException{

        log.debug("Convert a supplied Source to a SAXSource, DOMSource " +
            "or StreamSource");
        if (source instanceof SAXSource) {
            log.debug("Source is a SAXSource");
            return (SAXSource)source;
        }
        if (source instanceof DOMSource) {
            log.debug("Source is a DOMSource, so using DOMDriver to " +
                "emulate a SAXSource");
            InputSource is = new InputSource("dummy");
            Node startNode = ((DOMSource)source).getNode();
            Document doc;

            if (startNode instanceof Document) {
                doc = (Document)startNode;
            } else {
                doc = startNode.getOwnerDocument();
            }
            DOMDriver driver = new DOMDriver();
            driver.setDocument(doc);
            is.setSystemId(source.getSystemId());
            driver.setSystemId(source.getSystemId());
            return new SAXSource(driver, is);
        }
        if (source instanceof StreamSource) {

            log.debug("Source is a StreamSource");
            InputSource isource =
                TrAXHelper.getInputSourceForStreamSources(source, errorListener);
            return new SAXSource(isource);
        } else {

            IllegalArgumentException iE =
                    new IllegalArgumentException("Unknown type of source");
            // user ErrorListener if available
            if(errorListener != null) {
                try {
                    errorListener.fatalError(new TransformerConfigurationException(iE.getMessage(), iE));
                    return null;
                } catch( TransformerException e2) {
                    return null;
                }
            } else {
                log.error("Unknown type of source");
                throw iE;
            }
        }
    }


    //*************************************************************************
    // IMPLEMENTATION OF SAXTransformerFactory
    //*************************************************************************

    /**
     * Gets a <code>TemplatesHandler</code> object that can process
     * SAX ContentHandler events into a <code>Templates</code> object.
     * Implementation of the {@link SAXTransformerFactory}
     * @see SAXTransformerFactory
     * @return {@link TemplatesHandler} ready to parse a stylesheet.
     * @throws TransformerConfigurationException
     */
    public TemplatesHandler newTemplatesHandler()
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.info("create a TemplatesHandler-instance");
            TemplatesHandlerImpl thandler = new TemplatesHandlerImpl(this);
            return thandler;
        }
    }


    /**
     * Gets a <code>TransformerHandler</code> object that can process
     * SAX ContentHandler events into a Result.
     * The transformation is defined as an identity (or copy) transformation,
     * for example to copy a series of SAX parse events into a DOM tree.
     * Implementation of the {@link SAXTransformerFactory}
     * @return {@link TransformerHandler} ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    public TransformerHandler newTransformerHandler()
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.debug("get a TransformerHandler (identity transformation " +
                "or copy)");
            StreamSource streamSrc =
                new StreamSource(new StringReader(IDENTITY_TRANSFORM));
            return newTransformerHandler(streamSrc);
        }
    }


    /**
     * Gets a <code>TransformerHandler</code> object that can process
     * SAX ContentHandler events into a Result, based on the transformation
     * instructions specified by the argument.
     * Implementation of the {@link SAXTransformerFactory}
     * @param src The Source of the transformation instructions
     * @return {@link TransformerHandler} ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    public TransformerHandler newTransformerHandler(Source src)
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.debug("get a TransformerHandler-instance from Source " +
                src.getSystemId());
            Templates templates = newTemplates(src);
            return newTransformerHandler(templates);
        }
    }


    /**
     * Gets a <code>TransformerHandler</code> object that can process
     * SAX ContentHandler events into a Result, based on the Templates argument.
     * Implementation of the {@link SAXTransformerFactory}
     * @param templates - The compiled transformation instructions.
     * @return {@link TransformerHandler} ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    public TransformerHandler newTransformerHandler(Templates templates)
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            log.debug("get a TransformerHandler-instance from Templates ");
            Transformer internal = templates.newTransformer();
            TransformerHandlerImpl thandler = new TransformerHandlerImpl(internal);
            return thandler;
        }
    }


    /**
     * Creates an <code>XMLFilter</code> that uses the given <code>Source</code>
     * as the transformation instructions.
     * Implementation of the {@link SAXTransformerFactory}
     * @param src - The Source of the transformation instructions.
     * @return An {@link XMLFilter} object, or null if this feature is not
     *  supported.
     * @throws TransformerConfigurationException
     */
    public XMLFilter newXMLFilter(Source src)
        throws TransformerConfigurationException {

        log.debug("getting SAXTransformerFactory.FEATURE_XMLFILTER from Source "
            + src.getSystemId());
        XMLFilter xFilter = null;
        try {
            Templates templates = newTemplates(src);
            //get a XMLReader
            XMLReader parser = Processor.getXMLReader();
            xFilter = newXMLFilter(templates);
            xFilter.setParent(parser);
            return xFilter;
        } catch (SAXException ex) {

            // use ErrorListener if available
            if(errorListener != null) {
                try {
                    errorListener.fatalError(new TransformerException(ex));
                    return null;
                } catch( TransformerException e2) {
                    new TransformerConfigurationException(e2);
                    return null;
                }
            } else {
                log.fatal(ex);
                throw new TransformerConfigurationException(ex);
            }
        }
    }


    /**
     * Creates an XMLFilter, based on the Templates argument.
     * Implementation of the {@link SAXTransformerFactory}
     * @param templates - The compiled transformation instructions.
     * @return An {@link XMLFilter} object, or null if this feature is not
     *  supported.
     * @throws TransformerConfigurationException
     */
    public XMLFilter newXMLFilter(Templates templates)
        throws TransformerConfigurationException {

        log.debug("getting SAXTransformerFactory.FEATURE_XMLFILTER " +
            "from Templates");
        try {
            //Implementation
            return new TrAXFilter(templates);
        } catch(TransformerConfigurationException tE) {
            if(errorListener != null) {
                try {
                    errorListener.fatalError(tE);
                    return null;
                } catch( TransformerException e2) {
                    new TransformerConfigurationException(e2);
                    return null;
                }
            } else {
                log.fatal(tE);
                throw tE;
            }
    	}
    }
}
