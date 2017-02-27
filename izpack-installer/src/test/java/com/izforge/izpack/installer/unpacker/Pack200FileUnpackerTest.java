/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2012 Tim Anderson
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

package com.izforge.izpack.installer.unpacker;

import com.izforge.izpack.api.data.Blockable;
import com.izforge.izpack.api.data.OverrideType;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.os.FileQueue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.mockito.Mockito;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.*;
import java.util.zip.ZipEntry;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link Pack200FileUnpacker} class.
 *
 * @author Tim Anderson
 */
public class Pack200FileUnpackerTest extends AbstractFileUnpackerTest
{
    private PackFile packFile;
    private File sourceFile;

    /**
     * Verifies the target matches the source.
     *
     * @param source the source
     * @param target the target
     */
    @Override
    protected void checkTarget(File source, File target) throws IOException
    {
        assertTrue(target.exists());
        assertEquals(source.lastModified(), target.lastModified());

        // for pack200 can't do a size comparison as it modifies the jar structure, so compare the jar contents
        byte[] sourceBytes = getEntry(source, "source.txt");
        byte[] targetBytes = getEntry(target, "source.txt");
        assertArrayEquals(sourceBytes, targetBytes);
    }

    /**
     * Helper to create an unpacker.
     *
     * @param sourceDir the source directory
     * @param queue the file queue. May be {@code null}
     * @return a new unpacker
     */
    @Override
    protected FileUnpacker createUnpacker(File sourceDir, FileQueue queue) throws IOException
    {
        final String resourceName = "packs/pack200-" + this.packFile.getId();
        final Map<String, String> packerProperties = this.packFile.getPack200Properties();

        Pack200.Packer packer = Pack200.newPacker();
        if (!packerProperties.isEmpty())
        {
            packer.properties().putAll(packerProperties);
        }
        JarOutputStream installerJar = null;
        CountingOutputStream countingInstallerJarStream = null;
        JarFile jar = null;
        try
        {
            installerJar = new JarOutputStream(
                    FileUtils.openOutputStream(new File(sourceDir, "installer.jar")));
            installerJar.putNextEntry(new ZipEntry(resourceName));
            jar = new JarFile(this.sourceFile);
            countingInstallerJarStream = new CountingOutputStream(installerJar);
            packer.pack(jar, countingInstallerJarStream);
            this.packFile.setSize(countingInstallerJarStream.getByteCount());
        }
        finally
        {
            if (jar != null)
            {
                jar.close();
            }
            if (installerJar != null)
            {
                installerJar.closeEntry();
            }
            IOUtils.closeQuietly(countingInstallerJarStream);
            IOUtils.closeQuietly(installerJar);
        }

        PackResources resources = Mockito.mock(PackResources.class);
        JarInputStream stream = new JarInputStream(new FileInputStream(new File(sourceDir, "installer.jar")));
        JarEntry entry;
        while ((entry = stream.getNextJarEntry()) != null && !entry.getName().endsWith(resourceName));
        when(resources.getInputStream(resourceName)).thenReturn(stream);

        return new Pack200FileUnpacker(getCancellable(), resources, queue);
    }

    /**
     * Creates a new source file.
     *
     * @param baseDir the base directory
     * @return the source file
     * @throws IOException for any I/O error
     */
    @Override
    protected File createSourceFile(File baseDir) throws IOException
    {
        File source = super.createSourceFile(baseDir);

        this.sourceFile = new File(baseDir, "source.jar");
        JarOutputStream srcJar = new JarOutputStream(new FileOutputStream(this.sourceFile));
        FileInputStream stream = new FileInputStream(source);
        try
        {
            IoHelper.copyStreamToJar(stream, srcJar, source.getName(), source.lastModified());
        }
        finally
        {
            IOUtils.closeQuietly(stream);
            IOUtils.closeQuietly(srcJar);
        }

        return this.sourceFile;
    }

    /**
     * Returns the target file.
     *
     * @param baseDir the base directory
     * @return the target file
     */
    protected File getTargetFile(File baseDir)
    {
        return new File(baseDir, "target.jar");
    }

    /**
     * Returns a file from a jar as a byte array.
     *
     * @param file the jar file
     * @param name the entry name
     * @return the file content
     * @throws IOException for any I/O error
     */
    private byte[] getEntry(File file, String name) throws IOException
    {
        JarInputStream stream = new JarInputStream(new FileInputStream(file));
        try
        {
            JarEntry entry;
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            while ((entry = stream.getNextJarEntry()) != null)
            {
                if (entry.getName().endsWith(name))
                {
                    IOUtils.copy(stream, bytes);
                    return bytes.toByteArray();
                }
            }
            fail("Entry not found: " + name);
        }
        finally
        {
            stream.close();
        }
        return null;
    }

    @Override
    protected PackFile createPackFile(File baseDir, File source, File target, Blockable blockable) throws IOException
    {
        PackFile packFile = new PackFile(baseDir, source, target.getName(), null, OverrideType.OVERRIDE_TRUE,
                        null, blockable, new HashMap<String, String>());
        return (this.packFile = packFile);
    }
}
