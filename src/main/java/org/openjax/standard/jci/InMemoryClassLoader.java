/* Copyright (c) 2018 OpenJAX
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

package org.openjax.standard.jci;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.openjax.standard.net.MemoryURLStreamHandler;
import org.openjax.standard.util.Classes;
import org.openjax.standard.util.Enumerations;

/**
 * A {@code ClassLoader} that compiles sources specified in the constructor in
 * memory, and optionally writes the compiled classes to a destination
 * directory.
 *
 * @see InMemoryCompiler
 */
class InMemoryClassLoader extends ClassLoader {
  private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
  private final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
  private final Map<String,JavaByteCodeObject> classNameToByteCode = new HashMap<>();
  private final Map<String,Class<?>> classNameToClass = new HashMap<>();
  private final Set<String> resources = new HashSet<>();
  private final URL url;

  /**
   * Creates a new {@code InMemoryClassLoader} with the specified sources and
   * destination directory.
   *
   * @param parent The parent {@code ClassLoader}.
   * @param classNameToSource The map of class name {@code String} to source
   *          {@code JavaFileObject} object.
   * @param options Compiler options, or {@code null} for no options.
   * @param destDir The destination directory of the compiled classes, or
   *          {@code null} if the classes should not be written.
   * @throws CompilationException If an error has occurred while compiling the
   *           specified sources.
   * @throws IOException If an I/O error has occurred.
   */
  InMemoryClassLoader(final ClassLoader parent, final Map<String,JavaFileObject> classNameToSource, final Iterable<String> options, final File destDir) throws CompilationException, IOException {
    super(new ClassLoader(parent) {
      /**
       * Overloaded to force resource resolution to this InMemoryClassLoader.
       */
      @Override
      public URL getResource(final String name) {
        return null;
      }

      /**
       * Overloaded to force resource resolution to this InMemoryClassLoader.
       */
      @Override
      public Enumeration<URL> getResources(final String name) throws IOException {
        return null;
      }

      /**
       * Overloaded to force loading of classes defined in this
       * InMemoryClassLoader, by this InMemoryClassLoader.
       */
      @Override
      protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        return classNameToSource.containsKey(Classes.getRootDeclaringClassName(name)) ? null : super.loadClass(name, resolve);
      }
    });

    try (final JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(compiler.getStandardFileManager(diagnostics, null, null)) {
      @Override
      public JavaFileObject getJavaFileForOutput(final Location location, final String className, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
        JavaByteCodeObject javaByteCodeObject = classNameToByteCode.get(className);
        if (javaByteCodeObject == null)
          classNameToByteCode.put(className, javaByteCodeObject = new JavaByteCodeObject(className));

        return javaByteCodeObject;
      }
    }) {
      if (!compiler.getTask(null, fileManager, diagnostics, options, null, classNameToSource.values()).call())
        throw new CompilationException(diagnostics.getDiagnostics());
    }

    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (final JarOutputStream jos = new JarOutputStream(baos)) {
        for (final Map.Entry<String,JavaByteCodeObject> entry : classNameToByteCode.entrySet()) {
          if (!entry.getKey().endsWith("package-info"))
            loadClass(entry.getKey());

          final String name = entry.getKey().replace('.', '/').concat(".class");
          if (destDir != null) {
            final File file = new File(destDir, name);
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), entry.getValue().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
          }

          jos.putNextEntry(new JarEntry(name));
          jos.write(entry.getValue().getBytes());
          jos.closeEntry();
          resources.add(name);

          String pkg = entry.getKey();
          int dot;
          while ((dot = pkg.lastIndexOf('.')) != -1) {
            pkg = pkg.substring(0, dot);
            final String dir = pkg.replace('.', '/');
            if (!resources.contains(dir)) {
              jos.putNextEntry(new JarEntry(dir));
              resources.add(dir);

              if (getPackage(pkg) == null)
                definePackage(pkg, null, null, null, null, null, null, null);
            }
          }
        }
      }

      final URL memUrl = MemoryURLStreamHandler.createURL(baos.toByteArray());
      url = new URL("jar:" + memUrl + "!/");
    }
    catch (final ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {
    Class<?> cls = classNameToClass.get(name);
    if (cls == null) {
      final JavaByteCodeObject javaByteCodeObject = classNameToByteCode.get(name);
      if (javaByteCodeObject == null)
        throw new ClassNotFoundException(name);

      final byte[] b = javaByteCodeObject.getBytes();
      classNameToClass.put(name, cls = defineClass(name, b, 0, b.length));
    }

    return cls;
  }

  @Override
  protected URL findResource(final String name) {
    if (!resources.contains(name))
      return null;

    try {
      return new URL(url, name);
    }
    catch (final MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected Enumeration<URL> findResources(final String name) throws IOException {
    return resources.contains(name) ? Enumerations.singleton(new URL(url, name)) : Collections.emptyEnumeration();
  }
}