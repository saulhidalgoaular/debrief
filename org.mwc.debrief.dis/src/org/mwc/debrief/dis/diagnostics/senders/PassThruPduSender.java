package org.mwc.debrief.dis.diagnostics.senders;

import org.mwc.debrief.dis.listeners.IDISGeneralPDUListener;

import edu.nps.moves.dis.Pdu;

/**
 * simple class that sends a PDU straight to a listener (mostly for non-network
 * testing)
 *
 * @author ian
 *
 */
public class PassThruPduSender implements IPduSender {

	final private IDISGeneralPDUListener _listener;

	public PassThruPduSender(final IDISGeneralPDUListener listener) {
		_listener = listener;
	}

	@Override
	public void close() {
		_listener.complete("unknown");
	}

	@Override
	public void sendPdu(final Pdu pdu) {
		_listener.logPDU(pdu);
	}

}
