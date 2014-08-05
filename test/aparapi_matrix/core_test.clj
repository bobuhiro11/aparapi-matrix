(ns aparapi-matrix.core-test
  (:require [clojure.test :refer :all]
            [aparapi-matrix.core :refer :all]))

(deftest compliance-test
  (clojure.core.matrix.compliance-tester/compliance-test
    (aparapi-matrix.core/get-instance)))
(compliance-test)
