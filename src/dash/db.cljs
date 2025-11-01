(ns dash.db)

(def default-db
  {:actions []
   :api-data {}
   :context-menu nil
   :current-parent-id -1
   :data-args {}
   :events-and-handlers {}
   :edit-mode? false
   :settings {:api {}}
   :show-action-modal? false
   :show-context-menu? false
   :show-settings-modal? false
   :show-widget-modal? false
   :widgets {:name :root
             :id 1
             :parent-id nil
             :children []}})
