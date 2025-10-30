(ns dash.db)

(def default-db
  {:actions []
   :api-data {}
   :context-menu nil
   :data-args {}
   :events-and-handlers {}
   :current-container-id -1
   :edit-mode? false
   :selected-widget #{}
   :settings {}
   :show-actions-modal? false
   :show-context-menu? false
   :show-settings-modal? false
   :show-widget-modal? false
   :widgets {:name :root
             :id 1
             :parent-id nil
             :children []}})
