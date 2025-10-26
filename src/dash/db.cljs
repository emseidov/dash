(ns dash.db)

(def default-db
  {:current-container-id -1
   :edit-mode? false
   :selected-widget #{}
   :show-config-modal? false
   :show-widget-modal? false
   :widgets {:name :root
             :id -1
             :parent-id -1
             :children []}})
