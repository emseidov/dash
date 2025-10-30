(ns dash.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :edit-mode?
 (fn [db]
   (:edit-mode? db)))

(re-frame/reg-sub
 :show-widget-modal?
 (fn [db]
   (:show-widget-modal? db)))
;; fdd
(re-frame/reg-sub
 :widgets
 (fn [db]
   (:widgets db)))
(println)
(re-frame/reg-sub
 :show-actions-modal?
 (fn [db]
   (:show-actions-modal? db)))

(re-frame/reg-sub
 :selected-widget
 (fn [db]
   (:selected-widget db)))

(re-frame/reg-sub
 :current-container-id
 (fn [db]
   (:current-container-id db)))

(re-frame/reg-sub
 :actions
 (fn [db]
   (:actions db)))

(re-frame/reg-sub
 :events-and-handlers
 (fn [db]
   (:events-and-handlers db)))

(re-frame/reg-sub
 :api-data
 (fn [db [_ id]]
   (get-in db [:api-data id])))

(re-frame/reg-sub
 :context-menu
 (fn [db]
   (:context-menu db)))

(re-frame/reg-sub
 :show-context-menu?
 (fn [db]
   (:show-context-menu? db)))

(re-frame/reg-sub
 :show-settings-modal?
 (fn [db]
   (:show-settings-modal? db)))

(re-frame/reg-sub
 :settings
 (fn [db]
   (:settings db)))

(re-frame/reg-sub
 :data-args
 (fn [db]
   (:data-args db)))
