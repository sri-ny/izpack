package com.izforge.izpack.core.variable.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import com.izforge.izpack.api.substitutor.VariableSubstitutor;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;

public class CaseStyleFilterTest
{

    @Test
    public void testLowerCase()
    {
        final String text = "Some Text";
        VariableSubstitutor subst = new VariableSubstitutorImpl(new Properties());
        try
        {
            assertEquals("some text", new CaseStyleFilter("lowercase").filter(text, subst));
            assertEquals("some text", new CaseStyleFilter(CaseStyleFilter.Style.LOWERCASE).filter(text, subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

    @Test
    public void testUpperCase()
    {
        final String text = "Some Text";
        VariableSubstitutor subst = new VariableSubstitutorImpl(new Properties());
        try
        {
            assertEquals("SOME TEXT", new CaseStyleFilter("uppercase").filter(text, subst));
            assertEquals("SOME TEXT", new CaseStyleFilter(CaseStyleFilter.Style.UPPERCASE).filter(text, subst));
        }
        catch (Exception e)
        {
            fail(e.toString());
        }
    }

}
