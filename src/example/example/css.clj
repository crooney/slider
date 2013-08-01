(ns example.css
  (:require [garden.core :as g :refer [css]]
            [garden.util :as u]
            [garden.compiler :refer [compile-css]]))

(defn prefix [ps]
  (fn [x y]
    [:& (into {} (map (fn [z] [(str z (name x)) y]) ps))]))

(def mwk (prefix [nil "-moz-" "-webkit-"]))

(def side :424px)

(def slider [:.slider {:position :relative :margin [ :0 :auto]
                       :overflow :hidden :height side :width side}
             [:.btn {:color :#fff :background-color :#000 :height :100%
                       :width :10% :opacity :0 :position :absolute
                       :text-align :center :top :0px}
              (mwk :user-select :none)
              [:&:hover {:opacity :.5}]]
             [:.pane {:position :absolute :top :0 :left :0 :height :100%
                      :width :100%}]
             [:.sliderInactive {:opacity 0}]
             [:.sliderActive {:opacity 1}]])

(def octo [:#octo [:.btn {:color :#555 :background-color :#FFF :line-height side
                         :width :20%}
                  [:&:hover {:opacity :.6}]]])

(def topper [:#topper [:.btn {:width :100% :height :10% :line-height :55px
                              :left :0%}]])

(def anim-len :0.8s)

(def animation [[:.sliderForwardFrom {:opacity 0}
                 (mwk :animation [:toLeft anim-len])]
                [:.sliderForwardTo {:opacity 1}
                 (mwk :animation [:fromRight anim-len])]
                [:.sliderBackwardFrom {:opacity 0}
                 (mwk :animation [:toRight anim-len])]
                [:.sliderBackwardTo {:opacity 1}
                 (mwk :animation [:fromLeft anim-len])]])

;; I've got a pull request in to get this into garden
(defn at-keyframes
  "Create CSS at-rule(s) for `anim-name`."
  ([at-names anim-name rules]
    (let [render #(str "@" (u/to-str %) " " (u/to-str anim-name) " "
                       (u/left-brace) (compile-css rules) (u/right-brace))]
    (reduce str (map render at-names))))
  ([anim-name rules] (at-keyframes ["keyframes"] anim-name rules)))

(def mwk-keyframes (partial at-keyframes ["keyframes" "-moz-keyframes"
                                          "-webkit-keyframes"]))

(def keyframes
  (str
    (mwk-keyframes :fromRight [[:0% {:height :30% :left :90% :top :70%
                                     :opacity 0 :width :30%}]
                               [:100% {:height :100% :left :0% :top :0%
                                       :opacity 1 :width :100%}]])
    (mwk-keyframes :toRight [[:0% {:height :100% :left :0% :top :0% :opacity 1
                                   :width :100%}]
                             [:100% {:height :30% :left :120% :top :70%
                                     :opacity 0 :width :30%}]])
    (mwk-keyframes :fromLeft [[:0% {:height :30% :left :-90% :top :70%
                                    :opacity 0 :width :30%}]
                              [:100% {:height :100% :left :0% :top :0%
                                      :opacity 1 :width :100%}]])
    (mwk-keyframes :toLeft [[:0% {:height :100% :left :0% :top :0% :opacity 1
                                  :width :100%}]
                            [:100% {:height :30% :left :-20% :top :70%
                                    :opacity 0 :width :30%}]])))

(defn -main [& args]
  (let [of (if (seq? args) (first args) "resources/public/css/example.css")]
    (spit of (css {:output-style :expanded}
                  slider octo topper animation keyframes))))
