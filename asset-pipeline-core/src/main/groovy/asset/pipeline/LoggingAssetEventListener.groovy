package asset.pipeline

import groovy.util.logging.Slf4j

@Slf4j
class LoggingAssetEventListener implements AssetEventListener {

    @Override
    void triggerEvent(String eventName, String message) {
        log.info(message)
    }
}
