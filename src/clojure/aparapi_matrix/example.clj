(ns aparapi-matrix.example
  (:refer-clojure :exclude [* - + == /])
  (:require [aparapi-matrix.core :as ac]
            [clojure.core.matrix.protocols :as mp]
            [clojure.core.matrix.implementations :as mi]
            [clojure.core.matrix.compliance-tester :as mc])
  (:use     [clojure.core.matrix]
            [clojure.core.matrix.operators]))

(defn example-run []
  (mp/is-scalar? (ac/get-instance))
  (ac/get-substance (ac/get-instance))
  (ac/get-substance (mp/new-vector (ac/get-instance) 1))
  (mi/get-canonical-object (ac/get-instance))
  (mp/dimension-count (ac/get-instance) 0)
  (mp/dimension-count (ac/get-instance) 1)
  (mp/dimensionality (ac/get-instance))
  (mp/get-0d (ac/get-instance))
  (mp/get-1d (ac/get-instance) 0 )
  (mp/get-2d (ac/get-instance) 0 1)
  (mp/get-nd (ac/get-instance) [0 1])
  (mp/set-2d (ac/get-instance) 0 1 100)
  (mp/transpose (ac/get-instance))
  (mp/convert-to-nested-vectors (ac/get-instance))
  (class (mp/convert-to-nested-vectors (ac/get-instance)))
  (.toString (ac/get-instance))
  (str (ac/get-instance))
  (let [a (mp/construct-matrix (ac/get-instance) [[4 3] [3 2]])
        b (inverse a)] ; [[-2 3] [3 -4]]
    (mmul a b))
  (vec (ac/get-substance (mp/element-multiply (ac/get-instance) (ac/get-instance))))

  (set-current-implementation :aparapi-matrix)
  (shape (array [[1 2] [3 4] [5 6]])) ; [3 2]
  (dimension-count (array [[1 2] [3 4] [5 6]]) 0) ; 3
  (dimension-count (array [[1 2] [3 4] [5 6]]) 1) ; 2
  (new-matrix 3 2) ; [[0 0] [0 0] [0 0]]
  (class (mp/convert-to-nested-vectors (new-matrix 3 2)))
  (set-selection (array [[1 2] [3 4] [5 6]]) :all :all 10)
  (* (new-matrix 1000 100) (new-matrix 100 1000))
  (str ac/last-execution-mode " "ac/last-execution-time))

(ac/set-execution-mode :CPU)
(example-run)
