/*
 * $Id: ParserListenerMgr.java,v 1.1 2004/01/23 16:13:40 zubow Exp $
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

package net.sf.joost.trace;

import net.sf.joost.stx.ParserListener;
import net.sf.joost.instruction.AbstractInstruction;
import net.sf.joost.instruction.NodeBase;
import net.sf.joost.trax.TransformerImpl;

import java.util.List;
import java.util.TooManyListenersException;
import java.util.ArrayList;

/**
 * This class implements the {@link ParserListener}-Interface for
 * debug purpose.
 *
 * @author Zubow
 */
public class ParserListenerMgr implements ParserListener {

    private List parserListeners = null;

    /** default constructor */
    public ParserListenerMgr() {}


    /**
     * Check if parserlisteners are available.
     *
     * @return True if there are registered parserlisteners
     */
    public boolean hasParseListeners() {
        return (parserListeners != null);
    }

    /**
     * Add a parserlistener (debugging and profiling).
     * @param newParserListener A parserlistener to be added.
     *
     * @throws TooManyListenersException if there are to many registered listeners
     */
    public void addParseListener(ParserListener newParserListener)
            throws TooManyListenersException {
        // set Joost-Transformer in debug-mode
        // todo think about this ???
        TransformerImpl.DEBUG_MODE = true;
        if (parserListeners == null) {
            parserListeners = new ArrayList();
        }
        // add new parserlistener
        parserListeners.add(newParserListener);
    }

    /**
     * Remove a parserlistener.
     * @param oldParserListener A parserlistener to be removed.
     */
    public void removeParseListener(ParserListener oldParserListener) {
        if (parserListeners != null) {
            // remove the given parserlistener
            parserListeners.remove(oldParserListener);
        }
    }

    // ----------------------------------------------------------------------
    // Callback methods
    // ----------------------------------------------------------------------

    /** see {@link ParserListener#nodeCreated} */
    public void nodeCreated(NodeBase node) {
        if (hasParseListeners()) {
            for (int i = 0; i < parserListeners.size(); i++) {
                ParserListener pl = (ParserListener) parserListeners.get(i);
                pl.nodeCreated(node);
            }
        }
    }

    /** see {@link ParserListener#parseFinished} */
    public void parseFinished() {
        if (hasParseListeners()) {
            for (int i = 0; i < parserListeners.size(); i++) {
                ParserListener pl = (ParserListener) parserListeners.get(i);
                pl.parseFinished();
            }
        }
    }
}
