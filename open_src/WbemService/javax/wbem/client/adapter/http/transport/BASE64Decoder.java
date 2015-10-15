/*
 * 项 目 名:  ISM V100R006C00
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 2010,  All rights reserved.
 * 描    述:  Huawei PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.wbem.client.adapter.http.transport;

import java.io.OutputStream;
import java.io.PushbackInputStream;

/**
 * 提供一个用于BASE64的辅助类。
 *
 * @author  h90005710
 * @version  ISM V100R006C00,2014-01-24
 * @since  V100R006C00
 */
public class BASE64Decoder extends CharacterDecoder {

	/*每4个字节作为一个原子*/
    protected int bytesPerAtom() {
        return (4);
    }

    /*4的倍数，72作为*/
    protected int bytesPerLine() {
        return (72);
    }

	/** 数组字符映射表 */
    private final static char char_pem[] = {
                'A','B','C','D','E','F','G','H', 
                'I','J','K','L','M','N','O','P', 
                'Q','R','S','T','U','V','W','X', 
                'Y','Z','a','b','c','d','e','f', 
                'g','h','i','j','k','l','m','n', 
                'o','p','q','r','s','t','u','v', 
                'w','x','y','z','0','1','2','3', 
                '4','5','6','7','8','9','+','/'  
        };

    private final static byte pem_to_array[] = new byte[256];

    static {
        for (int i = 0; i < 255; i++) {
            pem_to_array[i] = -1;
        }
        for (int i = 0; i < char_pem.length; i++) {
            pem_to_array[char_pem[i]] = (byte) i;
        }
    }

    byte decodeBufferByte[] = new byte[4];

	/**
	 * 将BASE64的原子作为1,2,3,的字节数据
	 */
    protected void decodeAtom(PushbackInputStream inTempPushStream, OutputStream outTempStream, int remTemp)
        throws java.io.IOException
    {
        int     count;
        byte    tempNumber = -1, b = -1, c = -1, d = -1;

        if (remTemp < 2) {
            throw new DecodeFormatException("BASE64Decoder: Not enough bytes for an atom.");
        }
        do {
            count = inTempPushStream.read();
            if (count == -1) {
                throw new DecodeStreamException();
            }
        } while (count == '\n' || count == '\r');
        decodeBufferByte[0] = (byte) count;

        count = readFully(inTempPushStream, decodeBufferByte, 1, remTemp-1);
        if (count == -1) {
            throw new DecodeStreamException();
        }

        if (remTemp > 3 && decodeBufferByte[3] == '=') {
            remTemp = 3;
        }
        if (remTemp > 2 && decodeBufferByte[2] == '=') {
            remTemp = 2;
        }
        switch (remTemp) {
        case 4:
            d = pem_to_array[decodeBufferByte[3] & 0xff];
        case 3:
            c = pem_to_array[decodeBufferByte[2] & 0xff];
        case 2:
            b = pem_to_array[decodeBufferByte[1] & 0xff];
            tempNumber = pem_to_array[decodeBufferByte[0] & 0xff];
            break;
        }

        switch (remTemp) {
        case 2:
            outTempStream.write( (byte)(((tempNumber << 2) & 0xfc) | ((b >>> 4) & 3)) );
            break;
        case 3:
            outTempStream.write( (byte) (((tempNumber << 2) & 0xfc) | ((b >>> 4) & 3)) );
            outTempStream.write( (byte) (((b << 4) & 0xf0) | ((c >>> 2) & 0xf)) );
            break;
        case 4:
            outTempStream.write( (byte) (((tempNumber << 2) & 0xfc) | ((b >>> 4) & 3)) );
            outTempStream.write( (byte) (((b << 4) & 0xf0) | ((c >>> 2) & 0xf)) );
            outTempStream.write( (byte) (((c << 6) & 0xc0) | (d  & 0x3f)) );
            break;
        }
        return;
    }
}
