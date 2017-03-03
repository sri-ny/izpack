package com.izforge.izpack.util;

import java.io.*;

public class NoCloseInputStream extends FilterInputStream
{
    public NoCloseInputStream(InputStream in) {
        super(in);
    }

    @Override
    public void close() throws IOException
    {
        throw new IOException("Closing this input stream unexpectedly");
    }

    public void doClose() throws IOException {
        super.close();
    }
}
