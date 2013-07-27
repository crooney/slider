(ns example.css
  (:require [garden.core :as g :refer [css]]
            [garden.units :as u :refer [px pt pc]]
            [garden.color :as c :refer [rgb mix lighten darken]]))

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
                      :width :100%}]])

(def octo [:#octo [:.btn {:color :#555 :background-color :#FFF :line-height side
                         :width :20%}
                  [:&:hover {:opacity :.6}]]])

(def topper [:#topper [:.btn {:width :100% :height :10% :line-height :55px
                              :left :0%}]])

(defn -main [& args]
  (let [of (if (seq? args) (first args) "resources/public/css/example.css")]
    (spit of (css {:output-style :expanded} slider octo topper))))
