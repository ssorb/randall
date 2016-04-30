(defproject randall "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [org.clojure/clojurescript "1.8.51"]
                 [org.omcljs/om "0.9.0"]
                 [sablono "0.3.6"]]

  :plugins [[lein-figwheel "0.5.2"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild { 
              :builds [{:id "dev"
                        :source-paths ["src"]

                        ;; If no code is to be run, set :figwheel true for continued automagical reloading
                        :figwheel {:on-jsload "randall.core/on-js-reload"}

                        :compiler {:main randall.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/randall.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :source-map-timestamp true}}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/randall.js"
                                   :main randall.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"]
             ;:nrepl-port 7888
             })

