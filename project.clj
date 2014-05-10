(defproject randall "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/clojurescript "0.0-2197"]
                 [figwheel "0.1.1"]
                 [om "0.6.2"]
                 [sablono "0.2.16"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.1"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "randall"
              :source-paths ["src"]
              :compiler {
                :output-to "resources/public/js/compiled/randall.js"
                :output-dir "resources/public/js/compiled/out"
                :optimizations :none
                :source-map true}}]}
  :figwheel {:http-server-root "public" ;; default and assumes "resources"
             :server-port 3449 ;; default
             :css-dirs ["public/resources/css"] ;; watch and update CSS
             })
