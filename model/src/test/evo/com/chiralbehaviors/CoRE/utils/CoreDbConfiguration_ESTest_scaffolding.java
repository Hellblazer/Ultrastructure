/**
 * Scaffolding file used to store all the setups needed to run
 * tests automatically generated by EvoSuite
 * Fri Apr 08 20:07:14 GMT 2016
 */

package com.chiralbehaviors.CoRE.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

@EvoSuiteClassExclude
public class CoreDbConfiguration_ESTest_scaffolding {

    protected static ExecutorService          executor;

    private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties()
                                                                                                         .clone();

    @AfterClass
    public static void clearEvoSuiteFramework() {
        Sandbox.resetDefaultSecurityManager();
        executor.shutdownNow();
        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    }

    @BeforeClass
    public static void initEvoSuiteFramework() {
        org.evosuite.runtime.RuntimeSettings.className = "com.chiralbehaviors.CoRE.utils.CoreDbConfiguration";
        org.evosuite.runtime.GuiSupport.initialize();
        org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100;
        org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000;
        org.evosuite.runtime.RuntimeSettings.mockSystemIn = true;
        org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED;
        org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT();
        executor = Executors.newCachedThreadPool();
        org.evosuite.runtime.classhandling.JDKClassResetter.init();
        initializeClasses();
        org.evosuite.runtime.Runtime.getInstance()
                                    .resetRuntime();
    }

    private static void initializeClasses() {
        org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(CoreDbConfiguration_ESTest_scaffolding.class.getClassLoader(),
                                                                               "org.postgresql.core.QueryExecutor",
                                                                               "org.postgresql.util.GT",
                                                                               "org.postgresql.hostchooser.HostRequirement",
                                                                               "org.postgresql.hostchooser.HostRequirement$4",
                                                                               "org.postgresql.core.v3.ProtocolConnectionImpl",
                                                                               "org.postgresql.core.Logger",
                                                                               "org.postgresql.util.LruCache$CreateAction",
                                                                               "org.hsqldb.lib.Iterator",
                                                                               "org.postgresql.core.CachedQuery",
                                                                               "org.hsqldb.jdbc.JDBCDriver",
                                                                               "org.postgresql.util.SharedTimer",
                                                                               "org.postgresql.core.BaseConnection",
                                                                               "org.postgresql.core.ResultHandler",
                                                                               "org.hsqldb.jdbc.JDBCDriver$1",
                                                                               "org.postgresql.PGNotification",
                                                                               "org.postgresql.core.Version",
                                                                               "org.postgresql.core.PGStream",
                                                                               "org.postgresql.core.v2.ProtocolConnectionImpl",
                                                                               "org.postgresql.jdbc.CallableQueryKey",
                                                                               "org.hsqldb.lib.FileAccess",
                                                                               "org.postgresql.core.Encoding",
                                                                               "org.postgresql.core.PGBindException",
                                                                               "org.postgresql.core.TypeInfo",
                                                                               "org.hsqldb.DatabaseURL",
                                                                               "org.postgresql.hostchooser.HostChooser",
                                                                               "org.hsqldb.map.HashIndex",
                                                                               "org.postgresql.core.ProtocolConnection",
                                                                               "org.postgresql.core.v2.SocketFactoryFactory",
                                                                               "org.hsqldb.persist.HsqlProperties",
                                                                               "org.postgresql.util.HostSpec",
                                                                               "org.postgresql.util.CanEstimateSize",
                                                                               "org.postgresql.util.ServerErrorMessage",
                                                                               "org.postgresql.util.PSQLException",
                                                                               "org.hsqldb.map.ValuePoolHashMap",
                                                                               "org.hsqldb.map.BaseHashMap",
                                                                               "org.postgresql.fastpath.Fastpath",
                                                                               "org.postgresql.core.v2.ConnectionFactoryImpl",
                                                                               "org.postgresql.largeobject.LargeObjectManager",
                                                                               "org.postgresql.copy.CopyManager",
                                                                               "org.postgresql.jdbc.TimestampUtils",
                                                                               "org.postgresql.core.ConnectionFactory",
                                                                               "org.postgresql.core.v3.ConnectionFactoryImpl",
                                                                               "com.chiralbehaviors.CoRE.utils.CoreDbConfiguration",
                                                                               "org.postgresql.jdbc.PgConnection",
                                                                               "org.hsqldb.map.ValuePool",
                                                                               "org.hsqldb.lib.ObjectComparator",
                                                                               "org.postgresql.util.PSQLState",
                                                                               "org.postgresql.hostchooser.HostChooserFactory",
                                                                               "org.postgresql.PGProperty",
                                                                               "org.postgresql.core.v3.ConnectionFactoryImpl$UnsupportedProtocolException",
                                                                               "org.postgresql.PGConnection",
                                                                               "org.postgresql.hostchooser.SingleHostChooser",
                                                                               "org.postgresql.Driver$1",
                                                                               "org.postgresql.core.PGStream$1",
                                                                               "org.postgresql.hostchooser.HostStatus",
                                                                               "org.postgresql.hostchooser.HostRequirement$2",
                                                                               "org.postgresql.hostchooser.HostRequirement$3",
                                                                               "org.postgresql.core.Query",
                                                                               "org.postgresql.hostchooser.HostRequirement$1",
                                                                               "org.postgresql.util.LruCache$EvictAction",
                                                                               "org.postgresql.util.PSQLWarning",
                                                                               "org.postgresql.Driver");
    }

    private static void resetClasses() {
        org.evosuite.runtime.classhandling.ClassResetter.getInstance()
                                                        .setClassLoader(CoreDbConfiguration_ESTest_scaffolding.class.getClassLoader());

        org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses("com.chiralbehaviors.CoRE.utils.CoreDbConfiguration",
                                                                          "org.postgresql.util.SharedTimer",
                                                                          "org.postgresql.Driver",
                                                                          "org.hsqldb.jdbc.JDBCDriver",
                                                                          "org.postgresql.PGProperty",
                                                                          "org.postgresql.jdbc.PgConnection",
                                                                          "org.postgresql.core.v3.ConnectionFactoryImpl",
                                                                          "org.postgresql.core.v2.ConnectionFactoryImpl",
                                                                          "org.postgresql.core.ConnectionFactory",
                                                                          "org.postgresql.hostchooser.HostRequirement$1",
                                                                          "org.postgresql.hostchooser.HostRequirement$2",
                                                                          "org.postgresql.hostchooser.HostRequirement$3",
                                                                          "org.postgresql.hostchooser.HostRequirement$4",
                                                                          "org.postgresql.hostchooser.HostRequirement",
                                                                          "org.postgresql.util.PSQLException",
                                                                          "org.postgresql.util.GT",
                                                                          "org.postgresql.util.PSQLState",
                                                                          "org.hsqldb.DatabaseURL",
                                                                          "org.hsqldb.persist.HsqlProperties",
                                                                          "org.hsqldb.map.BaseHashMap",
                                                                          "org.hsqldb.map.ValuePool");
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
                                     "/var/folders/_r/y4_0rwd16zgblwjq7b_tbhk80000gn/T/EvoSuite_pathingJar7992993944466887421.jar");
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
