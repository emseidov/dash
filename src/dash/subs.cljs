(ns dash.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 :edit-mode?
 (fn [db]
   (:edit-mode? db)))

(reg-sub
 :widgets
 (fn [db]
   (:widgets db)))

(reg-sub
 :show-config-modal?
 (fn [db]
   (:show-config-modal? db)))
