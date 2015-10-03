package asset.pipeline.utils

import groovy.transform.CompileStatic

@CompileStatic
public class MultiOutputStream extends OutputStream{
	private final Collection<OutputStream> streams;

	public MultiOutputStream(Collection<OutputStream> streams) {
		if (streams == null)
			throw new NullPointerException();
		this.streams = streams
	}

	@Override
	public void write(int b) throws IOException {
		streams.each { OutputStream stream ->
			stream.write(b)
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		streams.each { OutputStream stream ->
			stream.write(b)
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		streams.each { OutputStream stream ->
			stream.write(b, off, len)
		}
	}

	@Override
	public void flush() throws IOException {
		streams.each { OutputStream stream ->
			stream.flush()
		}
	}
}