/* Copyright (c) 2018 LibJ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.libj.jci;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class InMemoryCompilerTest {
  private static final File compiledClassesDir = new File("target/test-classes");

  public interface ITest {
    void doSomething();
  }

  private static final String[] packages = {
    "org.libj.jci.test.one",
    "org.libj.jci.test.one.two",
    "org.libj.jci.test.one.two.three",
    "org.libj.jci.test.one.two.four",
    "org.libj.jci.test.one.two.four.five",
  };

  private static final String[] classes = {
    "Test1",
    "Test2",
    "Test3"
  };

  @Test
  @SuppressWarnings("unchecked")
  public void test() throws ClassNotFoundException, CompilationException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    final InMemoryCompiler compiler = new InMemoryCompiler();

    for (final String pkg : packages) // [A]
      for (final String cls : classes) // [A]
        compiler.addSource("/* Test class */\n// With a comment\npackage " + pkg + ";\npublic class " + cls + " implements " + ITest.class.getCanonicalName() + "{public void doSomething(){System.out.println(\"Hello world!\");}}");

    final ClassLoader classLoader = compiler.compile(compiledClassesDir, "-g");

    // loading and using our compiled class
    for (final String pkg : packages) { // [A]
      assertNotNull(classLoader.getResource(pkg.replace('.', '/')));
      for (final String cls : classes) { // [A]
        final Class<ITest> test = (Class<ITest>)classLoader.loadClass(pkg + "." + cls);
        final ITest iTest = test.getConstructor().newInstance();
        iTest.doSomething();
      }
    }
  }
}