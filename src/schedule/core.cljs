(ns schedule.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [oz.core :as oz]))

(def sample-data
  [{:power 1 :speed 2 :engine :v8}
   {:power 3 :speed 4 :engine :v6}
   {:power 8 :speed 7 :engine :v8}
   {:power 4 :speed 3.5 :engine :v8}
   {:power 5 :speed 4 :engine :v8}
   {:power 1.2 :speed 3 :engine :v6}
   {:power 3.2 :speed 3.8 :engine :v6}
   {:power 7.5 :speed 7.2 :engine :v6}])

(defn simple-vega-lite-example []
  [oz/vega-lite
   {:data {:values sample-data}
    :mark {:type :point
           :tooltip true}
    :width 500
    :height 400
    :encoding {:x {:field :power}
               :y {:field :speed}
               :color {:field :engine}
               :size {:value 80}}}])


(defn app []
  [:div
   [simple-vega-lite-example]])


(defn ^:dev/after-load render []
  (rdom/render [app] (.getElementById js/document "root")))


(defn ^:export init
  []
  (render))
