(ns dash.views
  (:require
   [clojure.string :as str]
   [re-frame.core :refer [subscribe dispatch]]
   [re-com.core :as re-com]
   [reagent.core :as reagent]
   [dash.subs :as subs]))

(defn title []
  [re-com/title
   :label "Dashboard"
   :level :level1])

(defn edit-mode-btn []
  (let [edit-mode? @(subscribe [:edit-mode?])
        label (str "Mode: " (if edit-mode? "Edit" "View"))]
    [re-com/button
     :style {:width "100px"}
     :label label
     :on-click #(dispatch [:toggle-edit-mode])]))

(defn header []
  [re-com/h-box
   :width "100%"
   :align :center
   :justify :between
   :children [[title] [edit-mode-btn]]])

(defn widget-list [{:keys [parent-id]}]
  (let [selected (reagent/atom #{})]
    (fn []
      [re-com/selection-list
       :multi-select? false
       :model @selected
       :choices [{:id :h-box :label "h-box"}
                 {:id :dropdown :label "dropdown"}
                 {:id :button :label "button"}
                 {:id :table :label "table"}]
       :on-change (fn [x]
                    (reset! selected x)
                    (dispatch [:add-widget (first x) (random-uuid) parent-id]))])))

(defn widget-modal [{:keys [backdrop-on-click parent-id]}]
  [re-com/modal-panel
   :backdrop-on-click #(backdrop-on-click)
   :child [widget-list {:parent-id parent-id}]])

(defn add-btn [{:keys [parent-id]}]
  (let [show? (reagent/atom false)]
    (fn []
      [:<>
       [re-com/md-circle-icon-button
        :md-icon-name "zmdi-plus"
        :on-click #(reset! show? true)]
       (when @show?
         [widget-modal
          {:backdrop-on-click #(reset! show? false)
           :parent-id parent-id}])])))

(defn h-box [{:keys [parent-id]}]
  [re-com/h-box
   :class "h-box"
   :width "100%"
   :align :center
   :padding "4px"
   :min-height "50px"
   :children [[add-btn {:parent-id parent-id}]]])

(defn dropdown []
  (let [model (reagent/atom :a)
        choices [{:id :a :label "Choice A"}
                 {:id :b :label "Choice B"}
                 {:id :c :label "Choice C"}]]
    [re-com/single-dropdown
     :choices choices
     :width "300px"
     :model @model
     :on-change #(reset! model %)]))

(defn table []
  (let [columns [{:id :id :header-label "id" :row-label-fn (fn [row] (:id row)) :width 50}
                 {:id :name :header-label "name" :row-label-fn (fn [row] (:name row)) :width 50}
                 {:id :id :header-label "age" :row-label-fn (fn [row] (:age row)) :width 50}]
        model (reagent/atom [{:id 1 :name "Alice" :age 30}
                             {:id 2 :name "Janice" :age 35}
                             {:id 3 :name "Banice" :age 38}])]
    [re-com/simple-v-table
     :columns columns
     :model model]))

(defn button []
  [re-com/button
   :label "Submit"])

(def widget-views
  {:h-box h-box
   :dropdown dropdown
   :button button
   :table table})

(defn render-widgets [widgets]
  (map
   (fn [{:keys [id name children parent-id]}]
     (let [view (get widget-views name)]
       (if (= name :h-box)
          ;; For h-box, pass parent-id and render children
         ^{:key id}
         [view {:parent-id parent-id}
          (when (seq children)
            (render-widgets children))]
          ;; For leaf widgets
         ^{:key id}
         [view])))
   widgets))

(defn render-widget [{:keys [id name children]} edit-mode?]
  (let [view (get widget-views name)]
    (if view
      (if (= name :h-box)
        ;; For containers, pass parent-id and render children
        [view {:parent-id id}
         (when (seq children)
           (for [child children]
             ^{:key (:id child)}
             (render-widget child edit-mode?)))]
        ;; For leaf widgets
        [view])
      [:div "Unknown widget: " (pr-str name)])))

(defn main []
  (let [edit-mode? @(subscribe [:edit-mode?])
        class (str/join " " ["main" (when edit-mode? "edit-mode")])
        widgets @(subscribe [:widgets])]
    [re-com/v-box
     :class class
     :width "100%"
     :children
     (concat
      (for [w widgets]
        ^{:key (:id w)}
        (render-widget w edit-mode?))
      (when edit-mode?
        [[add-btn {:parent-id nil}]]))]))
;
(defn app []
  [re-com/v-box
   :class "dash"
   :children [[header] [main]]])
