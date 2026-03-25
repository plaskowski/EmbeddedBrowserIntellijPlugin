'use strict';

let port;

const messageHandler = (e) => {
    if (e.source !== window || !e.data.type || e.data.type !== 'PrinterLogicClientRequest')
        return;
    port.postMessage(e.data);
};

// eslint-disable-next-line complexity
const connect = () => {
    const printerLogicClient = document.querySelector("#printerLogicClientInterface");
    const foundAttr = printerLogicClient ? printerLogicClient.getAttribute("chrome") : null;
    if (document.body.contains(printerLogicClient) && foundAttr === null || foundAttr === '' ) {
        printerLogicClient.setAttribute('chrome', '');
        // eslint-disable-next-line no-undef
        port = chrome.runtime.connect();

        port.onDisconnect.addListener((p) => {
            p.onMessage.removeListener();
            p.onDisconnect.removeListener();
            connect();
        });
        
        port.onMessage.addListener(function (message) {
            window.postMessage({
                type: 'PrinterLogicClientResponse',
                state: message.state,
                message: message.message,
                id: message.id
            }, '*');
        });

        window.addEventListener("message", messageHandler);
    }
};

(function () {
    connect();
})();
