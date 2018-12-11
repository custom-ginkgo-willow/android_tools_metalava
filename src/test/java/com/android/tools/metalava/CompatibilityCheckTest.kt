/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.metalava

import org.junit.Ignore
import org.junit.Test
import java.io.File

class
CompatibilityCheckTest : DriverTest() {
    @Test
    fun `Change between class and interface`() {
        check(
            warnings = """
                TESTROOT/load-api.txt:2: error: Class test.pkg.MyTest1 changed class/interface declaration [ChangedClass:23]
                TESTROOT/load-api.txt:4: error: Class test.pkg.MyTest2 changed class/interface declaration [ChangedClass:23]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class MyTest1 {
                  }
                  public interface MyTest2 {
                  }
                  public class MyTest3 {
                  }
                  public interface MyTest4 {
                  }
                }
                """,
            // MyTest1 and MyTest2 reversed from class to interface or vice versa, MyTest3 and MyTest4 unchanged
            signatureSource = """
                package test.pkg {
                  public interface MyTest1 {
                  }
                  public class MyTest2 {
                  }
                  public class MyTest3 {
                  }
                  public interface MyTest4 {
                  }
                }
                """
        )
    }

    @Test
    fun `Interfaces should not be dropped`() {
        check(
            warnings = """
                TESTROOT/load-api.txt:2: error: Class test.pkg.MyTest1 changed class/interface declaration [ChangedClass:23]
                TESTROOT/load-api.txt:4: error: Class test.pkg.MyTest2 changed class/interface declaration [ChangedClass:23]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class MyTest1 {
                  }
                  public interface MyTest2 {
                  }
                  public class MyTest3 {
                  }
                  public interface MyTest4 {
                  }
                }
                """,
            // MyTest1 and MyTest2 reversed from class to interface or vice versa, MyTest3 and MyTest4 unchanged
            signatureSource = """
                package test.pkg {
                  public interface MyTest1 {
                  }
                  public class MyTest2 {
                  }
                  public class MyTest3 {
                  }
                  public interface MyTest4 {
                  }
                }
                """
        )
    }

    @Test
    fun `Ensure warnings for removed APIs`() {
        check(
            warnings = """
                TESTROOT/current-api.txt:3: error: Removed method test.pkg.MyTest1.method(Float) [RemovedMethod:9]
                TESTROOT/current-api.txt:4: error: Removed field test.pkg.MyTest1.field [RemovedField:10]
                TESTROOT/current-api.txt:6: error: Removed class test.pkg.MyTest2 [RemovedClass:8]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class MyTest1 {
                    method public Double method(Float);
                    field public Double field;
                  }
                  public class MyTest2 {
                    method public Double method(Float);
                    field public Double field;
                  }
                }
                package test.pkg.other {
                }
                """,
            signatureSource = """
                package test.pkg {
                  public class MyTest1 {
                  }
                }
                """
        )
    }

    @Test
    fun `Flag invalid nullness changes`() {
        check(
            warnings = """
                TESTROOT/load-api.txt:5: error: Attempted to remove @Nullable annotation from method test.pkg.MyTest.convert3(Float) [InvalidNullConversion:135]
                TESTROOT/load-api.txt:5: error: Attempted to remove @Nullable annotation from parameter arg1 in test.pkg.MyTest.convert3(Float arg1) [InvalidNullConversion:135]
                TESTROOT/load-api.txt:6: error: Attempted to remove @NonNull annotation from method test.pkg.MyTest.convert4(Float) [InvalidNullConversion:135]
                TESTROOT/load-api.txt:6: error: Attempted to remove @NonNull annotation from parameter arg1 in test.pkg.MyTest.convert4(Float arg1) [InvalidNullConversion:135]
                TESTROOT/load-api.txt:7: error: Attempted to change parameter from @Nullable to @NonNull: incompatible change for parameter arg1 in test.pkg.MyTest.convert5(Float arg1) [InvalidNullConversion:135]
                TESTROOT/load-api.txt:8: error: Attempted to change method return from @NonNull to @Nullable: incompatible change for method test.pkg.MyTest.convert6(Float) [InvalidNullConversion:135]
                """,
            compatibilityMode = false,
            outputKotlinStyleNulls = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class MyTest {
                    method public Double convert1(Float);
                    method public Double convert2(Float);
                    method @Nullable public Double convert3(@Nullable Float);
                    method @NonNull public Double convert4(@NonNull Float);
                    method @Nullable public Double convert5(@Nullable Float);
                    method @NonNull public Double convert6(@NonNull Float);
                  }
                }
                """,
            // Changes: +nullness, -nullness, nullable->nonnull, nonnull->nullable
            signatureSource = """
                package test.pkg {
                  public class MyTest {
                    method @Nullable public Double convert1(@Nullable Float);
                    method @NonNull public Double convert2(@NonNull Float);
                    method public Double convert3(Float);
                    method public Double convert4(Float);
                    method @NonNull public Double convert5(@NonNull Float);
                    method @Nullable public Double convert6(@Nullable Float);
                  }
                }
                """
        )
    }

    @Test
    fun `Kotlin Nullness`() {
        check(
            warnings = """
                src/test/pkg/Outer.kt:5: error: Attempted to change method return from @NonNull to @Nullable: incompatible change for method test.pkg.Outer.method2(String,String) [InvalidNullConversion:135]
                src/test/pkg/Outer.kt:5: error: Attempted to change parameter from @Nullable to @NonNull: incompatible change for parameter string in test.pkg.Outer.method2(String string, String maybeString) [InvalidNullConversion:135]
                src/test/pkg/Outer.kt:6: error: Attempted to change parameter from @Nullable to @NonNull: incompatible change for parameter string in test.pkg.Outer.method3(String maybeString, String string) [InvalidNullConversion:135]
                src/test/pkg/Outer.kt:8: error: Attempted to change method return from @NonNull to @Nullable: incompatible change for method test.pkg.Outer.Inner.method2(String,String) [InvalidNullConversion:135]
                src/test/pkg/Outer.kt:8: error: Attempted to change parameter from @Nullable to @NonNull: incompatible change for parameter string in test.pkg.Outer.Inner.method2(String string, String maybeString) [InvalidNullConversion:135]
                src/test/pkg/Outer.kt:9: error: Attempted to change parameter from @Nullable to @NonNull: incompatible change for parameter string in test.pkg.Outer.Inner.method3(String maybeString, String string) [InvalidNullConversion:135]
                """,
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            outputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                    package test.pkg {
                      public final class Outer {
                        ctor public Outer();
                        method public final String? method1(String, String?);
                        method public final String method2(String?, String);
                        method public final String? method3(String, String?);
                      }
                      public static final class Outer.Inner {
                        ctor public Outer.Inner();
                        method public final String method2(String?, String);
                        method public final String? method3(String, String?);
                      }
                    }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    class Outer {
                        fun method1(string: String, maybeString: String?): String? = null
                        fun method2(string: String, maybeString: String?): String? = null
                        fun method3(maybeString: String?, string : String): String = ""
                        class Inner {
                            fun method2(string: String, maybeString: String?): String? = null
                            fun method3(maybeString: String?, string : String): String = ""
                        }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Java Parameter Name Change`() {
        check(
            warnings = """
                src/test/pkg/JavaClass.java:6: error: Attempted to remove parameter name from parameter newName in test.pkg.JavaClass.method1 in method test.pkg.JavaClass.method1 [ParameterNameChange:136]
                src/test/pkg/JavaClass.java:7: error: Attempted to change parameter name from secondParameter to newName in method test.pkg.JavaClass.method2 [ParameterNameChange:136]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class JavaClass {
                    ctor public JavaClass();
                    method public String method1(String parameterName);
                    method public String method2(String firstParameter, String secondParameter);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    @Suppress("all")
                    package test.pkg;
                    import androidx.annotation.ParameterName;

                    public class JavaClass {
                        public String method1(String newName) { return null; }
                        public String method2(@ParameterName("firstParameter") String s, @ParameterName("newName") String prevName) { return null; }
                    }
                    """
                ),
                supportParameterName
            ),
            extraArguments = arrayOf(ARG_HIDE_PACKAGE, "androidx.annotation")
        )
    }

    @Test
    fun `Kotlin Parameter Name Change`() {
        check(
            warnings = """
                src/test/pkg/KotlinClass.kt:4: error: Attempted to change parameter name from prevName to newName in method test.pkg.KotlinClass.method1 [ParameterNameChange:136]
                """,
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            outputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class KotlinClass {
                    ctor public KotlinClass();
                    method public final String? method1(String prevName);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    class KotlinClass {
                        fun method1(newName: String): String? = null
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Add flag new methods but not overrides from platform`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:6: error: Added method test.pkg.MyClass.method2(String) [AddedMethod:4]
                src/test/pkg/MyClass.java:7: error: Added field test.pkg.MyClass.newField [AddedField:5]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class MyClass {
                    method public String method1(String);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public class MyClass  {
                        private MyClass() { }
                        public String method1(String newName) { return null; }
                        public String method2(String newName) { return null; }
                        public int newField = 5;
                        public String toString() { return "Hello World"; }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Remove operator`() {
        check(
            warnings = """
                src/test/pkg/Foo.kt:4: error: Cannot remove `operator` modifier from method test.pkg.Foo.plus(String): Incompatible change [OperatorRemoval:137]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Foo {
                    ctor public Foo();
                    method public final operator void plus(String s);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    class Foo {
                        fun plus(s: String) { }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Remove vararg`() {
        check(
            warnings = """
                src/test/pkg/test.kt:3: error: Changing from varargs to array is an incompatible change: parameter x in test.pkg.TestKt.method2(int[] x) [VarargRemoval:139]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class TestKt {
                    ctor public TestKt();
                    method public static final void method1(int[] x);
                    method public static final void method2(int... x);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg
                    fun method1(vararg x: Int) { }
                    fun method2(x: IntArray) { }
                    """
                )
            )
        )
    }

    @Test
    fun `Add final`() {
        // Adding final on class or method is incompatible; adding it on a parameter is fine.
        // Field is iffy.
        check(
            warnings = """
                src/test/pkg/Java.java:4: error: Method test.pkg.Java.method has added 'final' qualifier [AddedFinal:13]
                src/test/pkg/Kotlin.kt:4: error: Method test.pkg.Kotlin.method has added 'final' qualifier [AddedFinal:13]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Java {
                    method public void method(int);
                  }
                  public class Kotlin {
                    ctor public Kotlin();
                    method public void method(String s);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    open class Kotlin {
                        fun method(s: String) { }
                    }
                    """
                ),
                java(
                    """
                        package test.pkg;
                        public class Java {
                            private Java() { }
                            public final void method(final int parameter) { }
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Inherited final`() {
        // Make sure that we correctly compare effectively final (inherited from surrounding class)
        // between the signature file codebase and the real codebase
        check(
            warnings = """
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Cls extends test.pkg.Parent {
                  }
                  public class Parent {
                    method public void method(int);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                        package test.pkg;
                        public final class Cls extends Parent {
                            private Cls() { }
                            @Override public void method(final int parameter) { }
                        }
                        """
                ),
                java(
                    """
                        package test.pkg;
                        public class Parent {
                            private Parent() { }
                            public void method(final int parameter) { }
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Implicit concrete`() {
        // Doclava signature files sometimes leave out overridden methods of
        // abstract methods. We don't want to list these as having changed
        // their abstractness.
        check(
            warnings = """
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Cls extends test.pkg.Parent {
                  }
                  public class Parent {
                    method public abstract void method(int);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                        package test.pkg;
                        public final class Cls extends Parent {
                            private Cls() { }
                            @Override public void method(final int parameter) { }
                        }
                        """
                ),
                java(
                    """
                        package test.pkg;
                        public class Parent {
                            private Parent() { }
                            public abstract void method(final int parameter);
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Implicit modifiers from inherited super classes`() {
        check(
            warnings = """
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Cls implements test.pkg.Interface {
                    method public void method(int);
                    method public final void method2(int);
                  }
                  public interface Interface {
                    method public void method2(int);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                        package test.pkg;
                        public final class Cls extends HiddenParent implements Interface {
                            private Cls() { }
                            @Override public void method(final int parameter) { }
                        }
                        """
                ),
                java(
                    """
                        package test.pkg;
                        class HiddenParent {
                            private HiddenParent() { }
                            public abstract void method(final int parameter) { }
                            public final void method2(final int parameter) { }
                        }
                        """
                ),
                java(
                    """
                        package test.pkg;
                        public interface Interface {
                            void method2(final int parameter) { }
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Wildcard comparisons`() {
        // Doclava signature files sometimes leave out overridden methods of
        // abstract methods. We don't want to list these as having changed
        // their abstractness.
        check(
            warnings = """
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class AbstractMap<K, V> implements java.util.Map {
                    method public java.util.Set<K> keySet();
                    method public V put(K, V);
                    method public void putAll(java.util.Map<? extends K, ? extends V>);
                  }
                  public abstract class EnumMap<K extends java.lang.Enum<K>, V> extends test.pkg.AbstractMap  {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                        package test.pkg;
                        @SuppressWarnings({"ConstantConditions", "NullableProblems"})
                        public abstract class AbstractMap<K, V> implements java.util.Map {
                            private AbstractMap() { }
                            public V put(K k, V v) { return null; }
                            public java.util.Set<K> keySet() { return null; }
                            public V put(K k, V v) { return null; }
                            public void putAll(java.util.Map<? extends K, ? extends V> x) { }
                        }
                        """
                ),
                java(
                    """
                        package test.pkg;
                        public abstract class EnumMap<K extends java.lang.Enum<K>, V> extends test.pkg.AbstractMap  {
                            private EnumMap() { }
                            public V put(K k, V v) { return null; }
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Added constructor`() {
        // Regression test for issue 116619591
        check(
            warnings = "src/test/pkg/AbstractMap.java:2: error: Added constructor test.pkg.AbstractMap() [AddedMethod:4]",
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class AbstractMap<K, V> implements java.util.Map {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                        package test.pkg;
                        @SuppressWarnings({"ConstantConditions", "NullableProblems"})
                        public abstract class AbstractMap<K, V> implements java.util.Map {
                        }
                        """
                )
            )
        )
    }

    @Test
    fun `Remove infix`() {
        check(
            warnings = """
                src/test/pkg/Foo.kt:5: error: Cannot remove `infix` modifier from method test.pkg.Foo.add2(String): Incompatible change [InfixRemoval:138]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Foo {
                    ctor public Foo();
                    method public final void add1(String s);
                    method public final infix void add2(String s);
                    method public final infix void add3(String s);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    class Foo {
                        infix fun add1(s: String) { }
                        fun add2(s: String) { }
                        infix fun add3(s: String) { }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Add seal`() {
        check(
            warnings = """
                src/test/pkg/Foo.kt: error: Cannot add 'sealed' modifier to class test.pkg.Foo: Incompatible change [AddSealed:140]
                """,
            compatibilityMode = false,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Foo {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg
                    sealed class Foo
                    """
                )
            )
        )
    }

    @Test
    fun `Remove default parameter`() {
        check(
            warnings = """
                src/test/pkg/Foo.kt:7: error: Attempted to remove default value from parameter s1 in test.pkg.Foo.method4 in method test.pkg.Foo.method4 [DefaultValueChange:144]
                """,
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class Foo {
                    ctor public Foo();
                    method public final void method1(boolean b, String? s1);
                    method public final void method2(boolean b, String? s1);
                    method public final void method3(boolean b, String? s1 = "null");
                    method public final void method4(boolean b, String? s1 = "null");
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package test.pkg

                    class Foo {
                        fun method1(b: Boolean, s1: String?) { }         // No change
                        fun method2(b: Boolean, s1: String? = null) { }  // Adding: OK
                        fun method3(b: Boolean, s1: String? = null) { }  // No change
                        fun method4(b: Boolean, s1: String?) { }         // Removed
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Removing method or field when still available via inheritance is OK`() {
        check(
            warnings = """
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Child extends test.pkg.Parent {
                    ctor public Child();
                    field public int field1;
                    method public void method1();
                  }
                  public class Parent {
                    ctor public Parent();
                    field public int field1;
                    field public int field2;
                    method public void method1();
                    method public void method2();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public class Parent {
                        public int field1 = 0;
                        public int field2 = 0;
                        public void method1() { }
                        public void method2() { }
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public class Child extends Parent {
                        public int field1 = 0;
                        @Override public void method1() { } // NO CHANGE
                        //@Override public void method2() { } // REMOVED OK: Still inherited
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Change field constant value, change field type`() {
        check(
            warnings = """
                src/test/pkg/Parent.java:5: error: Field test.pkg.Parent.field2 has changed value from 2 to 42 [ChangedValue:17]
                src/test/pkg/Parent.java:6: error: Field test.pkg.Parent.field3 has changed type from int to char [ChangedType:16]
                src/test/pkg/Parent.java:7: error: Field test.pkg.Parent.field4 has added 'final' qualifier [AddedFinal:13]
                src/test/pkg/Parent.java:8: error: Field test.pkg.Parent.field5 has changed 'static' qualifier [ChangedStatic:12]
                src/test/pkg/Parent.java:9: error: Field test.pkg.Parent.field6 has changed 'transient' qualifier [ChangedTransient:14]
                src/test/pkg/Parent.java:10: error: Field test.pkg.Parent.field7 has changed 'volatile' qualifier [ChangedVolatile:15]
                src/test/pkg/Parent.java:11: error: Field test.pkg.Parent.field8 has changed deprecation state true --> false [ChangedDeprecated:24]
                src/test/pkg/Parent.java:12: error: Field test.pkg.Parent.field9 has changed deprecation state false --> true [ChangedDeprecated:24]
                src/test/pkg/Parent.java:19: error: Field test.pkg.Parent.field94 has changed value from 1 to 42 [ChangedValue:17]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Parent {
                    ctor public Parent();
                    field public static final int field1 = 1; // 0x1
                    field public static final int field2 = 2; // 0x2
                    field public int field3;
                    field public int field4 = 4; // 0x4
                    field public int field5;
                    field public int field6;
                    field public int field7;
                    field public deprecated int field8;
                    field public int field9;
                    field public static final int field91 = 1; // 0x1
                    field public static final int field92 = 1; // 0x1
                    field public static final int field93 = 1; // 0x1
                    field public static final int field94 = 1; // 0x1
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;
                    import android.annotation.SuppressLint;
                    public class Parent {
                        public static final int field1 = 1;  // UNCHANGED
                        public static final int field2 = 42; // CHANGED VALUE
                        public char field3 = 3;              // CHANGED TYPE
                        public final int field4 = 4;         // ADDED FINAL
                        public static int field5 = 5;        // ADDED STATIC
                        public transient int field6 = 6;     // ADDED TRANSIENT
                        public volatile int field7 = 7;      // ADDED VOLATILE
                        public int field8 = 8;               // REMOVED DEPRECATED
                        /** @deprecated */ @Deprecated public int field9 = 8;  // ADDED DEPRECATED
                        @SuppressLint("ChangedValue")
                        public static final int field91 = 42;// CHANGED VALUE: Suppressed
                        @SuppressLint("ChangedValue:Field test.pkg.Parent.field92 has changed value from 1 to 42")
                        public static final int field92 = 42;// CHANGED VALUE: Suppressed with same message
                        @SuppressLint("ChangedValue: Field test.pkg.Parent.field93 has changed value from 1 to 42")
                        public static final int field93 = 42;// CHANGED VALUE: Suppressed with same message
                        @SuppressLint("ChangedValue:Field test.pkg.Parent.field94 has changed value from 10 to 1")
                        public static final int field94 = 42;// CHANGED VALUE: Suppressed but with different message
                    }
                    """
                ),
                suppressLintSource
            ),
            extraArguments = arrayOf(ARG_HIDE_PACKAGE, "android.annotation")
        )
    }

    @Test
    fun `Change annotation default method value change`() {
        check(
            inputKotlinStyleNulls = true,
            warnings = """
                src/test/pkg/ExportedProperty.java:15: error: Method test.pkg.ExportedProperty.category has changed value from "" to nothing [ChangedValue:17]
                src/test/pkg/ExportedProperty.java:14: error: Method test.pkg.ExportedProperty.floating has changed value from 1.0f to 1.1f [ChangedValue:17]
                src/test/pkg/ExportedProperty.java:16: error: Method test.pkg.ExportedProperty.formatToHexString has changed value from nothing to false [ChangedValue:17]
                src/test/pkg/ExportedProperty.java:13: error: Method test.pkg.ExportedProperty.prefix has changed value from "" to "hello" [ChangedValue:17]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public @interface ExportedProperty {
                    method public abstract boolean resolveId() default false;
                    method public abstract float floating() default 1.0f;
                    method public abstract String! prefix() default "";
                    method public abstract String! category() default "";
                    method public abstract boolean formatToHexString();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Retention;
                    import java.lang.annotation.RetentionPolicy;
                    import java.lang.annotation.Target;
                    import static java.lang.annotation.RetentionPolicy.SOURCE;

                    @Target({ElementType.FIELD, ElementType.METHOD})
                    @Retention(RetentionPolicy.RUNTIME)
                    public @interface ExportedProperty {
                        boolean resolveId() default false;            // UNCHANGED
                        String prefix() default "hello";              // CHANGED VALUE
                        float floating() default 1.1f;                // CHANGED VALUE
                        String category();                            // REMOVED VALUE
                        boolean formatToHexString() default false;    // ADDED VALUE
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- class to interface`() {
        check(
            warnings = """
                src/test/pkg/Parent.java:3: error: Class test.pkg.Parent changed class/interface declaration [ChangedClass:23]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Parent {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public interface Parent {
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- change implemented interfaces`() {
        check(
            warnings = """
                src/test/pkg/Parent.java:3: error: Class test.pkg.Parent no longer implements java.io.Closeable [RemovedInterface:11]
                src/test/pkg/Parent.java:3: error: Added interface java.util.List to class class test.pkg.Parent [AddedInterface:6]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class Parent implements java.io.Closeable, java.util.Map {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class Parent implements java.util.Map, java.util.List {
                        private Parent() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- change qualifiers`() {
        check(
            warnings = """
                src/test/pkg/Parent.java:3: error: Class test.pkg.Parent changed 'abstract' qualifier [ChangedAbstract:20]
                src/test/pkg/Parent.java:3: error: Class test.pkg.Parent changed 'static' qualifier [ChangedStatic:12]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Parent {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract static class Parent {
                        private Parent() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- final`() {
        check(
            warnings = """
                src/test/pkg/Class1.java:3: error: Class test.pkg.Class1 added 'final' qualifier [AddedFinal:13]
                TESTROOT/current-api.txt:3: error: Removed constructor test.pkg.Class1() [RemovedMethod:9]
                src/test/pkg/Class2.java:3: error: Class test.pkg.Class2 added 'final' qualifier but was previously uninstantiable and therefore could not be subclassed [AddedFinalUninstantiable:26]
                src/test/pkg/Class3.java:3: error: Class test.pkg.Class3 removed 'final' qualifier [RemovedFinal:27]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Class1 {
                      ctor public Class1();
                  }
                  public class Class2 {
                  }
                  public final class Class3 {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public final class Class1 {
                        private Class1() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public final class Class2 {
                        private Class2() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public class Class3 {
                        private Class3() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- visibility`() {
        check(
            warnings = """
                src/test/pkg/Class1.java:3: error: Class test.pkg.Class1 changed visibility from protected to public [ChangedScope:19]
                src/test/pkg/Class2.java:3: error: Class test.pkg.Class2 changed visibility from public to protected [ChangedScope:19]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  protected class Class1 {
                  }
                  public class Class2 {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public class Class1 {
                        private Class1() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    protected class Class2 {
                        private Class2() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- deprecation`() {
        check(
            warnings = """
                src/test/pkg/Class1.java:3: error: Class test.pkg.Class1 has changed deprecation state false --> true [ChangedDeprecated:24]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Class1 {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    /** @deprecated */
                    @Deprecated public class Class1 {
                        private Class1() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- superclass`() {
        check(
            warnings = """
                src/test/pkg/Class3.java:3: error: Class test.pkg.Class3 superclass changed from java.lang.Char to java.lang.Number [ChangedSuperclass:18]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class Class1 {
                  }
                  public abstract class Class2 extends java.lang.Number {
                  }
                  public abstract class Class3 extends java.lang.Char {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class Class1 extends java.lang.Short {
                        private Class1() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public abstract class Class2 extends java.lang.Float {
                        private Class2() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public abstract class Class3 extends java.lang.Number {
                        private Class3() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible class change -- type variables`() {
        check(
            warnings = """
                src/test/pkg/Class1.java:3: error: Class test.pkg.Class1 changed number of type parameters from 1 to 2 [ChangedType:16]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Class1<X> {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public class Class1<X,Y> {
                        private Class1() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible method change -- modifiers`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:5: error: Method test.pkg.MyClass.myMethod2 has changed 'abstract' qualifier [ChangedAbstract:20]
                src/test/pkg/MyClass.java:6: error: Method test.pkg.MyClass.myMethod3 has changed 'static' qualifier [ChangedStatic:12]
                src/test/pkg/MyClass.java:7: error: Method test.pkg.MyClass.myMethod4 has changed deprecation state true --> false [ChangedDeprecated:24]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass {
                      method public void myMethod2();
                      method public void myMethod3();
                      method deprecated public void myMethod4();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                        public native abstract void myMethod2(); // Note that Errors.CHANGE_NATIVE is hidden by default
                        public static void myMethod3() {}
                        public void myMethod4() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible method change -- final`() {
        check(
            warnings = """
                src/test/pkg/Outer.java:7: error: Method test.pkg.Outer.Class1.method1 has added 'final' qualifier [AddedFinal:13]
                src/test/pkg/Outer.java:19: error: Method test.pkg.Outer.Class4.method4 has removed 'final' qualifier [RemovedFinal:27]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class Outer {
                  }
                  public class Outer.Class1 {
                    method public void method1();
                  }
                  public final class Outer.Class2 {
                    method public void method2();
                  }
                  public final class Outer.Class3 {
                    method public void method3();
                  }
                  public class Outer.Class4 {
                    method public final void method4();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class Outer {
                        private Outer() {}
                        public class Class1 {
                            private Class1() {}
                            public final void method1() { } // Added final
                        }
                        public final class Class2 {
                            private Class2() {}
                            public final void method2() { } // Added final but class is effectively final so no change
                        }
                        public final class Class3 {
                            private Class3() {}
                            public void method3() { } // Removed final but is still effectively final
                        }
                        public class Class4 {
                            private Class4() {}
                            public void method4() { } // Removed final
                        }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible method change -- visibility`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:5: error: Method test.pkg.MyClass.myMethod1 changed visibility from protected to public [ChangedScope:19]
                src/test/pkg/MyClass.java:6: error: Method test.pkg.MyClass.myMethod2 changed visibility from public to protected [ChangedScope:19]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass {
                      method protected void myMethod1();
                      method public void myMethod2();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                        public void myMethod1() {}
                        protected void myMethod2() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible method change -- throws list`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:7: error: Method test.pkg.MyClass.method1 added thrown exception java.io.IOException [ChangedThrows:21]
                src/test/pkg/MyClass.java:8: error: Method test.pkg.MyClass.method2 no longer throws exception java.io.IOException [ChangedThrows:21]
                src/test/pkg/MyClass.java:9: error: Method test.pkg.MyClass.method3 no longer throws exception java.io.IOException [ChangedThrows:21]
                src/test/pkg/MyClass.java:9: error: Method test.pkg.MyClass.method3 no longer throws exception java.lang.NumberFormatException [ChangedThrows:21]
                src/test/pkg/MyClass.java:9: error: Method test.pkg.MyClass.method3 added thrown exception java.lang.UnsupportedOperationException [ChangedThrows:21]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass {
                      method public void finalize() throws java.lang.Throwable;
                      method public void method1();
                      method public void method2() throws java.io.IOException;
                      method public void method3() throws java.io.IOException, java.lang.NumberFormatException;
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    @SuppressWarnings("RedundantThrows")
                    public abstract class MyClass {
                        private MyClass() {}
                        public void finalize() {}
                        public void method1() throws java.io.IOException {}
                        public void method2() {}
                        public void method3() throws java.lang.UnsupportedOperationException {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible method change -- return types`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:5: error: Method test.pkg.MyClass.method1 has changed return type from float to int [ChangedType:16]
                src/test/pkg/MyClass.java:6: error: Method test.pkg.MyClass.method2 has changed return type from java.util.List<Number> to java.util.List<java.lang.Integer> [ChangedType:16]
                src/test/pkg/MyClass.java:7: error: Method test.pkg.MyClass.method3 has changed return type from java.util.List<Integer> to java.util.List<java.lang.Number> [ChangedType:16]
                src/test/pkg/MyClass.java:8: error: Method test.pkg.MyClass.method4 has changed return type from String to String[] [ChangedType:16]
                src/test/pkg/MyClass.java:9: error: Method test.pkg.MyClass.method5 has changed return type from String[] to String[][] [ChangedType:16]
                src/test/pkg/MyClass.java:10: error: Method test.pkg.MyClass.method6 has changed return type from T (extends java.lang.Object) to U (extends java.lang.Number) [ChangedType:16]
                src/test/pkg/MyClass.java:11: error: Method test.pkg.MyClass.method7 has changed return type from T to Number [ChangedType:16]
                src/test/pkg/MyClass.java:13: error: Method test.pkg.MyClass.method9 has changed return type from X (extends java.lang.Throwable) to U (extends java.lang.Number) [ChangedType:16]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass<T extends Number> {
                      method public float method1();
                      method public java.util.List<Number> method2();
                      method public java.util.List<Integer> method3();
                      method public String method4();
                      method public String[] method5();
                      method public <X extends java.lang.Throwable> T method6(java.util.function.Supplier<? extends X>);
                      method public <X extends java.lang.Throwable> T method7(java.util.function.Supplier<? extends X>);
                      method public <X extends java.lang.Throwable> Number method8(java.util.function.Supplier<? extends X>);
                      method public <X extends java.lang.Throwable> X method9(java.util.function.Supplier<? extends X>);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass<U extends Number> { // Changing type variable name is fine/compatible
                        private MyClass() {}
                        public int method1() { return 0; }
                        public java.util.List<Integer> method2() { return null; }
                        public java.util.List<Number> method3() { return null; }
                        public String[] method4() { return null; }
                        public String[][] method5() { return null; }
                        public <X extends java.lang.Throwable> U method6(java.util.function.Supplier<? extends X> arg) { return null; }
                        public <X extends java.lang.Throwable> Number method7(java.util.function.Supplier<? extends X> arg) { return null; }
                        public <X extends java.lang.Throwable> U method8(java.util.function.Supplier<? extends X> arg) { return null; }
                        public <X extends java.lang.Throwable> U method9(java.util.function.Supplier<? extends X> arg) { return null; }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Incompatible field change -- visibility and removing final`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:5: error: Field test.pkg.MyClass.myField1 changed visibility from protected to public [ChangedScope:19]
                src/test/pkg/MyClass.java:6: error: Field test.pkg.MyClass.myField2 changed visibility from public to protected [ChangedScope:19]
                src/test/pkg/MyClass.java:7: error: Field test.pkg.MyClass.myField3 has removed 'final' qualifier [RemovedFinal:27]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass {
                      field protected int myField1;
                      field public int myField2;
                      field public final int myField3;
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                        public int myField1 = 1;
                        protected int myField2 = 1;
                        public int myField3 = 1;
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Adding classes, interfaces and packages, and removing these`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:3: error: Added class test.pkg.MyClass [AddedClass:3]
                src/test/pkg/MyInterface.java:3: error: Added class test.pkg.MyInterface [AddedInterface:6]
                TESTROOT/current-api.txt:2: error: Removed class test.pkg.MyOldClass [RemovedClass:8]
                error: Added package test.pkg2 [AddedPackage:2]
                TESTROOT/current-api.txt:5: error: Removed package test.pkg3 [RemovedPackage:7]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyOldClass {
                  }
                }
                package test.pkg3 {
                  public abstract class MyOldClass {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public interface MyInterface {
                    }
                    """
                ),
                java(
                    """
                    package test.pkg2;

                    public abstract class MyClass2 {
                        private MyClass2() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Test removing public constructor`() {
        check(
            warnings = """
                TESTROOT/current-api.txt:3: error: Removed constructor test.pkg.MyClass() [RemovedMethod:9]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass {
                    ctor public MyClass();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Test type variables from text signature files`() {
        check(
            warnings = """
                src/test/pkg/MyClass.java:8: error: Method test.pkg.MyClass.myMethod4 has changed return type from S (extends java.lang.Object) to S (extends java.lang.Float) [ChangedType:16]
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public abstract class MyClass<T extends test.pkg.Number,T_SPLITR> {
                    method public T myMethod1();
                    method public <S extends test.pkg.Number> S myMethod2();
                    method public <S> S myMethod3();
                    method public <S> S myMethod4();
                    method public java.util.List<byte[]> myMethod5();
                    method public T_SPLITR[] myMethod6();
                  }
                  public class Number {
                    ctor public Number();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass<T extends Number,T_SPLITR> {
                        private MyClass() {}
                        public T myMethod1() { return null; }
                        public <S extends Number> S myMethod2() { return null; }
                        public <S> S myMethod3() { return null; }
                        public <S extends Float> S myMethod4() { return null; }
                        public java.util.List<byte[]> myMethod5() { return null; }
                        public T_SPLITR[] myMethod6() { return null; }
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;
                    public class Number {
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Test Kotlin extensions`() {
        check(
            inputKotlinStyleNulls = true,
            outputKotlinStyleNulls = true,
            omitCommonPackages = true,
            compatibilityMode = false,
            warnings = "",
            checkCompatibilityApi = """
                package androidx.content {
                  public final class ContentValuesKt {
                    ctor public ContentValuesKt();
                    method public static android.content.ContentValues contentValuesOf(kotlin.Pair<String,?>... pairs);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    "src/androidx/content/ContentValues.kt",
                    """
                    package androidx.content

                    import android.content.ContentValues

                    fun contentValuesOf(vararg pairs: Pair<String, Any?>) = ContentValues(pairs.size).apply {
                        for ((key, value) in pairs) {
                            when (value) {
                                null -> putNull(key)
                                is String -> put(key, value)
                                is Int -> put(key, value)
                                is Long -> put(key, value)
                                is Boolean -> put(key, value)
                                is Float -> put(key, value)
                                is Double -> put(key, value)
                                is ByteArray -> put(key, value)
                                is Byte -> put(key, value)
                                is Short -> put(key, value)
                                else -> {
                                    val valueType = value.javaClass.canonicalName
                                    throw IllegalArgumentException("Illegal value type")
                                }
                            }
                        }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Test Kotlin type bounds`() {
        check(
            inputKotlinStyleNulls = false,
            outputKotlinStyleNulls = true,
            omitCommonPackages = true,
            compatibilityMode = false,
            warnings = "",
            checkCompatibilityApi = """
                package androidx.navigation {
                  public final class NavDestination {
                    ctor public NavDestination();
                  }
                  public class NavDestinationBuilder<D extends androidx.navigation.NavDestination> {
                    ctor public NavDestinationBuilder(int id);
                    method public D build();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                kotlin(
                    """
                    package androidx.navigation

                    open class NavDestinationBuilder<out D : NavDestination>(
                            id: Int
                    ) {
                        open fun build(): D {
                            TODO()
                        }
                    }

                    class NavDestination
                    """
                )
            )
        )
    }

    @Test
    fun `Test inherited methods`() {
        check(
            warnings = """
                """,
            checkCompatibilityApi = """
                package test.pkg {
                  public class Child1 extends test.pkg.Parent {
                  }
                  public class Child2 extends test.pkg.Parent {
                    method public void method0(java.lang.String, int);
                    method public void method4(java.lang.String, int);
                  }
                  public class Child3 extends test.pkg.Parent {
                    method public void method1(java.lang.String, int);
                    method public void method2(java.lang.String, int);
                  }
                  public class Parent {
                    method public void method1(java.lang.String, int);
                    method public void method2(java.lang.String, int);
                    method public void method3(java.lang.String, int);
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public class Child1 extends Parent {
                        private Child1() {}
                        @Override
                        public void method1(String first, int second) {
                        }
                        @Override
                        public void method2(String first, int second) {
                        }
                        @Override
                        public void method3(String first, int second) {
                        }
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public class Child2 extends Parent {
                        private Child2() {}
                        @Override
                        public void method0(String first, int second) {
                        }
                        @Override
                        public void method1(String first, int second) {
                        }
                        @Override
                        public void method2(String first, int second) {
                        }
                        @Override
                        public void method3(String first, int second) {
                        }
                        @Override
                        public void method4(String first, int second) {
                        }
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public class Child3 extends Parent {
                        private Child3() {}
                        @Override
                        public void method1(String first, int second) {
                        }
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;
                    public class Parent {
                        private Parent() { }
                        public void method1(String first, int second) {
                        }
                        public void method2(String first, int second) {
                        }
                        public void method3(String first, int second) {
                        }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Partial text file which references inner classes not listed elsewhere`() {
        // This happens in system and test files where we only include APIs that differ
        // from the base IDE. When parsing these code bases we need to gracefully handle
        // references to inner classes.
        check(
            includeSystemApiAnnotations = true,
            warnings = """
                src/test/pkg/Bar.java:17: error: Added method test.pkg.Bar.Inner1.Inner2.addedMethod() to the system API [AddedMethod:4]
                TESTROOT/current-api.txt:4: error: Removed method test.pkg.Bar.Inner1.Inner2.removedMethod() [RemovedMethod:9]
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package other.pkg;

                    public class MyClass {
                        public class MyInterface {
                            public void test() { }
                        }
                    }
                    """
                ).indented(),
                java(
                    """
                    package test.pkg;
                    import android.annotation.SystemApi;

                    public class Bar {
                        public class Inner1 {
                            private Inner1() { }
                            @SuppressWarnings("JavaDoc")
                            public class Inner2 {
                                private Inner2() { }

                                /**
                                 * @hide
                                 */
                                @SystemApi
                                public void method() { }

                                /**
                                 * @hide
                                 */
                                @SystemApi
                                public void addedMethod() { }
                            }
                        }
                    }
                    """
                ),
                systemApiSource
            ),

            extraArguments = arrayOf(
                ARG_SHOW_ANNOTATION, "android.annotation.TestApi",
                ARG_HIDE_PACKAGE, "android.annotation",
                ARG_HIDE_PACKAGE, "android.support.annotation"
            ),

            checkCompatibilityApi =
            """
                package test.pkg {
                  public class Bar.Inner1.Inner2 {
                    method public void method();
                    method public void removedMethod();
                  }
                }
                """
        )
    }

    @Test
    fun `Partial text file which adds methods to show-annotation API`() {
        // This happens in system and test files where we only include APIs that differ
        // from the base IDE. When parsing these code bases we need to gracefully handle
        // references to inner classes.
        check(
            includeSystemApiAnnotations = true,
            warnings = """
                TESTROOT/current-api.txt:4: error: Removed method android.rolecontrollerservice.RoleControllerService.onClearRoleHolders() [RemovedMethod:9]
                src/android/rolecontrollerservice/RoleControllerService.java:7: warning: Added method android.rolecontrollerservice.RoleControllerService.onGrantDefaultRoles() to the system API [AddedAbstractMethod:31]
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package android.rolecontrollerservice;

                    public class Service {
                    }
                    """
                ).indented(),
                java(
                    """
                    package android.rolecontrollerservice;
                    import android.annotation.SystemApi;

                    /** @hide */
                    @SystemApi
                    public abstract class RoleControllerService extends Service {
                        public abstract void onGrantDefaultRoles();
                    }
                    """
                ),
                systemApiSource
            ),

            extraArguments = arrayOf(
                ARG_SHOW_ANNOTATION, "android.annotation.TestApi",
                ARG_HIDE_PACKAGE, "android.annotation",
                ARG_HIDE_PACKAGE, "android.support.annotation"
            ),

            checkCompatibilityApi =
                """
                package android.rolecontrollerservice {
                  public abstract class RoleControllerService extends android.rolecontrollerservice.Service {
                    ctor public RoleControllerService();
                    method public abstract void onClearRoleHolders();
                  }
                }
                """
        )
    }

    @Test
    fun `Test verifying simple removed API`() {
        check(
            warnings = """
                src/test/pkg/Bar.java:8: error: Added method test.pkg.Bar.newlyRemoved() to the removed API [AddedMethod:4]
                """,
            checkCompatibilityRemovedApiCurrent = """
                package test.pkg {
                  public class Bar {
                    ctor public Bar();
                    method public void removedMethod();
                  }
                  public class Bar.Inner {
                    ctor public Bar.Inner();
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;
                    @SuppressWarnings("JavaDoc")
                    public class Bar {
                        /** @removed */
                        public Bar() { }
                        // No longer removed: /** @removed */
                        public void removedMethod() { }
                        /** @removed */
                        public void newlyRemoved() { }

                        public void newlyAdded() { }

                        /** @removed */
                        public class Inner { }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Test verifying removed API`() {
        check(
            warnings = """
                """,
            checkCompatibilityRemovedApiCurrent = """
                package test.pkg {
                  public class Bar {
                    ctor public Bar();
                    method public void removedMethod();
                    field public int removedField;
                  }
                  public class Bar.Inner {
                    ctor public Bar.Inner();
                  }
                  public class Bar.Inner2.Inner3.Inner4 {
                    ctor public Bar.Inner2.Inner3.Inner4();
                  }
                  public class Bar.Inner5.Inner6.Inner7 {
                    field public int removed;
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;
                    @SuppressWarnings("JavaDoc")
                    public class Bar {
                        /** @removed */
                        public Bar() { }
                        public int field;
                        public void test() { }
                        /** @removed */
                        public int removedField;
                        /** @removed */
                        public void removedMethod() { }
                        /** @removed and @hide - should not be listed */
                        public int hiddenField;

                        /** @removed */
                        public class Inner { }

                        public class Inner2 {
                            public class Inner3 {
                                /** @removed */
                                public class Inner4 { }
                            }
                        }

                        public class Inner5 {
                            public class Inner6 {
                                public class Inner7 {
                                    /** @removed */
                                    public int removed;
                                }
                            }
                        }
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Regression test for bug 120847535`() {
        // Regression test for
        // 120847535: check-api doesn't fail on method that is in current.txt, but marked @hide @TestApi
        check(
            warnings = """
                TESTROOT/current-api.txt:6: error: Removed method test.view.ViewTreeObserver.registerFrameCommitCallback(Runnable) [RemovedMethod:9]
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.view;
                    import android.annotation.TestApi;
                    public final class ViewTreeObserver {
                         /**
                         * @hide
                         */
                        @TestApi
                        public void registerFrameCommitCallback(Runnable callback) {
                        }
                    }
                    """
                ).indented(),
                java(
                    """
                    package test.view;
                    public final class View {
                        private View() { }
                    }
                    """
                ).indented(),
                testApiSource
            ),

            api = """
                package test.view {
                  public final class View {
                  }
                  public final class ViewTreeObserver {
                    ctor public ViewTreeObserver();
                  }
                }
            """,
            extraArguments = arrayOf(
                ARG_HIDE_PACKAGE, "android.annotation",
                ARG_HIDE_PACKAGE, "android.support.annotation"
            ),

            checkCompatibilityApi = """
                package test.view {
                  public final class View {
                  }
                  public final class ViewTreeObserver {
                    ctor public ViewTreeObserver();
                    method public void registerFrameCommitCallback(java.lang.Runnable);
                  }
                }
                """
        )
    }

    @Test
    fun `Test release compatibility checking`() {
        // Different checks are enforced for current vs release API comparisons:
        // we don't flag AddedClasses etc. Removed classes *are* enforced.
        check(
            warnings = """
                src/test/pkg/Class1.java:3: error: Class test.pkg.Class1 added 'final' qualifier [AddedFinal:13]
                TESTROOT/released-api.txt:3: error: Removed constructor test.pkg.Class1() [RemovedMethod:9]
                src/test/pkg/MyClass.java:5: warning: Method test.pkg.MyClass.myMethod2 has changed 'abstract' qualifier [ChangedAbstract:20]
                src/test/pkg/MyClass.java:6: error: Method test.pkg.MyClass.myMethod3 has changed 'static' qualifier [ChangedStatic:12]
                TESTROOT/released-api.txt:14: error: Removed class test.pkg.MyOldClass [RemovedClass:8]
                TESTROOT/released-api.txt:17: error: Removed package test.pkg3 [RemovedPackage:7]
                """,
            checkCompatibilityApiReleased = """
                package test.pkg {
                  public class Class1 {
                      ctor public Class1();
                  }
                  public class Class2 {
                  }
                  public final class Class3 {
                  }
                  public abstract class MyClass {
                      method public void myMethod2();
                      method public void myMethod3();
                      method deprecated public void myMethod4();
                  }
                  public abstract class MyOldClass {
                  }
                }
                package test.pkg3 {
                  public abstract class MyOldClass {
                  }
                }
                """,
            sourceFiles = *arrayOf(
                java(
                    """
                    package test.pkg;

                    public final class Class1 {
                        private Class1() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public final class Class2 {
                        private Class2() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public class Class3 {
                        private Class3() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public abstract class MyNewClass {
                        private MyNewClass() {}
                    }
                    """
                ),
                java(
                    """
                    package test.pkg;

                    public abstract class MyClass {
                        private MyClass() {}
                        public native abstract void myMethod2(); // Note that Errors.CHANGE_NATIVE is hidden by default
                        public static void myMethod3() {}
                        public void myMethod4() {}
                    }
                    """
                )
            )
        )
    }

    @Test
    fun `Implicit nullness`() {
        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                // Signature format: 2.0
                package androidx.annotation {
                  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS) @java.lang.annotation.Target({java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PACKAGE}) public @interface RestrictTo {
                    method public abstract androidx.annotation.RestrictTo.Scope[] value();
                  }

                  public enum RestrictTo.Scope {
                    enum_constant @Deprecated public static final androidx.annotation.RestrictTo.Scope GROUP_ID;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope LIBRARY;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope LIBRARY_GROUP;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope SUBCLASSES;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope TESTS;
                  }
                }
                """,

            sourceFiles = *arrayOf(
                restrictToSource
            )
        )
    }

    @Test
    fun `Implicit nullness in compat format`() {
        // Make sure we put "static" in enum modifier lists when in v1/compat mode
        check(
            compatibilityMode = true,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                // Signature format: 2.0
                package androidx.annotation {
                  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS) @java.lang.annotation.Target({java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PACKAGE}) public @interface RestrictTo {
                    method public abstract androidx.annotation.RestrictTo.Scope[] value();
                  }

                  public static enum RestrictTo.Scope {
                    enum_constant @Deprecated public static final androidx.annotation.RestrictTo.Scope GROUP_ID;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope LIBRARY;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope LIBRARY_GROUP;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope SUBCLASSES;
                    enum_constant public static final androidx.annotation.RestrictTo.Scope TESTS;
                  }
                }
                """,

            sourceFiles = *arrayOf(
                restrictToSource
            )
        )
    }

    @Test
    fun `Java String constants`() {
        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                package androidx.browser.browseractions {
                  public class BrowserActionsIntent {
                    field public static final String EXTRA_APP_ID = "androidx.browser.browseractions.APP_ID";
                  }
                }
                """,

            sourceFiles = *arrayOf(
                java(
                    """
                     package androidx.browser.browseractions;
                     public class BrowserActionsIntent {
                        private BrowserActionsIntent() { }
                        public static final String EXTRA_APP_ID = "androidx.browser.browseractions.APP_ID";

                     }
                    """
                ).indented()
            )
        )
    }

    @Test
    fun `Classes with maps`() {
        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                // Signature format: 2.0
                package androidx.collection {
                  public class SimpleArrayMap<K, V> {
                  }
                }
                """,

            sourceFiles = *arrayOf(
                java(
                    """
                    package androidx.collection;

                    public class SimpleArrayMap<K, V> {
                        private SimpleArrayMap() { }
                    }
                    """
                ).indented()
            )
        )
    }

    @Test
    fun `Referencing type parameters in types`() {
        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            checkCompatibilityApi = """
                // Signature format: 2.0
                package androidx.collection {
                  public class MyMap<Key, Value> {
                    ctor public MyMap();
                    field public Key! myField;
                    method public Key! getReplacement(Key!);
                  }
                }
                """,

            sourceFiles = *arrayOf(
                java(
                    """
                    package androidx.collection;

                    public class MyMap<Key, Value> {
                        public Key getReplacement(Key key) { return null; }
                        public Key myField = null;
                    }
                    """
                ).indented()
            )
        )
    }

    @Test
    fun `Adding and removing reified`() {
        check(
            compatibilityMode = false,
            inputKotlinStyleNulls = true,
            warnings = """
            src/test/pkg/test.kt:5: error: Method test.pkg.TestKt.add made type variable T reified: incompatible change [ChangedThrows:21]
            src/test/pkg/test.kt:8: error: Method test.pkg.TestKt.two made type variable S reified: incompatible change [ChangedThrows:21]
            """,
            checkCompatibilityApi = """
                package test.pkg {
                  public final class TestKt {
                    ctor public TestKt();
                    method public static inline <T> void add(T! t);
                    method public static inline <reified T> void remove(T! t);
                    method public static inline <reified T> void unchanged(T! t);
                    method public static inline <S, reified T> void two(S! s, T! t);
                  }
                }
                """,

            sourceFiles = *arrayOf(
                kotlin(
                    """
                    @file:Suppress("NOTHING_TO_INLINE", "RedundantVisibilityModifier", "unused")

                    package test.pkg

                    inline fun <reified T> add(t: T) { }
                    inline fun <T> remove(t: T) { }
                    inline fun <reified T> unchanged(t: T) { }
                    inline fun <reified S, T> two(s: S, t: T) { }
                    """
                ).indented()
            )
        )
    }

    @Ignore("Not currently working: we're getting the wrong PSI results; I suspect caching across the two codebases")
    @Test
    fun `Test All Android API levels`() {
        // Checks API across Android SDK versions and makes sure the results are
        // intentional (to help shake out bugs in the API compatibility checker)

        // Expected migration warnings (the map value) when migrating to the target key level from the previous level
        val expected = mapOf(
            5 to "warning: Method android.view.Surface.lockCanvas added thrown exception java.lang.IllegalArgumentException [ChangedThrows:21]",
            6 to """
                warning: Method android.accounts.AbstractAccountAuthenticator.confirmCredentials added thrown exception android.accounts.NetworkErrorException [ChangedThrows:21]
                warning: Method android.accounts.AbstractAccountAuthenticator.updateCredentials added thrown exception android.accounts.NetworkErrorException [ChangedThrows:21]
                warning: Field android.view.WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL has changed value from 2008 to 2014 [ChangedValue:17]
                """,
            7 to """
                error: Removed field android.view.ViewGroup.FLAG_USE_CHILD_DRAWING_ORDER [RemovedField:10]
                """,

            // setOption getting removed here is wrong! Seems to be a PSI loading bug.
            8 to """
                warning: Constructor android.net.SSLCertificateSocketFactory no longer throws exception java.security.KeyManagementException [ChangedThrows:21]
                warning: Constructor android.net.SSLCertificateSocketFactory no longer throws exception java.security.NoSuchAlgorithmException [ChangedThrows:21]
                error: Removed method java.net.DatagramSocketImpl.getOption(int) [RemovedMethod:9]
                error: Removed method java.net.DatagramSocketImpl.setOption(int,Object) [RemovedMethod:9]
                warning: Constructor java.nio.charset.Charset no longer throws exception java.nio.charset.IllegalCharsetNameException [ChangedThrows:21]
                warning: Method java.nio.charset.Charset.forName no longer throws exception java.nio.charset.IllegalCharsetNameException [ChangedThrows:21]
                warning: Method java.nio.charset.Charset.forName no longer throws exception java.nio.charset.UnsupportedCharsetException [ChangedThrows:21]
                warning: Method java.nio.charset.Charset.isSupported no longer throws exception java.nio.charset.IllegalCharsetNameException [ChangedThrows:21]
                warning: Method java.util.regex.Matcher.appendReplacement no longer throws exception java.lang.IllegalStateException [ChangedThrows:21]
                warning: Method java.util.regex.Matcher.start no longer throws exception java.lang.IllegalStateException [ChangedThrows:21]
                warning: Method java.util.regex.Pattern.compile no longer throws exception java.util.regex.PatternSyntaxException [ChangedThrows:21]
                warning: Class javax.xml.XMLConstants added final qualifier [AddedFinal:13]
                error: Removed constructor javax.xml.XMLConstants() [RemovedMethod:9]
                warning: Method javax.xml.parsers.DocumentBuilder.isXIncludeAware no longer throws exception java.lang.UnsupportedOperationException [ChangedThrows:21]
                warning: Method javax.xml.parsers.DocumentBuilderFactory.newInstance no longer throws exception javax.xml.parsers.FactoryConfigurationError [ChangedThrows:21]
                warning: Method javax.xml.parsers.SAXParser.isXIncludeAware no longer throws exception java.lang.UnsupportedOperationException [ChangedThrows:21]
                warning: Method javax.xml.parsers.SAXParserFactory.newInstance no longer throws exception javax.xml.parsers.FactoryConfigurationError [ChangedThrows:21]
                warning: Method org.w3c.dom.Element.getAttributeNS added thrown exception org.w3c.dom.DOMException [ChangedThrows:21]
                warning: Method org.w3c.dom.Element.getAttributeNodeNS added thrown exception org.w3c.dom.DOMException [ChangedThrows:21]
                warning: Method org.w3c.dom.Element.getElementsByTagNameNS added thrown exception org.w3c.dom.DOMException [ChangedThrows:21]
                warning: Method org.w3c.dom.Element.hasAttributeNS added thrown exception org.w3c.dom.DOMException [ChangedThrows:21]
                warning: Method org.w3c.dom.NamedNodeMap.getNamedItemNS added thrown exception org.w3c.dom.DOMException [ChangedThrows:21]
                """,

            18 to """
                warning: Class android.os.Looper added final qualifier but was previously uninstantiable and therefore could not be subclassed [AddedFinalUninstantiable:26]
                warning: Class android.os.MessageQueue added final qualifier but was previously uninstantiable and therefore could not be subclassed [AddedFinalUninstantiable:26]
                error: Removed field android.os.Process.BLUETOOTH_GID [RemovedField:10]
                error: Removed class android.renderscript.Program [RemovedClass:8]
                error: Removed class android.renderscript.ProgramStore [RemovedClass:8]
                """,
            19 to """
                warning: Method android.app.Notification.Style.build has changed 'abstract' qualifier [ChangedAbstract:20]
                error: Removed method android.os.Debug.MemoryInfo.getOtherLabel(int) [RemovedMethod:9]
                error: Removed method android.os.Debug.MemoryInfo.getOtherPrivateDirty(int) [RemovedMethod:9]
                error: Removed method android.os.Debug.MemoryInfo.getOtherPss(int) [RemovedMethod:9]
                error: Removed method android.os.Debug.MemoryInfo.getOtherSharedDirty(int) [RemovedMethod:9]
                warning: Field android.view.animation.Transformation.TYPE_ALPHA has changed value from nothing/not constant to 1 [ChangedValue:17]
                warning: Field android.view.animation.Transformation.TYPE_ALPHA has added 'final' qualifier [AddedFinal:13]
                warning: Field android.view.animation.Transformation.TYPE_BOTH has changed value from nothing/not constant to 3 [ChangedValue:17]
                warning: Field android.view.animation.Transformation.TYPE_BOTH has added 'final' qualifier [AddedFinal:13]
                warning: Field android.view.animation.Transformation.TYPE_IDENTITY has changed value from nothing/not constant to 0 [ChangedValue:17]
                warning: Field android.view.animation.Transformation.TYPE_IDENTITY has added 'final' qualifier [AddedFinal:13]
                warning: Field android.view.animation.Transformation.TYPE_MATRIX has changed value from nothing/not constant to 2 [ChangedValue:17]
                warning: Field android.view.animation.Transformation.TYPE_MATRIX has added 'final' qualifier [AddedFinal:13]
                warning: Method java.nio.CharBuffer.subSequence has changed return type from CharSequence to java.nio.CharBuffer [ChangedType:16]
                """, // The last warning above is not right; seems to be a PSI jar loading bug. It returns the wrong return type!

            20 to """
                error: Removed method android.util.TypedValue.complexToDimensionNoisy(int,android.util.DisplayMetrics) [RemovedMethod:9]
                warning: Method org.json.JSONObject.keys has changed return type from java.util.Iterator to java.util.Iterator<java.lang.String> [ChangedType:16]
                warning: Field org.xmlpull.v1.XmlPullParserFactory.features has changed type from java.util.HashMap to java.util.HashMap<java.lang.String, java.lang.Boolean> [ChangedType:16]
                """,
            26 to """
                warning: Field android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE has changed value from 130 to 230 [ChangedValue:17]
                warning: Field android.content.pm.PermissionInfo.PROTECTION_MASK_FLAGS has changed value from 4080 to 65520 [ChangedValue:17]
                """,
            27 to ""
        )

        val suppressLevels = mapOf(
            1 to "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,ChangedDeprecated",
            7 to "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,ChangedDeprecated",
            18 to "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,RemovedMethod,ChangedDeprecated,ChangedThrows,AddedFinal,ChangedType,RemovedDeprecatedClass",
            26 to "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,RemovedMethod,ChangedDeprecated,ChangedThrows,AddedFinal,RemovedClass,RemovedDeprecatedClass",
            27 to "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,RemovedMethod,ChangedDeprecated,ChangedThrows,AddedFinal"
        )

        val loadPrevAsSignature = false

        for (apiLevel in 5..27) {
            if (!expected.containsKey(apiLevel)) {
                continue
            }
            println("Checking compatibility from API level ${apiLevel - 1} to $apiLevel...")
            val current = getAndroidJar(apiLevel)
            if (current == null) {
                println("Couldn't find $current: Check that pwd for test is correct. Skipping this test.")
                return
            }

            val previous = getAndroidJar(apiLevel - 1)
            if (previous == null) {
                println("Couldn't find $previous: Check that pwd for test is correct. Skipping this test.")
                return
            }
            val previousApi = previous.path

            // PSI based check

            check(
                checkDoclava1 = false,
                extraArguments = arrayOf(
                    "--omit-locations",
                    ARG_HIDE,
                    suppressLevels[apiLevel]
                        ?: "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,ChangedDeprecated,RemovedField,RemovedClass,RemovedDeprecatedClass" +
                        (if ((apiLevel == 19 || apiLevel == 20) && loadPrevAsSignature) ",ChangedType" else "")

                ),
                warnings = expected[apiLevel]?.trimIndent() ?: "",
                checkCompatibilityApi = previousApi,
                apiJar = current
            )

            // Signature based check
            if (apiLevel >= 21) {
                // Check signature file checks. We have .txt files for API level 14 and up, but there are a
                // BUNCH of problems in older signature files that make the comparisons not work --
                // missing type variables in class declarations, missing generics in method signatures, etc.
                val signatureFile = File("../../prebuilts/sdk/${apiLevel - 1}/public/api/android.txt")
                if (!(signatureFile.isFile)) {
                    println("Couldn't find $signatureFile: Check that pwd for test is correct. Skipping this test.")
                    return
                }
                val previousSignatureApi = signatureFile.readText(Charsets.UTF_8)

                check(
                    checkDoclava1 = false,
                    extraArguments = arrayOf(
                        "--omit-locations",
                        ARG_HIDE,
                        suppressLevels[apiLevel]
                            ?: "AddedPackage,AddedClass,AddedMethod,AddedInterface,AddedField,ChangedDeprecated,RemovedField,RemovedClass,RemovedDeprecatedClass"
                    ),
                    warnings = expected[apiLevel]?.trimIndent() ?: "",
                    checkCompatibilityApi = previousSignatureApi,
                    apiJar = current
                )
            }
        }
    }

    // TODO: Check method signatures changing incompatibly (look especially out for adding new overloaded
    // methods and comparator getting confused!)
    //   ..equals on the method items should actually be very useful!
}
