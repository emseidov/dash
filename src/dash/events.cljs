(ns dash.events
  (:require
   [re-frame.core :refer [reg-event-db]]
   [dash.db :as db]))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-db
 :toggle-edit-mode
 (fn [db _]
   (update db :edit-mode? not)))
