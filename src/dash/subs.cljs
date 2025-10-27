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

