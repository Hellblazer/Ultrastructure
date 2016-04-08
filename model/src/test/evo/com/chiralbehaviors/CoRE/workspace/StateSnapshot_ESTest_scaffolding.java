/**
 * Scaffolding file used to store all the setups needed to run
 * tests automatically generated by EvoSuite
 * Fri Apr 08 20:29:59 GMT 2016
 */

package com.chiralbehaviors.CoRE.workspace;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

@EvoSuiteClassExclude
public class StateSnapshot_ESTest_scaffolding {

    private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System.getProperties()
                                                                                                         .clone();

    @AfterClass
    public static void clearEvoSuiteFramework() {
        Sandbox.resetDefaultSecurityManager();
        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    }

    @BeforeClass
    public static void initEvoSuiteFramework() {
        org.evosuite.runtime.RuntimeSettings.className = "com.chiralbehaviors.CoRE.workspace.StateSnapshot";
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
        org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(StateSnapshot_ESTest_scaffolding.class.getClassLoader(),
                                                                               "org.jooq.impl.RecordOperation",
                                                                               "org.jooq.impl.TableRecordImpl",
                                                                               "org.jooq.BindingRegisterContext",
                                                                               "com.fasterxml.jackson.databind.JsonSerializable$Base",
                                                                               "org.jooq.types.UShort",
                                                                               "org.jooq.Converters",
                                                                               "org.jooq.impl.AbstractStore",
                                                                               "org.jooq.WindowOverStep",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.Facet",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.StatusCodeSequencing",
                                                                               "org.jooq.util.cubrid.CUBRIDDataType",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetwork",
                                                                               "org.jooq.Scope",
                                                                               "org.jooq.SQLDialect",
                                                                               "com.chiralbehaviors.CoRE.postgres.PostgresJSONJacksonJsonNodeConverter",
                                                                               "org.jooq.Select",
                                                                               "org.jooq.impl.Fields",
                                                                               "com.fasterxml.jackson.databind.node.ValueNode",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttributeAuthorization",
                                                                               "org.jooq.TableField",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.NetworkInference",
                                                                               "com.chiralbehaviors.CoRE.domain.Product",
                                                                               "org.jooq.util.firebird.FirebirdDataType",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.Existential",
                                                                               "org.jooq.Sequence",
                                                                               "com.chiralbehaviors.CoRE.workspace.StateSnapshot",
                                                                               "com.fasterxml.jackson.core.TreeNode",
                                                                               "org.jooq.QueryPartInternal",
                                                                               "org.jooq.Comparator",
                                                                               "org.jooq.types.ULong",
                                                                               "org.jooq.types.YearToMonth",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.SiblingSequencingAuthorization",
                                                                               "org.jooq.JoinType",
                                                                               "org.jooq.QuantifiedSelect",
                                                                               "org.jooq.WindowFinalStep",
                                                                               "com.fasterxml.jackson.databind.Module",
                                                                               "org.jooq.Row",
                                                                               "com.fasterxml.jackson.core.Versioned",
                                                                               "org.jooq.Table",
                                                                               "com.chiralbehaviors.CoRE.jooq.Ruleform",
                                                                               "org.jooq.SQL",
                                                                               "org.jooq.RenderContext",
                                                                               "org.jooq.Clause",
                                                                               "org.jooq.impl.RowImpl",
                                                                               "org.jooq.BetweenAndStep10",
                                                                               "org.jooq.types.UInteger",
                                                                               "org.jooq.UDT",
                                                                               "org.jooq.ResultQuery",
                                                                               "com.fasterxml.jackson.databind.node.BaseJsonNode",
                                                                               "com.fasterxml.jackson.databind.JsonSerializable",
                                                                               "org.jooq.BetweenAndStep14",
                                                                               "org.jooq.DSLContext",
                                                                               "org.jooq.BetweenAndStep13",
                                                                               "org.jooq.BetweenAndStep12",
                                                                               "org.jooq.types.UNumber",
                                                                               "org.jooq.exception.DataTypeException",
                                                                               "org.jooq.BetweenAndStep11",
                                                                               "org.jooq.BetweenAndStep18",
                                                                               "org.jooq.BetweenAndStep17",
                                                                               "org.jooq.BetweenAndStep16",
                                                                               "org.jooq.BetweenAndStep15",
                                                                               "org.jooq.BindingSetSQLOutputContext",
                                                                               "org.jooq.GroupField",
                                                                               "org.jooq.DataType",
                                                                               "org.jooq.exception.SQLDialectNotSupportedException",
                                                                               "org.jooq.Name",
                                                                               "org.jooq.Field",
                                                                               "org.jooq.impl.UpdatableRecordImpl",
                                                                               "org.jooq.BindingGetSQLInputContext",
                                                                               "org.jooq.DivideByOnStep",
                                                                               "org.jooq.EnumType",
                                                                               "org.jooq.BindingGetResultSetContext",
                                                                               "org.jooq.BindContext",
                                                                               "com.chiralbehaviors.CoRE.workspace.WorkspaceSnapshot",
                                                                               "org.jooq.Identity",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.AgencyExistentialGrouping",
                                                                               "org.jooq.TableLike",
                                                                               "org.jooq.util.h2.H2DataType",
                                                                               "org.jooq.UpdatableRecord",
                                                                               "org.jooq.types.Interval",
                                                                               "org.jooq.exception.DataAccessException",
                                                                               "org.jooq.WindowPartitionByStep",
                                                                               "org.jooq.BindingSetStatementContext",
                                                                               "org.jooq.BetweenAndStep19",
                                                                               "org.jooq.Record17",
                                                                               "org.jooq.Record18",
                                                                               "com.fasterxml.jackson.core.JsonProcessingException",
                                                                               "org.jooq.Record19",
                                                                               "org.jooq.Record13",
                                                                               "org.jooq.impl.AbstractField",
                                                                               "org.jooq.Record14",
                                                                               "org.jooq.Record15",
                                                                               "org.jooq.impl.SchemaImpl",
                                                                               "org.jooq.Record16",
                                                                               "org.jooq.Record10",
                                                                               "org.jooq.Record11",
                                                                               "org.jooq.RecordType",
                                                                               "org.jooq.SQLDialect$ThirdParty",
                                                                               "org.jooq.Record12",
                                                                               "org.jooq.SortField",
                                                                               "org.jooq.BetweenAndStep21",
                                                                               "org.jooq.BetweenAndStep20",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ParentSequencingAuthorization",
                                                                               "org.jooq.BetweenAndStep22",
                                                                               "com.chiralbehaviors.CoRE.json.CoREModule",
                                                                               "org.jooq.AttachableInternal",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ChildSequencingAuthorization",
                                                                               "org.jooq.Condition",
                                                                               "org.jooq.Record20",
                                                                               "org.jooq.Record4",
                                                                               "org.jooq.Record21",
                                                                               "org.jooq.Record5",
                                                                               "com.fasterxml.jackson.databind.node.NullNode",
                                                                               "org.jooq.Record22",
                                                                               "org.jooq.Record2",
                                                                               "org.jooq.Record3",
                                                                               "org.jooq.SelectField",
                                                                               "org.jooq.Record1",
                                                                               "com.fasterxml.jackson.databind.JsonNode",
                                                                               "org.jooq.types.UByte",
                                                                               "org.jooq.Record8",
                                                                               "org.jooq.Record9",
                                                                               "org.jooq.Record6",
                                                                               "org.jooq.WindowIgnoreNullsStep",
                                                                               "org.jooq.Record7",
                                                                               "org.jooq.Result",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.Job",
                                                                               "org.jooq.QueryPart",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.JobChronology",
                                                                               "org.jooq.util.hsqldb.HSQLDBDataType",
                                                                               "org.jooq.BetweenAndStep",
                                                                               "org.jooq.impl.SQLDataType",
                                                                               "org.jooq.BindingSQLContext",
                                                                               "org.jooq.FieldLike",
                                                                               "org.jooq.util.derby.DerbyDataType",
                                                                               "org.jooq.Row17",
                                                                               "org.jooq.Row16",
                                                                               "org.jooq.Row15",
                                                                               "org.jooq.Row14",
                                                                               "org.jooq.Row19",
                                                                               "org.jooq.Row18",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.SelfSequencingAuthorization",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.MetaProtocol",
                                                                               "org.jooq.Schema",
                                                                               "org.jooq.Row20",
                                                                               "com.fasterxml.jackson.core.JsonGenerationException",
                                                                               "com.chiralbehaviors.CoRE.phantasm.Phantasm",
                                                                               "org.jooq.Row22",
                                                                               "org.jooq.Row21",
                                                                               "org.jooq.Record",
                                                                               "com.chiralbehaviors.CoRE.jooq.Tables",
                                                                               "org.jooq.impl.TableFieldImpl",
                                                                               "org.jooq.tools.StringUtils",
                                                                               "com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain",
                                                                               "org.jooq.util.mariadb.MariaDBDataType",
                                                                               "org.jooq.impl.AbstractTable",
                                                                               "com.chiralbehaviors.CoRE.jooq.enums.Cardinality",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAttributeAuthorization",
                                                                               "org.jooq.impl.AbstractRecord",
                                                                               "org.jooq.Row13",
                                                                               "org.jooq.impl.TableImpl",
                                                                               "org.jooq.Row12",
                                                                               "org.jooq.TableOnStep",
                                                                               "org.jooq.Row11",
                                                                               "org.jooq.util.sqlite.SQLiteDataType",
                                                                               "org.jooq.Row10",
                                                                               "org.jooq.UniqueKey",
                                                                               "org.jooq.RowN",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAttribute",
                                                                               "org.jooq.BetweenAndStep1",
                                                                               "com.fasterxml.jackson.databind.module.SimpleModule",
                                                                               "org.jooq.BetweenAndStep3",
                                                                               "org.jooq.BetweenAndStep2",
                                                                               "org.jooq.Binding",
                                                                               "com.chiralbehaviors.CoRE.postgres.PostgresJSONJacksonJsonNodeBinding",
                                                                               "org.jooq.BetweenAndStep5",
                                                                               "org.jooq.BetweenAndStep4",
                                                                               "org.jooq.FieldOrRow",
                                                                               "org.jooq.impl.DefaultDataType",
                                                                               "org.jooq.BetweenAndStep7",
                                                                               "org.jooq.BetweenAndStep6",
                                                                               "com.chiralbehaviors.CoRE.domain.ExistentialRuleform",
                                                                               "com.chiralbehaviors.CoRE.jooq.enums.ReferenceType",
                                                                               "org.jooq.SortOrder",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttribute",
                                                                               "org.jooq.tools.JooqLogger",
                                                                               "org.jooq.Row1",
                                                                               "org.jooq.Row2",
                                                                               "org.jooq.Row3",
                                                                               "org.jooq.Row4",
                                                                               "org.jooq.Row5",
                                                                               "org.jooq.BetweenAndStep9",
                                                                               "org.jooq.Row6",
                                                                               "org.jooq.BetweenAndStep8",
                                                                               "org.jooq.TableRecord",
                                                                               "org.jooq.Row7",
                                                                               "org.jooq.exception.MappingException",
                                                                               "org.jooq.Row8",
                                                                               "org.jooq.impl.AbstractQueryPart",
                                                                               "org.jooq.Row9",
                                                                               "org.jooq.impl.ConvertedDataType",
                                                                               "org.jooq.util.postgres.PostgresDataType",
                                                                               "org.jooq.Context",
                                                                               "com.chiralbehaviors.CoRE.jooq.enums.ValueType",
                                                                               "org.jooq.WindowOrderByStep",
                                                                               "org.jooq.TablePartitionByStep",
                                                                               "com.fasterxml.jackson.databind.JsonMappingException",
                                                                               "org.jooq.Query",
                                                                               "org.jooq.types.DayToSecond",
                                                                               "org.jooq.BetweenAndStepN",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAuthorization",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.WorkspaceAuthorization",
                                                                               "org.jooq.impl.DefaultBinding",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.Protocol",
                                                                               "org.jooq.Attachable",
                                                                               "org.jooq.DatePart",
                                                                               "org.jooq.util.mysql.MySQLDataType",
                                                                               "org.jooq.Converter",
                                                                               "org.jooq.Configuration",
                                                                               "org.jooq.TableOptionalOnStep",
                                                                               "org.jooq.BindingGetStatementContext",
                                                                               "org.jooq.Converters$1",
                                                                               "org.jooq.Key",
                                                                               "com.chiralbehaviors.CoRE.jooq.tables.records.ExistentialRecord");
    }

