(ns schedule.model
  (:require [cljs.reader :as reader]
            [re-frame.core :as rf]
            [tick.core :as t]))

;; Helpers

(defn today-date? [date]
  (t/=
   (t/date (t/now))
   (t/date date)))

(defn tomorrow-date? [date]
  (t/=
   (t/>> (t/date (t/now)) (t/new-period 1 :days))
   (t/date date)))

(defn next-week-date? [date]
  (let [date-now (t/date (t/now))
        date-now+7-days (t/>> date-now (t/new-period 7 :days))]
    (t/<= date-now (t/date date) date-now+7-days)))

(def period-mapping
  {:today today-date?
   :tomorrow tomorrow-date?
   :next-week next-week-date?})

;; Subs

(rf/reg-sub
 ::text-area-value
 (fn [db _]
   (get-in db [:text-area :value])))

(rf/reg-sub
 ::filters
 (fn [db _]
   (get-in db [:filter])))

(rf/reg-sub
 ::selected-user?
 (fn [db [_ uid]]
   (contains? (get-in db [:filter :user-id]) uid)))

(rf/reg-sub
 ::all-users-selected?
 (fn [db _]
   (empty? (get-in db [:filter :user-id]))))

(rf/reg-sub
 ::selected-period?
 (fn [db [_ period]]
   (= period (get-in db [:filter :period]))))

(rf/reg-sub
 ::selected-period
 (fn [db _]
   (get-in db [:filter :period])))

;; TODO Get rid of tuple results
(rf/reg-sub
 ::text-area-edn-value
 :<-[::text-area-value]
 (fn [text _]
   (try
     (let [res (reader/read-string text)]
       [res nil])
     (catch js/Error e
       (js/console.log e)
       [nil (.-message e)]))))

(rf/reg-sub
 ::text-area-filtered-edn-value
 :<-[::text-area-edn-value]
 :<-[::filters]
 (fn [[[schedule err] filters] _]
   (if err
     [nil err]
     (let [period (:period filters)
           period-match? (get period-mapping period identity)
           user-id-param (:user-id filters)
           user-id-match? (if (seq user-id-param)
                            user-id-param
                            any?)
           filtered (filter (fn [{:keys [user-id start end]}]
                              (and (user-id-match? user-id)
                                   (or (period-match? start)
                                       (period-match? end)))) schedule)]
       [filtered nil]))))

(rf/reg-sub
 ::user-ids
 :<- [::text-area-edn-value]
 (fn [[records _] _]
   (->> records
        (map :user-id)
        (distinct))))

;; Events

(rf/reg-event-fx
 ::set-text-area-value
 (fn [{db :db} [_ value]]
   {:db (assoc-in db [:text-area :value] value)}))

(rf/reg-event-fx
 ::set-user-filter
 (fn [{db :db} [_ uid]]
   (let [user-ids (get-in db [:filter :user-id])]
     (if (contains? user-ids uid)
       {:db (update-in db [:filter :user-id] (fnil disj #{}) uid)}
       {:db (update-in db [:filter :user-id] (fnil conj #{}) uid)}))))

(rf/reg-event-fx
 ::set-date-filter
 (fn [{db :db} [_ period]]
   {:db (assoc-in db [:filter :period] period)}))

(rf/reg-event-fx
 ::reset-user-filter
 (fn [{db :db} _]
   {:db (assoc-in db [:filter :user-id] #{})}))


