(ns questlog.request
  (:import (java.util UUID)))

(defn with-id [req]
  (let [id (UUID/randomUUID)]
    (assoc req ::id id)))
