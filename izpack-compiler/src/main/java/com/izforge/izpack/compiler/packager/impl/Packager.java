/*
 * $Id$
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.packager.impl;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.PackInfo;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.listener.PackagerListener;
import com.izforge.izpack.compiler.merge.CompilerPathResolver;
import com.izforge.izpack.merge.MergeManager;
import com.izforge.izpack.merge.resolve.MergeableResolver;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

/**
 * The packager class. The packager is used by the compiler to put files into an
 * installer, and create the actual installer files.
 *
 * @author Julien Ponge
 * @author Chadwick McHenry
 */
public class Packager extends PackagerBase
{
    private static final Logger logger = Logger.getLogger(Packager.class.getName());

    private final CompilerData compilerData;

    /**
     * Constructs a <tt>Packager</tt>.
     *
     * @param properties        the properties
     * @param listener          the packager listener
     * @param jarOutputStream   the installer jar output stream
     * @param mergeManager      the merge manager
     * @param pathResolver      the path resolver
     * @param mergeableResolver the mergeable resolver
     * @param compilerData      the compiler data
     */
    public Packager(Properties properties, PackagerListener listener, JarOutputStream jarOutputStream,
                    MergeManager mergeManager, CompilerPathResolver pathResolver, MergeableResolver mergeableResolver,
                    CompilerData compilerData, RulesEngine rulesEngine)
    {
        super(properties, listener, jarOutputStream, mergeManager, pathResolver, mergeableResolver,
                compilerData, rulesEngine);
        this.compilerData = compilerData;
    }

