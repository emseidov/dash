(ns dash.utils
  (:require
   [re-frame.core :as rf]
   [com.rpl.specter :as s]
   [clojure.string :as str]))

(defn add-widget [widgets {:keys [parent-id] :as widget}]
  (s/transform
   (s/walker #(= (:id %) parent-id))
   #(update % :children conj widget)
   widgets))

(defn render-widget [widget props widget-map]
  (let [{:keys [id name children]} widget
        view (name widget-map)
        handle-context-menu #(do
                               (.preventDefault %)
                               (rf/dispatch [:set-context-menu {:x (.-clientX %)
                                                                :y (.-clientY %)
                                                                :widget-id id}])
                               (rf/dispatch [:set-show-context-menu true]))]
    (if (= name :container)
      [view (assoc props :id id :children children)]
      [:div {:on-context-menu #(handle-context-menu %)}
       [view (assoc props :id id)]])))

(defn fill-args [uri args]
  (let [counter (atom 0)]
    (str/replace uri #"\{arg\d+\}"
                 (fn [_]
                   (let [value (get args @counter "")]
                     (swap! counter inc)
                     value)))))

(defn to-dropdown-data [data]
  (map (fn [[k v]]
         {:id k :label (str (name k) " - " v)}) data))

(defn spread-colls
  [& colls]
  (vec (apply concat colls)))
