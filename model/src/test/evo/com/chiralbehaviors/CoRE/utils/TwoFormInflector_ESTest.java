/*
 * This file was automatically generated by EvoSuite
 * Fri Apr 08 20:04:19 GMT 2016
 */

package com.chiralbehaviors.CoRE.utils;

import static org.evosuite.runtime.EvoAssertions.assertThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.regex.PatternSyntaxException;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true)
public class TwoFormInflector_ESTest
        extends TwoFormInflector_ESTest_scaffolding {

    @Test(timeout = 4000)
    public void test00() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_ANGLICIZED;
        English english0 = new English(english_MODE0);
        String string0 = english0.getPlural("jumbo");
        assertEquals("jumbos", string0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test01() throws Throwable {
        English english0 = new English();
        String[] stringArray0 = new String[7];
        stringArray0[0] = "";
        String[][] stringArray1 = new String[2][6];
        stringArray1[0] = stringArray0;
        stringArray1[1] = stringArray0;
        english0.rule(stringArray1);
    }

    @Test(timeout = 4000)
    public void test02() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_ANGLICIZED;
        English english0 = new English(english_MODE0);
        english0.rule("tjg21pzUQPC'", "tjg21pzUQPC'");
    }

    @Test(timeout = 4000)
    public void test03() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[0][6];
        english0.irregular(stringArray0);
    }

    @Test(timeout = 4000)
    public void test04() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_CLASSICAL;
        English english0 = new English(english_MODE0);
        english0.irregular("X!", "X!");
    }

    @Test(timeout = 4000)
    public void test05() throws Throwable {
        English english0 = new English();
        String[] stringArray0 = new String[4];
        english0.uncountable(stringArray0);
    }

    @Test(timeout = 4000)
    public void test06() throws Throwable {
        English english0 = new English();
        String[] stringArray0 = new String[0];
        english0.categoryRule(stringArray0, "beef", "beef");
    }

    @Test(timeout = 4000)
    public void test07() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[4][6];
        String[] stringArray1 = new String[1];
        stringArray1[0] = "k83m`*[#?N";
        stringArray0[0] = stringArray1;
        // Undeclared exception!
        try {
            english0.rule(stringArray0);
            fail("Expecting exception: PatternSyntaxException");

        } catch (PatternSyntaxException e) {
            //
            // Unclosed character class near index 9
            // k83m`*[#?N
            //          ^
            //
            assertThrownBy("java.util.regex.Pattern", e);
        }
    }

    @Test(timeout = 4000)
    public void test08() throws Throwable {
        English english0 = new English();
        // Undeclared exception!
        try {
            english0.rule((String[][]) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test09() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[2][0];
        // Undeclared exception!
        try {
            english0.rule(stringArray0);
            fail("Expecting exception: ArrayIndexOutOfBoundsException");

        } catch (ArrayIndexOutOfBoundsException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test10() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_ANGLICIZED;
        English english0 = new English(english_MODE0);
        // Undeclared exception!
        try {
            english0.rule(")S", "I");
            fail("Expecting exception: PatternSyntaxException");

        } catch (PatternSyntaxException e) {
            //
            // Unmatched closing ')'
            // )S
            //
            assertThrownBy("java.util.regex.Pattern", e);
        }
    }

    @Test(timeout = 4000)
    public void test11() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_ANGLICIZED;
        English english0 = new English(english_MODE0);
        // Undeclared exception!
        try {
            english0.rule((String) null, (String) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test12() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[2][6];
        String[] stringArray1 = new String[4];
        stringArray1[0] = "r=.De+^#Fz&[gH";
        stringArray1[1] = "Qt";
        stringArray0[0] = stringArray1;
        // Undeclared exception!
        try {
            english0.irregular(stringArray0);
            fail("Expecting exception: PatternSyntaxException");

        } catch (PatternSyntaxException e) {
            //
            // Unclosed character class near index 18
            // R(?i)=.De+^#Fz&[gH$
            //                   ^
            //
            assertThrownBy("java.util.regex.Pattern", e);
        }
    }

    @Test(timeout = 4000)
    public void test13() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[7][6];
        String[] stringArray1 = new String[4];
        stringArray1[0] = "";
        stringArray0[0] = stringArray1;
        // Undeclared exception!
        try {
            english0.irregular(stringArray0);
            fail("Expecting exception: StringIndexOutOfBoundsException");

        } catch (StringIndexOutOfBoundsException e) {
            //
            // String index out of range: 0
            //
            assertThrownBy("java.lang.String", e);
        }
    }

    @Test(timeout = 4000)
    public void test14() throws Throwable {
        English english0 = new English();
        String[][] stringArray0 = new String[8][2];
        // Undeclared exception!
        try {
            english0.irregular(stringArray0);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test15() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_CLASSICAL;
        English english0 = new English(english_MODE0);
        String[][] stringArray0 = new String[2][1];
        // Undeclared exception!
        try {
            english0.irregular(stringArray0);
            fail("Expecting exception: ArrayIndexOutOfBoundsException");

        } catch (ArrayIndexOutOfBoundsException e) {
            //
            // 1
            //
            assertThrownBy("com.chiralbehaviors.CoRE.utils.TwoFormInflector",
                           e);
        }
    }

    @Test(timeout = 4000)
    public void test16() throws Throwable {
        English english0 = new English();
        // Undeclared exception!
        try {
            english0.irregular("6^(mps", "");
            fail("Expecting exception: StringIndexOutOfBoundsException");

        } catch (StringIndexOutOfBoundsException e) {
            //
            // String index out of range: 0
            //
            assertThrownBy("java.lang.String", e);
        }
    }

    @Test(timeout = 4000)
    public void test17() throws Throwable {
        English english0 = new English();
        // Undeclared exception!
        try {
            english0.irregular((String) null, "Ue>*[i!Ge+b*");
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test18() throws Throwable {
        English english0 = new English();
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
    public void test19() throws Throwable {
        String string0 = English.plural("(?Ai)stoma", -3213);
        assertEquals("(?Ai)stomas", string0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test20() throws Throwable {
        English.MODE english_MODE0 = English.MODE.ENGLISH_CLASSICAL;
        English english0 = new English(english_MODE0);
        String string0 = english0.getPlural("sarcomata");
        assertEquals("sarcomatas", string0);
        assertNotNull(string0);
    }
}
