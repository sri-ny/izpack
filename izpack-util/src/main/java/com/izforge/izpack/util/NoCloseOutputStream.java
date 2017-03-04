package com.izforge.izpack.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoCloseOutputStream extends FilterOutputStream
{
    public NoCloseOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void close() throws IOException
    {
        throw new IOException("Closing this output stream unexpectedly");
    }

    public void doClose() throws IOException {
        super.close();
    }
}
