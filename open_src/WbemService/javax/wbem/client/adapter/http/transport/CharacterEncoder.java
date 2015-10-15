package javax.wbem.client.adapter.http.transport;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 定义了字符编码的方法。
 */
public abstract class CharacterEncoder {

	/** 打印流*/
	protected PrintStream printTempStream;

	/**   返回每个原子编码的字节数*/
	abstract protected int bytesPerAtom();

	/**  返回每一行能够编码的字节数*/
	abstract protected int bytesPerLine();

	/**
	 * 编码整个缓冲区的前缀
	 */
	protected void encodeBufferPrefix(OutputStream outTempStream) throws IOException {
		printTempStream = new PrintStream(outTempStream);
	}

	/**
	 * 编码整个缓冲区的后缀
	 */
	protected void encodeBufferSuffix(OutputStream aStream) throws IOException {
	}

	/**
	 *  编码每一行的的前缀
	 */
	protected void encodeLinePrefix(OutputStream aStream, int aLength)
			throws IOException {
	}

	/**
	 * 编码每一行的的后缀
	 */
	protected void encodeLineSuffix(OutputStream aStream) throws IOException {
		printTempStream.println();
	}

	/** 将原子信息编码为字符*/
	abstract protected void encodeAtom(OutputStream aStream, byte someBytes[],
			int anOffset, int aLength) throws IOException;

	protected int readFully(InputStream inTemp, byte bufferTemp[])
			throws java.io.IOException {
		for (int i = 0; i < bufferTemp.length; i++) {
			int q = inTemp.read();
			if (q == -1)
				return i;
			bufferTemp[i] = (byte) q;
		}
		return bufferTemp.length;
	}

	/**
	 * 将输入流编码为字节，并用字符写入
	 */
	public void encode(InputStream inTempStream, OutputStream outStreamTemp)
			throws IOException {
		int j;
		int numTempBytes;
		byte tmpBytebuffer[] = new byte[bytesPerLine()];

		encodeBufferPrefix(outStreamTemp);

		while (true) {
			numTempBytes = readFully(inTempStream, tmpBytebuffer);
			if (numTempBytes == 0) {
				break;
			}
			encodeLinePrefix(outStreamTemp, numTempBytes);
			for (j = 0; j < numTempBytes; j += bytesPerAtom()) {

				if ((j + bytesPerAtom()) <= numTempBytes) {
					encodeAtom(outStreamTemp, tmpBytebuffer, j, bytesPerAtom());
				} else {
					encodeAtom(outStreamTemp, tmpBytebuffer, j, (numTempBytes) - j);
				}
			}
			if (numTempBytes < bytesPerLine()) {
				break;
			} else {
				encodeLineSuffix(outStreamTemp);
			}
		}
		encodeBufferSuffix(outStreamTemp);
	}

	/**
	 * 将缓冲区的数据编码并写入到输出流
	 */
	public void encode(byte aBuffer[], OutputStream aStream) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(aBuffer);
		encode(inStream, aStream);
	}

	/**
	 * 取缓冲区的字节，并返回字符串
	 */
	public String encode(byte aTempBuffer[]) {
		ByteArrayOutputStream outStreamTemp = new ByteArrayOutputStream();
		ByteArrayInputStream inStreamTemp = new ByteArrayInputStream(aTempBuffer);
		String returnVal = null;
		try {
			encode(inStreamTemp, outStreamTemp);
			returnVal = outStreamTemp.toString("8859_1");
		} catch (Exception IOException) {
			throw new Error("CharacterEncoder.encode internal error");
		}
		return (returnVal);
	}

	/**
	 * 从字节缓冲中返回字符数组
	 */
	private byte[] getBytes(ByteBuffer bbTemp) {
		
		byte[] bufTemp = null;

		if (bbTemp.hasArray()) {
			byte[] tmp = bbTemp.array();
			if ((tmp.length == bbTemp.capacity()) && (tmp.length == bbTemp.remaining())) {
				bufTemp = tmp;
				bbTemp.position(bbTemp.limit());
			}
		}

		if (bufTemp == null) {
			bufTemp = new byte[bbTemp.remaining()];
			bbTemp.get(bufTemp);
		}

		return bufTemp;
	}

	/**
	 * 编码缓冲区的数据，并将编码后的结果写入到输出流
	 */
	public void encode(ByteBuffer aBufferTemp, OutputStream aStreamTemp)
			throws IOException {
		byte[] buf = getBytes(aBufferTemp);
		encode(buf, aStreamTemp);
	}

	/**
	 * 将输入流编码为字节，并用字符写入
	 */
	public void encodeBuffer(InputStream inTempStream, OutputStream outEncodeStream)
			throws IOException {
		int jTemp;
		int numTempBytes;
		byte tempbuffer[] = new byte[bytesPerLine()];

		encodeBufferPrefix(outEncodeStream);

		while (true) {
			numTempBytes = readFully(inTempStream, tempbuffer);
			if (numTempBytes == 0) {
				break;
			}
			encodeLinePrefix(outEncodeStream, numTempBytes);
			for (jTemp = 0; jTemp < numTempBytes; jTemp += bytesPerAtom()) {
				if ((jTemp + bytesPerAtom()) <= numTempBytes) {
					encodeAtom(outEncodeStream, tempbuffer, jTemp, bytesPerAtom());
				} else {
					encodeAtom(outEncodeStream, tempbuffer, jTemp, (numTempBytes) - jTemp);
				}
			}
			encodeLineSuffix(outEncodeStream);
			if (numTempBytes < bytesPerLine()) {
				break;
			}
		}
		encodeBufferSuffix(outEncodeStream);
	}
	/**
	 * 取缓冲区的字节，并返回字符串
	 */
	public String encode(ByteBuffer aBuffer) {
		byte[] buf = getBytes(aBuffer);
		return encode(buf);
	}




	/**
	 * 编码缓冲区的字节缓冲，并将编码后的结果写入到输出流
	 */
	public String encodeBuffer(ByteBuffer aTempBuffer) {
		byte[] bufByte = getBytes(aTempBuffer);
		return encodeBuffer(bufByte);
	}

	
	/**
	 * 编码缓冲区的数据，并将编码后的结果写入到输出流
	 */
	public void encodeBuffer(byte aBufferByte[], OutputStream aStreamTemp)
			throws IOException {
		ByteArrayInputStream inTempStream = new ByteArrayInputStream(aBufferByte);
		encodeBuffer(inTempStream, aStreamTemp);
	}


	/**
	 * 编码缓冲区的数据，并将编码后的结果写入到输出流
	 */
	public void encodeBuffer(ByteBuffer aBufferTemp, OutputStream aTempStream)
			throws IOException {
		byte[] bufTemp = getBytes(aBufferTemp);
		encodeBuffer(bufTemp, aTempStream);
	}
	

	/**
	 * 取缓冲区的字节，并返回字符串
	 */
	public String encodeBuffer(byte aBufferTemp[]) {
		ByteArrayOutputStream outTempStream = new ByteArrayOutputStream();
		ByteArrayInputStream inTempStream = new ByteArrayInputStream(aBufferTemp);
		try {
			encodeBuffer(inTempStream, outTempStream);
		} catch (Exception IOException) {
			throw new Error("CharacterEncoder.encodeBuffer internal error");
		}
		return (outTempStream.toString());
	}

}
