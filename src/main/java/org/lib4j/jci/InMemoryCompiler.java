/* Copyright (c) 2006 lib4j
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.tools.JavaFileObject;

public final class InMemoryCompiler {
  public static void compile(final javax.tools.JavaCompiler compiler, final MemoryJavaFileManager fileManager, final Collection<String> options, final List<JavaFileObject> javaFiles, final File outDir) throws IOException {
    // We specify a task to the compiler. Compiler should use our file
    // manager and our list of "files".
    // Then we run the compilation with call()

    // any other options you want
    //options.addAll(Arrays.asList(options));

    compiler.getTask(null, fileManager, null, options, null, javaFiles).call();
    /*for (final JavaFileObject javaFileObject : javaFiles) {
      final String className = javaFileObject.getName().substring(1, javaFileObject.getName().length() - 5).replace(File.separatorChar, '.');
//      System.out.println(className);
//      fileManager.getClassLoader(null).loadClass(className);
    }*/

    fileManager.outputClasses(outDir);
    fileManager.outputClasses(new File(outDir.getParentFile().getParentFile(), "classes"));
  }
}