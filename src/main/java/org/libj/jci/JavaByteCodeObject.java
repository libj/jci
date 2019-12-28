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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * A {@link SimpleJavaFileObject} representing Java Bytecode (i.e. a ".class"
 * file).
 */
class JavaByteCodeObject extends SimpleJavaFileObject implements AutoCloseable {
  private static class ReleasableByteArrayOutputStream extends ByteArrayOutputStream {
    private void release() {
      this.buf = null;
    }
  }

  private final ReleasableByteArrayOutputStream baos = new ReleasableByteArrayOutputStream();

  /**
   * Creates a new {@link JavaByteCodeObject} with the specified name.
   *
   * @param name The name.
   */
  JavaByteCodeObject(final String name) {
    super(URI.create("bytecode:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
  }

  /**
   * Returns the bytecode as an {@link OutputStream}.
   *
   * @return The bytecode as an {@link OutputStream}.
   */
  @Override
  public OutputStream openOutputStream() {
    return baos;
  }

  /**
   * Returns the bytecode as a byte array.
   *
   * @return The bytecode as a byte array.
   */
  public byte[] getBytes() {
    return baos.toByteArray();
  }

  @Override
  public void close() {
    baos.release();
  }
}