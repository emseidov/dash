(ns dash.events
  (:require
   [re-frame.core :as rf]
   [ajax.core :as ajax]
   [dash.db :as db]
   [dash.utils :as u]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-db
 :toggle-edit-mode
 (fn [db _]
   (update db :edit-mode? not)))

(rf/reg-event-db
 :set-show-widget-modal
 (fn [db [_ value]]
   (assoc db :show-widget-modal? value)))

(rf/reg-event-db
 :add-widget
 (fn [db [_ widget-name parent-id]]
   (let [widget {:id (str (name widget-name) " (" (random-uuid) ")")
                 :parent-id parent-id
                 :name widget-name
                 :children []}]
     (update db :widget-tree u/add-widget widget))))

(rf/reg-event-db
 :set-show-action-modal
 (fn [db [_ value]]
   (assoc db :show-action-modal? value)))

(rf/reg-event-db
 :set-current-parent-id
 (fn [db [_ id]]
   (assoc db :current-parent-id id)))

(rf/reg-event-db
 :reg-event
 (fn [db [_ {:keys [widget-id key] :as event}]]
   (update-in db [:events-and-handlers widget-id :events]
              (fnil assoc {}) key event)))

(rf/reg-event-db
 :reg-handler
 (fn [db [_ {:keys [widget-id key] :as handler}]]
   (update-in db [:events-and-handlers widget-id :handlers]
              (fnil assoc {}) key handler)))

(rf/reg-event-db
 :save-actions
 (fn [db [_ actions]]
   (assoc db :actions actions)))

(rf/reg-event-fx
 :fetch-api-data
 (fn [_ [_ uri args widget-id]]
   (prn "ARGSSS" (vec (vals args)))
   (let [formatted-uri (u/fill-args uri (vec (vals args)))]
     (prn ":fech-api-data" formatted-uri uri args)
     {:http-xhrio {:method :get
                   :uri formatted-uri
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:fetch-api-data-success widget-id]
                   :on-failure [:fetch-api-data-failure]}})))

(rf/reg-event-db
 :fetch-api-data-success
 (fn [db [_ widget-id data]]
   (assoc-in db [:api-data widget-id] data)))

(rf/reg-event-db
 :fetch-api-data-failure
 (fn [_ [_ error]]
   (println "error: " error)))

(rf/reg-event-db
 :set-context-menu
 (fn [db [_ value]]
   (assoc db :context-menu value)))

(rf/reg-event-db
 :set-show-context-menu
 (fn [db [_ value]]
   (assoc db :show-context-menu? value)))

(rf/reg-event-db
 :set-show-settings-modal
 (fn [db [_ value]]
   (assoc db :show-settings-modal? value)))

(rf/reg-event-db
 :set-settings
 (fn [db [_ key value]]
   (assoc-in db [:settings key] value)))

(rf/reg-event-db
 :set-api-settings
 (fn [db [_ id value]]
   (assoc-in db [:settings :api id] value)))

(rf/reg-event-db
 :set-data-args
 (fn [db [_ id setter-id args]]
   (assoc-in db [:data-args id setter-id] args)))
