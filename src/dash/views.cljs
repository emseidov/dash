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
        label (if edit-mode? "View" "Edit")]
    [re-com/button
     :class "edit-btn"
     :label label
     :on-click #(dispatch [:toggle-edit-mode])]))

(defn header []
  [re-com/h-box
   :width "100%"
   :align :center
   :justify :between
   :children [[title] [edit-mode-btn]]])

(defn widget-list []
  (let [selected (reagent/atom #{})]
    (fn []
      [re-com/selection-list
       :multi-select? false
       :model @selected
       :choices [{:id 1 :label "v-box"}
                 {:id 2 :label "dropdown"}
                 {:id 3 :label "button"}
                 {:id 4 :label "table"}]
       :on-change #(reset! selected %)])))

(defn widget-modal [{:keys [backdrop-on-click]}]
  [re-com/modal-panel
   :backdrop-on-click #(backdrop-on-click)
   :child [widget-list]])

(defn add-btn []
  (let [show? (reagent/atom false)]
    (fn []
      [re-com/v-box
       :children [[re-com/md-circle-icon-button
                   :md-icon-name "zmdi-plus"
                   :on-click #(reset! show? true)]
                  (when @show?
                    [widget-modal
                     {:backdrop-on-click #(reset! show? false)}])]])))

(defn v-box []
  [re-com/v-box
   :class "v-box"
   :width "100%"
   :children []])

(defn main []
  (let [edit-mode? @(subscribe [:edit-mode?])
        class (str/join " " ["main" (when edit-mode? "edit-mode")])
        items (if edit-mode?
                [[add-btn]]
                [])]
    [re-com/v-box
     :class class
     :width "100%"
     :children items]))

(defn app []
  [re-com/v-box
   :class "dash"
   :children [[header] [main]]])
