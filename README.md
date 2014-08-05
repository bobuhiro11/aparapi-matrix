# aparapi-matrix

`aparapi-matrix` is a core.matrix implementation, and uses aparapi which is API for GPU calculation.

## Installation

1. download `Aparapi` from [https://code.google.com/p/aparapi/](https://code.google.com/p/aparapi/) and extract it.
2. set `:resource-paths`, `:jvm-opts`, `:dependencies` parameters as below:

```clojure
(defproject hoge "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["/Users/bobuhiro11/Aparapi_2012_01_23_MacOSX/aparapi.jar"]
  :jvm-opts ["-Djava.library.path=/Users/bobuhiro11/Aparapi_2012_01_23_MacOSX"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aparapi-matrix "0.1.0-SNAPSHOT"]])
```

## Basic usage

```clojure
(ns hoge.core
  (:refer-clojure :exclude [* - + == /])
  (:use aparapi-matrix.core)
  (:use clojure.core.matrix)
  (:use clojure.core.matrix.operators)
  (:use clojure.core.matrix.implementations))

;; use aparapi-matrix implementation
(set-current-implementation :aparapi-matrix)
(set-execution-mode :CPU) ; :CPU, :GPU, etc.

;; use matrix library (core.matrix)
(def a (matrix [[2 0] [0 2]]))

(* a [[1 2] [3 4]])

(sqrt a)

(shape (array [[1 2] [3 4] [5 6]])) ; [3 2]

(dimension-count (array [[1 2] [3 4] [5 6]]) 0)

(new-matrix 3 2) ; [[0 0] [0 0] [0 0]]

(set-selection (array [[1 2] [3 4] [5 6]]) :all :all 10)

(* (new-matrix 1000 100) (new-matrix 100 1000))

(print "mode:" last-execution-mode "time:" last-execution-time)
```
