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

(defn- print-context [ctx]
  (print-data ctx :magenta))

(defn- print-event [evt ctx]
  (join (print-data evt :cyan)
        (print-context ctx)))

(defn- print-error [evt ctx]
  (join (print-data evt :red)
        (print-context ctx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log event and context data                                               ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- request-info [{:keys [::request/id] :as req}]
  (let [summary (select-keys req [:request-method :uri :params :query-string
                                  :remote-addr])]
    {:summary summary, :request-id id}))

(defn- request-details [{:keys [::request/id] :as req}]
  (let [details (select-keys req [:character-encoding :content-length
                                  :content-type :headers :query-string
                                  :remote-addr :request-method :scheme
                                  :server-name :server-port :uri])]
    {:details details, :request-id id}))

(defn- response-info [{:keys [::request/id]} resp]
  {:response resp, :request-id id})

(defn- exception-details [{:keys [::request/id]} e]
  (let [exception-data (stactrace/parse-exception e)]
    {:exception exception-data, :request-id id}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; logger                                                                   ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord ToolsLogging []
  RequestLogger
  (log-request [_ req]
    (log/info (print-event :http.request/received (request-info req)))
    (log/debug (print-event :http.request/details (request-details req))))

  (log-response [_ req resp]
    (log/info (print-event :http.response/sending (response-info req resp))))

  (log-error [_ req resp]
    (log/error (print-error :http.response/error (response-info req resp))))

  (log-exception [_ req e]
    (log/error (print-error :http.response/exception (exception-details req e)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log middleware                                                           ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap-request-logger [handler config]
  (let [logger-config (assoc config :logger (->ToolsLogging))]
    (middleware/wrap-request-logger handler logger-config)))
