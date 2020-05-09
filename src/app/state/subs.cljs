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
    (let [{::graph/keys [token]} (graph/object-graph-query
                                  object-graph
                                  [::graph/token-id :access-token]
                                  [::graph/token])]
      token)))
