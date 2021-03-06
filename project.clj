(defproject crooney/slider "0.1.0"
  :description "Clojurescript slideshow library"
  :url "http://github.com/crooney/slider"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  ;; for core.async
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  ;;:repositories {"sonatype-staging" "https://oss.sonatype.org/content/groups/staging/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enfocus "2.0.0-SNAPSHOT"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]]
  :profiles {:example {:dependencies [[garden "0.1.0-beta6"]]
                       :source-paths ["src/example"]
                       :main example.core}}
  :aliases {"example" ["with-profile" "example" "run"]}
  :plugins [[lein-cljsbuild "0.3.2"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds
              {:dev
               {:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/slider.js"
                           :optimizations :whitespace
                           :pretty-print true}}
               :prod
               {:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/slider.js"
                           :optimizations :advanced
                           :pretty-print false}
                :jar true}}})
