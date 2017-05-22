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

package org.safris.commons.jci;

import java.net.URI;
import java.nio.CharBuffer;

import javax.tools.SimpleJavaFileObject;

public final class CharSequenceJavaFileObject extends SimpleJavaFileObject {
  /**
   * CharSequence representing the source code to be compiled
   */
  private CharSequence content;

  /**
   * This constructor will store the source code in the
   * internal "content" variable and register it as a
   * source code, using a URI containing the final class full name
   *
   * @param className
   *            name of the public final class in the source code
   * @param content
   *            source code to compile
   */
  public CharSequenceJavaFileObject(final String className, final CharSequence content) {
    super(URI.create("string:///" + className.replace('.', '/') + SimpleJavaFileObject.Kind.SOURCE.extension), SimpleJavaFileObject.Kind.SOURCE);
    this.content = content;
  }

  /**
   * Answers the CharSequence to be compiled. It will give
   * the source code stored in variable "content"
   */
  @Override
  public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
    return CharBuffer.wrap(content);
  }
}