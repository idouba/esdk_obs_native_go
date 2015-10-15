package javax.wbem.client.adapter.http.transport;


import java.io.IOException;
import java.io.OutputStream;

/**
 * 基于RFC1521. 34文档实现了BASE64字符编码类
 */

public class BASE64Encoder extends CharacterEncoder {

	/** 每个原子编码3个字节 */
	protected int bytesPerAtom() {
		return (3);
	}

	/**
	 * 每一行编码57个字节
	 */
	protected int bytesPerLine() {
		return (57);
	}

	/** 数组字符映射表 */
	private final static char CHAR_PEM[] = {
	// 0 1 2 3 4 5 6 7
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 1
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 2
			'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 3
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 4
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 5
			'w', 'x', 'y', 'z', '0', '1', '2', '3', // 6
			'4', '5', '6', '7', '8', '9', '+', '/' // 7
	};

	/**
	 * encodeAtom 采用输入的三个字节，并将它编码作为4 78 *可打印的字符
	 */
	protected void encodeAtom(OutputStream tempOutStream, byte tempData[], int tempOffset,
			int tempLength) throws IOException {
		byte aByte, bByte, cByte;

		if (tempLength == 1) {
			aByte = tempData[tempOffset];
			bByte = 0;
			cByte = 0;
			tempOutStream.write(CHAR_PEM[(aByte >>> 2) & 0x3F]);
			tempOutStream.write(CHAR_PEM[((aByte << 4) & 0x30) + ((bByte >>> 4) & 0xf)]);
			tempOutStream.write('=');
			tempOutStream.write('=');
		} else if (tempLength == 2) {
			aByte = tempData[tempOffset];
			bByte = tempData[tempOffset + 1];
			cByte = 0;
			tempOutStream.write(CHAR_PEM[(aByte >>> 2) & 0x3F]);
			tempOutStream.write(CHAR_PEM[((aByte << 4) & 0x30) + ((bByte >>> 4) & 0xf)]);
			tempOutStream.write(CHAR_PEM[((bByte << 2) & 0x3c) + ((cByte >>> 6) & 0x3)]);
			tempOutStream.write('=');
		} else {
			aByte = tempData[tempOffset];
			bByte = tempData[tempOffset + 1];
			cByte = tempData[tempOffset + 2];
			tempOutStream.write(CHAR_PEM[(aByte >>> 2) & 0x3F]);
			tempOutStream.write(CHAR_PEM[((aByte << 4) & 0x30) + ((bByte >>> 4) & 0xf)]);
			tempOutStream.write(CHAR_PEM[((bByte << 2) & 0x3c) + ((cByte >>> 6) & 0x3)]);
			tempOutStream.write(CHAR_PEM[cByte & 0x3F]);
		}
	}
}
