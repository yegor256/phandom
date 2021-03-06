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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.Charsets;

/**
 * Page to render.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.3
 */
@Immutable
interface Page {

    /**
     * Get its URI.
     * @return URI
     * @throws IOException If fails
     */
    URI uri() throws IOException;

    /**
     * Text page.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "html")
    @Loggable(Loggable.DEBUG)
    final class Text implements Page {
        /**
         * HTML content.
         */
        private final transient String html;
        /**
         * Public ctor.
         * @param content HTML content
         */
        public Text(final String content) {
            this.html = content;
        }
        @Override
        public URI uri() throws IOException {
            return new Temp(
                new ByteArrayInputStream(
                    this.html.getBytes(Charsets.UTF_8)
                ),
                ".html"
            ).file().toURI();
        }
    }

    /**
     * Web page.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "href")
    @Loggable(Loggable.DEBUG)
    final class Web implements Page {
        /**
         * HREF.
         */
        private final transient String href;
        /**
         * Public ctor.
         * @param uri URI
         */
        public Web(final URI uri) {
            this.href = uri.toString();
        }
        @Override
        public URI uri() {
            return URI.create(this.href);
        }
    }

}
