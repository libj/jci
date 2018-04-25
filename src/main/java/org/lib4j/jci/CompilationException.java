/* Copyright (c) 2017 lib4j
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class CompilationException extends Exception {
  private static final long serialVersionUID = -7384036082398812166L;

  private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

  public CompilationException() {
    super();
    this.diagnostics = new ArrayList<Diagnostic<? extends JavaFileObject>>(0);
  }

  public CompilationException(final String message) {
    super(message);
    this.diagnostics = Collections.emptyList();
  }

  public CompilationException(final Throwable cause) {
    super(cause);
    this.diagnostics = Collections.emptyList();
  }

  public CompilationException(final String message, final Throwable cause) {
    super(message, cause);
    this.diagnostics = Collections.emptyList();
  }

  public CompilationException(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super();
    this.diagnostics = Collections.unmodifiableList(diagnostics);
  }

  public CompilationException(final String message, final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(message);
    this.diagnostics = Collections.unmodifiableList(diagnostics);
  }

  public CompilationException(final Throwable cause, final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(cause);
    this.diagnostics = Collections.unmodifiableList(diagnostics);
  }

  public CompilationException(final String message, final Throwable cause, final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(message, cause);
    this.diagnostics = Collections.unmodifiableList(diagnostics);
  }

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }
}