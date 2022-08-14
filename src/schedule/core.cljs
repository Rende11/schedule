(ns schedule.core
  (:require [oz.core :as oz]
            [cljs.pprint :as pprint]
            [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [schedule.model :as sm]
            [tick.core :as t]))


(defn chart []
  (let [sample-data @(rf/subscribe [::sm/text-area-filtered-edn-value])
        [_ err] @(rf/subscribe [::sm/text-area-edn-value])
        selected-period @(rf/subscribe [::sm/selected-period])
        timeunit (if (#{:next-week} selected-period)
                   "dayhoursminutes"
                   "hoursminutes")]
    [:div.ml-5
     (if err
       [:div.text-center.mt-12 "Broken data..."]
       [oz/vega-lite
        {:data {:values (or sample-data [])}
         :width "750"
         :height "650"
         :resize true
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
                        :title "User"}}}])]))

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
       [:div.text-red-700 err])]))

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


(defn gen-interval [user-id from to amount]
  (for [n (range 0 amount)
        :let [now (t/date (t/now))
              shift-period (t/new-period n :days)
              date-from (t/>> (t/at now (t/time from)) shift-period)
              date-to (t/>> (t/at now (t/time to)) shift-period)]]
    {:user-id user-id :start (js/Date. date-from) :end (js/Date. date-to)}))

(comment
  (js/Date. (t/instant (t/zoned-date-time (t/at (t/date (t/now)) (t/time "08:00")))))
  (js/Date. (t/inst (t/at (t/date (t/now)) (t/time "08:00"))))
  (js/Date. (t/at (t/date (t/now)) (t/time "08:00")))
  (t/inst)
  )

(rf/reg-event-fx
 ::initialise
 (fn [_ _]
   {:db {:filter {:user-id #{}
                  :period :today}
         :text-area
         {:value
          (with-out-str
            (pprint/pprint
             (concat
              (gen-interval "Alex" "08:00" "12:00" 10)
              (gen-interval "Alex" "13:00" "14:00" 10)
              (gen-interval "Alex" "16:00" "18:00" 5)

              (gen-interval "Max" "09:00" "11:00" 10)
              (gen-interval "Max" "15:00" "18:00" 10)
              (gen-interval "Max" "12:00" "13:00" 1)

              (gen-interval "Ann" "09:00" "12:30" 5)
              (gen-interval "Ann" "14:00" "16:00" 5)
              (gen-interval "Ann" "18:00" "20:00" 5)
              (gen-interval "Ann" "21:00" "22:00" 2)

              (gen-interval "Nataly" "07:00" "13:00" 7)
              (gen-interval "Nataly" "14:00" "19:00" 7)

              (gen-interval "Fernando" "08:00" "09:00" 7)
              (gen-interval "Fernando" "10:00" "12:00" 7)
              (gen-interval "Fernando" "13:00" "15:00" 7)
              (gen-interval "Fernando" "16:00" "17:30" 7)
              (gen-interval "Fernando" "18:00" "19:00" 7))))}}}))


(defn ^:export init
  []
  (rf/dispatch-sync [::initialise])
  (render))

(comment
  (t/now)
  [{:start "2022-08-12T10:00:00.000Z" :end "2022-08-12T14:00:00.000Z" :user-id "Max"}]

  
  )
