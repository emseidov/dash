(ns dash.events
  (:require
   [re-frame.core :as re-frame]
   [dash.db :as db]
   [dash.utils :as utils]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :toggle-edit-mode
 (fn [db _]
   (update db :edit-mode? not)))

(re-frame/reg-event-db
 :set-show-widget-modal
 (fn [db [_ value]]
   (assoc db :show-widget-modal? value)))

(re-frame/reg-event-db
 :add-widget
 (fn [db [_ name id parent-id]]
   (let [widget {:id id
                 :parent-id parent-id
                 :name name
                 :children []}]
     (update db :widgets utils/add-widget widget))))

(re-frame/reg-event-db
 :set-show-config-modal
 (fn [db [_ value]]
   (assoc db :show-config-modal? value)))

(re-frame/reg-event-db
 :select-widget
 (fn [db [_ selected-widget]]
   (assoc db :selected-widget selected-widget)))

(re-frame/reg-event-db
 :set-current-container-id
 (fn [db [_ id]]
   (assoc db :current-container-id id)))
