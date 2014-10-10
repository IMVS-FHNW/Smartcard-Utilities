package ch.fhnw.imvs.smartcard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable class representing a BERTLV-Formatted message. The constructor
 * takes a bytearray, reads the data to fill the fields of this class and
 * recursively create children as necessary.
 * 
 * @author Christof Arnosti (christof.arnosti@fhnw.ch)
 * 
 */
public final class TLV {

	private static final int CLASS_MASK = 0xC0;
	private static final int CLASS_UNIVERSAL = 0x00;
	@SuppressWarnings("unused")
	private static final int CLASS_APPLICATION = 0x04;
	@SuppressWarnings("unused")
	private static final int CLASS_CONTEXT = 0x08;
	@SuppressWarnings("unused")
	private static final int CLASS_PRIVATE = 0x0C;

	private static final int IDENTIFIER_MASK = 0x1F;
	private static final int LENGTH_MASK = 0x80;

	private static final int CONSTRUCTED_MASK = 0x20;

	/**
	 * Contains the header bytes
	 */
	private final byte[] header;

	/**
	 * If this TLV has children: A List of children, null otherwise
	 */
	private final List<TLV> children;

	/**
	 * If this TLV has no children: The content, null otherwise
	 */
	private final byte[] content;

	/**
	 * Distinguish primitive from composed TLV
	 */
	private final boolean hasChildren;

	/**
	 * Length of the Type part of the header
	 */
	private final int typelength;

	/**
	 * Length of the Length part of the header
	 */
	private final int lengthlength;

	/**
	 * Default Constructor. Tries to construct a TLV Object out of the data,
	 * starting with byte 0 until the end.
	 * 
	 * @param data
	 *            bytearray of TLV-Encoded Data
	 */
	public TLV(byte[] data) {
		this(data, 0);
	}

	/**
	 * Returns the length of the byte-representation of this TLV, including
	 * header and value.
	 * 
	 * @return Length of this TLV.
	 */
	public int getLength() {
		if (children == null) {
			return content.length + header.length;
		} else {
			int ret = header.length;
			for (TLV child : children) {
				ret += child.getLength();
			}
			return ret;
		}
	}

	/**
	 * Returns the length of the value of this TLV as reported in the TLV
	 * header.
	 * 
	 * @return Length of the value.
	 */
	private int getValueLength() {
		return header[header.length - 1] & 0xff;
	}

	/**
	 * Returns a copy of the content (Value) of this TLV, if this TLV does not
	 * contain children.
	 * 
	 * @return copy of the content.
	 * @throws IllegalStateException
	 *             If this TLV has children
	 */
	public byte[] getContent() {
		if (hasChildren)
			throw new IllegalStateException(
					"Method cannot be called on a TLV with children");
		return Arrays.copyOf(content, content.length);
	}

	/**
	 * Returns a bytearray containing the byte representation of this TLV.
	 * 
	 * @return bytearray containing this TLV.
	 */
	public byte[] getRaw() {
		byte[] ret = new byte[getLength()];
		if (hasChildren) {
			int i = header.length;
			System.arraycopy(header, 0, ret, 0, header.length);
			for (TLV child : children) {
				int length = child.getLength();
				System.arraycopy(child.getRaw(), 0, ret, i, length);
				i += length;
			}
		} else {
			System.arraycopy(header, 0, ret, 0, header.length);
			System.arraycopy(content, 0, ret, header.length, content.length);
		}
		return ret;
	}

	/**
	 * Returns a copy of the header of this TLV
	 * 
	 * @return copy of the header.
	 */
	public byte[] getHeader() {
		return Arrays.copyOf(header, header.length);
	}

	/**
	 * Returns an unmodifiable list containing all children of this TLV
	 * 
	 * @return unmodifiable list of the children of this TLV
	 */
	public List<TLV> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * This constructor returns a TLV-Object representing the TLV-Formatted
	 * message at position <code>offset</code> in the <code>data</code>
	 * -bytearray.
	 * 
	 * @param data
	 *            Bytearray containing the TLV-Coded message.
	 * @param offset
	 *            Start of the TLV-Formatted message in the data-bytearray.
	 * @throws IllegalArgumentException
	 *             If data
	 */
	public TLV(byte[] data, int offset) {
		if (data == null) {
			throw new IllegalArgumentException("Data is not allowed to be null");
		}
		if (data.length <= 2 || data.length < 2 + offset) {
			throw new IllegalArgumentException(
					"data must at least contain two more bytes after offset");
		}

		// Header:
		// Longform
		if ((data[offset] & IDENTIFIER_MASK) == IDENTIFIER_MASK) {
			int i = 1;

			// Case: Just one extention header field present
			if (data.length < offset + i + 2) {
				throw new IllegalArgumentException("Data array ");
			}

			while ((data[offset + i] & 0x80) == 0x80) {
				i++;
				if (data.length < offset + i + 2) {
					throw new IllegalArgumentException("Data array ");
				}
			}
			// Last Type field
			i++;

			typelength = i;

			// Variable length length...
			if ((data[offset + i] & LENGTH_MASK) != LENGTH_MASK) {
				i += 1;
			} else if ((data[offset + i] ^ LENGTH_MASK) == 0) {
				throw new IllegalArgumentException(
						"Infinite length not supported");
			} else {
				i += 1 + ((data[offset + i] & 0xFF) ^ LENGTH_MASK);
			}
			lengthlength = i - typelength;

			header = new byte[i];
			System.arraycopy(data, offset, header, 0, header.length);
		}
		// Standard form
		else {
			int i = 1;
			typelength = 1;
			// Variable length length...
			if ((data[offset + i] & LENGTH_MASK) != LENGTH_MASK) {
				i += 1;
			} else if ((data[offset + i] ^ LENGTH_MASK) == 0) {
				throw new IllegalArgumentException(
						"Infinite length not supported");
			} else {
				i += 1 + ((data[offset + i] & 0xFF) - LENGTH_MASK);
			}

			lengthlength = i - typelength;
			header = new byte[i];
			System.arraycopy(data, offset, header, 0, header.length);
		}

		hasChildren = (header[0] & CONSTRUCTED_MASK) == CONSTRUCTED_MASK;

		// Simple element
		if (!hasChildren) {
			children = null;
			content = new byte[getValueLength()];
			System.arraycopy(data, offset + header.length, content, 0,
					getValueLength());
		}
		// Element with children
		else {
			content = null;
			children = new ArrayList<TLV>();
			int childoffset = offset + header.length;
			while (getValueLength() + offset + header.length > childoffset) {

				TLV child = new TLV(data, childoffset);
				children.add(child);
				childoffset += child.getLength();

				if (childoffset > getValueLength() + offset + header.length) {
					throw new IllegalArgumentException(
							"Size of children not correct");
				}
			}
		}
	}

