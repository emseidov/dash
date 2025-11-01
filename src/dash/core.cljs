(ns dash.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as rf]
   [dash.events :as events]
   [dash.subs :as subs]
   [dash.views :as views]
   [dash.config :as config]
   [day8.re-frame.http-fx]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main] root-el)))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
