(ns schedule.core
  (:require [cljs.reader :as reader]
            [cljs.tools.reader.edn :as edn]
            [oz.core :as oz]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [schedule.model :as sm]
            [tick.core :as t]))


(defn chart []
  (let [[sample-data _] @(rf/subscribe [::sm/text-area-filtered-edn-value])
        selected-period @(rf/subscribe [::sm/selected-period])
        timeunit (if (#{:next-week} selected-period)
                    "utc dayhoursminutes"
                    "utc hoursminutes")]
    [:div
     [oz/vega-lite
      {:data {:values (or sample-data [])}
       :width 600
       :height 500
       :mark {:type "bar"
              :color "#5046e4"
              ;; Tooltip works badly
              :tooltip true}
       :encoding {:x {:field :start
                      :timeUnit timeunit
                      :type :temporal
                      :title "From"}
                  :x2 {:field :end
                       :timeUnit timeunit 
                       :type :temporal
                       :title "To"}
                  :y {:field :user-id
                      :title "User"}}}]]))

(defn input-block
  []
  (let [[_ err] @(rf/subscribe [::sm/text-area-edn-value])]
    [:div.m-5 {:class "min-w-[30%]"}
     [:textarea.border-solid.border-2.border-indigo-600.rounded.p-2.w-full
      {:style {"height" "90vh"}
       :on-change (fn [e]
                    (let [value (-> e .-target .-value )]
                      (rf/dispatch [::sm/set-text-area-value value])))
       :value @(rf/subscribe [::sm/text-area-value])}]
     (when err
       [:div err])]))

(defn user-label-all []
  (let [all-users-selected? @(rf/subscribe [::sm/all-users-selected?])]
    [:button.p-1.my-1.border-solid.border-2.border-indigo-600.rounded
     {:class (when all-users-selected?
               "bg-indigo-600 text-white")
      :on-click #(rf/dispatch [::sm/reset-user-filter])}
     "All"]))

(defn user-label [uid]
  (let [selected-user? @(rf/subscribe [::sm/selected-user? uid])]
    [:button.p-1.my-1.border-solid.border-2.border-indigo-600.rounded
     {:class (when selected-user?
               "bg-indigo-600 text-white")
      :on-click #(rf/dispatch [::sm/set-user-filter uid])}
     uid]))

(defn select-user []
  (let [user-ids @(rf/subscribe [::sm/user-ids])]
    [:div.flex.flex-col
     [user-label-all]
     (for [uid user-ids]
       ^{:key uid}
       [user-label uid])]))

(defn date-label [{:keys [label value]}]
  (let [selected-period? @(rf/subscribe [::sm/selected-period? value])]
    [:button.px-3.py-1.my-1.mx-3.border-solid.border-2.border-indigo-600.rounded
     {:class (when selected-period?
               "bg-indigo-600 text-white")
      :on-click #(rf/dispatch [::sm/set-date-filter value])}
     label]))

(defn select-date []
  [:div.ml-8.flex
   [date-label {:label "Today"
                :value :today}]
   [date-label {:label "Tomorrow"
                :value :tomorrow}]
   [date-label {:label "Next week"
                :value :next-week}]])

(defn chart-bar []
  [:div.m-5.flex
   [select-user]
   [:div
    [select-date]
    [chart]]])

(defn app []
  []
  [:div
   [:div.flex
    [input-block]
    [chart-bar]]])

(defn ^:dev/after-load render []
  (rdom/render [app] (.getElementById js/document "root")))

(rf/reg-event-fx
 ::initialise
 (fn [_ _]
   {:db {:filter {:user-id #{}
                   :period :today}
         :text-area
         {:value
          (str [{:start #inst "2022-08-14T10:00:00.000Z" :end #inst "2022-08-14T14:00:00.000Z" :user-id "Max"}
                {:start #inst "2022-08-14T16:00:00.000Z" :end #inst "2022-08-14T18:00:00.000Z" :user-id "Max"}
                {:start #inst "2022-08-15T12:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "Max"}
                {:start #inst "2022-08-17T10:00:00.000Z" :end #inst "2022-08-17T18:00:00.000Z" :user-id "Max"}
                {:start #inst "2022-08-18T10:00:00.000Z" :end #inst "2022-08-18T18:00:00.000Z" :user-id "Max"}

                {:start #inst "2022-08-14T16:30:00.000Z" :end #inst "2022-08-14T17:30:00.000Z" :user-id "Oleg"}
                {:start #inst "2022-08-15T16:30:00.000Z" :end #inst "2022-08-15T17:30:00.000Z" :user-id "Oleg"}
                {:start #inst "2022-08-16T16:30:00.000Z" :end #inst "2022-08-16T17:30:00.000Z" :user-id "Oleg"}
                
                {:start #inst "2022-08-14T08:00:00.000Z" :end #inst "2022-08-14T10:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-08-14T11:00:00.000Z" :end #inst "2022-08-14T13:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-08-15T14:00:00.000Z" :end #inst "2022-08-15T15:45:00.000Z" :user-id "Ann"}
                {:start #inst "2022-08-15T16:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-08-15T19:00:00.000Z" :end #inst "2022-08-15T20:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-09-16T10:30:00.000Z" :end #inst "2022-09-16T20:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-09-17T10:30:00.000Z" :end #inst "2022-09-17T20:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-09-18T10:30:00.000Z" :end #inst "2022-09-18T20:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-09-19T10:30:00.000Z" :end #inst "2022-09-19T20:00:00.000Z" :user-id "Ann"}
                {:start #inst "2022-09-20T10:30:00.000Z" :end #inst "2022-09-20T20:00:00.000Z" :user-id "Ann"}

                {:start #inst "2022-08-14T10:00:00.000Z" :end #inst "2022-08-14T14:00:00.000Z" :user-id "MadMax"}
                {:start #inst "2022-08-14T16:00:00.000Z" :end #inst "2022-08-14T18:00:00.000Z" :user-id "MadMax"}
                {:start #inst "2022-08-15T12:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "MadMax"}
                {:start #inst "2022-08-17T10:00:00.000Z" :end #inst "2022-08-17T18:00:00.000Z" :user-id "MadMax"}
                {:start #inst "2022-08-18T10:00:00.000Z" :end #inst "2022-08-18T18:00:00.000Z" :user-id "MadMax"}

                {:start #inst "2022-08-14T16:30:00.000Z" :end #inst "2022-08-14T17:30:00.000Z" :user-id "Fernando"}
                {:start #inst "2022-08-15T16:30:00.000Z" :end #inst "2022-08-15T17:30:00.000Z" :user-id "Fernando"}
                {:start #inst "2022-08-16T16:30:00.000Z" :end #inst "2022-08-16T17:30:00.000Z" :user-id "Fernando"}
                
                {:start #inst "2022-08-14T08:00:00.000Z" :end #inst "2022-08-14T10:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-08-14T11:00:00.000Z" :end #inst "2022-08-14T13:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-08-15T14:00:00.000Z" :end #inst "2022-08-15T15:45:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-08-15T16:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-08-15T19:00:00.000Z" :end #inst "2022-08-15T20:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-09-16T10:30:00.000Z" :end #inst "2022-09-16T20:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-09-17T10:30:00.000Z" :end #inst "2022-09-17T20:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-09-18T10:30:00.000Z" :end #inst "2022-09-18T20:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-09-19T10:30:00.000Z" :end #inst "2022-09-19T20:00:00.000Z" :user-id "Nataly"}
                {:start #inst "2022-09-20T10:30:00.000Z" :end #inst "2022-09-20T20:00:00.000Z" :user-id "Nataly"}

                {:start #inst "2022-08-14T10:00:00.000Z" :end #inst "2022-08-14T14:00:00.000Z" :user-id "Bruce"}
                {:start #inst "2022-08-14T16:00:00.000Z" :end #inst "2022-08-14T18:00:00.000Z" :user-id "Bruce"}
                {:start #inst "2022-08-15T12:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "Bruce"}
                {:start #inst "2022-08-17T10:00:00.000Z" :end #inst "2022-08-17T18:00:00.000Z" :user-id "Bruce"}
                {:start #inst "2022-08-18T10:00:00.000Z" :end #inst "2022-08-18T18:00:00.000Z" :user-id "Bruce"}

                {:start #inst "2022-08-14T16:30:00.000Z" :end #inst "2022-08-14T17:30:00.000Z" :user-id "Luke"}
                {:start #inst "2022-08-15T16:30:00.000Z" :end #inst "2022-08-15T17:30:00.000Z" :user-id "Luke"}
                {:start #inst "2022-08-16T16:30:00.000Z" :end #inst "2022-08-16T17:30:00.000Z" :user-id "Luke"}
                
                {:start #inst "2022-08-14T08:00:00.000Z" :end #inst "2022-08-14T10:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-08-14T11:00:00.000Z" :end #inst "2022-08-14T13:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-08-15T14:00:00.000Z" :end #inst "2022-08-15T15:45:00.000Z" :user-id "Zena"}
                {:start #inst "2022-08-15T16:00:00.000Z" :end #inst "2022-08-15T18:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-08-15T19:00:00.000Z" :end #inst "2022-08-15T20:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-09-16T10:30:00.000Z" :end #inst "2022-09-16T20:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-09-17T10:30:00.000Z" :end #inst "2022-09-17T20:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-09-18T10:30:00.000Z" :end #inst "2022-09-18T20:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-09-19T10:30:00.000Z" :end #inst "2022-09-19T20:00:00.000Z" :user-id "Zena"}
                {:start #inst "2022-09-20T10:30:00.000Z" :end #inst "2022-09-20T20:00:00.000Z" :user-id "Zena"}])}}}))


(defn ^:export init
  []
  (rf/dispatch-sync [::initialise])
  (render))

(comment
  (t/now)
  [{:start "2022-08-12T10:00:00.000Z" :end "2022-08-12T14:00:00.000Z" :user-id "Max"}]

  
  )
