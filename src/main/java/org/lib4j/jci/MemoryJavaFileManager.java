/* Copyright (c) 2011 lib4j
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public final class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
  private final Map<String,MemoryJavaFileObject> map = new HashMap<String,MemoryJavaFileObject>();
  /**
   * Instance of JavaClassObject that will store the
   * compiled bytecode of our class
   */
  //private MemoryJavaFileObject jclassObject;

  /**
   * Will initialize the manager with the specified
   * standard java file manager
   *
   * @param standardManger
   */
  public MemoryJavaFileManager(final StandardJavaFileManager standardManager) {
    super(standardManager);
  }

  public Collection<MemoryJavaFileObject> outputClasses(final File dir) throws IOException {
    for (final Map.Entry<String,MemoryJavaFileObject> entry : map.entrySet()) {
      final File file = new File(dir, entry.getKey().replace('.', File.separatorChar) + ".class");
      if (!file.getParentFile().exists())
        file.getParentFile().mkdirs();

      try (final FileOutputStream fos = new FileOutputStream(file)) {
        fos.write(entry.getValue().getBytes());
      }
    }

    return map.values();
  }

  /**
   * Will be used by us to get the final class loader for our
   * compiled class. It creates an anonymous class
   * extending the SecureClassLoader which uses the
   * byte code created by the compiler and stored in
   * the JavaClassObject, and returns the final class for it
   */
  @Override
  public ClassLoader getClassLoader(final Location location) {
    return new SecureClassLoader() {
      @Override
      protected Class<?> findClass(final String name) throws ClassNotFoundException {
        synchronized (MemoryJavaFileManager.this.map) {
          MemoryJavaFileObject mc = MemoryJavaFileManager.this.map.remove(name);
          if (mc != null) {
            byte[] array = mc.getBytes();
            return defineClass(name, array, 0, array.length);
          }
        }

        return super.findClass(name);
      }
    };
  }

  /**
   * Gives the compiler an instance of the JavaClassObject
   * so that the compiler can write the byte code into it.
   */
  @Override
  public JavaFileObject getJavaFileForOutput(final Location location, final String className, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
    final MemoryJavaFileObject o = new MemoryJavaFileObject(className, kind);
    map.put(className, o);
    return o;
  }
}