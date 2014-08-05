(ns aparapi-matrix.core
  (:require [clojure.core.matrix :as m]
            [clojure.core.matrix.protocols :as mp]
            [clojure.core.matrix.implementations :as mi]
            [clojure.core.matrix.compliance-tester :as mc])
  (:use [clojure.stacktrace])
  (:import [aparapi_matrix MatMul MatMulEle]
           [com.amd.aparapi.Kernel.EXECUTION_MODE]))

;(defn matmultest []
;  "test whether matrix maltiply calc currectly or not."
;  (let [rowA     3
;        colArowB 2
;        colB     4
;        matA     (float-array [1 2 0 -1 5 1])
;        matB     (float-array [1 -2 4 0 0 -1 2 3])
;        ap (MatMul. matA matB rowA colArowB colB)]
;    (.setExecutionMode ap com.amd.aparapi.Kernel$EXECUTION_MODE/GPU)
;    (def time1 (System/currentTimeMillis))
;    (.execute ap (* rowA colB))
;    (println "Time taken for kernel execution "
;             (- (System/currentTimeMillis) time1))
;    (println "result: " (map #(str %) (.result ap)))
;    (.dispose ap)))

(def last-execution-mode :CPU)
(def last-execution-time -1)
(def execution-mode com.amd.aparapi.Kernel$EXECUTION_MODE/CPU)

(defn set-execution-mode [mode]
  (alter-var-root #'execution-mode
                  #(cond
                     (= %2 :GPU)  com.amd.aparapi.Kernel$EXECUTION_MODE/GPU
                     (= %2 :CPU)  com.amd.aparapi.Kernel$EXECUTION_MODE/CPU
                     (= %2 :JTP)  com.amd.aparapi.Kernel$EXECUTION_MODE/JTP
                     (= %2 :NONE) com.amd.aparapi.Kernel$EXECUTION_MODE/NONE
                     (= %2 :SEQ)  com.amd.aparapi.Kernel$EXECUTION_MODE/SEQ)
                  mode))

(defn get-execution-time [_ kernel]
  (.getExecutionTime kernel))

(defn get-execution-mode [_ kernel]
  (let [m (.getExecutionMode kernel)]
    (cond
      (= m com.amd.aparapi.Kernel$EXECUTION_MODE/GPU) :GPU
      (= m com.amd.aparapi.Kernel$EXECUTION_MODE/CPU) :CPU
      (= m com.amd.aparapi.Kernel$EXECUTION_MODE/JTP) :JTP
      (= m com.amd.aparapi.Kernel$EXECUTION_MODE/NONE) :NONE
      (= m com.amd.aparapi.Kernel$EXECUTION_MODE/SEQ)  :SEQ)))

(defn gather [col n]
  "gather n element in vector."
  (loop [s [] col col]
    (if (empty? col)
      s
      (recur (conj s (take n col))
             (drop n col)))))

(defn unnest [vect shape]
  "opposite to flatten function."
  (if (= (count shape) 1)
    vect
    (recur (gather vect (last shape))
           (drop-last shape))))

(defn get-dim [vect]
  "get dimension of nested vector or scaler."
  (if (not (vector? vect))
    0
    (if (and (> (count vect) 0) (vector? (first vect)))
      2
      1)))

(defprotocol IEditAparapi-matrix
  "protocol for mutable edit"
  (get-substance [m])
  (set-substance! [m val])
  (get-dimension [m])
  (set-dimention! [m val]))

(deftype Aparapi-matrix [^:volatile-mutable substance ^:volatile-mutable dimension]
  ;; if you use :volatile-mutable, you have to refresh JVM runtime.
  ;; dimension is 0,1,2 only.
  IEditAparapi-matrix
  (get-substance [m]
    (. m substance))
  (set-substance! [m val]
    (set! substance val))
  (get-dimension [m]
    (. m dimension))
  (set-dimention! [m val]
    (set! dimension val))
  Object
  (toString [m]
    (str (get-substance m)))
  mp/PImplementation
  (implementation-key [m]
    :aparapi-matrix)
  (meta-info [m]
    "core.matrix implementation using aparapi.")
  (construct-matrix [m data]
    "data is sequence or Aparapi-matrix object."
    (if (seq? data)
      (Aparapi-matrix. (vec data) (get-dim data))
      data))
  (new-vector [m length]
    (Aparapi-matrix. (vec (replicate length 0)) 1))
  (new-matrix [m rows colums]
    (Aparapi-matrix.
      (vec (replicate rows
                      (vec (replicate colums 0))))
      2))
  (new-matrix-nd [m shape]
    (cond (= (count shape) 0)
          (Aparapi-matrix. 0 0)
          (= (count shape) 1)
          (mp/new-vector m (first shape))
          (= (count shape) 2)
          (mp/new-matrix m (first shape) (second shape))
          :else
          (throw (Exception. "Error in Aparapi-matrix.core/new-matrix-nd"))))
  (supports-dimensionality? [m dimensions]
    (<= dimensions 2))
  mp/PDimensionInfo
  (dimensionality [m]
    (get-dimension m))
  (get-shape [m]
    (for [i (range (.dimension m))]
      (mp/dimension-count m i)))
  (is-scalar? [m]
    (= 0 (.dimension m)))
  (is-vector? [m]
    (= 1 (.dimension m)))
  (dimension-count [m dimension-number]
    (loop [sub (get-substance m)
           x dimension-number]
      (if (= x 0)
        (count sub)
        (recur (first sub) (- x 1)))))
  mp/PIndexedAccess
  (get-1d [m row]
    (nth (get-substance m) row))
  (get-2d [m row column]
    (nth (nth (get-substance m) row) column))
  (get-nd [m indexed]
    (loop [x (get-substance m)
           is indexed]
      (if (empty? is)
       x
       (recur (nth x (first is)) (rest is)))))
  mp/PIndexedSetting
  (set-1d [m row v]
    (Aparapi-matrix. (assoc (get-substance m) row v) (get-dimension m)))
  (set-2d [m row column v]
    (Aparapi-matrix. (assoc-in (get-substance m) [row column] v) (get-dimension m)))
  (set-nd [m indexes v]
    (Aparapi-matrix. (assoc-in (get-substance m) indexes v) (get-dimension m)))
  (is-mutable? [m]
    false)
  mp/PMatrixMultiply
  (matrix-multiply [a b]
    (let [matA (float-array (flatten (get-substance a)))
          matB (float-array (flatten (get-substance b)))
          rowA (mp/dimension-count a 0)
          colArowB (mp/dimension-count a 1)
          colB (mp/dimension-count b 1)
          ap (MatMul. matA matB rowA colArowB colB)]
      (.setExecutionMode ap execution-mode)
      (.execute ap (* rowA colB))
      (alter-var-root #'last-execution-mode get-execution-mode ap)
      (alter-var-root #'last-execution-time get-execution-time ap)
      (Aparapi-matrix. (unnest (.result ap) [rowA colB]) 2)))
  (element-multiply [a b]
    (let [shape (mp/get-shape a)
          matA (float-array (flatten (get-substance a)))
          matB (float-array (flatten (get-substance b)))
          ap (MatMulEle. matA matB)]
        (.setExecutionMode ap execution-mode)
        (.execute ap (reduce * shape))
        (alter-var-root #'last-execution-mode get-execution-mode ap)
        (alter-var-root #'last-execution-time get-execution-time ap)
        (Aparapi-matrix. (unnest (.result ap) shape) (count shape)))))

(defn get-instance []
  "get Aparapi-matrix instance."
  (Aparapi-matrix. [[1 2] [3 4] [5 6]] 2))

;; register Aparapi-matrix to core.matrix
(mi/register-implementation (get-instance))
