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

package org.lib4j.jci;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

public final class MemoryJavaFileObject extends SimpleJavaFileObject {
  /**
   * Byte code created by the compiler will be stored in this
   * ByteArrayOutputStream so that we can later get the
   * byte array out of it
   * and put it in the memory as an instance of our class.
   */
  private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

  /**
   * Registers the compiled class object under URI
   * containing the class full name
   *
   * @param name
   *            Full name of the compiled class
   * @param kind
   *            Kind of the data. It will be class in our case
   */
  public MemoryJavaFileObject(final String name, final JavaFileObject.Kind kind) {
    super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
  }

  /**
   * Will be used by our file manager to get the byte code that
   * can be put into memory to instantiate our class
   *
   * @return compiled byte code
   */
  public byte[] getBytes() {
    return bos.toByteArray();
  }

  /**
   * Will provide the compiler with an output stream that leads
   * to our byte array. This way the compiler will write everything
   * into the byte array that we will instantiate later
   */
  @Override
  public OutputStream openOutputStream() throws IOException {
    return bos;
  }
}