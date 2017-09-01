(ns questlog.middleware
  (:require [questlog.request-logger :as logger]
            [questlog.request :as request]))

(defn wrap-request-logger [handler logger]
  (fn [req]
    (let [req (request/with-id req)]

      (logger/log-request logger req)

      (try
        (let [{:keys [status] :as resp} (handler req)]
          (if (< status 500)
            (logger/log-response logger req resp)
            (logger/log-error logger req resp))
          resp)

        (catch Exception e
          (logger/log-exception logger req e)
          (throw e))))))
