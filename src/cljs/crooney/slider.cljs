(ns crooney.slider
  (:require [clojure.browser.repl :as repl]
            [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects]
            [cljs.core.async :as async :refer [timeout <! >! chan close!]])
  (:require-macros [cljs.core.async.macros :as am :refer [go alt!]]
                   [enfocus.macros :as em]))

(repl/connect "http://localhost:9000/repl")

(defn default-transition [from to t]
  (ef/at js/document
         [from] (effects/fade-out t)
         [to]   (effects/fade-in  t)))

(defn start-slider
  [id pause trans-time transition]
  (let [sels (fn [x] (map #(str "#" %)
                          (:p (ef/from js/document :p [x] (ef/get-prop :id)))))
        ps (sels ".pane")
        bs (sels ".button")
        c (chan)
        btn-click (fn [x] (events/listen :click #(go (>! c x))))]
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (btn-click (dec (count ps)))
             [(second bs)] (btn-click 1)))
    (go (while true (<! (timeout pause)) (>! c 1)))
    (go (loop [is (cycle ps)]
          (let [js (drop (<! c) is)]
            (transition (first is) (first js) trans-time)
            (recur js))))))
  ;;[id pause trans-time] (start-slider id pause trans-time default-transition))

(set! (.-onload js/window) #(start-slider "example" 4000 500 default-transition))

