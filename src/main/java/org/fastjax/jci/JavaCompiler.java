/* Copyright (c) 2006 FastJAX
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

package org.fastjax.jci;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.fastjax.exec.Processes;
import org.fastjax.io.FastFiles;
import org.fastjax.util.FastCollections;
import org.fastjax.util.zip.ZipWriter;

public final class JavaCompiler {
  private final Collection<File> classpathFiles;
  private final File destDir;
  private final ZipWriter destJar;

  public JavaCompiler(final File destDir, final File ... classpath) {
    this(destDir, FastCollections.asCollection(new LinkedHashSet<File>(), classpath));
  }

  public JavaCompiler(final File destDir, final Collection<File> classpath) {
    this.classpathFiles = classpath;
    if (destDir == null)
      throw new IllegalArgumentException("dir cannot be null");

    if (destDir.isFile())
      throw new IllegalArgumentException(destDir + " is a file!");

    this.destDir = destDir;
    this.destJar = null;
  }

  public JavaCompiler(final ZipWriter destJar, final File ... classpath) {
    this(destJar, FastCollections.asCollection(new LinkedHashSet<File>(), classpath));
  }

  public JavaCompiler(final ZipWriter destJar, final Collection<File> classpath) {
    this.classpathFiles = classpath;
    this.destDir = null;
    if (destJar == null)
      throw new IllegalArgumentException("jar cannot be null");

    if (destDir.isDirectory())
      throw new IllegalArgumentException(destDir + " is a directory!");

    this.destJar = destJar;
  }

  public void compile(final File ... files) throws CompilationException, IOException {
    compile(FastCollections.asCollection(new ArrayList<File>(), files));
  }

  public void compile(final Collection<File> files) throws CompilationException, IOException {
    if (files == null || files.size() == 0)
      throw new IllegalArgumentException("files == " + (files == null ? "null" : "[]"));

    final LinkedHashSet<File> javaSources = new LinkedHashSet<>();
    for (final File file : files) {
      if (file.isDirectory())
        javaSources.addAll(Files.walk(file.toPath()).map(p -> p.toFile()).filter(f -> f.getName().endsWith(".java")).collect(Collectors.toList()));
      else
        javaSources.add(file);
    }

    if (javaSources.size() == 0)
      throw new IllegalArgumentException("no source files: " + FastCollections.toString(files, ", "));

    if (destDir != null)
      toDir(destDir, javaSources);
    else
      toJar(destJar, javaSources);
  }

  private void toJar(final ZipWriter destJar, final LinkedHashSet<File> javaSources) throws CompilationException, IOException {
    final File tempDir = File.createTempFile("javac", ".tmp");
    toDir(tempDir, javaSources);
    final DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(final Path entry) {
        return true;
      }
    };

    FastFiles.deleteAllOnExit(tempDir.toPath(), fileFilter);
    try {
      toDir(tempDir, javaSources);
      Files.walk(tempDir.toPath()).forEach(p -> {
        final File file = p.toFile();
        try {
          if (file.isFile() && file.getName().endsWith(".class"))
            destJar.write(file.getPath().substring(tempDir.getPath().length() + 1), Files.readAllBytes(file.toPath()));
        }
        catch (final IOException e) {
          throw new IllegalStateException(e);
        }
      });
    }
    finally {
      FastFiles.deleteAllOnExit(tempDir.toPath(), fileFilter);
    }
  }

  private static String convertSlashes(final String s) {
    return s.replace('\\', '/');
  }

  private void toDir(final File destDir, final LinkedHashSet<File> javaSources) throws CompilationException, IOException {
    if (!destDir.exists() && !destDir.mkdirs())
      throw new IllegalArgumentException("Could not create directory " + destDir);

    final StringBuilder classpath = new StringBuilder();
    final URL locationBase = JavaCompiler.class.getProtectionDomain().getCodeSource().getLocation();
    if (locationBase != null)
      classpath.append(File.pathSeparatorChar).append(locationBase);

    if (classpathFiles != null)
      for (final File classpathFile : classpathFiles)
        if (classpathFile != null)
          classpath.append(File.pathSeparatorChar).append(classpathFile);

    final String javaClassPath = System.getProperty("java.class.path");
    if (javaClassPath != null && javaClassPath.length() > 0)
      classpath.append(File.pathSeparatorChar).append(javaClassPath);

    final File tempFile = File.createTempFile("javac", ".tmp");
    try (final FileWriter writer = new FileWriter(tempFile)) {
      writer.write("-Xlint:none\n");
      // FIXME: Used as a stop-gap solution to get JUnit in Eclipse to load classes compiled by this class (Java 9).
      if (!System.getProperty("java.version").startsWith("1."))
        writer.write("--release 8\n");

      if (classpath.length() > 0)
        writer.write("-cp \"" + classpath.substring(1) + "\"\n");

      writer.write("-d \"" + convertSlashes(destDir.getAbsolutePath()) + "\"");
      final Iterator<File> iterator = javaSources.iterator();
      while (iterator.hasNext())
        writer.write("\n\"" + convertSlashes(iterator.next().getAbsolutePath()) + "\"");
    }

    final String[] args = new String[] {"javac", "@" + tempFile.getAbsolutePath()};
    try {
      final int exitValue = Processes.forkSync(null, System.out, System.err, false, null, null, args);
      if (exitValue != 0)
        throw new CompilationException("\n  javac \\\n    " + new String(Files.readAllBytes(tempFile.toPath())).replace("\n", " \\\n    "));
    }
    catch (final InterruptedException e) {
      throw new CompilationException("\n  javac \\\n    " + new String(Files.readAllBytes(tempFile.toPath())).replace("\n", " \\\n    "));
    }
    finally {
      tempFile.delete();
    }
  }
}