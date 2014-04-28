/**
 * Copyright (c) 2013, phandom.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the phandom.org nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.phandom;

import com.rexsl.test.XhtmlMatchers;
import java.io.File;
import java.net.URI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test case for {@link Phandom}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class PhandomTest {

    /**
     * Temporary folder.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * Check for Phantomjs availability.
     */
    @Before
    public void installed() {
        Assume.assumeTrue(Phandom.isInstalled());
    }

    /**
     * Phandom can build DOM document.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsDomDocument() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(
                    StringUtils.join(
                        "<!DOCTYPE html>\n",
                        "<html><head>\n",
                        "<meta content='hi there' name='description'/>\n",
                        "</head><body><p>&euro;</p><a href='#'/></body></html>"
                    )
                ).dom()
            ),
            XhtmlMatchers.hasXPaths(
                "/html/body",
                "/html/head/meta[@name='description']",
                "//p[.='\u20ac']"
            )
        );
    }

    /**
     * Phandom can succeed on a broken DOM.
     * @throws Exception If some problem inside
     */
    @Test
    public void succeedsOnBrokenDom() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(IOUtils.toInputStream("<html>\nbroken")).dom()
            ),
            XhtmlMatchers.hasXPath("/html[head and body]")
        );
    }

    /**
     * Phandom can fail on a broken javascript.
     * @throws Exception If some problem inside
     */
    @Test(expected = RuntimeException.class)
    public void failsOnBrokenJavascript() throws Exception {
        new Phandom(
            "<html><body><script>a.call();</script>\n</body></html>"
        ).dom();
    }

    /**
     * Phandom can parse a huge HTML.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesLongHtml() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(
                    StringUtils.join(
                        "<html><head><script>//<![CDATA[\n",
                        "function onLoad() {",
                        "for (i=0; i<1000; ++i) {",
                        "var div = document.createElement('div');",
                        "div.innerHTML = i + '&lt;&#10;<b>&gt;</b>&#10;&amp;';",
                        "div.style.color = 'red';",
                        "div.setAttribute('class', 'foo');",
                        "document.body.appendChild(div);",
                        "document.body.removeChild(div);",
                        "}}\n//]]></script></head>",
                        "<body onload='onLoad();'></body></html>\n\n"
                    )
                ).dom()
            ),
            XhtmlMatchers.hasXPath("/html/body[count(div)=0]")
        );
    }

    /**
     * Phandom can parse a web page.
     * @throws Exception If some problem inside
     * @since 0.3
     */
    @Test
    public void parsesWebPage() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(new URI("http://www.xembly.org/")).dom()
            ),
            XhtmlMatchers.hasXPath("//body")
        );
    }

    /**
     * Phandom can parse a file on disc.
     * @throws Exception If some problem inside
     * @since 0.3
     */
    @Test
    public void parsesFile() throws Exception {
        final File file = this.temp.newFile("a.html");
        FileUtils.write(file, "<html><p>hi!</p></html>");
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(file).dom()
            ),
            XhtmlMatchers.hasXPath("//body[p='hi!']")
        );
    }

    /**
     * Phandom can parse XML+XSL.
     * @throws Exception If some problem inside
     * @since 0.3
     * @see http://stackoverflow.com/questions/23342952/does-phantomjs-render-xmlxsl
     */
    @Test
    @Ignore
    public void parsesXmlAndXsl() throws Exception {
        final File dir = this.temp.newFolder();
        final File main = new File(dir, "main.xml");
        FileUtils.write(
            main,
            "<?xml-stylesheet href='i.xsl' type='text/xsl'?><index/>"
        );
        FileUtils.write(
            new File(dir, "i.xsl"),
            StringUtils.join(
                "<xsl:stylesheet",
                " xmlns:xsl='http://www.w3.org/1999/XSL/Transform'",
                " xmlns='http://www.w3.org/1999/xhtml' version='1.0'>",
                "<xsl:template match='/'>",
                "<html>hello, XML!</html>",
                "</xsl:template>"
            )
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Phandom(main).dom()
            ),
            XhtmlMatchers.hasXPath("//head and //body")
        );
    }

}
