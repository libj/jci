# LibJ JCI

[![Build Status](https://github.com/libj/jci/actions/workflows/build.yml/badge.svg)](https://github.com/libj/jci/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/libj/jci/badge.svg)](https://coveralls.io/github/libj/jci)
[![Javadocs](https://www.javadoc.io/badge/org.libj/jci.svg)](https://www.javadoc.io/doc/org.libj/jci)
[![Released Version](https://img.shields.io/maven-central/v/org.libj/jci.svg)](https://mvnrepository.com/artifact/org.libj/jci)
![Snapshot Version](https://img.shields.io/nexus/s/org.libj/jci?label=maven-snapshot&server=https%3A%2F%2Foss.sonatype.org)

## Introduction

LibJ JCI is a Java API Extension for the Java Compiler Interface.

Java's tools API provides an abstract interface for the implementation of runtime compilers. Though this interface provides the full span of functionality necessary to compile Java source in runtime, the APIs fall short to provide a reference implementation to allow developers to easily integrate a runtime compiler into their applications.

LibJ JCI provides a reference implementation of Java's runtime compiler API with its `InMemoryCompiler`, which can be used to compile sources and load the resulting bytecode in runtime.

## Usage

The following example illustrates how to compile Java source into bytecode that is thereafter available to be loaded in the resulting `ClassLoader`.

```java
InMemoryCompiler compiler = new InMemoryCompiler();
compiler.addSource("public class HelloWorld {public String helloWorld() {return \"Hello world!\";}}");
ClassLoader classLoader = compiler.compile();

Class<?> cls = classLoader.loadClass("HelloWorld");
Object obj = cls.getConstructor().newInstance();
assertEquals("helloWorld", cls.getMethod("helloWorld").invoke(obj));
```

## Contributing

Pull requests are welcome. For major changes, please [open an issue](../../issues) first to discuss what you would like to change.

Please make sure to update tests as appropriate.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.