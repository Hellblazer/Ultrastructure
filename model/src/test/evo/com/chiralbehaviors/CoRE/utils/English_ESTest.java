/*
 * This file was automatically generated by EvoSuite
 * Fri Apr 08 20:09:50 GMT 2016
 */

package com.chiralbehaviors.CoRE.utils;

import static org.evosuite.runtime.EvoAssertions.assertThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true)
public class English_ESTest extends English_ESTest_scaffolding {

    @Test(timeout = 4000)
    public void test00() throws Throwable {
        String string0 = English.plural("Y<", 32);
        assertEquals("Y<s", string0);
    }

    @Test(timeout = 4000)
    public void test01() throws Throwable {
        String string0 = English.plural("", 1);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test02() throws Throwable {
        String string0 = English.plural("b-");
        assertEquals("b-s", string0);
    }

    @Test(timeout = 4000)
    public void test03() throws Throwable {
        English english0 = new English();
        String string0 = english0.getPlural((String) null, 1);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test04() throws Throwable {
        English english0 = new English();
        String string0 = english0.getPlural("", 1);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test05() throws Throwable {
        // Undeclared exception!
        try {
            English.plural((String) null, 0);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            assertThrownBy("com.chiralbehaviors.CoRE.utils.TwoFormInflector$CategoryRule",
                           e);
        }
    }

    @Test(timeout = 4000)
    public void test06() throws Throwable {
        English english0 = new English();
        String string0 = english0.getPlural("Ms&rhvG&i", 1);
        assertEquals("Ms&rhvG&i", string0);
    }

    @Test(timeout = 4000)
    public void test07() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_ANGLICIZED;
        English english0 = new English(english_MODE0);
        String string0 = english0.getPlural("\"xw/:^X-");
        assertEquals("\"xw/:^X-s", string0);
    }

    @Test(timeout = 4000)
    public void test08() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_CLASSICAL;
        English english0 = new English(english_MODE0);
        // Undeclared exception!
        try {
            english0.getPlural((String) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            assertThrownBy("com.chiralbehaviors.CoRE.utils.TwoFormInflector$CategoryRule",
                           e);
        }
    }

    @Test(timeout = 4000)
    public void test09() throws Throwable {
        new English((English.MODE) null);
    }

    @Test(timeout = 4000)
    public void test10() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_CLASSICAL;
        English.setMode(english_MODE0);
    }

    @Test(timeout = 4000)
    public void test11() throws Throwable {
        String string0 = English.plural((String) null, 1);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test12() throws Throwable {
        // Undeclared exception!
        try {
            English.plural((String) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            assertThrownBy("com.chiralbehaviors.CoRE.utils.TwoFormInflector$CategoryRule",
                           e);
        }
    }

    @Test(timeout = 4000)
    public void test13() throws Throwable {
        English english0 = new English();
        // Undeclared exception!
        try {
            english0.getPlural((String) null, 0);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            assertThrownBy("com.chiralbehaviors.CoRE.utils.TwoFormInflector$CategoryRule",
                           e);
        }
    }
}