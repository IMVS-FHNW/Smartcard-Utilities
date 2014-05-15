package ch.fhnw.imvs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BytesTransferConverterUtils {

	public static byte[] convert(List<byte[]> byteArrays) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream writeOut = new DataOutputStream(out);
			
			for (byte[] bs : byteArrays) {
				if (bs.length > Short.MAX_VALUE) {
					throw new IllegalArgumentException("Byte arrays with length greater than " + Short.MAX_VALUE + " not supported.");
				}
				writeOut.writeShort(bs.length);
				writeOut.write(bs);
			}

			byte[] buffer = out.toByteArray();
			writeOut.close();
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<byte[]> convert(byte[] transferBytes) {
		try {
			List<byte[]> byteArrays = new ArrayList<>();
			ByteArrayInputStream in = new ByteArrayInputStream(transferBytes);
			DataInputStream readIn = new DataInputStream(in);
			while (readIn.available() > 2) {
				short bufferLength = readIn.readShort();
				byte[] buffer = new byte[bufferLength];
				readIn.read(buffer);
				byteArrays.add(buffer);
			}
			return byteArrays;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
