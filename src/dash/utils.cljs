(ns dash.utils
  (:require
   [re-frame.core :as re-frame]
   [com.rpl.specter :as specter]
   [clojure.string :as str]))

(defn add-widget [widgets {:keys [parent-id] :as widget}]
  (if (= parent-id -1)
    (update widgets :children conj widget)
    (specter/transform
     (specter/walker #(= (:id %) parent-id))
     #(update % :children conj widget)
     widgets)))

(defn render-widget [{:keys [id name children]} widget-views register-event register-handler settings]
  (let [view (get widget-views name)]
    (if (= name :container)
      [view {:id id
             :children children
             :register-event register-event
             :register-handler register-handler
             :settings settings}]
      [:div {:on-context-menu (fn [event]
                                (.preventDefault event)
                                (re-frame/dispatch [:set-context-menu {:x (.-clientX event)
                                                                       :y (.-clientY event)
                                                                       :widget-id id}])
                                (re-frame/dispatch [:set-show-context-menu true]))}
       [view {:register-event register-event
              :register-handler register-handler
              :widget-id id
              :settings settings}]])))

(defn fill-args [uri args]
  (let [counter (atom 0)]
    (str/replace uri #"\{arg\d+\}"
                 (fn [_]
                   (let [value (get args @counter "")]
                     (swap! counter inc)
                     value)))))
