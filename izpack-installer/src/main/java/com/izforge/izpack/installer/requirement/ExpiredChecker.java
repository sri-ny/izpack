/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Bill Root
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

package com.izforge.izpack.installer.requirement;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.installer.RequirementChecker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Verifies that the installer has not expired.
 *
 * @author Bill Root
 */
public class ExpiredChecker implements RequirementChecker
{
    /**
     * The name of the variable holding the expiration date.
     */
    public static final String EXPIRE_DATE_VAR_NAME = "InstallerExpiresDate";

    /**
     * The installation data.
     */
    private final InstallData installData;

    /**
     * The prompt.
     */
    private final Prompt prompt;

    /**
     * Constructs a <tt>ExpiredChecker</tt>.
     *
     * @param installData the installation data
     * @param prompt      the prompt
     */
    public ExpiredChecker(InstallData installData, Prompt prompt)
    {
        this.installData = installData;
        this.prompt = prompt;
    }

    /**
     * Determines whether the installer expires, and if so, whether it has.
     *
     * @return <tt>true</tt> if installer has NOT expired, otherwise <tt>false</tt>
     */
    @Override
    public boolean check()
    {
      if (!expires())
          return true;

      try
      {
        if (expired())
        {
          showExpired();
          return false;
        }
        else
          return true;
      }
      catch (ParseException ex)
      {
        prompt.error(String.format(
                "Could not parse %s.  Please correct the installer.",
                EXPIRE_DATE_VAR_NAME));
        // we return true so user can workaround installer problem
        return true;
      }
      catch (Exception ex)
      {
        prompt.error(String.format(
                "Could not check expiration date because: %s.  Please correct the installer.",
                ex.toString(),
                EXPIRE_DATE_VAR_NAME));
        // we return true so user can workaround installer problem
        return true;
      }
    }

    private boolean expired() throws ParseException
    {
        String expirationDateStr = installData.getVariable(EXPIRE_DATE_VAR_NAME);
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD");
        Date expirationDate = dateFormat.parse(expirationDateStr);
        return new Date().after(expirationDate);
    }
    
    /**
     * Determines whether the installer expires.
     *
     * @return <tt>true</tt> if installer expires, otherwise <tt>false</tt>
     */
    private boolean expires()
    {
        String expirationDateStr = installData.getVariable(EXPIRE_DATE_VAR_NAME);
        return (expirationDateStr != null) && !expirationDateStr.isEmpty();
    }
    
    /**
     * Invoked when the installer has expired.
     * <p/>
     * This tells the user why we're canceling.
     */
    protected void showExpired()
    {
        String message = "This installer has expired.";
        if (installData.getInfo().getAppURL() != null)
        {
            String urlText = installData.getInfo().getAppURL();
            message += "\n\n" +
                        "Please download a new one from " + urlText;
        }
        prompt.error(message);
    }
}