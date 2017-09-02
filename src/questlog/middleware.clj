(ns questlog.middleware
  (:require [questlog.request-logger :as logger]
            [questlog.request :as request]))

(defn wrap-request-logger [handler {:keys [logger redact-keys] :as opts
                                    :or {redact-keys #{:authorization
                                                       :cookies
                                                       :password}}}]
  (fn [req]
    (let [req (request/with-id req)
          redacted-req (request/redact req redact-keys)]

      (logger/log-request logger redacted-req)

      (try
        (let [{:keys [status] :as resp} (handler req)
              redacted-resp (request/redact resp redact-keys)]
          (if (< status 500)
            (logger/log-response logger redacted-req redacted-resp)
            (logger/log-error logger redacted-req redacted-resp))
          resp)

        (catch Exception e
          (logger/log-exception logger redacted-req e)
          (throw e))))))
