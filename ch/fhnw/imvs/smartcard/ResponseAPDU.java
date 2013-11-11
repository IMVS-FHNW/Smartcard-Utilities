package ch.fhnw.imvs.smartcard;

/**
 * The Response APDU consists of two Parts: An optional Body of variable length
 * and a Trailer with two status bytes.
 * 
 * @author Christof Arnosti (christof.arnosti@fhnw.ch)
 * 
 */
public class ResponseAPDU {

	public static final int STATUS_OK = 0x9000;
	
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
	 * Create a ResponseAPDU out of a body and a statuscode
	 * 
	 * @param body bytearray of arbitary length or <code>null</code>. Null is treated as no body
	 * @param statuscode The last two bytes of the integer will be used as status code.
	 */
	public ResponseAPDU(byte[] body, int statuscode) {
		if (body == null) {
			body = new byte[0];
		}
		data = new byte[body.length + 2];
		System.arraycopy(body, 0, data, 0, body.length);
		data[data.length - 1] = (byte) statuscode;

		data[data.length - 2] = (byte) (statuscode >> 8);
	}

	/**
	 * Checks if this is a valid response APDU.
	 * 
	 * @return <code>true</code> if this is a valid response APDU,
	 *         <code>false</code> otherwise.
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
	 * 
	 * @return the first status byte.
	 */
	public byte getStatusByte1() {
		return data[data.length - 2];
	}

	/**
	 * The second status byte.
	 * 
	 * @return the second status byte.
	 */
	public byte getStatusByte2() {
		return data[data.length - 1];
	}

	/**
	 * The status bytes as an integer..
	 * 
	 * @return status byte.
	 */
	public int getStatusBytes() {
		return (getStatusByte1() << 8) | getStatusByte2();
	}

	/**
	 * Returns a bytearray of the Argument Data. If no Argument Data is present,
	 * an array of length 0 is returned.
	 * 
	 * @return Argument Data
	 */
	public byte[] getArgumentData() {
		byte[] ret = new byte[data.length - 2];
		System.arraycopy(data, 0, ret, 0, data.length - 2);
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
