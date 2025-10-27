(ns dash.db)

(def default-db
  {:actions {}
   :events-and-handlers {}
   ;; {:button {:name
   ;;                                :id
   ;;                                :events {:on-click {}}
   ;;                                :handlers nil}}
   :current-container-id -1
   :edit-mode? false
   :selected-widget #{}
   :show-actions-modal? false
   :show-widget-modal? false
   :widgets {:name :root
             :id -1
             :parent-id -1
             :children []}})
             ;; :children [{:id 0
             ;;             :parent-id -1
             ;;             :name :container
             ;;             :children [{:id 1
             ;;                         :parent-id 0
             ;;                         :name :dropdown
             ;;                         :children []}
             ;;                        {:id 2
             ;;                         :parent-id 0
             ;;                         :name :button
             ;;                         :children []}]}]}})
