(ns questlog.request-logger.tools-logging
  (:require [questlog.middleware :as middleware]
            [questlog.request-logger :refer [RequestLogger]]
            [questlog.request :as request]
            [clansi.core :as ansi]
            [clj-stacktrace.core :as stactrace]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log event formatting                                                     ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- join [evt ctx]
  (string/join ", " [evt ctx]))

(defn- print-data [data color]
  (-> data pr-str (ansi/style color)))

(defn- print-event [evt ctx]
  (join (print-data evt :cyan)
        (print-data ctx :magenta)))

(defn- print-error [evt ctx]
  (join (print-data evt :red)
        (print-data ctx :yellow)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log event and context data                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- request-info [req]
  (select-keys req [::request/id :headers :params :remote-addr :request-method
                    :query-string :uri]))

(defn- request-details [req]
  (select-keys req [::request/id :character-encoding :content-length
                    :content-type :query-string :remote-addr :request-method
                    :scheme :server-name :server-port :uri]))

(defn- response-info [req resp]
  (let [req-data (request-info req)]
    {:request req-data, :response resp}))

(defn- exception-details [req e]
  (let [req-data (request-info req)
        exception-data (stactrace/parse-exception e)]
    {:request req-data, :exception exception-data}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; logger                                                                   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord ToolsLogging []
  RequestLogger
  (log-request [_ req]
    (log/info (print-event ::request/received (request-info req)))
    (log/debug (print-event ::request/details (request-details req))))

  (log-response [_ req resp]
    (log/info (print-event ::request/response (response-info req resp))))

  (log-error [_ req resp]
    (log/error (print-error ::request/error (response-info req resp))))

  (log-exception [_ req e]
    (log/error (print-error ::request/exception (exception-details req e)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log middleware                                                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap-request-logger [handler config]
  (let [logger-config (assoc config :logger (->ToolsLogging))]
    (middleware/wrap-request-logger handler logger-config)))
