(ns questlog.request-test
  (:require [questlog.request :refer :all]
            [clojure.test :refer :all]))

(deftest with-id-test
  (testing "'with-id'"
    (let [req {:foo "bar" :baz "bip"}
          subject (with-id req)]

      (is (string? (:questlog.request/id subject))
          "adds a string under the `:questlog.request/id` key"))))

(deftest redact-test
  (testing "'redact'"
    (let [req {:foo "bar" :password "secret"
               :headers {"baz" "bip" "authorization" "super-secret"}}]

      (testing "with redact keys"
        (let [redact-keys #{:authorization :password}
              subject (redact req redact-keys)]

          (is (= redacted (:password subject))
              "redacts top level keys.")

          (is (= redacted (get (:headers subject) "authorization"))
              "redacts both string and nested keys.")))

      (testing "with no redact keys"
        (let [redact-keys #{}
              subject (redact req redact-keys)]

          (is (= req subject)
              "does not redact any keys."))))))
