(ns dash.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 :edit-mode?
 (fn [db]
   (:edit-mode? db)))

(rf/reg-sub
 :show-widget-modal?
 (fn [db]
   (:show-widget-modal? db)))

(rf/reg-sub
 :widget-tree
 (fn [db]
   (:widget-tree db)))

(rf/reg-sub
 :show-action-modal?
 (fn [db]
   (:show-action-modal? db)))

(rf/reg-sub
 :current-parent-id
 (fn [db]
   (:current-parent-id db)))

(rf/reg-sub
 :actions
 (fn [db]
   (:actions db)))

(rf/reg-sub
 :events-and-handlers
 (fn [db]
   (:events-and-handlers db)))

(rf/reg-sub
 :api-data
 (fn [db [_ id]]
   (get-in db [:api-data id])))

(rf/reg-sub
 :context-menu
 (fn [db]
   (:context-menu db)))

(rf/reg-sub
 :show-context-menu?
 (fn [db]
   (:show-context-menu? db)))

(rf/reg-sub
 :show-settings-modal?
 (fn [db]
   (:show-settings-modal? db)))

(rf/reg-sub
 :settings
 (fn [db]
   (:settings db)))

(rf/reg-sub
 :api-settings
 (fn [db [_ id]]
   (get-in db [:settings :api id])))

(rf/reg-sub
 :data-args
 (fn [db]
   (:data-args db)))
