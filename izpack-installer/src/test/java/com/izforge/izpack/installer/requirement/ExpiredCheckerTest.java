/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2015 Bill Root
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

import java.text.SimpleDateFormat;
import java.util.Date;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Tests the {@link ExpiredChecker} class.
 *
 * @author Bill Root
 */
public class ExpiredCheckerTest extends AbstractRequirementCheckerTest
{
  /**
   * CONSTANTS
   */
  private final long DAY_MILLISECONDS = 24 * 60 * 60 * 1000;
  private final long YEAR_MILLISECONDS = 365 * DAY_MILLISECONDS;

  
  /**
   * DATA
   */
  ExpiredChecker checker = new ExpiredChecker(installData, prompt);
  SimpleDateFormat dateFormat = new SimpleDateFormat(ExpiredChecker.EXPIRE_DATE_FORMAT);
  
  
  /**
   * METHODS
   */
  
  /**
   * Tests the {@link ExpiredChecker} when the installer has NOT expired.
   */
  @Test
  public void testNotExpired()
  {
    assertNotNull("This class requires variables", installData.getVariables());
    
    // no expiration date set
    assertTrue(checker.check());

    // expires tomorrow
    String tomorrow = dateFormat.format(new Date(new Date().getTime() + DAY_MILLISECONDS));
    installData.setVariable(ExpiredChecker.EXPIRE_DATE_VAR_NAME, tomorrow);
    assertTrue(checker.check());
    
    // expires a year from now
    String nextYear = dateFormat.format(new Date(new Date().getTime() + YEAR_MILLISECONDS));
    installData.setVariable(ExpiredChecker.EXPIRE_DATE_VAR_NAME, nextYear);
    assertTrue(checker.check());
  }

  
  /**
   * Tests the {@link ExpiredChecker} when the installer has expired.
   */
  @Test
  public void testExpired()
  {
    assertNotNull("This class requires variables", installData.getVariables());
    
    // expires today
    String today = dateFormat.format(new Date());
    installData.setVariable(ExpiredChecker.EXPIRE_DATE_VAR_NAME, today);
    assertFalse(checker.check());
    
    // expired yesterday
    String yesterday = dateFormat.format(new Date(new Date().getTime() - DAY_MILLISECONDS));
    installData.setVariable(ExpiredChecker.EXPIRE_DATE_VAR_NAME, yesterday);
    assertFalse(checker.check());
    
    // expired a year ago
    String lastYear = dateFormat.format(new Date(new Date().getTime() - YEAR_MILLISECONDS));
    installData.setVariable(ExpiredChecker.EXPIRE_DATE_VAR_NAME, lastYear);
    assertFalse(checker.check());
  }

}
