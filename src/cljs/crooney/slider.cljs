(ns crooney.slider
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [enfocus.effects :as effects]
            [cljs.core.async :as async :refer [timeout <! >! chan]])
  (:require-macros [cljs.core.async.macros :as am :refer [go]]
                   [enfocus.macros :as em]))

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

(def ^:private css-classes ["sliderActive" "sliderInactive" "sliderForwardTo"
                            "sliderForwardFrom" "sliderBackwardTo"
                            "sliderBackwardFrom"])
(defn- class-transition
  "Applies a class of 'sliderForwardTo' to the 'to' arg, and 'sliderForwardFrom'
  to the 'from' arg, unless 'back' is truthy, in which case s/For/Back/g."
  [from to _ back]
  (let [[f t] (if back ["sliderBackwardFrom" "sliderBackwardTo"]
                ["sliderForwardFrom" "sliderForwardTo"])]
    (ef/at js/document
           [from] (ef/do-> (apply ef/remove-class css-classes)
                           (ef/add-class f "sliderInactive"))
           [to]   (ef/do-> (apply ef/remove-class css-classes)
                           (ef/add-class t "sliderActive")))))

(defn- button-clicks
  "Assign the transmission of ':next' and ':prev' to forward and back buttons."
  [id c]
  (let [bs (class-selectors id ".btn")
        click (fn [x] (events/listen :click #(go (>! c x))))]
    (when (> (count bs) 1)
      (ef/at js/document
             [(first bs)] (click :next)
             [(second bs)] (click :prev)))))

(defn- control
  "Handle messages received on control channel, as returned by 'start'."
  [kw nav]
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
  simultaneous fade in/out.
  Returns a core.async channel that understands :freeze :thaw :next and :prev
  with the obvious meanings."
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

(defn- operate
  "Apply an enfocus func to a selector for all 'xs' in document. Returns result
  of 'eff' (enfocus func)."
  [eff sel f xs]
   (apply eff js/document
         (keep identity
               (mapcat (fn [x] [(when (keyword? x) x) [sel] (f x)]) xs))))

(def extract ^{:doc "'operate' on enfocus/from"} (partial operate ef/from))
(def infect ^{:doc "'operate' on enfocus/at"} (partial operate ef/at))

(def ^:private transitions {:default default-transition
                            :css class-transition
                            :fade default-transition})

(defn ^:export start-all
  "Start all sliders on page. Sliders have class 'slider' and should have
  the attributes 'data-pause' and 'data-trans-time' set to integer values,
  which will be passed to start. If 'data-transition' is not set then
  'default-transition' is used. 'data-transition' may be 'default' or 'css'.
  See 'start', 'default-transition' and 'class-transition'."
  []
  (let [f #(if (string? %) [%] %)
        g #(concat (map %2 (f %1)) (repeat %3))
        as (extract ".slider" ef/get-attr
                 [:id :data-pause :data-transition :data-trans-time])]
    (infect ".pane" ef/add-class ["sliderInactive"])
    (dorun (map start
                (f (:id as))
                (g (:data-pause as) int nil)
                (g (:data-trans-time as) int nil)
                (g (:data-transition as) #((keyword %) transitions) default-transition)))))
