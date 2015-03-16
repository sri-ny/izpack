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

package com.izforge.izpack.panels.userinput.console.combo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.izforge.izpack.panels.userinput.console.AbstractConsoleFieldTest;
import com.izforge.izpack.panels.userinput.field.Choice;
import com.izforge.izpack.panels.userinput.field.choice.TestChoiceFieldConfig;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;


/**
 * Tests the {@link ConsoleComboField}.
 *
 * @author Tim Anderson
 */
public class ConsoleComboFieldTest extends AbstractConsoleFieldTest
{

    /**
     * The choices.
     */
    private List<Choice> choices;

    /**
     * Default constructor.
     */
    public ConsoleComboFieldTest()
    {
    	super();
    	
        choices = Arrays.asList(new Choice("A", "A String"), new Choice("B", "B String"), new Choice("C", "C String"));
    }

    /**
     * Tests selection of the default value.
     */
    @Test
    public void testSelectDefaultValue()
    {
        String variable = "combo";
        ComboField model = new ComboField(new TestChoiceFieldConfig<Choice>(variable, choices, 1), installData);
        ConsoleComboField field = new ConsoleComboField(model, console, prompt);

        console.addScript("Select default", "\n");
        assertTrue(field.display());

        assertEquals("B", installData.getVariable(variable));
    }

    /**
     * Tests selection of a choice.
     */
    @Test
    public void testSelect()
    {
        String variable = "combo";
        ComboField model = new ComboField(new TestChoiceFieldConfig<Choice>(variable, choices, 1), installData);
        ConsoleComboField field = new ConsoleComboField(model, console, prompt);

        console.addScript("Select C", "2");
        assertTrue(field.display());

        assertEquals("C", installData.getVariable(variable));
    }

}
