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
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.lib4j.exec.Processes;
import org.lib4j.io.Files;
import org.safris.commons.lang.Resources;
import org.safris.commons.util.Collections;
import org.safris.commons.util.jar.Jar;
import org.safris.commons.util.zip.CachedFile;
import org.safris.commons.util.zip.Zips;

public final class JavaCompiler {
  public static final FileFilter JAVA_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(final File pathname) {
      return pathname.getName().endsWith(".java");
    }
  };

  private final Collection<File> classpathFiles;
  private final File destDir;
  private final Jar destJar;

  public JavaCompiler(final File destDir, final File ... classpath) {
    this(destDir, Collections.asCollection(LinkedHashSet.class, classpath));
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

  public JavaCompiler(final Jar destJar, final File ... classpath) {
    this(destJar, Collections.asCollection(LinkedHashSet.class, classpath));
  }

  public JavaCompiler(final Jar destJar, final Collection<File> classpath) {
    this.classpathFiles = classpath;
    this.destDir = null;
    if (destJar == null)
      throw new IllegalArgumentException("jar cannot be null");

    if (destDir.isDirectory())
      throw new IllegalArgumentException(destDir + " is a directory!");

    this.destJar = destJar;
  }

  public void compile(final File ... files) throws CompilationException, IOException {
    compile(Collections.asCollection(ArrayList.class, files));
  }

  public void compile(final Collection<File> files) throws CompilationException, IOException {
    if (files == null)
      throw new NullPointerException("files == null");

    if (files.size() == 0)
      throw new IllegalArgumentException("files.size() == 0");

    final LinkedHashSet<File> javaSources = new LinkedHashSet<File>();
    for (final File file : files) {
      if (file.isDirectory())
        javaSources.addAll(Files.listAll(destDir, JAVA_FILE_FILTER));
      else
        javaSources.add(file);
    }

    if (destDir != null)
      toDir(destDir, javaSources);
    else
      toJar(destJar, javaSources);
  }

  private void toJar(final Jar destJar, final LinkedHashSet<File> javaSources) throws CompilationException, IOException {
    final File tempDir = File.createTempFile("javac", ".tmp");
    toDir(tempDir, javaSources);
    final DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(final Path entry) {
        return true;
      }
    };

    Files.deleteAllOnExit(tempDir.toPath(), fileFilter);
    try {
      toDir(tempDir, javaSources);
      final Collection<File> files = Files.listAll(tempDir);
      final Collection<CachedFile> selected = new ArrayList<CachedFile>();
      for (final File file : files) {
        if (!file.isFile() || !file.getName().endsWith(".class"))
          continue;

        selected.add(new CachedFile(file.getPath().substring(tempDir.getPath().length() + 1), Files.getBytes(file)));
      }

      Zips.add(destJar.getFile(), selected);
    }
    finally {
      Files.deleteAllOnExit(tempDir.toPath(), fileFilter);
    }
  }

  private static String convertSlashes(final String s) {
    return s.replace("\\", "/");
  }

  private void toDir(final File destDir, final LinkedHashSet<File> javaSources) throws CompilationException, IOException {
    if (!destDir.exists() && !destDir.mkdirs())
      throw new IllegalArgumentException("Could not create directory " + destDir);

    String classpath = "";
    if (classpathFiles != null)
      for (final File classpathFile : classpathFiles)
        if (classpathFile != null)
          classpath += classpathFile + File.pathSeparator;

    classpath += System.getProperty("java.class.path");
    final File locationBase = Resources.getLocationBase(JavaCompiler.class);
    if (locationBase != null)
      classpath = convertSlashes(locationBase.getAbsolutePath()) + File.pathSeparator + classpath;

    final File tempFile = File.createTempFile("javac", ".tmp");
    try (final FileWriter out = new FileWriter(tempFile)) {
      out.write("-Xlint:none\n");
      out.write("-cp \"" + classpath + "\"\n");
      out.write("-d \"" + convertSlashes(destDir.getAbsolutePath()) + "\"");
      final Iterator<File> iterator = javaSources.iterator();
      while (iterator.hasNext())
        out.write("\n\"" + convertSlashes(iterator.next().getAbsolutePath()) + "\"");
    }

    final String[] args = new String[] {"javac", "@" + tempFile.getAbsolutePath()};
    try {
      final Process process = Processes.forkSync(null, System.out, System.err, false, args);
      if (process.exitValue() != 0)
        throw new CompilationException("\n  javac\n    " + new String(Files.getBytes(tempFile)).replace("\n", " \\\n    "));
    }
    catch (final InterruptedException e) {
      throw new CompilationException("\n  javac\n    " + new String(Files.getBytes(tempFile)).replace("\n", " \\\n    "));
    }
    finally {
      tempFile.delete();
    }
  }
}