	private void addToStringBuilder(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(" ");
		}
		sb.append("H: ").append(bufToString(header, 0, typelength))
				.append(", L: ")
				.append(bufToString(header, typelength, lengthlength));
		if (!hasChildren) {
			sb.append(" Item (")
					.append(((header[0] & CLASS_MASK) == CLASS_UNIVERSAL) ? getContentDescription(header[0]
							& IDENTIFIER_MASK)
							: "Non-Standard").append("): ")
					.append(bufToString(content, 0, content.length));
		} else {
			sb.append(" Sequence (")
					.append(((header[0] & CLASS_MASK) == CLASS_UNIVERSAL) ? getContentDescription(header[0]
							& IDENTIFIER_MASK)
							: "Non-Standard").append("): [\n");
			for (TLV child : children) {
				child.addToStringBuilder(sb, indent + 1);
			}
			for (int i = 0; i < indent; i++) {
				sb.append(" ");
			}
			sb.append("]");
		}
		sb.append("\n");
	}

	/**
	 * Human-readable representation of this TLV.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		addToStringBuilder(sb, 0);
		return sb.toString();
	}

	private String getContentDescription(int bytevalue) {
		switch (bytevalue) {
		case 0x0:
			return "EOC (End-of-Content)";
		case 0x1:
			return "BOOLEAN";
		case 0x2:
			return "INTEGER";
		case 0x3:
			return "BIT STRING";
		case 0x4:
			return "OCTET STRING";
		case 0x5:
			return "NULL";
		case 0x6:
			return "OBJECT IDENTIFIER";
		case 0x7:
			return "Object Descriptor";
		case 0x8:
			return "EXTERNAL";
		case 0x9:
			return "REAL (float)";
		case 0xA:
			return "ENUMERATED";
		case 0xB:
			return "EMBEDDED PDV";
		case 0xC:
			return "UTF8String";
		case 0xD:
			return "RELATIVE-OID";
		case 0xE:
			return "(reserved)";
		case 0xF:
			return "(reserved)";
		case 0x10:
			return "SEQUENCE and SEQUENCE OF";
		case 0x11:
			return "SET and SET OF";
		case 0x12:
			return "NumericString";
		case 0x13:
			return "PrintableString";
		case 0x14:
			return "T61String";
		case 0x15:
			return "VideotexString";
		case 0x16:
			return "IA5String";
		case 0x17:
			return "UTCTime";
		case 0x18:
			return "GeneralizedTime";
		case 0x19:
			return "GraphicString";
		case 0x1A:
			return "VisibleString";
		case 0x1B:
			return "GeneralString";
		case 0x1C:
			return "UniversalString";
		case 0x1D:
			return "CHARACTER STRING";
		case 0x1E:
			return "BMPString";
		default:
			return "(use long-form)";
		}
	}

	/**
	 * Get a child of the current TLV by the index, ordered by their occurence
	 * in the original bytearray.
	 * 
	 * @param number
	 *            The Index of the child.
	 * @return The child TLV.
	 * @throws IllegalStateException
	 *             If this method was called on a TLV without children.
	 * @throws IndexOutOfBoundsException
	 *             If the index of the child is invalid.
	 */
	public TLV getChildByNumber(int number) {
		if (!hasChildren)
			throw new IllegalStateException(
					"Method cannot be called on a TLV without children");
		return children.get(number);
	}

	/**
	 * Get a specific child of this TLV, identified by its Type.
	 * 
	 * @param header
	 *            A bytearray containing the type to search for.
	 * @return Specific child of the current TLV if the type matches the
	 *         parameter, <code>null</code> otherwise
	 */
	public TLV getChildByTag(byte[] header) {
		TLV ret = null;
		if (hasChildren) {
			for (TLV child : children) {
				if (Arrays.equals(Arrays.copyOf(child.header, child.header.length - 1)
						, header)) {
					return child;
				}
			}
		}
		return ret;
	}

	private String bufToString(byte[] buf, int start, int len) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i > 0)
				s.append(" ");
			String hex = Integer.toHexString(0xFF & buf[start + i]);
			if (hex.length() == 1) {
				s.append('0');
			}
			s.append(hex);
		}
		return s.toString();
	}

	/**
	 * Equals based on the raw bytes which were used to create this TLV-Object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj.getClass() != TLV.class)
			return false;
		return Arrays.equals(getRaw(), ((TLV) obj).getRaw());
	}

}
