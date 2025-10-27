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
        (let [{:keys [register-handler widget-id]} (reagent/props this)]
          (println "Hello from dropdown-widget component-did-mount!")
          (register-handler {:key "log" :fn log :widget-id widget-id})))
      ;; :component-did-update
      ;; (fn [this prev-props]
      ;;   (let [{:keys [register-handler widget-id]} (reagent/props this)]
      ;;     (register-handler {:key "log" :fn log :widget-id widget-id})))
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
        (let [{:keys [register-event widget-id]} (reagent/props this)]
          (reset! handle-click (register-event {:key "handle-click" :fn @handle-click :widget-id widget-id}))))
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

(defn add-action-button [{:keys [actions on-click]}]
  [re-com/button
   :label "Add Action"
   :on-click #(on-click)])

(defn action-connector []
  (let [show (reagent/atom nil)
        draft-actions (reagent/atom {})
        draft-atom (reagent/atom [])]
    (fn []
      (let [actions (re-frame/subscribe [:actions])
            events-and-handlers (re-frame/subscribe [:events-and-handlers])]
        (reset! draft-atom (range (count @actions)))
        [re-com/h-box
         :height "600px"
         :width "600px"
         :justify :between
         :children [[re-com/v-box
                     :style {:flex 1}
                     :children (into
                                [[add-action-button {:actions @draft-atom
                                                     :on-click #(swap! draft-atom conj (str "Atom - " (inc (count @draft-atom))))}]
                                 [re-com/button
                                  :label "Submit"
                                  :on-click #(re-frame/dispatch [:add-action @draft-actions])]]
                                (mapv (fn [action]
                                        ^{:key action}
                                        [:div
                                         [:div action]
                                         [:div {:style {:margin-left 20}
                                                :on-click #(reset! show "events")} "Wait event"]
                                         [:div {:style {:margin-left 40}
                                                :on-click #(reset! show "handlers")} "Handler"]]) @draft-atom))]
                    [re-com/v-box
                     :style {:flex 1}
                     :children (cond
                                 (= @show "events") (mapv (fn [[id {:keys [events]}]]
                                                            [:div
                                                             (str "id - " id)
                                                             [:div ""
                                                              (if (empty? (mapv (fn [[name]]
                                                                                  [:span name]) events))
                                                                "empty"
                                                                (first (mapv (fn [[name value]]
                                                                               [:span {:on-click #(swap! draft-actions merge {id value})} name]) events)))]]) @events-and-handlers)
                                 (= @show "handlers") (mapv (fn [[id {:keys [handlers]}]]
                                                              [:div
                                                               (str "id - " id)
                                                               [:div ""
                                                                (if (empty? (mapv (fn [[name]]
                                                                                    [:span name]) handlers))
                                                                  "empty"
                                                                  (first (mapv (fn [[name value]]
                                                                                 [:span {:on-click #(swap! draft-actions merge {id value})} name]) handlers)))]]) @events-and-handlers)

                                 :else [[:span ""]])]]]))))

(defn actions-modal []
  [re-com/modal-panel
   :backdrop-on-click #(re-frame/dispatch [:set-show-actions-modal false])
   :child [action-connector]])

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
  (reagent/create-class
   {:component-did-update
    ;; (fn [this prev-props]
    ;;   (let [{:keys actions} (reagent/props this)
    ;;         prev-actions (:actions prev-props)]
    ;;     (when (not= actions prev-actions)
    ;;       ))
    (fn [] ())
    :render
    (fn []
      (let [edit-mode? (re-frame/subscribe [:edit-mode?])
            class (str/join " " ["dash" (when @edit-mode? "edit-mode")])
            widgets (re-frame/subscribe [:widgets])
            actions (re-frame/subscribe [:actions])
            register-event (fn [{:keys [widget-id] :as event}]
                             (re-frame/dispatch [:reg-event event])
                             (fn []
                               (println "dash" @actions widget-id event)
                               ((:fn event))
                               ((:fn (get @actions 2)))))
            register-handler (fn [handler]
                               (re-frame/dispatch [:reg-handler handler]))
                         ;;   (event)
                         ;;   ((:handler @events-and-handlers))))
            elements (for [widget (:children @widgets)]
                       ^{:key (:id widget)}
                       (utils/render-widget widget widget-views register-event register-handler))]
        [re-com/v-box
         :class class
         :width "100%"
         :children (vec
                    (concat
                     elements
                     (when @edit-mode?
                       [[add-widget-button {:parent-id -1}]])))]))}))

(defn mode-button []
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])
        label (str "Mode: " (if @edit-mode? "Edit" "View"))]
    [re-com/button
     :label label
     :style {:width "100px"}
     :on-click #(re-frame/dispatch [:toggle-edit-mode])]))

(defn actions-button []
  [:<>
   [re-com/button
    :label "Actions"
    :on-click #(re-frame/dispatch [:set-show-actions-modal true])]])

(defn dash-controls []
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])]
    [re-com/h-box
     :align :center
     :gap "10px"
     :children [(when @edit-mode? [actions-button]) [mode-button]]]))

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
  (let [show-widget-modal? (re-frame/subscribe [:show-widget-modal?])
        show-actions-modal? (re-frame/subscribe [:show-actions-modal?])
        actions (re-frame/subscribe [:actions])]
    [re-com/v-box
     :class "main"
     :children [[header]
                [dash {:actions actions}]
                (when @show-widget-modal?
                  [widget-modal])
                (when @show-actions-modal?
                  [actions-modal])]]))
