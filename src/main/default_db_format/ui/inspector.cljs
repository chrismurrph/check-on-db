(ns default-db-format.ui.inspector
  (:require [fulcro-css.css :as css]
            [fulcro.client.mutations :as mutations]
            [default-db-format.ui.domain :as ui]
            ;[fulcro.inspect.ui.data-history :as data-history]
            ;[fulcro.inspect.ui.data-viewer :as data-viewer]
            ;[fulcro.inspect.ui.data-watcher :as data-watcher]
            ;[fulcro.inspect.ui.element :as element]
            ;[fulcro.inspect.ui.network :as network]
            ;[fulcro.inspect.ui.transactions :as transactions]
            [fulcro.client.dom :as dom]
            [fulcro.client.primitives :as prim]))

(prim/defui ^:once Inspector
  static prim/InitialAppState
  (initial-state [_ state]
    {
     ::id           (random-uuid)
     ::tab          ::page-db
     ;::app-state    (-> (prim/get-initial-state data-history/DataHistory state)
     ;                   (assoc-in [::data-history/watcher ::data-watcher/root-data ::data-viewer/expanded]
     ;                     {[] true}))
     ;::element      (prim/get-initial-state element/Panel nil)
     ;::network      (prim/get-initial-state network/NetworkHistory nil)
     ;::transactions (prim/get-initial-state transactions/TransactionList [])
     })

  static prim/Ident
  (ident [_ props] [::id (::id props)])

  static prim/IQuery
  (query [_] [::tab ::id
              ;{::app-state (prim/get-query data-history/DataHistory)}
              ;{::element (prim/get-query element/Panel)}
              ;{::network (prim/get-query network/NetworkHistory)}
              ;{::transactions (prim/get-query transactions/TransactionList)}
              ])

  static css/CSS
  (local-rules [_] [[:.container {:display        "flex"
                                  :flex-direction "column"
                                  :width          "100%"
                                  :height         "100%"
                                  :overflow       "hidden"}]
                    [:.tabs {:font-family   ui/label-font-family
                             :font-size     ui/label-font-size
                             :display       "flex"
                             :background    "#f3f3f3"
                             :color         ui/color-text-normal
                             :border-bottom "1px solid #ccc"
                             :user-select   "none"}]
                    [:.tab {:cursor  "pointer"
                            :padding "6px 10px 5px"}
                     [:&:hover {:background "#e5e5e5"
                                :color      ui/color-text-strong}]
                     [:&.tab-selected {:border-bottom "2px solid #5c7ebb"
                                       :color         ui/color-text-strong
                                       :margin-bottom "-1px"}]
                     [:&.tab-disabled {:color  ui/color-text-faded
                                       :cursor "default"}
                      [:&:hover {:background "transparent"}]]]
                    [:.tab-content {:flex     "1"
                                    :overflow "auto"
                                    :display  "flex"}
                     [:&.spaced {:padding "10px"}]]])
  (include-children [_] [
                         #_data-history/DataHistory
                         #_network/NetworkHistory
                         #_transactions/TransactionList
                         #_element/Panel
                         ])

  Object
  (render [this]
    (let [{::keys   [app-state tab element network transactions]} (prim/props this)
          css      (css/get-classnames Inspector)
          tab-item (fn [{:keys [title html-title disabled? page]}]
                     (dom/div #js {:className (cond-> (:tab css)
                                                disabled? (str " " (:tab-disabled css))
                                                (= tab page) (str " " (:tab-selected css)))
                                   :title     html-title
                                   :onClick   #(if-not disabled?
                                                 (mutations/set-value! this ::tab page))}
                       title))]
      (dom/div #js {:className (:container css)}
        (dom/div #js {:className (:tabs css)}
          (tab-item {:title "DB" :page ::page-db})
          (tab-item {:title "Element" :page ::page-element})
          (tab-item {:title "Transactions" :page ::page-transactions})
          (tab-item {:title "Network" :page ::page-network})
          (tab-item {:title "OgE" :disabled? true}))

        #_(case tab
          ::page-db
          (dom/div #js {:className (:tab-content css)}
            (data-history/data-history app-state))

          ::page-element
          (dom/div #js {:className (:tab-content css)}
            (element/panel element))

          ::page-transactions
          (dom/div #js {:className (:tab-content css)}
            (transactions/transaction-list transactions))

          ::page-network
          (dom/div #js {:className (:tab-content css)}
            (network/network-history network))

          (dom/div #js {:className (:tab-content css)}
            "Invalid page " (pr-str tab)))))))

(def inspector (prim/factory Inspector))
