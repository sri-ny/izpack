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

package com.izforge.izpack.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class WordUtil
{
    private static final String NEWLINE = System.getProperty("line.separator");

    public static void main(String[] args) throws FileNotFoundException
    {
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream("/home/rkrell/Downloads/pentaho-eula-wrap-config-1.0.7-eula.txt"));
            for (String line : WordUtil.wordWrap(in, 80))
            {
                System.out.println(line);
            }
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (IOException e) {}
        }
    }

    public static String wordWrap(String text, int maxCharsPerLine)
    {
        StringBuffer sb = new StringBuffer();
        for (String line : wordWrap(new ByteArrayInputStream(text.getBytes()), maxCharsPerLine))
        {
            sb.append(line + NEWLINE);
        }
        return sb.toString();
    }

    public static List<String> wordWrap(InputStream in, int maxCharsPerLine)
    {
        List<String> lines = new ArrayList<String>();
        Scanner scanner = new Scanner(in);

        while (scanner.hasNextLine())
        {
            String readLine = scanner.nextLine();
            StringBuffer sb = new StringBuffer();

            StringTokenizer tokenizer = new StringTokenizer(readLine);
            // Add explicit line breaks from original document
            if (tokenizer.countTokens() == 0)
            {
                lines.add(new String());
            }

            while (tokenizer.hasMoreTokens())
            {
                String word = tokenizer.nextToken();
                int len = word.length();

                // FIXME Add explicit trailing whitespace from original document
                /*
                if (len == 0)
                {
                    word = " ";
                    len = 1;
                }
                */

                if (sb.length() > 0)
                {
                    if (len + 1 > maxCharsPerLine - sb.length())
                    {
                        lines.add(sb.toString());
                        sb = new StringBuffer();
                    }
                    else
                    {
                        if (len < maxCharsPerLine)
                        {
                            sb.append(' ');
                        }
                    }
                }
                sb.append(word);
            }
            if (sb.length() > 0)
            {
                lines.add(sb.toString());
            }
        }

        return lines;
    }
}
