(defproject coursework "1.0.0"
  :description "A SOFM trainer written in Clojure."
  :url "http://github.com/samcoy3/clj-sofm"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [seesaw "1.4.5"]
                 [incanter "1.5.7"]]
  :main ^:skip-aot coursework.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
