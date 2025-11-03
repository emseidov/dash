(ns dash.utils
  (:require
   [re-frame.core :as rf]
   [com.rpl.specter :as s]
   [cljs-time.format :as f]
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
         {:id (name k) :label (str (name k) " - " v)}) data))

(def data {"base" "EUR"
           "start_date" "1999-12-30"
           "end_date" "2000-12-29"
           :rates {"1999-12-30" {"AUD" 1.5422
                                 "CAD" 1.4608
                                 "CHF" 1.6051
                                 "CYP" 0.57667},
                   "2000-01-03" {"AUD" 1.5346
                                 "CAD" 1.4577
                                 "CHF" 1.6043
                                 "CYP" 0.5767}}})

;; {:id :date :label "date" :row-label-fn (fn [row] (:date))}
;; {id: :AUD :label AUD :row-label-fn (fn [row] (:AUD))}
;;
;; {:date 2000-01-03 AUD: 1.5346 }
;; (conj (map (fn [[k]]
;;          {:id k :label k :row-label-fn (fn [row] (k row))})
;;        (second (first (:rates data)))) {:id :date})
;;
;; (map (fn [[k v]]
;;        v) (:rates data))
(conj '(1 2 3) 4)
(defn to-table-columns [data]
  (vec (conj  (map (fn [[k]]
                     {:id k :header-label (name k) :row-label-fn (fn [row] (k row)) :width 65})
                   (second (first (:rates data))))
              {:id :date :header-label "DATE" :row-label-fn (fn [row] (:date row)) :width 90})))

(defn to-table-data [data]
  (mapv (fn [[k v]]
          (assoc v :date (name k))) (:rates data)))

(defn spread-colls
  [& colls]
  (vec (apply concat colls)))

(defn format-date [date-str]
  (->> date-str
       str
       (f/parse (f/formatter "yyyyMMdd'T'HHmmss"))
       (f/unparse (f/formatter "yyyy-MM-dd"))))

