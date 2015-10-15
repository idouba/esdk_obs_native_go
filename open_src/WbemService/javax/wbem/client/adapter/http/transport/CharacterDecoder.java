/*
 * 项 目 名:  ISM V100R006C00
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 2010,  All rights reserved.
 * 描    述:  Huawei PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.wbem.client.adapter.http.transport;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 提供一个用于BASE64的辅助类。
 *
 * @author  h90005710
 * @version  ISM V100R006C00,2014-01-24
 * @since  V100R006C00
 */
public abstract class CharacterDecoder {

    /**返回每个原子解码返回的字节数 */
    abstract protected int bytesPerAtom();

    /** 返回每行能够编码的最大字节数*/
    abstract protected int bytesPerLine();

    /** 编码缓冲区的开始处 */
    protected void decodeBufferPrefix(PushbackInputStream tempInStream, OutputStream tempOutStream) throws IOException { }

    /** 编码缓冲前缀*/
    protected void decodeBufferSuffix(PushbackInputStream tempInStream, OutputStream tempOutStream) throws IOException { }

    /**
     * 该方法返回将要编码的字节数
     */
    protected int decodeLinePrefix(PushbackInputStream tempInStream, OutputStream tempOutStream) throws IOException {
        return (bytesPerLine());
    }

    /**
     * 如果没有错误检测或错误码，通常调用该方法
     */
    protected void decodeLineSuffix(PushbackInputStream tempInStream, OutputStream tempOutStream) throws IOException { }

    /**
     * 这个方法做实际的解码。将待解码的字节数写入到输出流当中。
     */
    protected void decodeAtom(PushbackInputStream tempInStream, OutputStream tempOutStream, int l) throws IOException {
        throw new DecodeStreamException();
    }

    /**
     * 读入字节
     */
    protected int readFully(InputStream tempIn, byte byteBuff[], int offset, int len)
        throws java.io.IOException {
        for (int tempNumber = 0; tempNumber < len; tempNumber++) {
            int q = tempIn.read();
            if (q == -1)
                return ((tempNumber == 0) ? -1 : tempNumber);
            byteBuff[tempNumber+offset] = (byte)q;
        }
        return len;
    }

    /**
     * 从输入流读取解码，并将结果写到输出流
     */
    public void decodeBuffer(InputStream tempInStream, OutputStream tempOutStream) throws IOException {
        int     countNumber;
        int     allBytes = 0;

        PushbackInputStream pubshBackInput = new PushbackInputStream (tempInStream);
        decodeBufferPrefix(pubshBackInput, tempOutStream);
        while (true) {
            int lengthTemp;

            try {
                lengthTemp = decodeLinePrefix(pubshBackInput, tempOutStream);
                for (countNumber = 0; (countNumber+bytesPerAtom()) < lengthTemp; countNumber += bytesPerAtom()) {
                    decodeAtom(pubshBackInput, tempOutStream, bytesPerAtom());
                    allBytes += bytesPerAtom();
                }
                if ((countNumber + bytesPerAtom()) == lengthTemp) {
                    decodeAtom(pubshBackInput, tempOutStream, bytesPerAtom());
                    allBytes += bytesPerAtom();
                } else {
                    decodeAtom(pubshBackInput, tempOutStream, lengthTemp - countNumber);
                    allBytes += (lengthTemp - countNumber);
                }
                decodeLineSuffix(pubshBackInput, tempOutStream);
            } catch (DecodeStreamException e) {
                break;
            }
        }
        decodeBufferSuffix(pubshBackInput, tempOutStream);
    }

    /**
     * 可选的解码接口，将字符串解码
     */
    public byte decodeBuffer(String inputTempString)[] throws IOException {
        byte    inputBuffer[] = new byte[inputTempString.length()];
        ByteArrayInputStream inByteStream;
        ByteArrayOutputStream outByteStream;

        inputTempString.getBytes(0, inputTempString.length(), inputBuffer, 0);
        inByteStream = new ByteArrayInputStream(inputBuffer);
        outByteStream = new ByteArrayOutputStream();
        decodeBuffer(inByteStream, outByteStream);
        return (outByteStream.toByteArray());
    }

    /**
     * 将输入流中的内容解码并写入缓冲
     */
    public byte decodeBuffer(InputStream inTemp)[] throws IOException {
        ByteArrayOutputStream outByteStreamTemp = new ByteArrayOutputStream();
        decodeBuffer(inTemp, outByteStreamTemp);
        return (outByteStreamTemp.toByteArray());
    }

    /**
     * 将字符串转换为ByteBuffer
     * 
     */
    public ByteBuffer decodeBufferToByteBuffer(String inputTempStr)
        throws IOException {
        return ByteBuffer.wrap(decodeBuffer(inputTempStr));
    }

    /**
     * 将输入流中的内容转换为ByteBuffer
     */
    public ByteBuffer decodeBufferToByteBuffer(InputStream tempInput)
        throws IOException {
        return ByteBuffer.wrap(decodeBuffer(tempInput));
    }
}
