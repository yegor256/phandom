/**
 * Copyright (c) 2013-2017, phandom.org
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseProcess;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * PhantomJS DOM.
 *
 * <p>Use it to parse XML/XHTML/HTML document using PhantomJS, for example:
 *
 * <pre>Document dom = new Phandom(
 *   "&lt;html&gt;&lt;p&gt;Hey!&lt;/p&gt;&lt;/html&gt;"
 * ).dom();
 * Element element = dom.getElementByTag("p");</pre>
 *
 * <p>The most popular use case for the class would be its usage
 * in a unit test, to make sure your HTML document (together with its
 * embedded JavaScript scripts) is renderable by a browser:
 *
 * <pre>import com.rexsl.test.XhtmlMatchers;
 * import org.hamcrest.MatcherAssert;
 * import org.junit.Assume;
 * import org.junit.Test;
 * import org.phandom.Phandom;
 * public class HtmlTest {
 *   &#64;Test
 *   public void testPageRenderability() {
 *     Assume.assumeTrue(Phandom.isInstalled());
 *     MatcherAssert.assertThat(
 *       new Phandom("&lt;html>&lt;p>Hey!&lt;/p>&lt;/html>").dom(),
 *       XhtmlMatchers.hasXPath("//body/p[.='Hey!']")
 *     );
 *   }
 * }</pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "page")
@Loggable(Loggable.DEBUG)
public final class Phandom {

    /**
     * Name of binary phantomjs.
     */
    private static final String BIN = "phantomjs";

    /**
     * Pattern to match phantomjs version.
     */
    private static final Pattern VERSION =
        Pattern.compile("\\d+\\.\\d+\\.\\d+");

    /**
     * The page to render.
     */
    private final transient Page page;

    /**
     * Public ctor.
     * @param content Content to encapsulate
     */
    public Phandom(@NotNull final String content) {
        this(new Page.Text(content));
    }

    /**
     * Public ctor.
     * @param stream Stream with content
     * @throws IOException If fails to read the stream
     */
    public Phandom(@NotNull final InputStream stream) throws IOException {
        this(IOUtils.toString(stream, CharEncoding.UTF_8));
    }

    /**
     * Public ctor.
     * @param uri URI to render
     * @throws IOException If fails to read the stream
     * @since 0.3
     */
    public Phandom(@NotNull final URI uri) throws IOException {
        this(new Page.Web(uri));
    }

    /**
     * Public ctor.
     * @param url URL to render
     * @throws IOException If fails to read the stream
     * @since 0.3
     */
    public Phandom(@NotNull final URL url) throws IOException {
        this(URI.create(url.toString()));
    }

    /**
     * Public ctor.
     * @param file File to render
     * @throws IOException If fails to read the stream
     * @since 0.3
     */
    public Phandom(@NotNull final File file) throws IOException {
        this(file.toURI());
    }

    /**
     * Public ctor.
     * @param src Page with sources
     * @since 0.3
     */
    private Phandom(final Page src) {
        this.page = src;
    }

    /**
     * PhantomJS binary is installed?
     * @return TRUE if installed
     * @since 0.2
     */
    public static boolean isInstalled() {
        String stdout;
        try {
            stdout = new VerboseProcess(
                new ProcessBuilder(Phandom.BIN, "--version"),
                Level.FINE, Level.FINE
            ).stdoutQuietly().trim();
        } catch (final IllegalStateException ex) {
            stdout = ex.getLocalizedMessage();
        }
        return Phandom.VERSION.matcher(stdout).matches();
    }

    /**
     * Get DOM.
     * @return DOM
     * @throws IOException If fails
     */
    public Document dom() throws IOException {
        final Process process = this.builder().start();
        process.getOutputStream().close();
        return Phandom.parse(
            new VerboseProcess(process, Level.FINE, Level.FINE).stdout()
        );
    }

    /**
     * Create process builder.
     * @return Builder
     * @throws IOException If fails
     */
    public ProcessBuilder builder() throws IOException {
        final InputStream src = this.getClass().getResourceAsStream("dom.js");
        try {
            return new ProcessBuilder(
                Phandom.BIN,
                new Temp(src, ".js").file().getAbsolutePath(),
                this.page.uri().toString()
            );
        } finally {
            src.close();
        }
    }

    /**
     * Parse XML into DOM.
     * @param xml XML to parse
     * @return DOM
     * @throws IOException If fails
     */
    private static Document parse(final String xml) throws IOException {
        if (xml.isEmpty()) {
            throw new IOException(
                // @checkstyle LineLength (1 line)
                "phantomjs produced an empty output instead of HTML, looks like an internal bug of phandom"
            );
        }
        try {
            return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(IOUtils.toInputStream(xml, CharEncoding.UTF_8));
        } catch (final ParserConfigurationException ex) {
            Logger.warn(
                Phandom.class,
                "XML parsing failure on HTML by phantomjs:\n%s", xml
            );
            throw new IOException("internal parsing error of phandom", ex);
        } catch (final SAXException ex) {
            Logger.warn(
                Phandom.class,
                "SAX failure on HTML by phantomjs:\n%s", xml
            );
            throw new IOException("internal SAX error of phandom", ex);
        }
    }

}
