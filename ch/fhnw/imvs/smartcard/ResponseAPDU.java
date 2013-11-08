package ch.fhnw.imvs.smartcard;

public class ResponseAPDU {

	private final byte[] data;

	public ResponseAPDU(byte[] data) {
		if (data != null) {
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		} else {
			this.data = new byte[0];
		}
	}

	public boolean isValidAPDU() {
		if (data.length > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public byte getStatusByte1() {
		return data[data.length - 2];
	}
	
	public byte getStatusByte2() {
		return data[data.length - 1];
	}
	
	public int getStatusBytes() {
		return (getStatusByte1() << 8) | getStatusByte2();
	}
	
	public byte[] getArgumentData() {
		byte[] ret = new byte[data.length-2];
		System.arraycopy(data, 0, ret, 0, data.length-2);
		return ret;
	}
	
	
	/**
	 * Returns a Copy of the underlying bytearray.
	 * 
	 * @return A Copy of the <code>byte[]</code>-Representation of this APDU.
	 */
	public byte[] getRaw() {
		byte[] ret = new byte[data.length];
		System.arraycopy(data, 0, ret, 0, data.length);
		return ret;
	}
}
