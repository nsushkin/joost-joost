/*
 * $Id: StxEmitter.java,v 1.1 2002/08/27 09:40:51 obecker Exp $
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


package net.sf.joost.emitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import net.sf.joost.trax.TrAXConstants;

/**
 * Common interface - All EmitterImplementations have to implement this
 * interface.
 * @author Zubow
 */
public interface StxEmitter
    extends ContentHandler, LexicalHandler, TrAXConstants {
}
