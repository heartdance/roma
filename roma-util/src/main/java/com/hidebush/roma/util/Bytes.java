package com.hidebush.roma.util;

/**
 * Created by htf on 2020/9/27.
 */
public class Bytes {

    public static int toInt(byte[] bytes) {
        return toInt(bytes, 0, bytes.length);
    }

    public static int toInt(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = result << 8 | bytes[offset + i] & 0xFF;
        }
        return result;
    }

    public static String toHex(byte[] bytes) {
        return toHex(bytes, 0, bytes.length);
    }

    private static final char[] hexDict = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};
    public static String toHex(byte[] bytes, int offset, int length) {
        char[] hex = new char[length * 2];
        for (int i = 0; i < length; i++) {
            hex[i * 2] = hexDict[bytes[offset + i] >>> 4 & 0x0F];
            hex[i * 2 + 1] = hexDict[bytes[offset + i] & 0x0F];
        }
        return new String(hex);
    }

    public static byte[] toBytes(int num, int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[length - i - 1] = (byte) ((num >>> (i * 8)) & 0xFF);
        }
        return bytes;
    }

    public static byte[] merge(byte[]... bytes) {
        int mergeLength = 0;
        for (byte[] item : bytes) {
            mergeLength += item.length;
        }
        byte[] merge = new byte[mergeLength];
        int offset = 0;
        for (byte[] item : bytes) {
            System.arraycopy(item, 0, merge, offset, item.length);
            offset += item.length;
        }
        return merge;
    }
}
