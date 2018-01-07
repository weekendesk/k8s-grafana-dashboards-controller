const JSONStream = require('json-stream');

const w = (k8sResource, labelSelector) => {
    const jsonStream = new JSONStream();
    const stream = k8sResource.getStream({ qs: { watch: true, labelSelector } });
    stream.pipe(jsonStream);
    return jsonStream;
}

class Watcher {
    constructor(k8sResource, labelSelector) {
        this.resource = k8sResource;
        this.labelSelector = labelSelector || "";
    }

    watch(handlers = {}) {
        w(this.resource, this.labelSelector).on("data", event => {
            try {
                switch (event.type) {
                    case "ADDED":
                        handlers.onAdded && handlers.onAdded(event.object);
                        break;
                    case "MODIFIED":
                        handlers.onModified && handlers.onModified(event.object);
                        break;
                    case "DELETED":
                        handlers.onDeleted && handlers.onDeleted(event.object);
                        break;
                }

            } catch (error) {
                handlers.onUnhandledError && handlers.onUnhandledError(error);
            }
        })
    }
}

module.exports = Watcher;