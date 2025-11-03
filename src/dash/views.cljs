(ns dash.views
  (:require
   [re-frame.core :as rf]
   [re-com.core :as rc]
   [reagent.core :as r]
   [dash.utils :as u]))

;; Crafted with love (and plenty of parentheses) by an aspiring Clojurist ♥️

(declare show-widget-modal-button widget-map)

(defn table-widget [{:keys [id]}]
  (r/with-let [set-data-args (fn [args caller-id]
                               (rf/dispatch [:set-data-args id caller-id args]))
               fetch (fn []
                       (let [data-args @(rf/subscribe [:data-args])
                             settings @(rf/subscribe [:settings])
                             uri (get-in settings [:api id])]
                         (rf/dispatch [:fetch-api-data uri (get data-args id) id])))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [reg-handler id]} (r/props this)]
          (reg-handler {:key "set-data-args"
                        :fn set-data-args
                        :widget-id id})
          (reg-handler {:key "fetch"
                        :fn fetch
                        :widget-id id})))
      :reagent-render
      (fn [{:keys [id]}]
        (let [data @(rf/subscribe [:api-data id])
              columns (u/to-table-columns data)
              model (r/reaction (u/to-table-data data))]

          (if  (seq data)
            [rc/simple-v-table
             :class "table-widget"
             :columns columns
             :model model]
            [:div.table-no-data
             [:span "No Data to Show!"]])))})))

(defn datepicker-widget [{:keys []}]
  (r/with-let [model (r/atom nil)
               date-object (atom nil)
               handle-change-date (r/atom (fn []
                                            (reset! model @date-object)))
               log (fn []
                     (println "Hello from datepicker-widget reg-handler" @model))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [id reg-event reg-handler]} (r/props this)]
          (reset! handle-change-date (reg-event {:key "on-change"
                                                 :fn @handle-change-date
                                                 :widget-id id}))
          (reg-handler {:key "log"
                        :fn log
                        :widget-id id})))
      :reagent-render
      (fn []
        [rc/datepicker-dropdown
         :width "260px"
         :class "datepicker-widget"
         :format "yyyy-MM-dd"
         :model model
         :on-change (fn [date]
                      (reset! date-object date)
                      (@handle-change-date (u/format-date date)))])})))