    private JarOutputStream getJarOutputStream(File jarFile) throws IOException
    {
        JarOutputStream jarOutputStream = null;
        FileOutputStream fileOutputStream = null;
        FileUtils.deleteQuietly(jarFile);
        if (compilerData.isMkdirs())
        {
            FileUtils.forceMkdirParent(jarFile);
        }
        try
        {
            fileOutputStream = new FileOutputStream(jarFile);
            jarOutputStream = new JarOutputStream(fileOutputStream);
            int level = compilerData.getComprLevel();
            if (level >= 0 && level < 10)
            {
                jarOutputStream.setLevel(level);
            } else
            {
                jarOutputStream.setLevel(Deflater.BEST_COMPRESSION);
            }
        }
        finally
        {
            IOUtils.closeQuietly(jarOutputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }

        return jarOutputStream;
    }

    /**
     * Write packs to the installer jar, or each to a separate jar.
     *
     * @throws IOException for any I/O error
     */
    @Override
    protected void writePacks() throws IOException
    {
        List<PackInfo> packs = getPacksList();
        final int num = packs.size();
        sendMsg("Writing " + num + " Pack" + (num > 1 ? "s" : "") + " into installer");

        // Map to remember pack number and bytes offsets of back references
        Map<File, Object[]> storedFiles = new HashMap<File, Object[]>();

        List<PackFile> pack200Files = new ArrayList<PackFile>();

        // Force UTF-8 encoding in order to have proper ZipEntry names.
        JarOutputStream installerJar = getInstallerJar();

        int packNumber = 0;
        IXMLElement root = new XMLElementImpl("packs");

        for (PackInfo packInfo : packs)
        {
            Pack pack = packInfo.getPack();
            pack.setFileSize(0);

            sendMsg("Writing Pack " + packNumber + ": " + pack.getName(), PackagerListener.MSG_VERBOSE);

            ZipEntry entry;
            JarOutputStream packJar = installerJar;
            if (packSeparateJars())
            {
                // TODO REFACTOR : Use a mergeManager for each packages that will be added to the main merger
                String jarFile = getInfo().getInstallerBase() + ".pack-" + pack.getName() + ".jar";
                packJar = getJarOutputStream(new File(jarFile));
                entry = new ZipEntry("packs/pack-" + pack.getName());
            } else
            {
                entry = new ZipEntry(RESOURCES_PATH + "packs/pack-" + pack.getName());
            }

            packJar.putNextEntry(entry);
            packJar.flush(); // flush before we start counting

            CountingOutputStream dos = new CountingOutputStream(packJar);
            ObjectOutputStream objOut = new ObjectOutputStream(dos);

            for (PackFile packFile : packInfo.getPackFiles())
            {
                boolean addFile = !pack.isLoose();
                File file = packInfo.getFile(packFile);

                boolean pack200 = packFile.isPack200Jar();

                // use a back reference if file was in previous pack, and in
                // same jar
                Object[] info = storedFiles.get(file);
                if (info != null && !packSeparateJars())
                {
                    packFile.setPreviousPackFileRef((String) info[0], (Long) info[1]);
                    addFile = false;
                }

                if (addFile && !packFile.isDirectory())
                {
                    long pos = dos.getByteCount(); // get the position

                    if (pack200)
                    {
                        /*
                         * Warning!
						 *
						 * Pack200 archives must be stored in separated streams,
						 * as the Pack200 unpacker reads the entire stream...
						 *
						 * See http://java.sun.com/javase/6/docs/api/java/util/jar/Pack200.Unpacker.html
						 */
                        pack200Files.add(packFile);
                    }
                    else
                    {
                        PackCompression comprFormat = getInfo().getCompressionFormat();
                        OutputStream finalStream;
                        CountingOutputStream proxyOutputStream =  new CountingOutputStream(new CloseShieldOutputStream(objOut));
                        if (comprFormat != PackCompression.DEFAULT)
                        {
                            switch (comprFormat)
                            {
                                case LZMA:
                                    // LZMA as output stream supported from commons-compress 1.13 (requires JDK 1.7)
                                    // for now create it from the Tukaani Project (tukaani.org)
                                    finalStream = new LZMAOutputStream(proxyOutputStream, new LZMA2Options(), -1);
                                    break;
                                case DEFLATE:
                                    DeflateParameters deflateParameters = new DeflateParameters();
                                    deflateParameters.setCompressionLevel(Deflater.BEST_COMPRESSION);
                                    new DeflateCompressorOutputStream(proxyOutputStream, deflateParameters);
                                default:
                                    try
                                    {
                                        finalStream = new CompressorStreamFactory().createCompressorOutputStream(
                                                comprFormat.toName(),
                                                proxyOutputStream);
                                    }
                                    catch (CompressorException e)
                                    {
                                        throw new IOException(e);
                                    }
                            }

                            try
                            {
                                long bytesWritten = FileUtils.copyFile(file, finalStream);
                                proxyOutputStream.flush();
                                finalStream.close();
                                if (bytesWritten != packFile.length())
                                {
                                    throw new IOException("File size mismatch when reading " + file);
                                }
                                packFile.setSize(proxyOutputStream.getByteCount());
                                logger.fine("File " + packFile.getTargetPath() + " compressed from "
                                            + packFile.length() + " to " + packFile.size());
                            }
                            finally
                            {
                                IOUtils.closeQuietly(finalStream);
                                IOUtils.closeQuietly(proxyOutputStream);
                            }
                        }
                        else
                        {
                            long bytesWritten = FileUtils.copyFile(file, objOut);
                            if (bytesWritten != packFile.length())
                            {
                                throw new IOException("File size mismatch when reading " + file);
                            }
                        }
                    }

                    // see IZPACK-799
                    storedFiles.put(file, new Object[]{pack.getName(), pos}); // TODO
                }

                // even if not written, it counts towards pack size
                pack.addFileSize(packFile.length());
            }

            if (pack.getFileSize() > pack.getSize())
            {
                pack.setSize(pack.getFileSize());
            }

            // Cleanup
            objOut.flush();
            packJar.closeEntry();

            // close pack specific jar if required
            if (packSeparateJars())
            {
                packJar.close();
            }

            IXMLElement child = new XMLElementImpl("pack", root);
            child.setAttribute("name", pack.getName());
            child.setAttribute("size", Long.toString(pack.getSize()));
            child.setAttribute("fileSize", Long.toString(pack.getFileSize()));
            if (pack.getLangPackId() != null)
            {
                child.setAttribute("id", pack.getLangPackId());
            }
            root.addChild(child);

            packNumber++;
        }

        // Now that we know sizes, write pack metadata to primary jar.
        installerJar.putNextEntry(new ZipEntry(PACKSINFO_RESOURCE_PATH));
        ObjectOutputStream out = new ObjectOutputStream(installerJar);
        out.writeObject(packs);
        out.flush();
        installerJar.closeEntry();

        for (PackFile pack200PackFile : pack200Files)
        {
            Pack200.Packer packer = createPack200Packer(pack200PackFile);
            installerJar.putNextEntry(new ZipEntry(RESOURCES_PATH + "packs/pack200-" + pack200PackFile.getId()));
            JarFile jar = new JarFile(pack200PackFile.getFile());
            try
            {
                packer.pack(jar, installerJar);
            }
            finally
            {
                jar.close();
                installerJar.closeEntry();
            }
        }
    }

    private Pack200.Packer createPack200Packer(PackFile packFile)
    {
        Pack200.Packer packer = Pack200.newPacker();
        Map<String, String> defaultPackerProperties = packer.properties();
        Map<String,String> localPackerProperties = packFile.getPack200Properties();
        if (localPackerProperties != null)
        {
            defaultPackerProperties.putAll(localPackerProperties);
        }
        return packer;
    }

    @Override
    public void addConfigurationInformation(IXMLElement data) {}
}
