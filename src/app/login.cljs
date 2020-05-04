(ns app.login
  "Manage the concerns of logging a user into the application.

  TODO: Handle errors in any of the cases!
  TODO: May be useful to build a protocol, but for now just
  using Auth0 out-of-the-box."
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]
            ["@auth0/auth0-spa-js" :as auth0-spa-js]
            [goog.object :as go]))

;; Refer to shadow-cljs.edn [:builds :build-id :closure-defines]
;; for how to provide these as env vars so that they get compiled in!
(goog-define ^String DOMAIN "")
(goog-define ^String CLIENT_ID "")
(goog-define ^String AUDIENCE "")

(def domain DOMAIN)
(def client-id CLIENT_ID)
(def audience AUDIENCE)

(def auth-config {:domain domain :client_id client-id :audience audience})

(defn contains-auth-redirect?
  "This is the way the auth0 docs indicate to manage the concern for vanilla js

  https://auth0.com/docs/quickstart/spa/vanillajs"
  [window-location-search]
  (and
   (string/includes? window-location-search "code=")
   (string/includes? window-location-search "state=")))

(defn request-auth-client!
  "Manages the concern of storing the auth-client,
  which is used to manage the OIDC dance to ensure that a user
  is authenticated."
  []
  (cond-> (auth0-spa-js/createAuth0Client
           (clj->js auth-config))
    true
    (.then (fn [auth-client]
             (rf/dispatch-sync [:store-auth-client auth-client])
             auth-client))

    true
    (.then (fn [auth-client]
             (.isAuthenticated auth-client)))
    true
    (.then (fn [is-authenticated?]
             (cond
               (and (not is-authenticated?)
                    (contains-auth-redirect? js/window.location.search))
               (rf/dispatch [:handle-auth-redirect])

               (and (not is-authenticated?)
                    (not (contains-auth-redirect? js/window.location.search)))
               (rf/dispatch [:login])

               ;;TODO: Indicate unexpected state
               )))))

(defn request-login!
  "Manages the concern of entering the OIDC dance to authenticate a user"
  [auth-client]
  (.loginWithRedirect auth-client
                      (clj->js {:redirect_uri js/window.location.origin})))

(defn handle-auth-redirect!
  "Manages the concerns of storing auth-token and user, assumes that the
  application is in a state where the auth-client can manage the redirect
  portion of OIDC dance."
  [auth-client]
  (cond-> (.handleRedirectCallback auth-client)
    true
    (.then (fn [_]
             (.getTokenSilently auth-client)))
    true
    (.then (fn [token]
             (rf/dispatch [:store-auth-token token])))
    true
    (.then (fn [_]
             (cond-> (.isAuthenticated auth-client)
               true
               (.then #(when %
                         (js/console.log "handle-auth-redirect!: User is authenticated.")
                         (.getUser auth-client)))
               true
               (.then #(when %
                         (let [user (js->clj % :keywordize-keys true)]
                           (js/console.log
                            (str "handle-auth-redirect!: User:"
                                 \newline
                                 (with-out-str
                                   (pprint user))))
                           (rf/dispatch [:store-user user])))))
             ;; Remove the auth0-specific URL search-params
             (let [search-params (js/URLSearchParams. js/window.location.search)]
               (js/console.warn (.toString search-params))
               (doseq [k ["code" "state"]]
                 (.delete search-params k))
               (let [new-url    (js/window.URL. js/window.location.href)
                     new-search (.toString search-params)]
                 ;; NOTE: This is mutating new-url!
                 (go/set new-url "search" new-search)
                 (js/window.history.pushState (clj->js {})
                                              js/document.title
                                              new-url)))))))

(defn request-logout!
  "Manages the concern of logging out a user"
  [auth-client]
  (.logout auth-client
   (clj->js {:returnTo js/window.location.origin})))

(defn welcome
  []
  (let [user @(rf/subscribe [:user])]
    [:div
     [:div (str "Welcome, " (:name user))]
     [:input {:type     "button"
              :value    "Logout"
              :on-click #(rf/dispatch [:logout])}]]))

(defn not-logged-in
  []
  [:div "User is not logged in."])
