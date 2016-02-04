/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
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

package com.izforge.izpack.panels.packs;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.api.handler.Prompt;
import com.izforge.izpack.api.handler.Prompt.Option;
import com.izforge.izpack.api.handler.Prompt.Options;
import com.izforge.izpack.api.handler.Prompt.Type;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.AbstractConsolePanel;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.installer.util.PackHelper;
import com.izforge.izpack.util.Console;

/**
 * Console implementation for the TreePacksPanel.
 * <p/>
 * Based on PacksConsolePanelHelper
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class PacksConsolePanel extends AbstractConsolePanel implements ConsolePanel
{

    private Messages messages;
    private HashMap<String, Pack> names;
    private List<Pack> selectedPacks;

    private final Prompt prompt;
    private final InstallData installData;

    public PacksConsolePanel(PanelView<ConsolePanel> panel, InstallData installData, Prompt prompt)
    {
        super(panel);
        this.prompt = prompt;
        this.installData = installData;

        //load the packs lang messages if exists
        try
        {
            messages = installData.getMessages().newMessages(Resources.PACK_TRANSLATIONS_RESOURCE_NAME);
        }
        catch (ResourceNotFoundException exception)
        {
            // no packs messages resource, so fall back to the default
            messages = installData.getMessages();
        }

    }

    /**
     * Runs the panel using the supplied properties.
     *
     * @param installData the installation data
     * @param properties  the properties
     * @return <tt>true</tt> if the installation is successful, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Properties properties)
    {
        return true;
    }

    /**
     * Runs the panel using the specified console.
     *
     * @param installData the installation data
     * @param console     the console
     * @return <tt>true</tt> if the panel ran successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean run(InstallData installData, Console console)
    {
        super.run(installData, console);

        out(Type.INFORMATION, installData.getMessages().get("PacksPanel.info"));
        out(Type.INFORMATION, "");

        List<Pack> availablePacks = installData.getAvailablePacks();
        computePacks(availablePacks);
        if (selectedPacks == null)
        {
            selectedPacks = new LinkedList<Pack>();
            for (Pack pack : availablePacks)
            {
                if (!pack.isHidden())
                {
                    // Initially, add all available packs by default
                    selectedPacks.add(pack);
                }
            }
        }

        // private HashMap<String, Pack> names;
        for (String key : names.keySet())
        {
            drawHelper(key, selectedPacks, installData);
        }
        out(Type.INFORMATION, "Done!");

        installData.setSelectedPacks(selectedPacks);

        if (selectedPacks.size() == 0)
        {
            out(Type.WARNING, "You have not selected any packs!");
            return promptRerunPanel(installData, console);
        }
        return promptEndPanel(installData, console);
    }

    private void out(Type type, String message)
    {
        prompt.message(type, message);
    }


    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param pack          - the pack to install
     * @param selectedPacks - the packs that are selected by the user are added there
     * @param installData   - Database of izpack
     */
    private void drawHelper(final String pack, final List<Pack> selectedPacks, final InstallData installData)
    {
        Pack p = names.get(pack);
        Boolean conditionSatisfied = checkCondition(installData, p);

        //get the pack localized name
        String packName = PackHelper.getPackName(p, messages);

        final boolean required = p.isRequired();
        final boolean packConditionTrue = conditionSatisfied == null || conditionSatisfied.booleanValue();

        if (packConditionTrue)
        {
            // Pack 'condition' not set or 'condition' evaluates true
            if (required)
            {
                // Force selecting the pack
                out(Type.INFORMATION, "  [x] Pack '" + packName + "' required");
            }
            else
            {
                boolean contained = selectedPacks.contains(p);
                String cbView = contained ? "x" : " ";
                if (askUser("  ["+ cbView +"] Include optional pack '" + packName + "'", (contained ? Option.YES : Option.NO)))
                {
                    if (!contained)
                    {
                        selectedPacks.add(p);
                    }
                }
                else
                {
                    if (contained)
                    {
                        selectedPacks.remove(p);
                    }
                }
            }
        }
        else
        {
            // Force not selecting the pack
            selectedPacks.remove(p);
            out(Type.INFORMATION, "  [ ] Pack '" + packName + "' not enabled");
        }
    }

    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData - the data of izpack
     * @param pack        - the pack whose condition needs to be checked·
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(InstallData installData, Pack pack)
    {
        if (pack.hasCondition())
        {
            return installData.getRules().isConditionTrue(pack.getCondition());
        }
        else
        {
            return null;
        }
    }

    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter>·
     *
     * @return boolean  - true if condition above satisfied. Otherwise false
     */
    private boolean askUser(String message, Option defaultOption)
    {
        return Option.YES == prompt.confirm(Type.QUESTION, message, Options.YES_NO, defaultOption);
    }

    /**
     * Computes pack related installDataGUI like the names or the dependencies state.
     *
     * @param packs The list of packs.
     */
    private void computePacks(List<Pack> packs)
    {
        names = new LinkedHashMap<String, Pack>();
        for (Pack pack : packs)
        {
            names.put(pack.getName(), pack);
        }
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new PacksPanelAutomationHelper().createInstallationRecord(installData, panelRoot);
    }
}
