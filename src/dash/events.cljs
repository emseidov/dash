(ns dash.events
  (:require
   [re-frame.core :as re-frame]
   [ajax.core :as ajax]
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
 (fn [db [_ name parent-id]]
   (let [widget {:id (random-uuid)
                 :parent-id parent-id
                 :name name
                 :children []}]
     (update db :widgets utils/add-widget widget))))

(re-frame/reg-event-db
 :set-show-actions-modal
 (fn [db [_ value]]
   (assoc db :show-actions-modal? value)))

(re-frame/reg-event-db
 :select-widget
 (fn [db [_ selected-widget]]
   (assoc db :selected-widget selected-widget)))

(re-frame/reg-event-db
 :set-current-container-id
 (fn [db [_ id]]
   (assoc db :current-container-id id)))

(re-frame/reg-event-db
 :reg-event
 (fn [db [_ {:keys [widget-id key] :as event}]]
   (update-in db [:events-and-handlers widget-id :events]
              (fn [x]
                (println x)
                (assoc (or x {}) key event)))))

(re-frame/reg-event-db
 :reg-handler
 (fn [db [_ {:keys [widget-id key] :as handler}]]
   (update-in db [:events-and-handlers widget-id :handlers]
              (fn [x]
                (println x)
                (assoc (or x {}) key handler)))))

(re-frame/reg-event-db
 :add-action
 (fn [db [_ action]]
   (update db :actions #(conj % action))))

(re-frame/reg-event-fx
 :fetch-api-data
 (fn [_ [_ uri key]]
   {:http-xhrio {:method          :get
                 :uri             uri
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:fetch-api-data-success key]}}))

(re-frame/reg-event-db
 :fetch-api-data-success
 (fn [db [_ key data]]
   (update db :api-data #(assoc % key data))))

(re-frame/reg-event-db
 :set-context-menu
 (fn [db [_ value]]
   (assoc db :context-menu value)))

(re-frame/reg-event-db
 :set-show-context-menu
 (fn [db [_ value]]
   (assoc db :show-context-menu? value)))

(re-frame/reg-event-db
 :set-show-settings-modal
 (fn [db [_ value]]
   (assoc db :show-settings-modal? value)))

(re-frame/reg-event-db
 :set-settings
 (fn [db [_ key value]]
   (println key value)
   (update db :settings #(assoc % key value))))

(re-frame/reg-event-db
 :set-data-args
 (fn [db [_ id args]]
   (println "set-data-args" id args)
   (update-in db [:data-args id] #(conj % args))))
