/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
