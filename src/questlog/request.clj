(ns questlog.request
  (:require [clojure.walk :refer [prewalk]])
  (:import (java.util UUID)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; request id generation                                                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn with-id [req]
  (let [id (UUID/randomUUID)]
    (assoc req ::id id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; request map redaction                                                    ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def redacted "Standard replacement value for redacted map keys."
  "[REDACTED]")

(defn- redact-map-entry [entry redact-keys]
  (let [[k v] entry]
    (if (get redact-keys k)
      [k redacted]
      entry)))

(defn- entry-redacter [redact-keys]
  (fn [x]
    (if (map-entry? x)
      (redact-map-entry x redact-keys)
      x)))

(defn redact [req redact-keys]
  (let [redacter (entry-redacter redact-keys)]
    (prewalk redacter req)))
