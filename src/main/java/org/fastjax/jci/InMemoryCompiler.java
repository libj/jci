/* Copyright (c) 2018 FastJAX
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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

import org.fastjax.cdm.lexer.Keyword;
import org.fastjax.cdm.lexer.Lexer;
import org.fastjax.cdm.lexer.Lexer.Token;

/**
 * A Java compiler that compiles Java Source from String, and loads the compiled
 * Bytecode into an {@code InMemoryClassLoader}.
 *
 * @see InMemoryClassLoader
 */
public class InMemoryCompiler {
  private final Map<String,JavaFileObject> classNameToSource = new HashMap<>();

  /**
   * Compile the sources that have been added to this {@code InMemoryCompiler}.
   *
   * @return A {@code ClassLoader} which contains the compiled and loaded
   *         classes.
   * @throws ClassNotFoundException If a class cannot be found.
   * @throws CompilationException If a compilation exception has occurred.
   * @throws IOException If an I/O error has occurred.
   */
  public ClassLoader compile() throws ClassNotFoundException, CompilationException, IOException {
    return new InMemoryClassLoader(classNameToSource);
  }

  /**
   * Adds Java source for compilation.
   *
   * @param source The source to be added.
   * @throws CompilationException If the class name could not be determined from
   *           the {@code source} argument.
   */
  public void addSource(final String source) throws CompilationException {
    final boolean[] success = new boolean[1];
    try {
      Lexer.tokenize(new StringReader(source), source.length(), new Lexer.Token.Listener() {
        private int inParen = 0;
        private int start = -2;
        private StringBuilder className;

        @Override
        public boolean onToken(final Token token, final int start, final int end) {
          if (className != null) {
            if (token == Lexer.Delimiter.PAREN_OPEN) {
              ++inParen;
            }
            else if (token == Lexer.Delimiter.PAREN_CLOSE) {
              --inParen;
            }
            else if (inParen == 0 && (token == Keyword.CLASS || token == Keyword.INTERFACE)) {
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