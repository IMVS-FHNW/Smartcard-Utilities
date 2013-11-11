package ch.fhnw.imvs.smartcard;

/**
 * The Response APDU consists of two Parts: An optional Body of variable length and a Trailer with two status bytes.
 * 
 * @author Christof Arnosti (christof.arnosti@fhnw.ch)
 *
 */
public class ResponseAPDU {


	/**
	 * Contains the bytearray-Representation of this APDU
	 */
	private final byte[] data;

	/**
	 * Creates a ResponseAPDU object out of the raw bytes of an APDU.
	 * 
	 * @param data
	 *            The Bytearray containing the Response APDU
	 */
	public ResponseAPDU(byte[] data) {
		if (data != null) {
			this.data = new byte[data.length];
			System.arraycopy(data, 0, this.data, 0, data.length);
		} else {
			this.data = new byte[0];
		}
	}

	/**
	 * Checks if this is a valid response APDU.
	 * @return <code>true</code> if this is a valid response APDU, <code>false</code> otherwise.
	 */
	public boolean isValidAPDU() {
		if (data.length > 1) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * The first status byte.
	 * @return the first status byte.
	 */
	public byte getStatusByte1() {
		return data[data.length - 2];
	}
	
	/**
	 * The second status byte.
	 * @return the second status byte.
	 */
	public byte getStatusByte2() {
		return data[data.length - 1];
	}
	
	/**
	 * The status bytes as an integer..
	 * @return status byte.
	 */
	public int getStatusBytes() {
		return (getStatusByte1() << 8) | getStatusByte2();
	}
	
	/**
	 * Returns a bytearray of the Argument Data. If no Argument Data is present, an array of length 0 is returned.
	 * @return Argument Data
	 */
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
	
	/**
	 * Returns true if the underlying bytearray is equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o instanceof ResponseAPDU) {
				return data.equals(((ResponseAPDU) o).data);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
