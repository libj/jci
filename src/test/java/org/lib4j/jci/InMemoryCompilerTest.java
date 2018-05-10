/* Copyright (c) 2018 lib4j
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

package org.lib4j.jci;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class InMemoryCompilerTest {
  public static interface ITest {
    public void doSomething();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test() throws ClassNotFoundException, CompilationException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException, NoSuchMethodException {
    final InMemoryCompiler compiler = new InMemoryCompiler();
    compiler.addSource("/* Test class */\r// With a comment\npackage org.libx4j.cdm.lexer;\npublic class Test implements org.lib4j.jci.InMemoryCompilerTest.ITest{public void doSomething(){System.out.println(\"Hello world!\");}}");
    compiler.compile();

    // loading and using our compiled class
    final Class<ITest> test = (Class<ITest>)compiler.loadClass("org.libx4j.cdm.lexer.Test");
    final ITest iTest = test.getConstructor().newInstance();
    iTest.doSomething();
  }
}