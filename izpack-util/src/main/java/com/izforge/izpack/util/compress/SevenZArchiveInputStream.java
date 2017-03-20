/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util.compress;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class SevenZArchiveInputStream extends ArchiveInputStream
{
    private final SevenZFile zFile;

    public SevenZArchiveInputStream(File file) throws IOException
    {
        this.zFile = new SevenZFile(file);
    }

    @Override
    public ArchiveEntry getNextEntry() throws IOException
    {
        final SevenZArchiveEntry sevenZArchiveEntry = zFile.getNextEntry();

        return new ArchiveEntry()
        {
            @Override
            public String getName()
            {
                return sevenZArchiveEntry.getName();
            }

            @Override
            public long getSize()
            {
                return sevenZArchiveEntry.getSize();
            }

            @Override
            public boolean isDirectory()
            {
                return sevenZArchiveEntry.isDirectory();
            }

            @Override
            public Date getLastModifiedDate()
            {
                return sevenZArchiveEntry.getLastModifiedDate();
            }
        };
    }

    @Override
    public int read() throws IOException
    {
        return zFile.read();
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return zFile.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return zFile.read(b, off, len);
    }

    @Override
    public void close() throws IOException
    {
        zFile.close();
    }

    @Override
    public String toString()
    {
        return zFile.toString();
    }

    @Override
    public boolean canReadEntryData(ArchiveEntry archiveEntry)
    {
        return (archiveEntry instanceof SevenZArchiveEntry);
    }
}
