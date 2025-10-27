(ns dash.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [reagent.core :as reagent]
   [clojure.string :as str]
   [dash.utils :as utils]
   [dash.subs]))

(declare add-widget-button widget-views)

(def dropdown-data [{:id :a :label "Choice A"}
                    {:id :b :label "Choice B"}
                    {:id :c :label "Choice C"}])

(def table-colums [{:id :id :header-label "id" :row-label-fn (fn [row] (:id row)) :width 340}
                   {:id :name :header-label "name" :row-label-fn (fn [row] (:name row)) :width 340}
                   {:id :id :header-label "age" :row-label-fn (fn [row] (:age row)) :width 340}])

(def table-data [{:id 1 :name "Alice" :age 30}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}
                 {:id 1 :name "Alice" :age 30}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}
                 {:id 1 :name "Alice" :age 30}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}
                 {:id 1 :name "Alice" :age 30}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}
                 {:id 1 :name "Alice" :age 30}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}
                 {:id 2 :name "Janice" :age 35}
                 {:id 3 :name "Banice" :age 38}])

(def tree-select-data [{:id 1 :label "events" :group [:root :container-1 :dropdown-1]}
                       {:id 2 :label "events" :group [:root :container-1 :dropdown-2]}
                       {:id 3 :label "events" :group [:root :container-1 :dropdown-3]}
                       {:id 4 :label "events" :group [:root :container-1 :button-1]}
                       {:id 5 :label "events" :group [:root :container-2 :table-1]}])

(def my-atom (atom 0))

(defn container-widget [{:keys [id children register-event register-handler]}]
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])
        elements (for [child children]
                   ^{:key (:id child)}
                   (utils/render-widget child widget-views register-event register-handler))]
    [re-com/h-box
     :align :center
     :class "container-widget"
     :padding "8px"
     :width "100%"
     :children (vec
                (concat elements
                        (when @edit-mode?
                          [[add-widget-button {:parent-id id}]])))]))

(defn dropdown-widget []
  (let [log (fn []
              (println "Hello from dropdown-widget register-method!"))]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [register-handler]} (reagent/props this)]
          (println "Hello from dropdown-widget component-did-mount!")
          (register-handler log)))
      :render
      (fn []
        (let [model (reagent/atom :a)
              choices dropdown-data]
          [re-com/single-dropdown
           :choices choices
           :class "dropdown-widget"
           :width "300px"
           :model @model
           :on-change #(reset! model %)]))})))

(defn button-widget []
  (let [handle-click (reagent/atom (fn []
                                     (println "Hello from button-widget register-event!")))]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (println "Hello from button-widget component-did-mount!")
        (let [{:keys [register-event]} (reagent/props this)]
          (reset! handle-click (register-event @handle-click))))
      :render
      (fn []
        [re-com/button
         :label "Submit"
         :on-click #(@handle-click)])})))

(defn table-widget []
  (let [columns table-colums
        model (reagent/atom table-data)]
    [re-com/simple-v-table
     :class "table-widget"
     :columns columns
     :model model]))

(def widget-views
  {:container container-widget
   :dropdown dropdown-widget
   :button button-widget
   :table table-widget})

(defn widget-list []
  (let [selected-widget (re-frame/subscribe [:selected-widget])]
    [re-com/selection-list
     :height "400px"
     :width "400px"
     :choices (map (fn [[k _]]
                     {:id k :label (name k)}) widget-views)
     :model @selected-widget
     :multi-select? false
     :on-change #(re-frame/dispatch [:select-widget %])]))

(defn widget-modal []
  (let [selected-widget (re-frame/subscribe [:selected-widget])
        current-container-id (re-frame/subscribe [:current-container-id])]
    [re-com/modal-panel
     :backdrop-on-click #(re-frame/dispatch [:set-show-widget-modal false])
     :child [re-com/v-box
             :children [[widget-list]
                        [re-com/button
                         :label "Add"
                         :on-click #(do
                                      (re-frame/dispatch [:select-widget #{}])
                                      (re-frame/dispatch [:set-show-widget-modal false])
                                      (re-frame/dispatch
                                       [:add-widget
                                        (first @selected-widget)
                                        @my-atom
                                        @current-container-id])
                                      (swap! my-atom inc))]]]]))

(defn add-widget-button [{:keys [parent-id]}]
  [:<>
   [re-com/md-circle-icon-button
    :md-icon-name "zmdi-plus"
    :on-click #(do
                 (re-frame/dispatch [:set-current-container-id parent-id])
                 (re-frame/dispatch [:set-show-widget-modal true]))]])

(defn dash []
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])
        class (str/join " " ["dash" (when @edit-mode? "edit-mode")])
        widgets (re-frame/subscribe [:widgets])
        events-and-handlers (reagent/atom {:handler nil
                                           :event nil})
        register-handler (fn [handler]
                           (swap! events-and-handlers #(assoc % :handler handler)))
        register-event (fn [event]
                         (swap! events-and-handlers #(assoc % :event event))
                         (fn []
                           (event)
                           ((:handler @events-and-handlers))))
        elements (for [widget (:children @widgets)]
                   ^{:key (:id widget)}
                   (utils/render-widget widget widget-views register-event register-handler))]
    (reagent/create-class
     {:component-did-mount
      (fn []
        ())
      :render
      (fn [] [re-com/v-box
              :class class
              :width "100%"
              :children (vec
                         (concat
                          elements
                          (when @edit-mode?
                            [[add-widget-button {:parent-id -1}]])))])})))

(defn mode-button []
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])
        label (str "Mode: " (if @edit-mode? "Edit" "View"))]
    [re-com/button
     :label label
     :style {:width "100px"}
     :on-click #(re-frame/dispatch [:toggle-edit-mode])]))

(defn config-modal []
  (let [selected (reagent/atom #{})]
    [re-com/modal-panel
     :backdrop-on-click #(re-frame/dispatch [:set-show-config-modal false])
     :child [re-com/h-box
             :height "600px"
             :width "600px"
             :children [[re-com/box
                         :child [re-com/tree-select
                                 :choices tree-select-data
                                 :initial-expanded-groups :all
                                 :model @selected
                                 :on-change #(reset! selected %)]]
                        [re-com/box
                         :child [:span "Hello"]]]]]))

(defn config-button []
  (let [show-config-modal? (re-frame/subscribe [:show-config-modal?])]
    [:<>
     [re-com/button
      :label "Events & Handlers"
      :on-click #(re-frame/dispatch [:set-show-config-modal true])]
     (when @show-config-modal? [config-modal])]))

(defn dash-controls []
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])]
    [re-com/h-box
     :align :center
     :gap "10px"
     :children [(when @edit-mode? [config-button]) [mode-button]]]))

(defn title []
  [re-com/title
   :label "Dashboard"
   :level :level1])

(defn header []
  [re-com/h-box
   :align :center
   :justify :between
   :width "100%"
   :children [[title] [dash-controls]]])

(defn main []
  (let [show-widget-modal? (re-frame/subscribe [:show-widget-modal?])]
    [re-com/v-box
     :class "main"
     :children [[header] [dash] (when @show-widget-modal?
                                  [widget-modal])]]))
