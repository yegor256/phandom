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

// see https://github.com/ariya/phantomjs/wiki/API-Reference-WebPage
var page = require('webpage').create();
var system = require('system');
if (system.args.length === 1) {
    console.log('Usage: dom.js <file-path>');
    phantom.exit();
}
var start = Date.now();
var failure = false;
function stderr(msg) {
    var msec = Date.now() - start;
    system.stderr.writeLine((msec / 1000).toFixed(3) + ': ' + msg);
}
page.onConsoleMessage = function (msg) {
    stderr(msg);
};
page.onError = function(msg, trace) {
    var stack = [];
    if (trace && trace.length) {
        trace.forEach(
            function(t) {
                stack.push(
                    '  ' + t.file + ': ' + t.line
                    + (t.function ? ' (in ' + t.function + ')' : '')
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
        console.log(page.content);
        phantom.exit(0);
    }
);
