package de.akquinet.chameria.services;

import org.junit.Assert;
import org.junit.Test;

import de.akquinet.chameria.services.ActivationUtils;

public class ActivationUtilsTest {
    
    @Test
    public void testContainsArgument() {
        String[] args = new String[] {
                "--foo",  "-bar=toto",  "dede", "de"
        };
        
        Assert.assertTrue(ActivationUtils.containsArgument(args, "--foo"));
        Assert.assertFalse(ActivationUtils.containsArgument(args, "foo"));
        Assert.assertFalse(ActivationUtils.containsArgument(args, "-foo"));
        
        Assert.assertTrue(ActivationUtils.containsArgument(args, "-bar"));
        Assert.assertFalse(ActivationUtils.containsArgument(args, "-ba"));
        Assert.assertFalse(ActivationUtils.containsArgument(args, "bar"));
        Assert.assertFalse(ActivationUtils.containsArgument(args, "-bar="));
        
        Assert.assertTrue(ActivationUtils.containsArgument(args, "dede"));
        Assert.assertTrue(ActivationUtils.containsArgument(args, "de"));
        
        Assert.assertFalse(ActivationUtils.containsArgument(new String[] {}, "bar"));
        Assert.assertFalse(ActivationUtils.containsArgument(null, "bar"));
        Assert.assertFalse(ActivationUtils.containsArgument(new String[] {"bar"}, ""));
        Assert.assertFalse(ActivationUtils.containsArgument(new String[] {"bar"}, null));
        Assert.assertTrue(ActivationUtils.containsArgument(new String[] {"bar"}, "bar"));
    }
    
    @Test
    public void testgetArgument() {
        String[] args = new String[] {
                "--foo",  "-bar=toto",  "dede", "de"
        };
        
        
        Assert.assertEquals("toto", ActivationUtils.getArgumentValue(args, "-bar"));
        Assert.assertEquals("de", ActivationUtils.getArgumentValue(args, "dede"));
        Assert.assertNull(ActivationUtils.getArgumentValue(args, "de"));
    }
    
    @Test
    public void testOpen() {
        String[] args = new String[] {
                "--foo",  "-open=toto",  "dede", "de"
        };
        
        
        Assert.assertEquals("toto", ActivationUtils.getOpenArgument(args));
        Assert.assertEquals("toto", ActivationUtils.getOpenArgument(new String[] {"-open", "toto"}));
        Assert.assertNull(ActivationUtils.getOpenArgument(new String[] {"-open"}));
        Assert.assertNull(ActivationUtils.getOpenArgument(new String[] {}));
        
    }


}
