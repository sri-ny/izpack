package com.izforge.izpack.api.data;

import java.io.Serializable;

import com.izforge.izpack.api.rules.RulesEngine;

public class ConfigurationOption implements Serializable
{
    private static final long serialVersionUID = 2616397619106736648L;

    private final String value;

    private final String conditionId;

    private final String defaultValue;


    public ConfigurationOption(String value, String conditionId, String defaultValue)
    {
        super();
        this.value = value;
        this.conditionId = conditionId;
        this.defaultValue = defaultValue;
    }

    public ConfigurationOption(String value, String conditionId)
    {
        this(value, conditionId, null);
    }

    public ConfigurationOption(String value)
    {
        this(value, null);
    }

    /**
     * Get the option's current value according to the optional condition
     *
     * @return the current value
     */
    public String getValue(RulesEngine rules)
    {
        final String result;
        if (rules == null | conditionId == null || rules.isConditionTrue(conditionId))
        {
            result = value;
        }
        else
        {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "value='"+value+"'"
                +(conditionId==null?"":"conditionId+"+conditionId+"'")
                +(defaultValue==null?"":"defaultValue+"+defaultValue+"'");
    }
}
