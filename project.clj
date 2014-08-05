(defproject aparapi-matrix "0.1.0-SNAPSHOT"
  :description "core.matrix implementation using aparapi which is API for GPU calculation."
  :url "http://bobuhiro11.net"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :resource-paths ["/Users/bobuhiro11/Aparapi_2012_01_23_MacOSX/aparapi.jar"]
  :jvm-opts ["-Djava.library.path=/Users/bobuhiro11/Aparapi_2012_01_23_MacOSX"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [net.mikera/core.matrix "0.26.0"]
                 [net.mikera/vectorz-clj "0.23.0"]
                 [org.clojure/tools.trace "0.7.5"]])
