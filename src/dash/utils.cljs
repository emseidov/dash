(ns dash.utils
  (:require
   [com.rpl.specter :as specter]))

(defn add-widget [widgets {:keys [parent-id] :as widget}]
  (if (= parent-id -1)
    (update widgets :children conj widget)
    (specter/transform
     [:children specter/ALL #(= (:id %) parent-id) :children]
     #(conj % widget)
     widgets)))

(defn render-widget [{:keys [id name children]} widget-views]
  (let [view (get widget-views name)]
    (if (= name :container)
      [view {:id id :children children}]
      [view])))
