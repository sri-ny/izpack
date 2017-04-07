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

package com.izforge.izpack.panels.userinput.field;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.ConfigurationOption;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.handler.DefaultConfigurationHandler;
import com.izforge.izpack.panels.userinput.processor.Processor;
import com.izforge.izpack.panels.userinput.processorclient.ProcessingClient;
import com.izforge.izpack.panels.userinput.processorclient.ValuesProcessingClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * FieldProcessor is a wrapper around a {@link Processor}.
 *
 * @author Tim Anderson
 */
public class FieldProcessor extends DefaultConfigurationHandler
{

    /**
     * The configuration.
     */
    private final Config config;

    /**

     * The processor class name.
     */
    private final String className;

    /**
     * The name of the variable holding the original value before processing (optional)
     */
    private final String originalValueVariable;

    /**
     * The original value before processing (optional)
     */
    private String originalValue;

    /**
     * The cached processor instance.
     */
    private Processor processor;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(FieldProcessor.class.getName());


    /**
     * Constructs a {@code FieldProcessor}.
     *
     * @param processor the processor element
     * @param config    the configuration
     */
    public FieldProcessor(IXMLElement processor, Config config)
    {
        className = config.getAttribute(processor, "class");
        originalValueVariable = config.getAttribute(processor, "backupVariable");
        addConfigurationOptions(processor);
        this.config = config;
    }

    /**
     * Processes a set of values.
     *
     * @param values the values to process
     * @return the result of the processing
     * @throws IzPackException if processing fails
     */
    public String process(String... values)
    {
        String result;
        try
        {
            if (processor == null)
            {
                processor = config.getFactory().create(className, Processor.class);
            }

            Set<String> configParams = getNames();
            Map<String, String> configMap = null;
            if (configParams != null)
            {
                configMap = new HashMap<String, String>();
                for (String param : configParams)
                {
                    configMap.put(param, getConfigurationOptionValue(param, null));
                }
            }
            ProcessingClient client = new ValuesProcessingClient(values, configMap);
            originalValue = client.getText();
            result = processor.process(client);
        }
        catch (Throwable exception)
        {
            logger.log(Level.WARNING, "Processing using " + className + " failed: " + exception.getMessage(),
                       exception);
            if (exception instanceof IzPackException)
            {
                throw (IzPackException) exception;
            }
            throw new IzPackException("Processing using " + className + " failed: " + exception.getMessage(),
                                      exception);
        }
        return result;
    }

    public String getBackupVariable()
    {
        return originalValueVariable;
    }

    public String getOriginalValue()
    {
        return originalValue;
    }

    private void addConfigurationOptions(IXMLElement processor)
    {
        IXMLElement configurationElement = processor.getFirstChildNamed("configuration");
        if (configurationElement != null)
        {
            logger.fine("Found configuration section for '" + processor.getName() + "' element");
            List<IXMLElement> params = configurationElement.getChildren();
            for (IXMLElement param : params)
            {
                String elementName = param.getName();
                String name;
                final String value;
                if (elementName.equals("param"))
                {
                    // Format: <param name="option_1" value="value_1" />
                    name = param.getAttribute("name");
                    value = param.getAttribute("value");
                } else
                {
                    // Format: <option_1>value_1</option_1>
                    name = param.getName();
                    value = param.getContent();
                }
                final ConfigurationOption option = new ConfigurationOption(value);
                logger.fine("-> Adding configuration option " + name + " (" + option + ")");
                addConfigurationOption(name, option);
            }
        }
    }
}
