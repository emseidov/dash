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

(defn container-widget [{:keys [id children register-event register-handler settings]}]
  (let [edit-mode? (re-frame/subscribe [:edit-mode?])
        elements (for [child children]
                   ^{:key (:id child)}
                   (utils/render-widget child widget-views register-event register-handler settings))]
    [re-com/h-box
     :align :center
     :class "container-widget"
     :padding "8px"
     :width "100%"
     :children (vec
                (concat elements
                        (when @edit-mode?
                          [[add-widget-button {:parent-id id}]])))]))

(defn dropdown-widget [{:keys [widget-id]}]
  (let [set-data-args (fn [args]
                        (println "set-data-args dropdown-widget")
                        (re-frame/dispatch [:set-data-args args widget-id]))
        fetch (fn []
                (let [settings (re-frame/subscribe [:settings])
                      uri (get @settings widget-id)]
                      ;; args (re-frame/subscribe [:data-args widget-id])]
                  (println "fetch dropdown-widget" uri widget-id)
                  (re-frame/dispatch [:fetch-api-data uri widget-id])))]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [register-handler widget-id]} (reagent/props this)]
          (println "Hello from dropdown-widget component-did-mount!")
          (register-handler {:key "set-data-args"
                             :fn set-data-args
                             :widget-id widget-id})
          (register-handler {:key "fetch"
                             :fn fetch
                             :widget-id widget-id})))
      ;; :component-did-update
      ;; (fn [this prev-props]
      ;;   (let [{:keys [register-handler widget-id]} (reagent/props this)]
      ;;     (register-handler {:key "log" :fn log :widget-id widget-id})))
      :component-did-update
      (fn [this prev-props]
        (let [{:keys [settings widget-id]} (reagent/props this)
              prev-settings (:settings prev-props)
              widget-settings (get settings widget-id)
              prev-widget-settings (get prev-settings widget-id)]
          (println "dropdown-widget component-did-update" settings prev-settings)
          (when (not= widget-settings prev-widget-settings)
            (fetch))))
      :reagent-render
      (fn [{:keys [widget-id settings]}]
        (let [data (re-frame/subscribe [:api-data widget-id])
              model (reagent/atom nil)
              choices (or @data [])]
          (println "dropdown-widget render")
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
          (reset! handle-click (register-event {:key "handle-click"
                                                :fn @handle-click
                                                :widget-id widget-id}))))
      :render
      (fn []
        [re-com/button
         :label "Submit"
         :on-click #(@handle-click)])})))

(defn table-widget [{:keys [widget-id]}]
  (let [columns table-colums
        model (reagent/atom table-data)
        set-data-args (fn [args]
                        (re-frame/dispatch [:set-data-args args widget-id]))

        fetch (fn []
                (let [settings (re-frame/subscribe [:settings])
                      uri (get @settings widget-id)
                      args (re-frame/subscribe [:data-args widget-id])]

                  (println "fetch table-widget" uri args)

                  (re-frame/dispatch [:fetch-api-data uri args])))]
    (reagent/create-class
     {:component-did-mount
      (fn [this]
        ((let [{:keys [register-handler widget-id]} (reagent/props this)]
           (register-handler {:key "log"
                              :fn set-data-args
                              :widget-id widget-id})
           (register-handler {:key "fetch"
                              :fn fetch
                              :widget-id widget-id}))))

      :render
      (fn []
        [re-com/simple-v-table
         :class "table-widget"
         :columns columns
         :model model])})))

(defn tree-select-widget []
  (let [model (reagent/atom #{})]
    (reagent/create-class
     {:render
      (fn []
        [re-com/tree-select-dropdown
         :width "200px"
         :choices [{:id 1 :label "1" :group [:all]}
                   {:id 2 :label "2" :group [:all]}]
         :model @model
         :initial-expanded-groups :all
         :on-change #()])})))

(defn date-picker-widget []
  (reagent/create-class
   {:render
    (fn []
      [re-com/datepicker-dropdown
       :class "date-picker-widget"
       :on-change #()])}))

(def widget-views
  {:container container-widget
   :dropdown dropdown-widget
   :button button-widget
   :table table-widget
   :tree-select tree-select-widget
   :date-picker date-picker-widget})

(defn settings-modal []
  (let [{:keys [widget-id]} @(re-frame/subscribe [:context-menu])
        prev-api (or (get @(re-frame/subscribe [:settings]) widget-id) "")
        api (reagent/atom prev-api)]
    [re-com/modal-panel
     :class "settings-modal"
     :backdrop-on-click #(re-frame/dispatch [:set-show-settings-modal false])
     :child [:div
             [:div
              [:span "API"]
              [re-com/input-text
               :change-on-blur? false
               :model api
               :on-change #(reset! api %)]]
             [re-com/button
              :label "Save"
              :on-click #(do
                           (re-frame/dispatch [:set-settings widget-id @api])
                           (re-frame/dispatch [:set-show-settings-modal false]))]]]))

(defn context-menu []
  (reagent/create-class
   {:component-did-mount
    (fn [this]
      (let [handler (fn [] (re-frame/dispatch [:set-show-context-menu false]))]
        (aset this "clickHandler" handler)
        (.addEventListener js/document "click" handler)))

    :component-will-unmount
    (fn [this]
      (.removeEventListener js/document "click" (aget this "clickHandler")))
    :render
    (fn []
      (let [{:keys [x y]} @(re-frame/subscribe [:context-menu])]
        [:div {:class "context-menu ti-eba-mamata"
               :style {:top y
                       :left x}}
         [:div {:on-click #(re-frame/dispatch [:set-show-settings-modal true])} "Settings"]
         [:div {:on-click #()} "Remove"]]))}))

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
        [re-com/h-box
         :height "600px"
         :width "600px"
         :justify :between
         :children [[re-com/v-box
                     :style {:flex 1}
                     :children (into
                                [[add-action-button {:actions @draft-atom
                                                     :on-click #(swap! draft-atom conj (str "Action - " (inc (count @draft-atom))))}]
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
                                                                (if (empty? (mapv (fn [[namee]]
                                                                                    (prn "handlers" handlers)
                                                                                    (prn "events and handlers" @events-and-handlers)
                                                                                    [:span namee]) handlers))
                                                                  "empty"
                                                                  (let [[[an av] [bn bv]] (seq handlers)]
                                                                    [:<>
                                                                     [:div {:on-click #(swap! draft-actions merge {an av})} an]
                                                                     [:div {:on-click #(swap! draft-actions merge {bn bv})} bn]]))]])
                                                                  ;; (mapv (fn [[namee value]]
                                                                  ;;         (prn "x" namee)
                                                                  ;;         ^{:key namee}
                                                                  ;;         [:span "a"])
                                                                  ;;   ;; (fn [[name value]]
                                                                  ;;   ;;       [:span {:on-click #(swap! draft-actions merge {id value})} name]) 
                                                                  ;;       handlers)
                                                                  ;;
                                                            @events-and-handlers)

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
     :choices (map (fn [[key _]]
                     {:id key :label (name key)}) widget-views)
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
                                        @current-container-id]))]]]]))

(defn add-widget-button [{:keys [parent-id]}]
  [:<>
   [re-com/md-circle-icon-button
    :md-icon-name "zmdi-plus"
    :on-click #(do
                 (re-frame/dispatch [:set-current-container-id parent-id])
                 (re-frame/dispatch [:set-show-widget-modal true]))]])

(defn dash []
  (reagent/create-class
   {:component-did-mount
    ;; (fn [this prev-props]
    ;;   (let [{:keys actions} (reagent/props this)
    ;;         prev-actions (:actions prev-props)]
    ;;     (when (not= actions prev-actions)
    ;;       ))
    (fn [])
    :reagent-render
    (fn []
      (let [edit-mode? (re-frame/subscribe [:edit-mode?])
            widgets (re-frame/subscribe [:widgets])
            actions (re-frame/subscribe [:actions])
            api-data (re-frame/subscribe [:api-data])
            settings (re-frame/subscribe [:settings])
            register-event (fn [{:keys [widget-id] :as event}]
                             (re-frame/dispatch [:reg-event event])
                             (fn []
                               (println "dash" @actions widget-id event)
                               (let [x
                                     (filter
                                      (fn [m]
                                        (= (first (keys m)) widget-id))
                                      @actions)]
                                 (prn "x" (get (first x) widget-id))
                                 ((:fn (get (first x) widget-id)))
                                 (let [[_ [_ bv] [_ cv]] (seq (first x))]
                                   ((:fn bv))
                                   ((:fn cv))))))
            register-handler (fn [handler]
                               (re-frame/dispatch [:reg-handler handler]))
                         ;;   (event)
                         ;;   ((:handler @events-and-handlers))))
            class (str/join " " ["dash" (when @edit-mode? "edit-mode")])
            elements (for [widget (:children @widgets)]
                       ^{:key (:id widget)}
                       (utils/render-widget widget widget-views register-event register-handler @settings))]
        [re-com/v-box
         :class class
         :width "100%"
         :children (vec
                    (concat
                     elements
                     (when @edit-mode?
                       [[add-widget-button {:parent-id 1}]])))]))}))

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
        show-context-menu? (re-frame/subscribe [:show-context-menu?])
        show-settings-modal? (re-frame/subscribe [:show-settings-modal?])
        actions (re-frame/subscribe [:actions])]
    [re-com/v-box
     :class "main"
     :children [[header]
                [dash {:actions actions}]
                (when @show-widget-modal?
                  [widget-modal])
                (when @show-actions-modal?
                  [actions-modal])
                (when @show-context-menu?
                  [context-menu])
                (when @show-settings-modal?
                  [settings-modal])]]))
