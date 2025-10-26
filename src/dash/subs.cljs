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

(re-frame/reg-sub
 :widgets
 (fn [db]
   (:widgets db)))

(re-frame/reg-sub
 :show-config-modal?
 (fn [db]
   (:show-config-modal? db)))

(re-frame/reg-sub
 :selected-widget
 (fn [db]
   (:selected-widget db)))

(re-frame/reg-sub
 :current-container-id
 (fn [db]
   (:current-container-id db)))
