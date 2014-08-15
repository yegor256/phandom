[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/phandom)](http://www.rultor.com/p/yegor256/phandom)

[![Build Status](https://travis-ci.org/yegor256/phandom.svg?branch=master)](https://travis-ci.org/yegor256/phandom)

# PhantomJS Java DOM Builder

**Phandom** is a Java DOM builder that uses PhantomJS. For example,
you have an HTML page that has to be renderable in a browser and you
want to validate this fact:

```java
import com.rexsl.test.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.phandom.Phandom;
public class HtmlTest {
  @Test
  public void testPageRenderability() {
    MatcherAssert.assertThat(
      new Phandom("<html><body><p>Hey!</p></body></html>").dom(),
      XhtmlMatchers.hasXPath("//p[.='Hey!']")
    );
  }
}
```

## Download and Maven Dependency

Just use this dependency in Maven:

```xml
<dependency>
  <groupId>com.jcabi.incubator</groupId>
  <artifactId>phandom</artifactId>
  <version>0.2.2</version>
</dependency>
```

Or download latest release [here](https://github.com/yegor256/phandom/releases).

## How to contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

> mvn clean install -Pqulice

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/phandom/issues/new).
