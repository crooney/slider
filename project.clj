(defproject crooney/slider "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  ;; for core.async
  ;;:repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :repositories {"sonatype-staging"
                 "https://oss.sonatype.org/content/groups/staging/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1845-RC1"]
                 [compojure "1.1.5"]
                 [enfocus "2.0.0-SNAPSHOT"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [ring/ring-jetty-adapter "1.2.0"]]
  :plugins [[lein-cljsbuild "0.3.2"]
            [lein-ring "0.8.6"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/slider.js"
                           :optimizations :whitespace
                           :pretty-print true}
                :jar true}]}
  :ring {:handler crooney.server/app})
