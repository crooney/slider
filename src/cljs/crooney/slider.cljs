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
  [from to t _]
  (ef/at js/document
         [from] (effects/fade-out t)
         [to]   (effects/fade-in  t)))

(def ^:private css-classes ["sliderForwardTo" "sliderForwardFrom"
                            "sliderBackwardTo" "sliderBackwardFrom"])
(defn- class-transition
  "Applies a class of 'sliderForwardTo' to the 'to' arg, and 'sliderForwardFrom'
  to the 'from' arg, unless 'back' is truthy, in which case s/For/Back/g."
  [from to _ back]
  (let [[f t] (if back ["sliderBackwardFrom" "sliderBackwardTo"]
                ["sliderForwardFrom" "sliderForwardTo"])]
    (ef/at js/document
           [from] (ef/do-> (apply ef/remove-class css-classes)
                           (ef/add-class f))
           [to]   (ef/do-> (apply ef/remove-class css-classes)
                           (ef/add-class t)))))

(defn ^:export start
  "Start an indefinite slideshow of children of div 'id' that have the 'pane'
  class. If div 'id' also has children of the button class the first two act
  as backward and forward buttons in that order. There is a period of 'delay'
  milliseconds between slides and each transition lasts 'trans-time'.
  'trans-time' is part of the 'delay', and not additional. 'pane's should have
  absolute position and fill 100% of the parent div 'id'. 'transition' should
  be a function taking from and to selectors followed by 'trans-time' and a
  boolean indicating a forward or backward move. The default transition is a
  simultaneous fade in/out."
  ([id delay trans-time transition]
  (let [ps (class-selectors id ".pane")
        bs (class-selectors id ".button")
        c (chan)
        btn-click (fn [x] (events/listen :click #(go (>! c x))))]
    #_(js/alert [id ps bs])
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (btn-click -1)
             [(second bs)] (btn-click 1)))
    (when (pos? delay)
      (go (while true (<! (timeout delay)) (>! c 1))))
    (go (loop [is (cycle ps)]
          (let [x (<! c)
                js (drop (+ (count ps) x) is)]
            (transition (first is) (first js) trans-time (when (neg? x) :back))
            (recur js))))
    c))
  ([id delay trans-time] (start id delay trans-time default-transition)))

(def ^:private transitions {:default default-transition
                            :css class-transition
                            :fade default-transition})

(defn- extract-times [cc]
  (ef/from js/document
           :id [cc] (ef/get-prop :id)
           :delay [cc] (ef/get-attr :data-delay)
           :transition [cc] (ef/get-attr :data-transition)
           :trans-time [cc] (ef/get-attr :data-trans-time)))

(defn ^:export start-all
  "Start all sliders on page. Sliders have class 'slider' and should have
  the attributes 'data-delay' and 'data-trans-time' set to integer values,
  which will be passed to start. If 'data-transition' is not set then
  'default-transition' is used. 'data-transition' may be 'default' or 'css'."
  []
  (let [ss (extract-times ".slider")]
    (dorun (map start
                (:id ss)
                (concat (map int (:delay ss)) (repeat nil))
                (concat (map int (:trans-time ss)) (repeat nil))
                (concat (map #((keyword %) transitions) (:transition ss))
                        (repeat default-transition))))))
