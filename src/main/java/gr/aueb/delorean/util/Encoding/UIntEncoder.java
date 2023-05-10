package gr.aueb.delorean.util.Encoding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class UIntEncoder {
    public static void write(long number, ByteArrayOutputStream outputStream) throws IOException {
        if (number > Math.pow(2, 8 * 4) - 1 || number < 0)
            throw new UnsupportedOperationException("Can't save number " + number + " as unsigned Int");
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt((int) (number & 0xffffffffL));
        outputStream.write(buffer.array());
    }

    public static long read(ByteArrayInputStream inputStream) throws IOException {
        byte[] byteArray = new byte[Integer.BYTES];
        int k = inputStream.read(byteArray);
        if (k != Integer.BYTES)
            throw new IOException();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(byteArray);
        buffer.flip();

        return buffer.getInt() & 0xffffffffL;
    }

    public static void writeWithFlag(int number, ByteArrayOutputStream outputStream) throws IOException {
        if (number < Math.pow(2, 8) - 1) {
            UByteEncoder.write((short) number, outputStream);
        } else {
            UByteEncoder.write((short) (Math.pow(2, 8) - 1), outputStream);
            write(number, outputStream);
        }
    }

    public static long readWithFlag(ByteArrayInputStream inputStream) throws IOException {
        long number = UByteEncoder.read(inputStream);
        if (number == Math.pow(2, 8) - 1)
            number = read(inputStream);

        return number;
    }
}
