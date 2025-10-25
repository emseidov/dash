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

(defn add-widget-to-tree [tree id parent-id new-widget]
  (mapv
   (fn [node]
     (if (= (:id node) parent-id)
       (update node :children (fnil conj []) new-widget)
       (if (:children node)
         (assoc node :children (add-widget-to-tree (:children node) id parent-id new-widget))
         node)))
   tree))

;; (reg-event-db
;;  :add-widget
;;  (fn [db [_ widget]]
;;    (update db :widgets conj widget)))

(reg-event-db
 :add-widget
 (fn [db [_ widget-name id parent-id]]
   (let [new-widget {:id id
                     :parent-id parent-id
                     :name widget-name}]
     (if parent-id
       (update db :widgets add-widget-to-tree id parent-id new-widget)
       (update db :widgets conj new-widget)))))

(reg-event-db
 :show-config-modal
 (fn [db _]
   (assoc db :show-config-modal? true)))

(reg-event-db
 :hide-config-modal
 (fn [db _]
   (assoc db :show-config-modal? false)))