    private static void resetClasses() {
        org.evosuite.runtime.classhandling.ClassResetter.getInstance()
                                                        .setClassLoader(StateSnapshot_ESTest_scaffolding.class.getClassLoader());

        org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses("com.chiralbehaviors.CoRE.workspace.WorkspaceSnapshot",
                                                                          "org.jooq.impl.AbstractQueryPart",
                                                                          "org.jooq.Clause",
                                                                          "org.jooq.impl.AbstractTable",
                                                                          "org.jooq.impl.TableImpl",
                                                                          "org.jooq.impl.SchemaImpl",
                                                                          "com.chiralbehaviors.CoRE.jooq.Ruleform",
                                                                          "org.jooq.impl.Fields",
                                                                          "org.jooq.SQLDialect",
                                                                          "org.jooq.impl.DefaultDataType",
                                                                          "org.jooq.impl.DefaultBinding",
                                                                          "org.jooq.Converters",
                                                                          "org.jooq.Converters$1",
                                                                          "org.jooq.util.cubrid.CUBRIDDataType",
                                                                          "org.jooq.util.derby.DerbyDataType",
                                                                          "org.jooq.util.firebird.FirebirdDataType",
                                                                          "org.jooq.util.h2.H2DataType",
                                                                          "org.jooq.util.hsqldb.HSQLDBDataType",
                                                                          "org.jooq.util.mariadb.MariaDBDataType",
                                                                          "org.jooq.util.mysql.MySQLDataType",
                                                                          "org.jooq.util.postgres.PostgresDataType",
                                                                          "org.jooq.util.sqlite.SQLiteDataType",
                                                                          "org.jooq.impl.SQLDataType",
                                                                          "org.jooq.impl.AbstractField",
                                                                          "org.jooq.impl.TableFieldImpl",
                                                                          "org.jooq.tools.StringUtils",
                                                                          "com.chiralbehaviors.CoRE.postgres.PostgresJSONJacksonJsonNodeBinding",
                                                                          "org.jooq.impl.ConvertedDataType",
                                                                          "com.chiralbehaviors.CoRE.postgres.PostgresJSONJacksonJsonNodeConverter",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttribute",
                                                                          "com.chiralbehaviors.CoRE.jooq.enums.Cardinality",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.AgencyExistentialGrouping",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ChildSequencingAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.enums.ExistentialDomain",
                                                                          "com.chiralbehaviors.CoRE.jooq.enums.ValueType",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.Existential",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialAttributeAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetwork",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAttribute",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ExistentialNetworkAttributeAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.Facet",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.Job",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.JobChronology",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.MetaProtocol",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.NetworkInference",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.ParentSequencingAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.Protocol",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.SelfSequencingAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.SiblingSequencingAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.StatusCodeSequencing",
                                                                          "com.chiralbehaviors.CoRE.jooq.enums.ReferenceType",
                                                                          "com.chiralbehaviors.CoRE.jooq.tables.WorkspaceAuthorization",
                                                                          "com.chiralbehaviors.CoRE.jooq.Tables",
                                                                          "org.jooq.impl.RowImpl");
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
                                     "/var/folders/_r/y4_0rwd16zgblwjq7b_tbhk80000gn/T/EvoSuite_pathingJar4624945833138405438.jar");
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
