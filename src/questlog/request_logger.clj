(ns questlog.request-logger)

(defprotocol RequestLogger
  (log-request [logger request] "Log that a request was received.")
  (log-response [logger request response] "Log that a response will be sent.")
  (log-error [logger request response] "Log a handler error.")
  (log-exception [logger request e] "Log a handler exception."))
