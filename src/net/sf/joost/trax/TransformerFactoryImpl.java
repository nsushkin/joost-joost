/*
 * $Id: TransformerFactoryImpl.java,v 1.12 2003/09/03 15:07:03 obecker Exp $
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
 * Contributor(s): Oliver Becker.
 */


package net.sf.joost.trax;

import net.sf.joost.TransformerHandlerResolver;
import net.sf.joost.stx.Processor;

//JAXP
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;


/**
 * This class implements the TransformerFactory-Interface for TraX.
 * With the help of this factory you can get a templates-object or
 * directly a transformer-object for the transformation process. If you
 * use a SAXResult you can simply downcast to SAXTransformerFactory
 * and use it like a Sax-Parser.
 * @author Zubow
 */
public class TransformerFactoryImpl extends SAXTransformerFactory
    implements TrAXConstants{

    // Define a static logger variable so that it references the
    // Logger instance named "TransformerFactoryImpl".
    private static org.apache.commons.logging.Log log =
        org.apache.commons.logging.
        LogFactory.getLog(TransformerFactoryImpl.class);

    // Member
    private   URIResolver uriResolver               = null;
    private   ErrorListener errorListener           = null;
    protected TransformerHandlerResolver thResolver = null;

    // init default errorlistener
    // visible for TemplatesImpl
    protected ConfigurationErrListener defaultErrorListener =
            new ConfigurationErrListener();

    // indicates if the transformer is working in debug mode
    private boolean debugmode                       = false;

    // Synch object to guard against setting values from the TrAX interface
    // or reentry while the transform is going on.
    private Boolean reentryGuard = new Boolean(true);

    /**
     * The default constructor.
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

        defaultErrorListener.fatalError(tE);
        return null;
    }


    /**
     * Allows the user to retrieve specific attributes of the underlying
     * implementation.
     * @param name The attribute name.
     * @return An object according to the attribute-name
     * @throws IllegalArgumentException When such a attribute does not exists.
     */
    public Object getAttribute(String name)
        throws IllegalArgumentException {

        if (name.equals(KEY_TH_RESOLVER)) {
            return thResolver;
        } else if (name.equals(DEBUG_FEATURE)) {
            return new Boolean(debugmode);
        } else {
            log.warn("Feature not supported: " + name);
            throw new IllegalArgumentException("Feature not supported: " + name);
        }
    }

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation. An attribute in this context is defined to
     * be an option that the implementation provides.
     * @param name Name of the attribute (key)
     * @param value Value of the attribute.
     * @throws IllegalArgumentException
     */
    public void setAttribute(String name, Object value)
        throws IllegalArgumentException {

        if (name.equals(KEY_TH_RESOLVER)) {
            thResolver = (TransformerHandlerResolver)value;
        } else if (name.equals(DEBUG_FEATURE)) {
            this.debugmode = ((Boolean)value).booleanValue();
        } else {
            log.warn("Feature not supported: " + name);
            throw new IllegalArgumentException("Feature not supported: " + name);
        }
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
            defaultErrorListener.setUserErrorListener(errorListener);
        }
    }

    /**
     * Getter for {@link #uriResolver}
     * @return The registered <code>URIResolver</code>
     */
    public URIResolver getURIResolver() {
        return uriResolver;
    }

    /**
     * Setter for {@link #uriResolver}
     * @param resolver The <code>URIResolver</code> object.
     */
    public void setURIResolver(URIResolver resolver) {

        synchronized (reentryGuard) {
            this.uriResolver = resolver;
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

        String errMsg = "Unknown feature " + name;
        TransformerConfigurationException tE =
                new TransformerConfigurationException(errMsg);

        try {
            defaultErrorListener.error(tE);
            return false;
        } catch (TransformerException e) {
            throw new IllegalArgumentException(errMsg);
        }
    }


    /**
     * Creates a new Templates for Transformations.
     * @param source The <code>Source</code> of the stylesheet.
     * @return A <code>Templates</code> object or <code>null</code> when an error
     *  occured (no user defined ErrorListener)
     * @throws TransformerConfigurationException
     */
    public Templates newTemplates(Source source)
        throws TransformerConfigurationException {

        synchronized (reentryGuard) {
            if (log.isDebugEnabled())
                log.debug("get a Templates-instance from Source " +
                          source.getSystemId());
            try {
                SAXSource saxSource = TrAXHelper.getSAXSource(source, errorListener);
                Templates template = 
                    new TemplatesImpl(saxSource.getXMLReader(),
                                      saxSource.getInputSource(), this);
                return template;
            } catch (TransformerException tE) {
                defaultErrorListener.fatalError(tE);
                return null;
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
            return(transformer);
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
            log.debug("create a TemplatesHandler-instance");
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
            if (log.isDebugEnabled())
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
     * @return An {@link XMLFilter} object, or <code>null</code> if this feature is not
     *  supported.
     * @throws TransformerConfigurationException
     */
    public XMLFilter newXMLFilter(Source src)
        throws TransformerConfigurationException {

        if (log.isDebugEnabled())
            log.debug("getting SAXTransformerFactory.FEATURE_XMLFILTER " +
                      "from Source " + src.getSystemId());
        XMLFilter xFilter = null;
        try {
            Templates templates = newTemplates(src);
            //get a XMLReader
            XMLReader parser = Processor.getXMLReader();
            xFilter = newXMLFilter(templates);
            xFilter.setParent(parser);
            return xFilter;
        } catch (SAXException ex) {
            TransformerConfigurationException tE =
                    new TransformerConfigurationException(ex.getMessage(), ex);
            defaultErrorListener.fatalError(tE);
            return null;
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
            defaultErrorListener.fatalError(tE);
            return null;
    	}
    }
}
