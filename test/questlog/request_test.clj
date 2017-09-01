(ns questlog.request-test
  (:require [questlog.request :refer :all]
            [clojure.test :refer :all]))

(deftest with-id-test
  (testing "'with-id'"
    (let [req {:foo "bar" :baz "bip"}
          subject (with-id req)]

      (is (uuid? (:questlog.request/id subject))
          "adds a uuid under the `:questlog.request/id` key"))))
