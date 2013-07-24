(ns crooney.slider
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects]
            [cljs.core.async :as async :refer [timeout <! >! chan]])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]
                   [enfocus.macros :as em]))

(defn class-selectors
  "Extract all ids that match selector x and make them id selectors: e.g. #foo."
  [x]
  (map #(str "#" %)
       (:p (ef/from js/document :p [x] (ef/get-prop :id)))))

(defn default-transition
  "Simple simultaneous fade in/out lasting t milliseconds."
  [from to t]
  (ef/at js/document
         [from] (effects/fade-out t)
         [to]   (effects/fade-in  t)))

(defn start-slider
  "Start an indefinite slideshow of children of div 'id' that have the 'pane'
  class. If div 'id' also has children of the button class the first two act
  as backward and forward buttons in that order. There is a period of 'pause'
  milliseconds between slides and each transition lasts 'trans-time'.
  'trans-time' is part of the 'pause', and not additional. 'pane's should have
  absolute position and fill 100% of the parent div 'id'. 'transition' should
  be a function taking from and to selectors followed by 'trans-time'.
  'default-transition' provides a pleasing fade in/out."
  [id pause trans-time transition]
  (let [pre (when id (str "#" id " "))
        ps (class-selectors (str pre ".pane"))
        bs (class-selectors (str pre ".button"))
        c (chan)
        btn-click (fn [x] (events/listen :click #(go (>! c x))))]
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (btn-click (dec (count ps)))
             [(second bs)] (btn-click 1)))
    (when (pos? pause)
      (go (while true (<! (timeout pause)) (>! c 1))))
    (go (loop [is (cycle ps)]
          (let [js (drop (<! c) is)]
            (transition (first is) (first js) trans-time)
            (recur js))))))

(set! (.-onload js/window) #(start-slider "example" 4000 500 default-transition))

