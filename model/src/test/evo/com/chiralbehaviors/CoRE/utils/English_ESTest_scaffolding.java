/**
 * Scaffolding file used to store all the setups needed to run
 * tests automatically generated by EvoSuite
 * Fri Apr 08 20:09:50 GMT 2016
 */

package com.chiralbehaviors.CoRE.utils;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

@EvoSuiteClassExclude
public class English_ESTest_scaffolding {

    private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties()
                                                                                                         .clone();

    @AfterClass
    public static void clearEvoSuiteFramework() {
        Sandbox.resetDefaultSecurityManager();
        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    }

    @BeforeClass
    public static void initEvoSuiteFramework() {
        org.evosuite.runtime.RuntimeSettings.className = "com.chiralbehaviors.CoRE.utils.English";
        org.evosuite.runtime.GuiSupport.initialize();
        org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100;
        org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000;
        org.evosuite.runtime.RuntimeSettings.mockSystemIn = true;
        org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED;
        org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT();
        org.evosuite.runtime.classhandling.JDKClassResetter.init();
        initializeClasses();
        org.evosuite.runtime.Runtime.getInstance()
                                    .resetRuntime();
    }

    private static void initializeClasses() {
        org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(English_ESTest_scaffolding.class.getClassLoader(),
                                                                               "com.chiralbehaviors.CoRE.utils.English",
                                                                               "com.chiralbehaviors.CoRE.utils.English$MODE",
                                                                               "com.chiralbehaviors.CoRE.utils.TwoFormInflector$Rule",
                                                                               "com.chiralbehaviors.CoRE.utils.TwoFormInflector$RegExpRule",
                                                                               "com.chiralbehaviors.CoRE.utils.TwoFormInflector$CategoryRule",
                                                                               "com.chiralbehaviors.CoRE.utils.TwoFormInflector");
    }

    private static void resetClasses() {
        org.evosuite.runtime.classhandling.ClassResetter.getInstance()
                                                        .setClassLoader(English_ESTest_scaffolding.class.getClassLoader());

        org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses("com.chiralbehaviors.CoRE.utils.English$MODE",
                                                                          "com.chiralbehaviors.CoRE.utils.English");
    }

    private org.evosuite.runtime.thread.ThreadStopper threadStopper = new org.evosuite.runtime.thread.ThreadStopper(org.evosuite.runtime.thread.KillSwitchHandler.getInstance(),
                                                                                                                    3000);

    @After
    public void doneWithTestCase() {
        threadStopper.killAndJoinClientThreads();
        org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance()
                                                    .safeExecuteAddedHooks();
        org.evosuite.runtime.classhandling.JDKClassResetter.reset();
        resetClasses();
        org.evosuite.runtime.sandbox.Sandbox.doneWithExecutingSUTCode();
        org.evosuite.runtime.agent.InstrumentingAgent.deactivate();
        org.evosuite.runtime.GuiSupport.restoreHeadlessMode();
    }

    @Before
    public void initTestCase() {
        threadStopper.storeCurrentThreads();
        threadStopper.startRecordingTime();
        org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance()
                                                    .initHandler();
        org.evosuite.runtime.sandbox.Sandbox.goingToExecuteSUTCode();
        setSystemProperties();
        org.evosuite.runtime.GuiSupport.setHeadless();
        org.evosuite.runtime.Runtime.getInstance()
                                    .resetRuntime();
        org.evosuite.runtime.agent.InstrumentingAgent.activate();
    }

    public void setSystemProperties() {

        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
        java.lang.System.setProperty("java.vm.vendor", "Oracle Corporation");
        java.lang.System.setProperty("java.specification.version", "1.8");
        java.lang.System.setProperty("java.home",
                                     "/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre");
        java.lang.System.setProperty("java.awt.headless", "true");
        java.lang.System.setProperty("user.home", "/Users/hhildebrand");
        java.lang.System.setProperty("user.dir",
                                     "/Users/hhildebrand/git/Ultrastructure/model");
        java.lang.System.setProperty("java.io.tmpdir",
                                     "/var/folders/_r/y4_0rwd16zgblwjq7b_tbhk80000gn/T/");
        java.lang.System.setProperty("awt.toolkit",
                                     "sun.lwawt.macosx.LWCToolkit");
        java.lang.System.setProperty("file.encoding", "UTF-8");
        java.lang.System.setProperty("file.separator", "/");
        java.lang.System.setProperty("java.awt.graphicsenv",
                                     "sun.awt.CGraphicsEnvironment");
        java.lang.System.setProperty("java.awt.printerjob",
                                     "sun.lwawt.macosx.CPrinterJob");
        java.lang.System.setProperty("java.class.path",
                                     "/var/folders/_r/y4_0rwd16zgblwjq7b_tbhk80000gn/T/EvoSuite_pathingJar4741277692469645904.jar");
        java.lang.System.setProperty("java.class.version", "52.0");
        java.lang.System.setProperty("java.endorsed.dirs",
                                     "/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/endorsed");
        java.lang.System.setProperty("java.ext.dirs",
                                     "/Users/hhildebrand/Library/Java/Extensions:/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/ext:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java");
        java.lang.System.setProperty("java.library.path", "lib");
        java.lang.System.setProperty("java.runtime.name",
                                     "Java(TM) SE Runtime Environment");
        java.lang.System.setProperty("java.runtime.version", "1.8.0_45-b14");
        java.lang.System.setProperty("java.specification.name",
                                     "Java Platform API Specification");
        java.lang.System.setProperty("java.specification.vendor",
                                     "Oracle Corporation");
        java.lang.System.setProperty("java.vendor", "Oracle Corporation");
        java.lang.System.setProperty("java.vendor.url",
                                     "http://java.oracle.com/");
        java.lang.System.setProperty("java.version", "1.8.0_45");
        java.lang.System.setProperty("java.vm.info", "mixed mode");
        java.lang.System.setProperty("java.vm.name",
                                     "Java HotSpot(TM) 64-Bit Server VM");
        java.lang.System.setProperty("java.vm.specification.name",
                                     "Java Virtual Machine Specification");
        java.lang.System.setProperty("java.vm.specification.vendor",
                                     "Oracle Corporation");
        java.lang.System.setProperty("java.vm.specification.version", "1.8");
        java.lang.System.setProperty("java.vm.version", "25.45-b02");
        java.lang.System.setProperty("line.separator", "\n");
        java.lang.System.setProperty("os.arch", "x86_64");
        java.lang.System.setProperty("os.name", "Mac OS X");
        java.lang.System.setProperty("os.version", "10.11.3");
        java.lang.System.setProperty("path.separator", ":");
        java.lang.System.setProperty("user.country", "US");
        java.lang.System.setProperty("user.language", "en");
        java.lang.System.setProperty("user.name", "hhildebrand");
        java.lang.System.setProperty("user.timezone", "America/Los_Angeles");
    }
}
