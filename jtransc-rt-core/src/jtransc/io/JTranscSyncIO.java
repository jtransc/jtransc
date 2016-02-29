package jtransc.io;

public class JTranscSyncIO {
	static public Impl impl = new Impl() {
		@Override
		public ImplStream open(String path) {
			return new ImplStream() {
				@Override
				native public void setPosition(long offset);

				@Override
				native public long getPosition();

				@Override
				native public long getLength();

				@Override
				native public int read(byte[] data, int offset, int size);

				@Override
				native public void close();
			};
		}
	};

	static public class ByteStream implements ImplStream {
		private int position;
		private byte[] data;

		public ByteStream(byte[] data) {
			this.data = data;
			this.position = 0;
		}

		@Override
		public void setPosition(long offset) {
			this.position = (int) offset;
		}

		@Override
		public long getPosition() {
			return this.position;
		}

		@Override
		public long getLength() {
			return this.data.length;
		}

		@Override
		public int read(byte[] data, int offset, int size) {
			int available = (int) (getLength() - getPosition());
			if (available <= 0) return -1;
			int toRead = Math.min(available, size);
			for (int n = 0; n < toRead; n++) {
				data[offset + n] = this.data[this.position + n];
			}
			this.position += toRead;
			return toRead;
		}

		@Override
		public void close() {

		}
	}

	public interface Impl {
		ImplStream open(String path);
	}

	public interface ImplStream {
		void setPosition(long offset);
		long getPosition();
		long getLength();
		int read(byte[] data, int offset, int size);
		void close();
	}
}
