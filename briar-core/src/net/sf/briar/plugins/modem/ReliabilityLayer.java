package net.sf.briar.plugins.modem;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

class ReliabilityLayer implements ReadHandler, WriteHandler {

	private static final Logger LOG =
			Logger.getLogger(ReliabilityLayer.class.getName());

	private final WriteHandler writeHandler;
	private final BlockingQueue<byte[]> writes;

	private volatile Receiver receiver = null;
	private volatile SlipDecoder decoder = null;
	private volatile ReceiverInputStream inputStream = null;
	private volatile SenderOutputStream outputStream = null;
	private volatile Thread writer = null;
	private volatile boolean valid = true;

	ReliabilityLayer(WriteHandler writeHandler) {
		this.writeHandler = writeHandler;
		writes = new LinkedBlockingQueue<byte[]>();
	}

	void init() {
		SlipEncoder encoder = new SlipEncoder(this);
		Sender sender = new Sender(encoder);
		receiver = new Receiver(sender);
		decoder = new SlipDecoder(receiver);
		inputStream = new ReceiverInputStream(receiver);
		outputStream = new SenderOutputStream(sender);
		writer = new Thread("ReliabilityLayer") {
			@Override
			public void run() {
				try {
					while(valid) {
						byte[] b = writes.take();
						if(b.length == 0) return; // Poison pill
						if(LOG.isLoggable(INFO))
							LOG.info("Writing " + b.length + " bytes");
						writeHandler.handleWrite(b);
					}
				} catch(InterruptedException e) {
					if(LOG.isLoggable(WARNING))
						LOG.warning("Interrupted while writing");
					valid = false;
					Thread.currentThread().interrupt();
				} catch(IOException e) {
					if(LOG.isLoggable(WARNING))
						LOG.warning("Interrupted while writing");
					valid = false;
				}
			}
		};
		writer.start();
	}

	InputStream getInputStream() {
		return inputStream;
	}

	OutputStream getOutputStream() {
		return outputStream;
	}

	void invalidate() {
		valid = false;
		receiver.invalidate();
		writes.add(new byte[0]); // Poison pill
	}

	// The modem calls this method to pass data up to the SLIP decoder
	public void handleRead(byte[] b) throws IOException {
		if(!valid) throw new IOException("Connection closed");
		if(LOG.isLoggable(INFO)) LOG.info("Read " + b.length + " bytes");
		decoder.handleRead(b);
	}

	// The SLIP encoder calls this method to pass data down to the modem
	public void handleWrite(byte[] b) throws IOException {
		if(!valid) throw new IOException("Connection closed");
		if(LOG.isLoggable(INFO)) LOG.info("Queueing " + b.length + " bytes");
		if(b.length > 0) writes.add(b);
	}
}