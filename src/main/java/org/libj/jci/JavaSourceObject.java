/* Copyright (c) 2018 LibJ
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

package org.libj.jci;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * A {@link SimpleJavaFileObject} representing Java Source (i.e. a ".java" file).
 */
class JavaSourceObject extends SimpleJavaFileObject {
  private final String source;

  /**
   * Creates a new {@link JavaSourceObject} with the specified name and source.
   *
   * @param name The name.
   * @param source The source.
   */
  JavaSourceObject(final String name, final String source) {
    super(URI.create("source:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
    this.source = source;
  }

  /**
   * Returns the {@link CharSequence}.
   *
   * @param ignoreEncodingErrors Whether to ignore encoding errors.
   * @return The {@link CharSequence}.
   */
  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
    return source;
  }
}