(defn button-widget [{:keys [id]}]
  (r/with-let [handle-click (r/atom (fn []
                                      (println "Hello from button-widget reg-event!")))
               set-data-args (fn [args caller-id]
                               (rf/dispatch [:set-data-args id caller-id args]))
               log-args (fn []
                          ())]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [reg-event reg-handler id]} (r/props this)]
          (reset! handle-click (reg-event {:key "on-click"
                                           :fn @handle-click
                                           :widget-id id}))
          (reg-handler {:key "set-data-args"
                        :fn set-data-args
                        :widget-id id})
          (reg-handler {:key "log-args"
                        :fn log-args
                        :widget-id id})))
      :reagent-render
      (fn []
        [rc/button
         :class "button-widget btn-default"
         :label "Submit"
         :on-click #(@handle-click)])})))

;; These will be improved
;; 1. I saw rf/subscribe warning outside of render context late
;;    and didn't have enough time to come with a new solution, but I have few ideas.
;; 2. Will get rid of create class and event-register r/atom usage. I use r/atoms
;;    only for draft-states, so that I don't trigger rerenders on subscribers 
;;    for temporary states.
(defn dropdown-widget [{:keys [id]}]
  (r/with-let [model (r/atom nil)
               handle-change (r/atom (fn [choice]
                                       (reset! model choice)))
               set-data-args (fn [args]
                               (rf/dispatch [:set-data-args id args]))
               fetch (fn []
                       (let [data-args @(rf/subscribe [:data-args])
                             settings @(rf/subscribe [:settings])
                             uri (get-in settings [:api id])]
                         (rf/dispatch [:fetch-api-data uri (get data-args id) id])))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [reg-event reg-handler id]} (r/props this)]
          (reset! handle-change (reg-event {:key "on-change"
                                            :fn @handle-change
                                            :widget-id id}))
          (reg-handler {:key "set-data-args"
                        :fn set-data-args
                        :widget-id id})
          (reg-handler {:key "fetch"
                        :fn fetch
                        :widget-id id})))
      :reagent-render
      (fn [{:keys [id]}]
        (let [data (or @(rf/subscribe [:api-data id]) [])
              choices (u/to-dropdown-data data)]
          [rc/single-dropdown
           :class "dropdown-widget"
           :choices choices
           :model model
           :on-change #(@handle-change %)]))})))

(defn container-widget [{:keys [id children] :as props}]
  (let [edit-mode? @(rf/subscribe [:edit-mode?])
        widgets (for [widget children]
                  ^{:key (:id widget)}
                  (u/render-widget widget props widget-map))]
    [rc/h-box
     :class "container-widget"
     :align :center
     :children (u/spread-colls widgets (when edit-mode?
                                         [[show-widget-modal-button
                                           {:parent-id id}]]))]))

(def widget-map
  {:container container-widget
   :dropdown dropdown-widget
   :button button-widget
   :datepicker datepicker-widget
   :table table-widget})

(defn save-settings-button [{:keys [on-click]}]
  (let [handle-click #(on-click)]
    [rc/button
     :class "save-settings-btn btn-default"
     :label "Save"
     :on-click handle-click]))

(defn api-settings-field [{:keys [model on-change]}]
  (let [handle-change #(on-change %)]
    [:div
     {:class "api-settings-field"}
     [:span "API"] [rc/input-text
                    :change-on-blur? false
                    :model model
                    :on-change handle-change]]))

(defn settings-modal []
  (r/with-let [widget-id (-> @(rf/subscribe [:context-menu])
                             :widget-id)
               draft-api (-> @(rf/subscribe [:api-settings widget-id])
                             r/atom)
               handle-hide #(rf/dispatch [:set-show-settings-modal false])
               handle-change-draft-api #(reset! draft-api %)
               handle-save-settings (fn []
                                      (rf/dispatch [:set-api-settings widget-id @draft-api])
                                      (rf/dispatch [:set-show-settings-modal false]))]
    [rc/modal-panel
     :class "settings-modal"
     :backdrop-on-click handle-hide
     :child [:div
             [api-settings-field
              {:model draft-api
               :on-change handle-change-draft-api}]
             [save-settings-button
              {:on-click handle-save-settings}]]]))

(defn context-menu []
  (r/with-let [handle-close (fn [_]
                              (rf/dispatch [:set-show-context-menu false]))]
    (.addEventListener js/document "click" handle-close)
    (let [{:keys [x y]} @(rf/subscribe [:context-menu])]
      [:div.context-menu
       {:style {:top y :left x}}
       [:div {:on-click #(rf/dispatch [:set-show-settings-modal true])} "Settings"]
       [:div {:on-click #()} "Remove"]])
    (finally
      (.removeEventListener js/document "click" handle-close))))

(defn save-actions-button [{:keys [on-click]}]
  (let [handle-click #(on-click)]
    [rc/button
     :class "save-actions-btn btn-default"
     :label "Save"
     :on-click handle-click]))

(defn handler-selector [{:keys [widget-id action action-idx handlers on-select-handler]}]
  (let [model (->> (:handlers action)
                   (some #(when (= widget-id (first %)) (second %)))
                   set)
        choices (mapv (fn [[name]]
                        {:label name
                         :id name}) handlers)
        handle-select-handler #(on-select-handler (vec %) widget-id action-idx)]
    [:div.handler-selector
     widget-id
     [rc/selection-list
      :model model
      :choices choices
      :on-change #(handle-select-handler %)]]))

(defn event-selector [{:keys [widget-id action action-idx events on-select-event]}]
  (let [model (if (= widget-id (get-in action [:event :widget-id]))
                (as-> (:event action) %
                  (:name %)
                  (hash-map :name % :widget-id widget-id)
                  (hash-set %))
                #{})]
    [:div.event-selector
     widget-id
     [rc/selection-list
      :model model
      :multi-select? false
      :choices (mapv (fn [[name]]
                       {:label name :id {:name name :widget-id widget-id}}) events)
      :on-change (fn [event]
                   (on-select-event (first event) action-idx))]]))

(defn event-and-handler-selector
  [{:keys [show action action-idx events-and-handlers on-select-event on-select-handler]}]
  (let [event-selectors (mapv (fn [[widget-id {:keys [events]}]]
                                ^{:id widget-id}
                                [event-selector
                                 {:widget-id widget-id
                                  :action action
                                  :action-idx action-idx
                                  :events events
                                  :on-select-event on-select-event}]) events-and-handlers)
        handler-selectors (mapv (fn [[widget-id {:keys [handlers]}]]
                                  ^{:id widget-id}
                                  [handler-selector
                                   {:widget-id widget-id
                                    :action action
                                    :action-idx action-idx
                                    :handlers handlers
                                    :on-select-handler on-select-handler}]) events-and-handlers)]
    [rc/v-box
     :class "events-and-handlers-view"
     :children (cond
                 (= show :events) event-selectors
                 (= show :handlers) handler-selectors
                 :else [[:span ""]])]))

(defn action-selector [{:keys [idx on-click-events on-click-handlers]}]
  [:div
   {:class "action-selector"}
   "Action " (inc idx)
   [:div
    {:on-click #(on-click-events idx)}
    "Wait Event"]
   [:div
    {:on-click #(on-click-handlers idx)}
    "Handler"]])

(defn create-action-button [{:keys [on-click]}]
  (let [handle-click #(on-click)]
    [rc/button
     :class "create-action-btn btn-default"
     :label "Add Action"
     :on-click handle-click]))

(defn action-connector []
  (r/with-let [events-and-handlers @(rf/subscribe [:events-and-handlers])
               draft-actions (-> @(rf/subscribe [:actions]) r/atom)
               curr-action-idx (r/atom nil)
               show (r/atom nil)
               handle-create-action #(swap! draft-actions conj
                                            {:event {}
                                             :handlers {}})
               handle-show-events (fn [idx]
                                    (reset! curr-action-idx idx)
                                    (reset! show :events))
               handle-show-handlers (fn [idx]
                                      (reset! curr-action-idx idx)
                                      (reset! show :handlers))
               handle-select-event (fn [event idx]
                                     (swap! draft-actions assoc-in [idx :event] event))
               handle-select-handler (fn [handlers widget-id idx]
                                       (swap! draft-actions assoc-in [idx :handlers widget-id] handlers))
               handle-save-actions #(rf/dispatch [:save-actions @draft-actions])]
    (let [action-selectors (map-indexed (fn [idx action]
                                          [action-selector
                                           {:action action
                                            :idx idx
                                            :on-click-events handle-show-events
                                            :on-click-handlers handle-show-handlers}]) @draft-actions)]
      [:<>
       [rc/title
        :class "widget-list-title"
        :label "Actions"
        :level :level2]
       [rc/h-box
        :class "action-connector"
        :justify :between
        :children [[rc/v-box
                    :children (u/spread-colls [[create-action-button
                                                {:on-click handle-create-action}]]
                                              action-selectors)]
                   [rc/v-box
                    :children [[event-and-handler-selector
                                {:show @show
                                 :action-idx @curr-action-idx
                                 :action (get @draft-actions @curr-action-idx)
                                 :events-and-handlers events-and-handlers
                                 :on-select-event handle-select-event
                                 :on-select-handler handle-select-handler}]
                               [rc/h-box
                                :children [[save-actions-button
                                            {:on-click handle-save-actions}]]]]]]]])))

(defn action-modal []
  (let [handle-hide #(rf/dispatch [:set-show-action-modal false])]
    [rc/modal-panel
     :class "action-modal"
     :backdrop-on-click handle-hide
     :child [action-connector]]))

(defn add-widget-button [{:keys [on-click]}]
  (let [handle-click #(on-click)]
    [rc/button
     :class "add-widget-btn btn-default"
     :label "Add"
     :on-click handle-click]))

(defn widget-list [{:keys [model choices on-change]}]
  (let [handle-change #(on-change %)]
    [:div
     [rc/title
      :class "widget-list-title"
      :label "Widgets"
      :level :level2]
     [rc/selection-list
      :class "widget-list"
      :choices choices
      :model model
      :multi-select? false
      :on-change handle-change]]))

(defn widget-modal []
  (r/with-let [current-parent-id @(rf/subscribe [:current-parent-id])
               selected-widget (r/atom #{})
               widget-choices (map (fn [[key _]]
                                     {:id key
                                      :label (name key)}) widget-map)
               handle-select-widget #(reset! selected-widget %)
               handle-add-widget (fn []
                                   (rf/dispatch [:set-show-widget-modal false])
                                   (rf/dispatch [:add-widget (first @selected-widget) current-parent-id]))]
    [rc/modal-panel
     :class "widget-modal"
     :backdrop-on-click #(rf/dispatch [:set-show-widget-modal false])
     :child [rc/v-box
             :children [[widget-list
                         {:model selected-widget
                          :choices widget-choices
                          :on-change handle-select-widget}]
                        [add-widget-button
                         {:on-click handle-add-widget}]]]]))

(defn show-widget-modal-button [{:keys [parent-id]}]
  (let [handle-show-widget-modal (fn []
                                   (rf/dispatch [:set-current-parent-id parent-id])
                                   (rf/dispatch [:set-show-widget-modal true]))]
    [rc/md-circle-icon-button
     :class "show-widget-modal-btn"
     :md-icon-name "zmdi-plus"
     :on-click handle-show-widget-modal]))

(defn dash []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])
        widget-tree @(rf/subscribe [:widget-tree])
        reg-event (fn [{:keys [widget-id key] :as event}]
                    (rf/dispatch [:reg-event event])
                    (fn [args]
                      (let [actions @(rf/subscribe [:actions])
                            events-and-handlers @(rf/subscribe [:events-and-handlers])
                            action (some #(when (= widget-id (get-in % [:event :widget-id])) %) actions)
                            active-event (get-in events-and-handlers [widget-id :events key])
                            ;; Last minute bug fix at 4 AM :D I will write those properly
                            filtered-events-and-handlers (filter #(contains? (:handlers action) (first %)) events-and-handlers)
                            active-handlers (map (fn [[widget-id evt-and-handlrs]]
                                                   (filter (fn [[handlr-name]]
                                                             (some #(= % handlr-name) (get (:handlers action) widget-id))) (:handlers evt-and-handlrs))) filtered-events-and-handlers)]
                        (when active-event
                          ((:fn active-event) args))
                        (doseq [[[_ handler]]  active-handlers]
                          ((:fn handler) args widget-id)))))
        reg-handler #(rf/dispatch [:reg-handler %])
        props {:reg-event reg-event
               :reg-handler reg-handler}
        widgets (for [widget (:children widget-tree)]
                  ^{:key (:id widget)}
                  (u/render-widget widget props widget-map))
        class (str "dash " (when edit-mode? "edit-mode"))]
    [rc/v-box
     :class class
     :children (u/spread-colls widgets (when edit-mode?
                                         [[show-widget-modal-button
                                           {:parent-id 1}]]))]))

(defn toggle-mode-button []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])
        label (str "Mode: " (if edit-mode? "Edit" "View"))
        handle-toggle-edit-mode #(rf/dispatch [:toggle-edit-mode])]
    [rc/button
     :class "toggle-mode-btn btn-default"
     :label label
     :on-click handle-toggle-edit-mode]))

(defn show-action-modal-button []
  (let [handle-show-action-modal #(rf/dispatch [:set-show-action-modal true])]
    [rc/button
     :class "show-action-modal-btn btn-default"
     :label "Actions"
     :on-click handle-show-action-modal]))

(defn dash-controls []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])]
    [rc/h-box
     :class "dash-controls"
     :align :center
     :children [(when edit-mode?
                  [show-action-modal-button])
                [toggle-mode-button]]]))

(defn title []
  [rc/title
   :class "title"
   :label "Dashboard"
   :level :level1])

(defn header []
  [rc/h-box
   :class "header"
   :align :center
   :justify :between
   :children [[title]
              [dash-controls]]])

(defn main []
  (let [show-widget-modal? @(rf/subscribe [:show-widget-modal?])
        show-action-modal? @(rf/subscribe [:show-action-modal?])
        show-context-menu? @(rf/subscribe [:show-context-menu?])
        show-settings-modal? @(rf/subscribe [:show-settings-modal?])]
    [rc/v-box
     :class "main"
     :children [[header]
                [dash]
                (when show-widget-modal?
                  [widget-modal])
                (when show-action-modal?
                  [action-modal])
                (when show-context-menu?
                  [context-menu])
                (when show-settings-modal?
                  [settings-modal])]]))
