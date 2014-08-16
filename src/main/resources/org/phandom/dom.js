/**
 * Copyright (c) 2013-2014, phandom.org
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

/*global require: false, phantom: false, console: false, window: false */

phantom.onError = function(msg, trace) {
    var stack = [];
    if (trace && trace.length) {
        trace.forEach(
            function (t) {
                stack.push(
                    '  |- ' + t.file + ': ' + t.line
                    + (t['function'] ? ' (in ' + t['function'] + ')' : '')
                );
            }
        );
    }
    console.log('phantom.onError: ' + msg + '\n' + stack.join('\n'));
    phantom.exit(-1);
};

if (phantom.version.major * 100 + phantom.version.minor < 109) {
    console.log(
        'phantomjs version 1.9 or newer is required, yours is '
        + phantom.version.major
        + '.' + phantom.version.minor
        + '.' + phantom.version.patch
    );
    phantom.exit(-1);
}

// see https://github.com/ariya/phantomjs/wiki/API-Reference-WebPage
var page = require('webpage').create();
var system = require('system');
if (system.args.length === 1) {
    console.log('Usage: dom.js <file-path>');
    phantom.exit(-1);
}
var start = Date.now();
var failure = false;
function stderr(msg) {
    var msec = Date.now() - start;
    system.stderr.writeLine((msec / 1000).toFixed(3) + ': ' + msg);
}
stderr(
    'phantomjs ' + phantom.version.major
    + '.' + phantom.version.minor
    + '.' + phantom.version.patch
);
page.onConsoleMessage = function (msg) {
    stderr(msg);
};
page.onError = function(msg, trace) {
    var stack = [];
    if (trace && trace.length) {
        trace.forEach(
            function (t) {
                stack.push(
                    '  ' + t.file + ': ' + t.line
                    + (t['function'] ? ' (in ' + t['function'] + ')' : '')
                );
            }
        );
    }
    stderr('onError: ' + msg + '\n' + stack.join('\n'));
    failure = true;
};
page.onAlert = function(msg) {
    stderr('onAlert:  ' + msg);
};
page.onResourceError = function(resourceError) {
    stderr(
        'onResourceError: unable to load resource #'
        + resourceError.id + ' from "' + resourceError.url + '"'
    );
    stderr(
        'onResourceError: error code #'
        + resourceError.errorCode + ' with "'
        + resourceError.errorString + '"'
    );
};
page.onResourceRequested = function(requestData, networkRequest) {
    stderr(
        'onResourceRequested: #' + requestData.id
        + ' with "' + JSON.stringify(requestData) + '"'
    );
};
page.onLoadStarted = function() {
    var current = page.evaluate(
        function() {
            return window.location.href;
        }
    );
    stderr('onLoadStarted: current page ' + current + ' will disappear...');
    stderr('onLoadStarted: now loading a new page...');
};
page.onLoadFinished = function(status) {
    stderr('onLoadFinished: status=' + status);
};
stderr('URL to render: ' + system.args[1]);
page.open(
    system.args[1],
    function (status) {
        if (failure) {
            stderr('javascript errors, see log above');
            phantom.exit(1);
        }
        if (status !== 'success') {
            stderr('page loading status is "' + status + '"');
            phantom.exit(2);
        }
        console.log(
            page.evaluate(
                function () {
                    /**
                    * borrowed from https://github.com/jindw/xmldom
                    */
                    var ELEMENT_NODE = 1,
                        ATTRIBUTE_NODE = 2,
                        TEXT_NODE = 3,
                        CDATA_SECTION_NODE = 4,
                        ENTITY_REFERENCE_NODE = 5,
                        ENTITY_NODE = 6,
                        PROCESSING_INSTRUCTION_NODE = 7,
                        COMMENT_NODE = 8,
                        DOCUMENT_NODE = 9,
                        DOCUMENT_TYPE_NODE = 10,
                        DOCUMENT_FRAGMENT_NODE = 11,
                        NOTATION_NODE = 12,
                        buf = [];
                    function _xmlEncoder(c) {
                        return (c === '<' && '&lt;')
                            || (c === '>' && '&gt;')
                            || (c === '&' && '&amp;')
                            || (c === '"' && '&quot;')
                            || '&#' + c.charCodeAt() + ';';
                    }
                    function serialize(node, buf) {
                        var attrs = node.attributes,
                            len = attrs ? attrs.length : 0,
                            child = node.firstChild,
                            nodeName = node.tagName,
                            isHTML = 'http://www.w3.org/1999/xhtml' === node.namespaceURI,
                            i = 0,
                            pubid = node.publicId,
                            sysid = node.systemId,
                            sub = node.internalSubset;
                        switch (node.nodeType) {
                            case ELEMENT_NODE:
                                buf.push('<', nodeName.toLowerCase());
                                for (; i < len; i++) {
                                    serialize(attrs.item(i), buf, isHTML);
                                }
                                if (child || (isHTML && !/^(?:meta|link|img|br|hr|input)$/i.test(nodeName))) {
                                    buf.push('>');
                                    if (isHTML && /^script$/i.test(nodeName)) {
                                        if (child) {
                                            buf.push(child.data);
                                        }
                                    } else {
                                        while (child) {
                                            serialize(child, buf);
                                            child = child.nextSibling;
                                        }
                                    }
                                    buf.push('</', nodeName.toLowerCase(), '>');
                                } else {
                                    buf.push('/>');
                                }
                                break;
                            case DOCUMENT_NODE:
                            case DOCUMENT_FRAGMENT_NODE:
                                while (child) {
                                    serialize(child, buf);
                                    child = child.nextSibling;
                                }
                                break;
                            case ATTRIBUTE_NODE:
                                buf.push(
                                    ' ',
                                    node.name.toLowerCase(), '="' ,
                                    node.value.replace(/[<&"]/g,_xmlEncoder),
                                    '"'
                                );
                                break;
                            case TEXT_NODE:
                                buf.push(node.data.replace(/[<&]/g,_xmlEncoder));
                                break;
                            case CDATA_SECTION_NODE:
                                buf.push('<![CDATA[', node.data, ']]>');
                                break;
                            case COMMENT_NODE:
                                buf.push("<!--", node.data, "-->");
                                break;
                            case DOCUMENT_TYPE_NODE:
                                buf.push('<!DOCTYPE ', node.name);
                                if (pubid) {
                                    buf.push(' PUBLIC "', pubid);
                                    if (sysid && sysid !== '.') {
                                        buf.push( '" "', sysid);
                                    }
                                    buf.push('">');
                                } else if (sysid && sysid !== '.') {
                                    buf.push(' SYSTEM "', sysid, '">');
                                } else {
                                    if (sub) {
                                        buf.push(' [', sub, ']');
                                    }
                                    buf.push('>');
                                }
                                break;
                            case PROCESSING_INSTRUCTION_NODE:
                                buf.push("<?", node.target, " ", node.data, "?>");
                                break;
                            case ENTITY_REFERENCE_NODE:
                                buf.push('&', node.nodeName.toLowerCase(), ';');
                                break;
                            //case ENTITY_NODE:
                            //case NOTATION_NODE:
                            default:
                                buf.push('??', node.nodeName);
                                break;
                        }
                    }
                    serialize(document, buf);
                    return buf.join('');
                }
            )
        );
        // see http://stackoverflow.com/questions/22872162
        setTimeout(function() { phantom.exit(0); }, 100);
    }
);
