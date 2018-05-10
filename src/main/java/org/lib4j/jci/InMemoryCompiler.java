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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.lib4j.cdm.lexer.Keyword;
import org.lib4j.cdm.lexer.Lexer;
import org.lib4j.cdm.lexer.Lexer.Token;

public class InMemoryCompiler extends ClassLoader {
  private final JavaCompiler compiler;
  private final DiagnosticCollector<JavaFileObject> diagnostics;
  private final Map<String,JavaByteCodeObject> classNameToByteCode = new HashMap<String,JavaByteCodeObject>();
  private final Map<String,JavaFileObject> classNameToSource = new HashMap<String,JavaFileObject>();

  private class FileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    protected FileManager() {
      super(compiler.getStandardFileManager(diagnostics, null, null));
    }

    @Override
    public JavaFileObject getJavaFileForOutput(final Location location, final String className, final JavaFileObject.Kind kind, final FileObject sibling) throws IOException {
      JavaByteCodeObject javaByteCodeObject = classNameToByteCode.get(className);
      // The javaByteCodeObject will be null for inner classes, so it must be created at the moment it is requested by the compiler
      if (javaByteCodeObject == null)
        classNameToByteCode.put(className, javaByteCodeObject = new JavaByteCodeObject(className));

      return javaByteCodeObject;
    }
  }

  public InMemoryCompiler() {
    this.compiler = ToolProvider.getSystemJavaCompiler();
    this.diagnostics = new DiagnosticCollector<JavaFileObject>();
  }

  public void compile() throws CompilationException, IOException {
    try (final JavaFileManager fileManager = new FileManager()) {
      final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, classNameToSource.values());
      if (!task.call())
        throw new CompilationException(diagnostics.getDiagnostics());
    }

    for (final Map.Entry<String,JavaByteCodeObject> entry : classNameToByteCode.entrySet()) {
      final byte[] bytes = classNameToByteCode.get(entry.getKey()).getBytes();
      defineClass(entry.getKey(), bytes, 0, bytes.length);

      String pkg = entry.getKey();
      int dot;
      while ((dot = pkg.lastIndexOf('.')) != -1) {
        pkg = pkg.substring(0, dot);
        if (getDefinedPackage(pkg) == null)
          definePackage(pkg, null, null, null, null, null, null, null);
      }
    }
  }

  public void addSource(final String source) throws CompilationException {
    final boolean[] success = new boolean[1];
    try {
      Lexer.tokenize(new StringReader(source), source.length(), new Lexer.Token.Listener() {
        private int start = -2;
        private StringBuilder className;

        @Override
        public boolean onToken(final Token token, final int start, final int end) {
          if (className != null) {
            if (token == Keyword.CLASS || token == Keyword.INTERFACE) {
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
              InMemoryCompiler.this.classNameToByteCode.put(string, new JavaByteCodeObject(string));
              success[0] = true;
              return false;
            }
          }
          else if (this.start != -2) {
            if (this.start == -1 && token == Lexer.Span.WORD)
              this.start = start;
            else if (token == Lexer.Delimiter.SEMI_COLON) {
              className = new StringBuilder(source.substring(this.start, start)).append('.');
              this.start = -2;
            }
          }
          else if (token == Keyword.PACKAGE) {
            this.start = -1;
          }
          else if (token == Keyword.CLASS || token == Keyword.INTERFACE) {
            className = new StringBuilder();
            this.start = -1;
          }
          else if (token == Lexer.Delimiter.BRACE_OPEN) {
            return false;
          }

          return true;
        }
      });
    }
    catch (final IOException e) {
      throw new CompilationException(e);
    }

    if (!success[0])
      throw new CompilationException("Could not determine class name");
  }
}