(ns crooney.slider
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects]
            [cljs.core.async :as async :refer [timeout <! >! chan]])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]
                   [enfocus.macros :as em]))

(defn- class-selectors
  "Extract all ids that match selector x and make them id selectors: e.g. #foo."
  [id cc]
  (let [pre (when id (str "#" id " "))]
    (->> (ef/get-prop :id)
         (ef/from js/document :workaround [(str pre cc)])
         (:workaround)
         (remove nil?)
         (map (partial str pre "#")))))

(defn- default-transition
  "Simple simultaneous fade in/out lasting t milliseconds."
  [from to t]
  (ef/at js/document
         [from] (effects/fade-out t)
         [to]   (effects/fade-in  t)))

(defn ^:export start
  "Start an indefinite slideshow of children of div 'id' that have the 'pane'
  class. If div 'id' also has children of the button class the first two act
  as backward and forward buttons in that order. There is a period of 'delay'
  milliseconds between slides and each transition lasts 'trans-time'.
  'trans-time' is part of the 'delay', and not additional. 'pane's should have
  absolute position and fill 100% of the parent div 'id'. 'transition' should
  be a function taking from and to selectors followed by 'trans-time'.
  The default transition is a simultaneous fade in/out."
  ([id delay trans-time transition]
  (let [ps (class-selectors id ".pane")
        bs (class-selectors id ".button")
        c (chan)
        btn-click (fn [x] (events/listen :click #(go (>! c x))))]
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (btn-click (dec (count ps)))
             [(second bs)] (btn-click 1)))
    (when (pos? delay)
      (go (while true (<! (timeout delay)) (>! c 1))))
    (go (loop [is (cycle ps)]
          (let [js (drop (<! c) is)]
            (transition (first is) (first js) trans-time)
            (recur js))))))
  ([id delay trans-time] (start id delay trans-time default-transition)))

(defn- extract-times [cc]
  (ef/from js/document
           :id [cc] (ef/get-prop :id)
           :delay [cc] (ef/get-attr :data-delay)
           :trans-time [cc] (ef/get-attr :data-trans-time)))

(defn ^:export start-all
  "Start all sliders on page. Sliders have class 'slider' and should have
  the attributes 'data-delay' and 'data-trans-time' set to integer values,
  which will be passed to start. 'default-transition' is used."
  []
  (let [ss (extract-times ".slider")]
    (dorun (map start
                (:id ss)
                (map int (:delay ss))
                (map int (:trans-time ss))))))
