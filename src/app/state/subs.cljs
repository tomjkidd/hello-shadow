(ns app.state.subs
  (:require [re-frame.core :refer [reg-sub]]
            [app.graph :as graph]))

(reg-sub :auth-client
  (fn [{:keys [auth-client]} _]
    auth-client))

(reg-sub :auth-token
  (fn [{:keys [auth-token]} _]
    auth-token))

(reg-sub :logged-in?
  (fn [{:keys [auth-token]} _]
    (some? auth-token)))

(reg-sub :user
  (fn [{:keys [user]} _]
    user))

(reg-sub :access-token
  (fn [{:keys [object-graph]}]
    (let [{:token/keys [value]} (graph/object-graph-query
                                 object-graph
                                 [:token/id :access-token]
                                 [:token/value])]
      value)))

(reg-sub :page
  (fn [{:keys [page]}]
    page))

(reg-sub :page-query
  (fn [{:keys [page] :as _db}]
    ;; TODO: The app db should be all that is required to determine what needs to render.
    ;;       Right now, just the page is sufficient
    ;; Construct an EQL query that provides the data in the shape of the UI
    (case page
      :home
      [[:ui-login/id :logged-in-user] [{:ui-login/access-token [:token/value]}]]
      :profile
      [[:ui-login/id :logged-in-user] [{:ui-login/user [:user/id :user/name :user/email]}]]

      ;; else
      [])))

(reg-sub :page-props
  (fn object-graph-query-results [db _ [[ident eql]]]
    ;; Use the page-query eql to get the props for the page
    (let [result (graph/object-graph-query
                  db
                  ident
                  eql)]
      (js/console.warn {:query-results result})
      result)))
