(ns dash.views
  (:require
   [re-frame.core :as rf]
   [re-com.core :as rc]
   [reagent.core :as r]
   [dash.utils :as u]))

;; Crafted with love (and plenty of parentheses) by an aspiring Clojurist ♥️

(declare open-widget-modal-button widget-views)

(defn container-widget [{:keys [id children] :as props}]
  (let [edit-mode? (rf/subscribe [:edit-mode?])
        widgets (mapv (fn [widget]
                        ^{:key (:id widget)}
                        (u/render-widget widget props widget-views)) children)
        content (into widgets
                      (when @edit-mode?
                        [[open-widget-modal-button {:parent-id id}]]))]
    [rc/h-box
     :class "container-widget"
     :children content]))

(defn dropdown-widget [{:keys [widget-id]}]
  (let [model (r/atom nil)
        set-data-args (fn [args]
                        (rf/dispatch [:set-data-args args widget-id]))
        fetch (fn []
                (let [settings (rf/subscribe [:settings])
                      uri (get-in @settings [:api widget-id])]
                  (prn "settings................................" @settings uri)
                  (rf/dispatch [:fetch-api-data uri widget-id])))
        handle-change (r/atom (fn [choice]
                                (reset! model choice)))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [register-event register-handler widget-id]} (r/props this)]
          (reset! handle-change (register-event {:key "on-change"
                                                 :fn @handle-change
                                                 :widget-id widget-id}))
          (register-handler {:key "set-data-args"
                             :fn set-data-args
                             :widget-id widget-id})
          (register-handler {:key "fetch"
                             :fn fetch
                             :widget-id widget-id})))
      :component-did-update
      (fn [this prev-props]
        (let [{:keys [settings widget-id]} (r/props this)
              prev-settings (:settings prev-props)
              widget-settings (get settings widget-id)
              prev-widget-settings (get prev-settings widget-id)]
          (println "dropdown-widget component-did-update" settings prev-settings)
          (when (not= widget-settings prev-widget-settings)
            (fetch))))
      :reagent-render
      (fn [{:keys [widget-id settings]}]
        (let [data (or @(rf/subscribe [:api-data widget-id]) [])
              choices (u/to-dropdown-data data)]
          [rc/single-dropdown
           :class "dropdown-widget"
           :choices choices
           :model model
           :on-change #(@handle-change %)]))})))

(defn button-widget [{:keys [widget-id]}]
  (let [handle-click (r/atom (fn []
                               (println "Hello from button-widget register-event!")))
        set-data-args (fn [args]
                        (rf/dispatch [:set-data-args widget-id args]))
        log (fn [args]
              (let [data-args @(rf/subscribe [:data-args])
                    widget-data-args (get data-args widget-id)]))]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (let [{:keys [register-event register-handler widget-id]} (r/props this)]
          (reset! handle-click (register-event {:key "on-click"
                                                :fn @handle-click
                                                :widget-id widget-id}))
          (register-handler {:key "set-data-args"
                             :fn set-data-args
                             :widget-id widget-id})
          (register-handler {:key "log"
                             :fn log
                             :widget-id widget-id})))
      :reagent-render
      (fn []
        [rc/button
         :class "button-widget btn-default"
         :label "Submit"
         :on-click #(@handle-click)])})))

(def widget-views
  {:container container-widget
   :dropdown dropdown-widget
   :button button-widget})

(defn save-settings-button [{:keys [on-click]}]
  [rc/button
   :class "save-settings-btn btn-default"
   :label "Save"
   :on-click #(on-click)])

(defn api-settings-field [{:keys [model on-change]}]
  [:div
   {:class "api-settings-field"}
   [:span "API"] [rc/input-text
                  :change-on-blur? false
                  :model model
                  :on-change #(on-change %)]])

(defn settings-modal []
  (r/with-let [widget-id (-> @(rf/subscribe [:context-menu])
                             :widget-id)
               draft-api (-> @(rf/subscribe [:api-settings widget-id])
                             r/atom)
               handle-close-settings-modal #(rf/dispatch [:set-show-settings-modal false])
               handle-change-draft-api #(reset! draft-api %)
               handle-save-settings (fn []
                                      (rf/dispatch [:set-api-settings widget-id @draft-api])
                                      (rf/dispatch [:set-show-settings-modal false]))]

    [rc/modal-panel
     :class "settings-modal"
     :backdrop-on-click handle-close-settings-modal
     :child [:div
             [api-settings-field
              {:model draft-api
               :on-change handle-change-draft-api}]
             [save-settings-button
              {:on-click handle-save-settings}]]]))

(defn context-menu []
  (r/with-let [handle-close-context-menu
               (fn [_]
                 (rf/dispatch [:set-show-context-menu false]))]

    (.addEventListener js/document "click" handle-close-context-menu)

    (let [{:keys [x y]} @(rf/subscribe [:context-menu])]
      [:div.context-menu
       {:style {:top y :left x}}
       [:div {:on-click #(rf/dispatch [:set-show-settings-modal true])} "Settings"]
       [:div {:on-click #()} "Remove"]])

    (finally
      (.removeEventListener js/document "click" handle-close-context-menu)
      (println "Context menu unmounted"))))

(defn save-actions-button [{:keys [on-click]}]
  [rc/button
   :class "save-actions-btn btn-default"
   :label "Save"
   :on-click #(on-click)])
(hash-set "wtf")
(defn events-or-handlers [{:keys [show action action-idx events-and-handlers on-select-event on-select-handlers]}]
  (prn "events-or-handlers" action)
  (let [x 0]
    [rc/v-box
     :children (cond
                 (= show :events) (mapv (fn [[wid {:keys [events]}]]
                                          [:div
                                           (str wid)
                                           (let [model (as-> (:event action) %
                                                         (:name %)
                                                         (hash-map :name % :wid wid)
                                                         (hash-set %))]
                                             [rc/selection-list
                                              :model model
                                              :multi-select? false
                                              :choices (mapv (fn [[name]]
                                                               {:label name :id {:name name :wid wid}}) events)
                                              :on-change (fn [event]
                                                           (on-select-event (first event) action-idx))])]) events-and-handlers)
                 (= show :handlers) (mapv (fn [[wid {:keys [handlers]}]]
                                            (let [model (->> (:handlers action)
                                                             (some #(when (= wid (first %)) (second %)))
                                                             set)]
                                              (prn "MODEEEL" model)
                                              [:div
                                               (str wid)
                                               [rc/selection-list
                                                :model model
                                                :choices (mapv (fn [[name]]
                                                                 {:label name :id name}) handlers)
                                                :on-change #(on-select-handlers (vec %) wid action-idx)]])) events-and-handlers)
                 :else [[:span ""]])]))

(defn actions-item [{:keys [action idx on-click-events on-click-handlers]}]
  [:div
   {:class "actions-item"}
   "Action " (inc idx)
   [:div
    {:on-click #(on-click-events idx)}
    "Wait Event"]
   [:div
    {:on-click #(on-click-handlers idx)}
    "Handler"]])

(defn add-action-button [{:keys [on-click]}]
  [rc/button
   :class "add-action-btn btn-default"
   :label "Add Action"
   :on-click #(on-click)])
(concat [0] [1 2])
(defn action-connector []
  (r/with-let [events-and-handlers @(rf/subscribe [:events-and-handlers])
               draft-actions (-> @(rf/subscribe [:actions])
                                 r/atom)
               curr-action-idx (r/atom nil)
               show (r/atom nil)
               handle-add-action #(swap! draft-actions conj {:event {}
                                                             :handlers {}})
               handle-click-events (fn [idx]
                                     (reset! curr-action-idx idx)
                                     (reset! show :events))
               handle-click-handlers (fn [idx]
                                       (reset! curr-action-idx idx)
                                       (reset! show :handlers))
               handle-select-event (fn [event idx]
                                     (prn "..........." event idx)
                                     (swap! draft-actions assoc-in [idx :event] event))
               handle-select-handlers (fn [handlers wid idx x]
                                        (prn "handler bre" handlers)
                                        (swap! draft-actions assoc-in [idx :handlers wid] handlers))
               events (map (fn [[widget-id {:keys [events]}]]
                             {widget-id events}) events-and-handlers)
               handlers (map (fn [[widget-id {:keys [handlers]}]]
                               {widget-id handlers}) events-and-handlers)]
    (prn "draft-actions" @draft-actions)
    (let [action-items (map-indexed (fn [idx action]
                                      [actions-item
                                       {:action action
                                        :idx idx
                                        :on-click-events handle-click-events
                                        :on-click-handlers handle-click-handlers}]) @draft-actions)]

      [rc/h-box
       :class "action-connector"
       :justify :between
       :children [[rc/v-box
                   :children (into [[add-action-button
                                     {:on-click handle-add-action}]]
                                   action-items)]
                  [rc/v-box
                   :children [[events-or-handlers
                               {:show @show
                                :action-idx @curr-action-idx
                                :action (get @draft-actions @curr-action-idx)
                                :events-and-handlers events-and-handlers
                                :on-select-event handle-select-event
                                :on-select-handlers handle-select-handlers}]
                              [rc/h-box
                               :children [[save-actions-button
                                           {:on-click #(rf/dispatch [:save-actions @draft-actions])}]]]]]]])))

(defn action-modal []
  [rc/modal-panel
   :class "action-modal"
   :backdrop-on-click #(rf/dispatch [:set-show-action-modal false])
   :child [action-connector]])

(defn add-widget-button [{:keys [on-click]}]
  [rc/button
   :class "add-widget-btn btn-default"
   :label "Add"
   :on-click #(on-click)])

(defn widget-list [{:keys [model choices on-change]}]
  [rc/selection-list
   :class "widget-list"
   :choices choices
   :model model
   :multi-select? false
   :on-change #(on-change %)])

(defn widget-modal []
  (let [current-parent-id @(rf/subscribe [:current-parent-id])
        selected-widget (r/atom #{})
        widget-choices (map (fn [[k _]]
                              {:id k
                               :label (name k)}) widget-views)
        handle-select-widget #(reset! selected-widget %)
        handle-add-widget #(do
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

(defn open-widget-modal-button [{:keys [parent-id]}]
  [rc/md-circle-icon-button
   :class "open-widget-modal-btn"
   :md-icon-name "zmdi-plus"
   :on-click #(do
                (rf/dispatch [:set-current-parent-id parent-id])
                (rf/dispatch [:set-show-widget-modal true]))])

(defn dash []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])
        widgets @(rf/subscribe [:widgets])
        register-event (fn [{:keys [widget-id key] :as event}]

                         (r/reaction)
                         (rf/dispatch [:reg-event event])
                         (fn [args]
                           (prn "OOOOOOOOOOOO")
                           (let [actions @(rf/subscribe [:actions])
                                 events-and-handlers @(rf/subscribe [:events-and-handlers])
                                 target (get-in events-and-handlers [widget-id :events key])
                                 action (some #(when (= widget-id (get-in % [:event :wid])) %) actions)]
                             ((:fn target))
                             (prn "......... aloooo" (:handlers action))
                             (doseq [[_ {:keys [handlers]}] (->> events-and-handlers
                                                                 (filter #(contains? (:handlers action) (first %))))]

                               (doseq [[_ handler] handlers]
                                 (prn "HANDLERRRR" handler)

                                 ((:fn handler)))))

;; (let [x
                           ;;       (filter
                           ;;        (fn [m]
                           ;;          (= (first (keys m)) widget-id))
                           ;;        actions)]
                           ;;   (prn "x" (get (first x) widget-id))
                           ;;   ((:fn (get (first x) widget-id)) args)
                           ;;   (let [[_ [_ bv] [_ cv]] (seq (first x))]
                           ;;     ((:fn bv) args)
                           ;;     ((:fn cv) args))
                           ))
        register-handler (fn [handler]
                           (rf/dispatch [:reg-handler handler]))
        class (str "dash " (when edit-mode? "edit-mode"))
        props {:register-event register-event
               :register-handler register-handler}
        widget-elements (mapv (fn [widget]
                                ^{:key (:id widget)}
                                (u/render-widget widget props widget-views)) (:children widgets))
        content (into widget-elements
                      (when edit-mode?
                        [[open-widget-modal-button {:parent-id 1}]]))]
    [rc/v-box
     :class class
     :children content]))

(defn toggle-mode-button []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])
        label (str "Mode: " (if edit-mode? "Edit" "View"))]
    [rc/button
     :class "toggle-mode-btn btn-default"
     :label label
     :on-click #(rf/dispatch [:toggle-edit-mode])]))

(defn open-action-modal-button []
  [rc/button
   :class "open-action-modal-btn btn-default"
   :label "Actions"
   :on-click #(rf/dispatch [:set-show-action-modal true])])

(defn dash-controls []
  (let [edit-mode? @(rf/subscribe [:edit-mode?])]
    [rc/h-box
     :class "dash-controls"
     :children [(when edit-mode? [open-action-modal-button])
                [toggle-mode-button]]]))

(defn title []
  [rc/title
   :class "title"
   :label "Dashboard"
   :level :level1])

(defn header []
  [rc/h-box
   :class "header"
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
