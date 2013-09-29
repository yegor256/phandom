# PhantomJS Java DOM Builder

**Phandom** is a Java DOM builder that uses PhantomJS. For example,
you have an HTML page that has to be renderable in a browser and you
want to validate this fact:

```java
public class HtmlTest {
  @Test
  public void testPageRenderability() {
    MatcherAssert.assertThat(
      new Phandom("<html><body><p>Hey!</p></body></html>").dom(),
      XhtmlMatchers.hasXPath("//xhtml:p[.='Hey!']")
    );
  }
}
```
