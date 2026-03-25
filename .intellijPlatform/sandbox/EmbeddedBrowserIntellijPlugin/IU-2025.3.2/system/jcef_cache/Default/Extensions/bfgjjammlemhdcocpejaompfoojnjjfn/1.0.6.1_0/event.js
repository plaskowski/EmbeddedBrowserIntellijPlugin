'use strict';

function getPlatformInfo() {
    return new Promise((resolve, reject) => {
        try {
            chrome.runtime.getPlatformInfo((platformInfo) => {
                resolve(platformInfo);
            });
        } catch (err) {
            reject(err);
        }
    });
}

function terminate() {
    return new Promise((resolve) => {
        const tooltip = 'PrinterLogic Extension - This extension does not run on Chrome OS devices';
        chrome.action.setIcon({
            path: {
                16: 'icon16grey.png',
                48: 'icon48grey.png',
                128: 'icon128grey.png',
            },
        });
        chrome.action.setTitle({ title: tooltip });
        chrome.action['disable'](() => {
            resolve();
        });
    });
}

async function validateEnv() {
    const { os } = await getPlatformInfo();
    if (os !== 'cros') return;
    await terminate();
    throw new Error('Invalid Environment, exiting extension');
}

(function () {
    validateEnv().then(function () {
        chrome.runtime.onConnect.addListener(function (portExtension) {
            portExtension.onMessage.addListener(function (messageExtension) {
                var portNative = chrome.runtime.connectNative('com.printerlogic.host.native.client');
                var onMessageCalled = false;
                portNative.onDisconnect.addListener(function () {
                    if (onMessageCalled) {
                        return;
                    }
                    var message = 'Could not communicate with the PrinterLogic client. It may not be installed or running.';
                    portExtension.postMessage({
                        state: 'error',
                        message: message,
                        id: messageExtension.id
                    });
                });
                portNative.onMessage.addListener(function (messageNative) {
                    onMessageCalled = true;
                    portExtension.postMessage({
                        state: messageNative.state,
                        message: messageNative.message,
                        id: messageExtension.id
                    });
                });
                portNative.postMessage({
                    command: messageExtension.command,
                    parameters: messageExtension.parameters
                });
            });
        });
    });
})();
