package com.izforge.izpack.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.izforge.izpack.util.config.SingleConfigurableTask.Entry;
import com.izforge.izpack.util.config.base.Config;
import com.izforge.izpack.util.config.base.Options;
import com.izforge.izpack.util.config.base.spi.OptionsBuilder;

public class SingleConfigurableTaskTest
{
    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

//    @Test
//    public void testPropertiesCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTestTask task = new SingleOptionFileTestTask();
//
//        URL oldFileUrl = getClass().getResource("oldversion.properties");
//        URL newFileUrl = getClass().getResource("newversion.properties");
//        URL expectedFileUrl = getClass().getResource("expected_after_merge.properties");
//        assertNotNull("Old file missing", oldFileUrl);
//        assertNotNull("New file missing", newFileUrl);
//        assertNotNull("Expected result file missing", expectedFileUrl);
//
//        File oldFile = new File(oldFileUrl.getFile());
//        File newFile = new File(newFileUrl.getFile());
//        File expectedFile = new File(expectedFileUrl.getFile());
//        File toFile = tmpDir.newFile("to.properties");
//
//        task.setToFile(toFile);
//        task.setOldFile(oldFile);
//        task.setNewFile(newFile);
//        task.setCleanup(false);
//        task.setCreate(true);
//        task.setPatchPreserveEntries(false);
//        task.setPatchPreserveValues(true);
//        task.setPatchResolveVariables(false);
//        task.setOperator("=");
//
//        try
//        {
//            task.execute();
//        }
//        catch (Exception e)
//        {
//            fail("Task could not be executed: " + e.getMessage());
//        }
//
//        printFileContent(toFile);
//        printFileContent(expectedFile);
//        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
//    }
//
//    @Test
//    public void testWrapperCommentsAtEnd() throws IOException
//    {
//        SingleOptionFileTestTask task = new SingleOptionFileTestTask();
//
//        URL oldFileUrl = getClass().getResource("oldversion.wrapper.conf");
//        URL newFileUrl = getClass().getResource("newversion.wrapper.conf");
//        URL expectedFileUrl = getClass().getResource("expected_after_merge.wrapper.conf");
//        assertNotNull("Old file missing", oldFileUrl);
//        assertNotNull("New file missing", newFileUrl);
//        assertNotNull("Expected result file missing", expectedFileUrl);
//
//        File oldFile = new File(oldFileUrl.getFile());
//        File newFile = new File(newFileUrl.getFile());
//        File expectedFile = new File(expectedFileUrl.getFile());
//        File toFile = tmpDir.newFile("to.wrapper.conf");
//
//        task.setToFile(toFile);
//        task.setOldFile(oldFile);
//        task.setNewFile(newFile);
//        task.setCleanup(false);
//        task.setCreate(true);
//        task.setPatchPreserveEntries(false);
//        task.setPatchPreserveValues(true);
//        task.setPatchResolveVariables(false);
//        task.setOperator("=");
//        task.setEncoding(Charset.forName("UTF-8"));
//        task.setHeaderComment(true);
//
//        try
//        {
//            task.execute();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//            fail("Task could not be executed: " + e.getMessage());
//        }
//
//        printFileContent(toFile);
//        printFileContent(expectedFile);
//        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "UTF-8"), true);
//    }

    @Test
    public void testAutoNumberingPatchPreserveEntries() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = OptionsBuilder.newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");

