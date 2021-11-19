package snownee.kiwi.util;

import io.netty.buffer.ByteBuf;

public class BitBufferHelper {

	private ByteBuf source;
	public final boolean write;
	private byte bits;
	private int bufferedByte;

	public BitBufferHelper(ByteBuf source, boolean write) {
		this.source = source;
		this.write = write;
	}

	public void source(ByteBuf source) {
		bits = 0;
		bufferedByte = 0;
		this.source = source;
	}

	private byte readBit() {
		if (bits == 0) {
			bufferedByte = source.readByte();
		}
		int ret = bufferedByte >> (7 - bits) & 1;
		if (++bits == 8) {
			end();
		}
		return (byte) ret;
	}

	private void writeBit(int i) {
		if (i != 0) {
			bufferedByte |= 1 << (7 - bits);
		}
		if (++bits == 8) {
			end();
		}
	}

	public void end() {
		if (write && bits != 0) {
			source.writeByte(bufferedByte);
		}
		bits = 0;
		bufferedByte = 0;
	}

	public boolean readBoolean() {
		return readBit() == 1;
	}

	public void writeBoolean(boolean bool) {
		writeBit(bool ? 1 : 0);
	}

	public int readBits(int size) {
		int ret = 0;
		for (int j = size - 1; j >= 0; j--) {
			int i = readBit();
			ret |= i << j;
		}
		return ret;
	}

	public void writeBits(int i, int size) {
		for (int j = size - 1; j >= 0; j--) {
			writeBit(i >> j & 1);
		}
	}

}
