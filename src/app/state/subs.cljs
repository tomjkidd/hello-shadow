(ns app.state.subs
  (:require [re-frame.core :refer [reg-sub]]
            [app.graph :as graph]))

(reg-sub :logged-in?
  (fn [db]
    (-> db
        (graph/object-graph-query
         [:ui-login/id graph/ui-login-id]
         [{:ui-login/access-token [:token/value]}])
        :ui-login/access-token
        :token/value
        some?)))

(reg-sub :page
  (fn [{:keys [page]}]
    page))

(reg-sub :page-query
  (fn [{:keys [page-query]}]
    page-query))

(reg-sub :page-props
  (fn object-graph-query-results [db _ [[ident eql]]]
    ;; Use the page-query eql to get the props for the page
    (let [result (graph/object-graph-query
                  db
                  ident
                  eql)]
      (js/console.warn {:query-results result})
      result)))
