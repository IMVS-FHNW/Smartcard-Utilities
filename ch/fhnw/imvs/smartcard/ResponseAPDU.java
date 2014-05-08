package ch.fhnw.imvs.smartcard;

import ch.fhnw.imvs.util.ConverterUtils;

/**
 * The Response APDU consists of two Parts: An optional Body of variable length
 * and a Trailer with two status bytes. <br>
 * Extended APDU can be split in multiple standard APDUs.
 * 
 * @author Christof Arnosti (christof.arnosti@fhnw.ch), Jonas Lauener
 *         (jonas.lauener@fhnw.ch)
 * 
 */
public class ResponseAPDU {

	private static final short MAXIMUM_STAND_APDU_ARGUMENT_LENGTH = 251;

	public static final short STATUS_OK = (short) 0x9000;

	public static final byte STATUS_B1_OK_BYTES_AVAILABLE = (byte) 0x61;

	public static final short STATUS_UNKNOWN_ERROR = 0x6F00;

	public static final short STATUS_INS_NOT_SUPPORTED = 0x6D00;

	public static final short STATUS_RECORD_NOT_FOUND = 0x6A83;

	public static final short STATUS_COMMAND_RUNNING = (short) 0x9100;

	public static final short STATUS_COMMAND_NOT_ALLOWED = 0x6900;

	/**
	 * Contains the bytearray-Representation of this APDU
	 */
	private final byte[] data;

	/**
	 * Create a ResponseAPDU only with a statuscode
	 * 
	 * @param statuscode
	 *            The last two bytes of the integer will be used as status code.
	 */
	public ResponseAPDU(short statuscode) {
		this(null, statuscode);
	}

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
	 * @param body
	 *            bytearray of arbitary length or <code>null</code>. Null is
	 *            treated as no body
	 * @param statuscode
	 *            The last two bytes of the integer will be used as status code.
	 */
	public ResponseAPDU(byte[] body, short statuscode) {
		if (body == null) {
			body = new byte[0];
		}
		data = new byte[body.length + 2];
		System.arraycopy(body, 0, data, 0, body.length);
		data[data.length - 1] = (byte) statuscode;

		data[getArgumentDataLength()] = (byte) (statuscode >> 8);
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
	 * Check if this is a extended response APDU.
	 * 
	 * @return <code>true</code> if argument data length is greater than 256,
	 *         <code>false</code> otherwise.
	 */
	public boolean isExtendedAPDU() {
		return getArgumentDataLength() > MAXIMUM_STAND_APDU_ARGUMENT_LENGTH;
	}

	/**
	 * The first status byte.
	 * 
	 * @return the first status byte.
	 */
	public byte getStatusByte1() {
		return data[getArgumentDataLength()];
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
	public short getStatusBytes() {
		return (short) ((getStatusByte1() << 8) | getStatusByte2());
	}

	/**
	 * Returns a bytearray of the Argument Data. If no Argument Data is present,
	 * an array of length 0 is returned.
	 * 
	 * @return Argument Data
	 */
	public byte[] getArgumentData() {
		byte[] ret = new byte[getArgumentDataLength()];
		System.arraycopy(data, 0, ret, 0, getArgumentDataLength());
		return ret;
	}

	/**
	 * Returns length of the Argument Data. If no Argument Data is present, 0 is
	 * returned.
	 * 
	 * @return length of Argument Data
	 */
	public int getArgumentDataLength() {
		int argumentDataLength = data.length - 2;
		if (argumentDataLength < 0) {
			return 0;
		}
		return argumentDataLength;
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
	 * Returns a non-extended ResponseAPDU. The status bytes indicate if more
	 * ResponseAPDU are following.
	 * 
	 * @param parcelNumber
	 *            An extended ResponseAPDU has to be parceled in multiple
	 *            non-extended ResponseAPDUs. Starting from zero.
	 * @return A non-extended {@link ResponseAPDU}.
	 */
	public ResponseAPDU getStandardResponseAPDU(int parcelNumber) {
		if (parcelNumber < 0 || parcelNumber >= getStandardResponseAPDUCount()) {
			throw new IndexOutOfBoundsException(String.valueOf(parcelNumber));
		}
		if (!isExtendedAPDU()) {
			return this;
		}

		int argumentDataLength = getArgumentDataLength();
		int startPosition = parcelNumber * MAXIMUM_STAND_APDU_ARGUMENT_LENGTH;
		int length = Math.min(argumentDataLength - startPosition, MAXIMUM_STAND_APDU_ARGUMENT_LENGTH);

		byte[] argumentData = new byte[length];
		System.arraycopy(data, startPosition, argumentData, 0, length);

		short statusBytes;

		int nextParcelStartPosition = startPosition + length;
		int nextParcelLength = Math.min(argumentDataLength - nextParcelStartPosition,
				MAXIMUM_STAND_APDU_ARGUMENT_LENGTH);
		if (nextParcelLength > 0) {
			statusBytes = (short) ((STATUS_B1_OK_BYTES_AVAILABLE << 8) | nextParcelLength);
		} else {
			statusBytes = getStatusBytes();
		}
		return new ResponseAPDU(argumentData, statusBytes);
	}

	public int getStandardResponseAPDUCount() {
		return ((getArgumentDataLength() - 1) / MAXIMUM_STAND_APDU_ARGUMENT_LENGTH) + 1;
	}

	/**
	 * Returns true if the underlying bytearray is equal.
	 */
	@Override
	public boolean equals(Object o) {
		return (o instanceof ResponseAPDU) && data.equals(((ResponseAPDU) o).data);
	}

	@Override
	public String toString() {
		return ConverterUtils.toHexString(getRaw());
	}

}
