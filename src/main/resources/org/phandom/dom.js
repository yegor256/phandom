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
function log(msg) {
    console.log('console: ' + msg);
}
page.onConsoleMessage = function (msg) {
    log(msg);
};
page.onError = function(msg, trace) {
    var msgStack = ['ERROR: ' + msg];
    if (trace && trace.length) {
        msgStack.push('TRACE:');
        trace.forEach(
            function(t) {
                msgStack.push(' -> ' + t.file + ': ' + t.line + (t.function ? ' (in function "' + t.function + '")' : ''));
            }
        );
    }
    log(msgStack.join('\n'));
};
page.onAlert = function(msg) {
    log('ALERT: ' + msg);
};
page.onResourceError = function(resourceError) {
    log('Unable to load resource (#' + resourceError.id + 'URL:' + resourceError.url + ')');
    log('Error code: ' + resourceError.errorCode + '. Description: ' + resourceError.errorString);
};
page.onResourceRequested = function(requestData, networkRequest) {
    log('Request (#' + requestData.id + '): ' + JSON.stringify(requestData));
};
page.onLoadStarted = function() {
    var current = page.evaluate(function() {
        return window.location.href;
    });
    log('Current page ' + current +' will disappear...');
    log('Now loading a new page...');
};
page.onLoadFinished = function(status) {
    log('Status: ' + status);
};
log('URL to render: ' + system.args[1]);
page.open(
    system.args[1],
    function (status) {
        if (status === 'success') {
            console.log(page.content);
        } else {
            log(status);
        }
        phantom.exit();
    }
);