        Options toOptions = new Options(config);
        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(false);
        task.execute();
        Options result = task.getResult();
        Assert.assertTrue(result.keySet().contains("abc.xyz0."));
        assertEquals( 3, result.length("abc.xyz0.") );
        assertEquals("value0", result.get("abc.xyz0.", 0));
        assertEquals("value1", result.get("abc.xyz0.", 1));
        assertEquals("value2", result.get("abc.xyz0.", 2));
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndMoreValues() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = OptionsBuilder.newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = OptionsBuilder.newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");
        ob2.handleOption("abc.xyz0.2", "value5");
        ob2.handleOption("abc.xyz0.3", "value6");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();
        Assert.assertTrue(result.keySet().contains("abc.xyz0."));
        assertEquals(4, result.length("abc.xyz0.") );
        assertEquals("value3", result.get("abc.xyz0.", 0));
        assertEquals("value4", result.get("abc.xyz0.", 1));
        assertEquals("value5", result.get("abc.xyz0.", 2));
        assertEquals("value6", result.get("abc.xyz0.", 3));
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndLessValues() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = OptionsBuilder.newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");
        ob.handleOption("abc.xyz0.2", "value2");
        ob.handleOption("abc.xyz0.3", "value3");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = OptionsBuilder.newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");
        ob2.handleOption("abc.xyz0.2", "value5");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        task.execute();
        Options result = task.getResult();
        Assert.assertTrue(result.keySet().contains("abc.xyz0."));
        assertEquals( 4, result.length("abc.xyz0.") );
        assertEquals("value3", result.get("abc.xyz0.", 0));
        assertEquals("value4", result.get("abc.xyz0.", 1));
        assertEquals("value5", result.get("abc.xyz0.", 2));
    }

    @Test
    public void testAutoNumberingPatchPreserveEntriesAndValuesWithOverride() throws Exception
    {
        Config config = new Config();
        config.setAutoNumbering(true);
        Options fromOptions = new Options(config);
        OptionsBuilder ob = OptionsBuilder.newInstance(fromOptions);
        ob.handleOption("abc.xyz0.0", "value0");
        ob.handleOption("abc.xyz0.1", "value1");

        Options toOptions = new Options(config);
        OptionsBuilder ob2 = OptionsBuilder.newInstance(fromOptions);
        ob2.handleOption("abc.xyz0.0", "value3");
        ob2.handleOption("abc.xyz0.1", "value4");

        SingleOptionTestTask task = new SingleOptionTestTask(fromOptions, toOptions);
        task.setAutoNumbering(true);
        task.setPatchPreserveEntries(true);
        task.setPatchPreserveValues(true);
        Entry entry = new Entry();
        entry.setKey("abc.xyz0.0");
        entry.setValue("value0_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz0.1");
        entry.setValue("value1_overridden");
        task.addEntry(entry);
        entry = new Entry();
        entry.setKey("abc.xyz.unnumbered");
        entry.setValue("value_unnumbered_overridden");
        task.addEntry(entry);
        task.execute();
        Options result = task.getResult();
        Assert.assertTrue(result.keySet().contains("abc.xyz0."));
        assertEquals( 2, result.length("abc.xyz0.") );
        Assert.assertNull(result.get("abc.xyz0.0"));
        Assert.assertNull(result.get("abc.xyz0.1"));
        Assert.assertNotNull(result.get("abc.xyz0.", 0));
        Assert.assertNotNull(result.get("abc.xyz0.", 1));
        Assert.assertNotNull(result.get("abc.xyz.unnumbered"));
        assertEquals("value0_overridden", result.get("abc.xyz0.", 0));
        assertEquals("value1_overridden", result.get("abc.xyz0.", 1));
        assertEquals("value_unnumbered_overridden", result.get("abc.xyz.unnumbered"));
    }


    @Test
    public void testIniCommentsAtEnd() throws IOException, URISyntaxException
    {
        SingleIniFileTask task = new SingleIniFileTask();

        URL oldFileUrl = getClass().getResource("oldversion.ini");
        URL newFileUrl = getClass().getResource("newversion.ini");
        URL expectedFileUrl = getClass().getResource("expected_after_merge.ini");
        assertNotNull("Old file missing", oldFileUrl);
        assertNotNull("New file missing", newFileUrl);
        assertNotNull("Expected result file missing", expectedFileUrl);

        File oldFile = new File(oldFileUrl.toURI());
        File newFile = new File(newFileUrl.toURI());
        File expectedFile = new File(expectedFileUrl.toURI());
        File toFile = tmpDir.newFile("to.ini");

        task.setToFile(toFile);
        task.setOldFile(oldFile);
        task.setNewFile(newFile);
        task.setCleanup(false);
        task.setCreate(true);
        task.setPatchPreserveEntries(false);
        task.setPatchPreserveValues(true);
        task.setPatchResolveVariables(false);

        try
        {
            task.execute();
        }
        catch (Exception e)
        {
            fail("Task could not be executed: " + e.getMessage());
        }

        printFileContent(toFile);
        printFileContent(expectedFile);
        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
    }

    @Test
    public void testIniCommentsAtEndWithSpaceInPath() throws IOException, URISyntaxException
    {
        SingleIniFileTask task = new SingleIniFileTask();

        URL oldFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/oldversion2.ini");
        URL newFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/newversion2.ini");
        URL expectedFileUrl = getClass().getResource("/com/izforge/izpack/util/config with space/expected_after_merge2.ini");
        assertNotNull("Old file missing", oldFileUrl);
        assertNotNull("New file missing", newFileUrl);
        assertNotNull("Expected result file missing", expectedFileUrl);

        System.out.println("The oldFileUrl: "+ oldFileUrl.toURI().toString());

        File oldFile = new File(oldFileUrl.toURI());
        File newFile = new File(newFileUrl.toURI());
        File expectedFile = new File(expectedFileUrl.toURI());
        File toFile = tmpDir.newFile("to.ini");

        task.setToFile(toFile);
        task.setOldFile(oldFile);
        task.setNewFile(newFile);
        task.setCleanup(false);
        task.setCreate(true);
        task.setPatchPreserveEntries(false);
        task.setPatchPreserveValues(true);
        task.setPatchResolveVariables(false);

        try
        {
            task.execute();
        }
        catch (Exception e)
        {
            fail("Task could not be executed: " + e.getMessage());
        }

        printFileContent(toFile);
        printFileContent(expectedFile);
        assertEquals(FileUtils.contentEqualsIgnoreEOL(expectedFile, toFile, "ISO-8859-1"), true);
    }

    private void printFileContent(File file)
    {
        BufferedReader br;

        System.out.println();
        System.out.println("+++ " + file + " +++");
        try
        {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
