(ns app.state.events
  (:require [app.state.db :as db]
            [re-frame.core :as rf]
            [app.login :as login]
            [app.graph :as graph]))

(rf/reg-event-db :initialize-db
  (fn [_ _]
    db/default-db))

(rf/reg-event-db :store-auth-client
  (fn [db [_ client]]
    (assoc db :auth-client client)))

(rf/reg-event-db :store-auth-token
  (fn [db [_ token]]
    (let [token-id :access-token]
      (-> db
          (graph/object-graph-merge ::graph/token
                                    {:token/id    token-id
                                     :token/value token})
          (graph/object-graph-merge ::graph/ui-login
                                    {:ui-login/id           graph/ui-login-id
                                     :ui-login/access-token [:token/id token-id]})))))

(rf/reg-event-db :store-user
  (fn [db [_ user]]
    (let [user-id (:sub user)]
      (-> db
          (graph/object-graph-merge ::graph/user
                                    {:user/id    user-id
                                     :user/name  (:name user)
                                     :user/email (:email user)})
          (graph/object-graph-merge ::graph/ui-login
                                    {:ui-login/id   graph/ui-login-id
                                     :ui-login/user [:user/id user-id]})))))

(rf/reg-event-fx :nav
  (fn [{:keys [db]} [_ nav-target :as nav-event]]
    ;; TODO: Add route/match information for page, and use as graph
    {:db          (-> db
                      (assoc :page nav-target)
                      (assoc :page-query (graph/get-page-query nav-target)))
     :request-nav nav-event}))

(rf/reg-fx :request-nav
  (fn [nav-target]
    ;; TODO: Integrate this call
    ;(df/load! SPA :all-accounts Account)
    (js/console.warn {:todo (str "Perform navigation to " nav-target)})))

(rf/reg-event-db :logout
  (fn [db [_]]
    (dissoc db :ui-login/id :user/id :token/id)))

(rf/reg-event-fx :initialize-auth
  (fn [{:keys [db]} [_]]
    {:request-auth-client nil}))

(rf/reg-event-fx :login
  (fn [{:keys [db]}]
    (let [{:keys [auth-client]} db]
      {:request-login auth-client})))

(rf/reg-event-fx :handle-auth-redirect
  (fn [{:keys [db]}]
    (let [{:keys [auth-client]} db]
      {:handle-auth-redirect auth-client})))

(rf/reg-event-fx :logout
  (fn [{:keys [db]}]
    (let [{:keys [auth-client]} db]
      {:request-logout auth-client})))

(rf/reg-event-fx :print-db
  (fn [{:keys [db]} [_]]
    {:print-db db}))

;; The auth-client is needed to issue requests to handle getting a token
(rf/reg-fx :request-auth-client
  (fn []
    (login/request-auth-client!)))

;; This represents the actual request to the auth-client to authenticate the user
(rf/reg-fx :request-login
  (fn [auth-client]
    (login/request-login! auth-client)))

;; This represents handling the response from the auth-client to authenticate the user
(rf/reg-fx :handle-auth-redirect
  (fn [auth-client]
    (login/handle-auth-redirect! auth-client)))

;; This represents handling the auth-client's notion of a user requested logout operation
(rf/reg-fx :request-logout
  (fn [auth-client]
    (login/request-logout! auth-client)))

(rf/reg-fx :print-db
  (fn [db]
    (js/console.warn db)))
