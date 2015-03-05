package com.izforge.izpack.core.variable.filters;

import com.izforge.izpack.api.data.ValueFilter;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.api.substitutor.VariableSubstitutor;

public class CaseStyleFilter implements ValueFilter
{
    private static final long serialVersionUID = 1L;

    public enum Style {LOWERCASE,UPPERCASE};
    private Style style;
    
    public CaseStyleFilter(Style style) 
    {
       this.style = style; 
    }

    public CaseStyleFilter(String style)
    {
        try 
        {
            this.style = Style.valueOf(style.toUpperCase());
        }
        catch (RuntimeException e)  //    IllegalArgumentException || NullPointerException
        {
            // Do nothing, will be reported by validate()
        }
    }

    @Override
    public void validate() throws Exception
    {
        if (style==null) 
        {
            throw new CompilerException("casestyle Filter has been initialized with unknown style");
        }
    }

    @Override
    public String filter(String value, VariableSubstitutor... substitutors) throws Exception
    {
        switch (style)
        {
        case LOWERCASE: return value.toLowerCase();
        case UPPERCASE: return value.toUpperCase();
        default:        throw new CompilerException("casestyle Filter has been initialized with unimplemented style");
        }
    }
}
