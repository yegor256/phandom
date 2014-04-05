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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link Phandom}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class PhandomTest {

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
                        "div.innerHTML = '&lt;&#10;<b>&gt;</b>&#10;&amp;';",
                        "div.style.color = 'red';",
                        "div.setAttribute('class', 'foo');",
                        "document.body.appendChild(div);",
                        "}}\n//]]></script></head>",
                        "<body onload='onLoad();'></body></html>\n\n"
                    )
                ).dom()
            ),
            XhtmlMatchers.hasXPath("/html/body[count(div)=1000]")
        );
    }

}
