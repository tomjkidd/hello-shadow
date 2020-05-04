(ns app.state.subs
  (:require [re-frame.core :refer [reg-sub]]))

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
