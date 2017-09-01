(ns questlog.middleware
  (:require [questlog.request-logger :as logger]
            [questlog.request :as request]))

(defn wrap-request-logger [handler {:keys [logger redact-keys] :as opts
                                    :or {redact-keys #{:authorization
                                                       :password}}}]
  (fn [req]
    (let [req (request/with-id req)
          redacted-req (request/redact req redact-keys)]

      (logger/log-request logger redacted-req)

      (try
        (let [{:keys [status] :as resp} (handler req)]
          (if (< status 500)
            (logger/log-response logger redacted-req resp)
            (logger/log-error logger redacted-req resp))
          resp)

        (catch Exception e
          (logger/log-exception logger redacted-req e)
          (throw e))))))
