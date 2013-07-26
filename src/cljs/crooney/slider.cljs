(ns crooney.slider
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects]
            [cljs.core.async :as async :refer [timeout <! >! chan]])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]
                   [enfocus.macros :as em]))

;; TODO: fix docstrings

;; diagnostic printing for debugging.  Don't judge me.
(defn- log [& more] (.log js/console (apply pr-str more)))

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

(defn- button-clicks [id c]
  (let [bs (class-selectors id ".btn")
        click (fn [x] (events/listen :click #(go (>! c x))))]
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (click :next)
             [(second bs)] (click :prev)))))

(defn- control [kw nav]
  (case kw
    :freeze (chan)
    :thaw   (do (go (>! nav 0)) nav)
    (:next :prev) (do (let [c (chan)]
                    (go (>! c (if (= :next kw) 1 -1)))
                    c))
    nav))

(defn ^:export start
  "Start an indefinite slideshow of children of div 'id' that have the 'pane'
  class. If div 'id' also has children of the btn class the first two act
  as forward and backward buttons in that order. There is a period of 'pause'
  milliseconds between slides and each transition lasts 'trans-time'.
  'trans-time' is part of the 'pause', and not additional. 'pane's should have
  absolute position and fill 100% of the parent div 'id'. 'transition' should
  be a function taking from and to selectors followed by 'trans-time' and a
  boolean indicating a forward or backward move. The default transition is a
  simultaneous fade in/out."
  ([id pause trans-time trans]
  (let [ps (class-selectors id ".pane")
        ctrl (chan)]
    (trans nil (first ps) 0 nil)
    (button-clicks id ctrl)
    (go (>! ctrl :thaw))
    (go (loop [is (cycle ps) nav (chan)]
          (let [[v c] (alts! [nav ctrl])]
            (if (= c ctrl)
              (->> (control v nav) (recur is))
              (let [js (drop (+ v (count ps)) is)]
                (when-not (zero? v)
                  (trans (first is) (first js) trans-time
                         (when (neg? v) :back)))
                (go (<! (timeout pause)) (>! nav 1))
                (recur js nav))))))
    ctrl))
  ([id pause trans-time] (start id pause trans-time default-transition)))

(def ^:private transitions {:default default-transition
                            :css class-transition
                            :fade default-transition})

(defn- extract-times [cc]
  (ef/from js/document
           :id [cc] (ef/get-prop :id)
           :pause [cc] (ef/get-attr :data-pause)
           :transition [cc] (ef/get-attr :data-transition)
           :trans-time [cc] (ef/get-attr :data-trans-time)))

(defn ^:export start-all
  "Start all sliders on page. Sliders have class 'slider' and should have
  the attributes 'data-pause' and 'data-trans-time' set to integer values,
  which will be passed to start. If 'data-transition' is not set then
  'default-transition' is used. 'data-transition' may be 'default' or 'css'."
  []
  (let [f #(if (string? %) [%] %)
        g #(concat (map %2 (f %1)) (repeat %3))
        {i :id d :pause tt :trans-time t :transition} (extract-times ".slider")]
    (dorun (map start
                (f i)
                (g d int nil)
                (g tt int nil)
                (g t #((keyword %) transitions) default-transition)))))
