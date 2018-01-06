const JSONStream = require('json-stream');

const w = (k8sResource) => {
    const jsonStream = new JSONStream();
    const stream = k8sResource.getStream({ qs: { watch: true } });
    stream.pipe(jsonStream);
    return jsonStream;
}

class Watcher {
    constructor(k8sResource) {
        this.resource = k8sResource;
    }

    watch(handlers = {}) {
        w(this.resource).on("data", event => {
            try {
                switch (event.type) {
                    case "ADDED":
                        if (handlers.onAdded) {
                            handlers.onAdded(event.object);
                        }
                        break;
                    case "MODIFIED":
                        if (handlers.onModified) {
                            handlers.onModified(event.object);
                        }
                        break;
                    case "DELETED":
                        if (handlers.onDeleted) {
                            handlers.onDeleted(event.object);
                        }
                        break;
                }

            } catch (error) {
                if (handlers.onUnhandledError) {
                    handlers.onUnhandledError(error);
                }
            }
        })
    }
}

module.exports = Watcher;