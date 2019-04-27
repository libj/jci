/* Copyright (c) 2017 OpenJAX
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

package org.openjax.ext.jci;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * An exception that signals an error during compilation of sources.
 */
public class CompilationException extends Exception {
  private static final long serialVersionUID = -7384036082398812166L;

  private static String buildMessage(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    if (diagnostics == null || diagnostics.size() == 0)
      return null;

    final StringBuilder builder = new StringBuilder(diagnostics.size() + " Errors\n");
    final Iterator<Diagnostic<? extends JavaFileObject>> iterator = diagnostics.iterator();
    for (int i = 0; iterator.hasNext(); ++i) {
      if (i > 0)
        builder.append('\n');

      builder.append(iterator.next());
    }

    return builder.toString();
  }

  private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

  /**
   * Constructs a new {@code CompilationException} with the specified detail
   * message.
   *
   * @param message The detail message.
   */
  public CompilationException(final String message) {
    this(message, null);
  }

  /**
   * Constructs a new {@code CompilationException} with the specified cause.
   *
   * @param cause The cause.
   */
  public CompilationException(final Throwable cause) {
    this((String)null, cause);
  }

  /**
   * Constructs a new {@code CompilationException} with the specified detail
   * message and cause.
   *
   * @param message The detail message.
   * @param cause The cause.
   */
  public CompilationException(final String message, final Throwable cause) {
    super(message, cause);
    this.diagnostics = Collections.emptyList();
  }

  /**
   * Constructs a new {@code CompilationException} with the specified list of
   * {@link Diagnostic} objects.
   *
   * @param diagnostics The list of {@link Diagnostic} objects.
   */
  public CompilationException(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    this(buildMessage(diagnostics), null);
  }

  /**
   * Constructs a new {@code CompilationException} with the specified list of
   * {@link Diagnostic} objects and cause.
   *
   * @param diagnostics The list of {@link Diagnostic} objects.
   * @param cause The cause.
   */
  public CompilationException(final List<Diagnostic<? extends JavaFileObject>> diagnostics, final Throwable cause) {
    super(buildMessage(diagnostics), cause);
    this.diagnostics = diagnostics == null ? Collections.emptyList() : Collections.unmodifiableList(diagnostics);
  }

  /**
   * @return The list of {@link Diagnostic} objects. If no diagnostics were
   *         provided, this method returns an empty list.
   */
  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }
}