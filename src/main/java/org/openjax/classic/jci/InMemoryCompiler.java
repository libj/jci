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

package org.openjax.classic.jci;

import static org.openjax.classic.util.function.Throwing.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaFileObject;

import org.openjax.classic.cdm.lexer.Keyword;
import org.openjax.classic.cdm.lexer.Lexer;
import org.openjax.classic.cdm.lexer.Lexer.Token;
import org.openjax.classic.util.FastCollections;

/**
 * A Java compiler that compiles Java Source from String, and loads the compiled
 * Bytecode into an {@code InMemoryClassLoader}.
 *
 * @see InMemoryClassLoader
 */
public class InMemoryCompiler {
  private static ClassLoader digestOptions(final ClassLoader classLoader, final List<String> options, final List<File> classpath) {
    if (classpath == null)
      return classLoader;

    options.add("-cp");
    options.add(FastCollections.toString(classpath, File.pathSeparatorChar));
    return new URLClassLoader(classpath.stream().map(rethrow((File f) -> f.toURI().toURL())).toArray(URL[]::new), classLoader);
  }

  private final Map<String,JavaFileObject> classNameToSource = new HashMap<>();

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler},
   * and, if compilation is successful, write compiled classes to the specified
   * destination directory.
   *
   * @param classLoader The {@code ClassLoader} for resolution of linked
   *          classes.
   * @param classpath Compiler classpath, or {@code null} for no classpath.
   * @param destDir The destination directory of the compiled classes, or
   *          {@code null} if the classes should not be written.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final ClassLoader classLoader, final List<File> classpath, final File destDir, final String ... options) throws CompilationException, IOException {
    final List<String> optionsList = options != null && options.length > 0 ? FastCollections.asCollection(new ArrayList<>(), options) : new ArrayList<>();
    return new InMemoryClassLoader(digestOptions(classLoader, optionsList, classpath), classNameToSource, optionsList, destDir);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler}.
   *
   * @param classLoader The {@code ClassLoader} for resolution of linked
   *          classes.
   * @param classpath Compiler classpath, or {@code null} for no classpath.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws ClassNotFoundException If a class cannot be found.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final ClassLoader classLoader, final List<File> classpath, final String ... options) throws ClassNotFoundException, CompilationException, IOException {
    return compile(classLoader, classpath, null, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler},
   * and, if compilation is successful, write compiled classes to the specified
   * destination directory. This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), classpath, destDir, options)}
   * </blockquote>
   *
   * @param classpath Compiler classpath, or {@code null} for no classpath.
   * @param destDir The destination directory of the compiled classes, or
   *          {@code null} if the classes should not be written.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final List<File> classpath, final File destDir, final String ... options) throws CompilationException, IOException {
    return compile(ClassLoader.getSystemClassLoader(), classpath, destDir, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler}.
   * This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), classpath, options)}
   * </blockquote>
   *
   * @param classpath Compiler classpath, or {@code null} for no classpath.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws ClassNotFoundException If a class cannot be found.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final List<File> classpath, final String ... options) throws ClassNotFoundException, CompilationException, IOException {
    return compile(ClassLoader.getSystemClassLoader(), classpath, null, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler},
   * and, if compilation is successful, write compiled classes to the specified
   * destination directory. This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), null, destDir, options)}
   * </blockquote>
   *
   * @param classLoader The {@code ClassLoader} for resolution of linked
   *          classes.
   * @param destDir The destination directory of the compiled classes, or
   *          {@code null} if the classes should not be written.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final ClassLoader classLoader, final File destDir, final String ... options) throws CompilationException, IOException {
    return compile(classLoader, null, destDir, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler}.
   * This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), null, options)}
   * </blockquote>
   *
   * @param classLoader The {@code ClassLoader} for resolution of linked
   *          classes.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws ClassNotFoundException If a class cannot be found.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final ClassLoader classLoader, final String ... options) throws ClassNotFoundException, CompilationException, IOException {
    return compile(classLoader, null, null, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler},
   * and, if compilation is successful, write compiled classes to the specified
   * destination directory. This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), null, destDir, options)}
   * </blockquote>
   *
   * @param destDir The destination directory of the compiled classes, or
   *          {@code null} if the classes should not be written.
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final File destDir, final String ... options) throws CompilationException, IOException {
    return compile(ClassLoader.getSystemClassLoader(), null, destDir, options);
  }

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler}.
   * This method is equivalent to calling:
   * <p>
   * <blockquote>
   * {@code compile(ClassLoader.getSystemClassLoader(), null, options)}
   * </blockquote>
   *
   * @param options Compiler options.
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws ClassNotFoundException If a class cannot be found.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile(final String ... options) throws ClassNotFoundException, CompilationException, IOException {
    return compile(ClassLoader.getSystemClassLoader(), null, null, options);
  }

  /**
   * Adds Java source for compilation.
   *
   * @param source The source to be added.
   * @throws IllegalArgumentException If the class name could not be determined from
   *           the {@code source} argument.
   */
  public void addSource(final String source) {
    final boolean[] success = new boolean[1];
    try {
      Lexer.tokenize(new StringReader(source), source.length(), new Lexer.Token.Listener() {
        private int inParen = 0;
        private int start = -2;
        private StringBuilder className;
        private Token lastKeyword;

        @Override
        public void onStartDocument() {
        }

        @Override
        public boolean onToken(final Token token, final int start, final int end) {
          if (className != null) {
            if (token == Lexer.Delimiter.PAREN_OPEN) {
              ++inParen;
            }
            else if (token == Lexer.Delimiter.PAREN_CLOSE) {
              --inParen;
            }
            else if (inParen == 0 && (token == Keyword.CLASS || token == Keyword.INTERFACE || token == Keyword.ENUM || token == Keyword.$INTERFACE)) {
              lastKeyword = token;
              this.start = -1;
            }
            else if (this.start == -1) {
              if (token == Lexer.Span.WORD)
                this.start = start;
            }
            else if (this.start != -2 && token == Lexer.Span.WHITESPACE) {
              className.append(source.substring(this.start, start));
              final String string = className.toString();
              InMemoryCompiler.this.classNameToSource.put(string, new JavaSourceObject(string, source));
              success[0] = true;
              return false;
            }
          }
          else if (this.start != -2) {
            if (this.start == -1 && token == Lexer.Span.WORD) {
              this.start = start;
            }
            else if (token == Lexer.Delimiter.SEMI_COLON) {
              className = new StringBuilder(source.substring(this.start, start)).append('.');
              this.start = -2;
            }
          }
          else if (token == Keyword.PACKAGE) {
            lastKeyword = token;
            this.start = -1;
          }
          else if (token == Keyword.CLASS || token == Keyword.INTERFACE || token == Keyword.ENUM || token == Keyword.$INTERFACE) {
            lastKeyword = token;
            className = new StringBuilder();
            this.start = -1;
          }
          else if (token == Lexer.Delimiter.BRACE_OPEN) {
            return false;
          }

          return true;
        }

        @Override
        public void onEndDocument() {
          if (lastKeyword == Keyword.PACKAGE && className != null) {
            className.append("package-info");
            final String string = className.toString();
            InMemoryCompiler.this.classNameToSource.put(string, new JavaSourceObject(string, source));
            success[0] = true;
          }
        }
      });
    }
    catch (final IOException e) {
      throw new IllegalStateException(e);
    }

    if (!success[0])
      throw new IllegalArgumentException("Could not determine class name: \n" + source);
  }
}