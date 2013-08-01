(ns example.core
  (:require [example.css :as css]))

(defn -main
  "Delegate to -mains of components"
  [& args]
  (apply css/-main args))
