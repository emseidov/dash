(ns dash.utils
  (:require
   [com.rpl.specter :as specter]))

(defn add-widget [widgets {:keys [parent-id] :as widget}]
  (if (= parent-id -1)
    (update widgets :children conj widget)
    (specter/transform
     (specter/walker #(= (:id %) parent-id))
     #(update % :children conj widget)
     widgets)))

(defn render-widget [{:keys [id name children]} widget-views register-event register-handler]
  (let [view (get widget-views name)]
    (if (= name :container)
      [view {:id id :children children :register-event register-event :register-handler register-handler}]
      [view {:register-event register-event :register-handler register-handler :widget-id id}])))
