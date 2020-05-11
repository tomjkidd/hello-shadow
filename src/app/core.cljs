(ns app.core
  "This namespace contains your application and is the entrypoint for 'yarn start'."
  (:require [reagent.dom :as rdom]
            [app.hello :as hello]
            [app.login :as login]
            [app.header :as header]
            [app.profile :as profile]
            [re-frame.core :as rf]

            [app.graph :as graph]
            [com.fulcrologic.fulcro.application :as app]
            [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]

            [app.state.subs]   ;; to load subscriptions
            [app.state.events];; to load events
            [app.routes]
            ))

(defn ui-page
  [logged-in? page page-props]
  (into [:div]
        (if logged-in?
          (cons [header/header]
                (case page
                  :home    [[hello/hello page-props]]
                  :profile [[profile/profile page-props]]
                  nil))
          [[login/not-logged-in]])))

(defn ui-app
  []
  (let [logged-in? @(rf/subscribe [:logged-in?])
        page @(rf/subscribe [:page])
        page-query (rf/subscribe [:page-query])
        page-props @(rf/subscribe [:page-props] [page-query])]
    [ui-page logged-in? page page-props]))

(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (rdom/render [ui-app] (.getElementById js/document "app")))

(defn ^:export main
  "Run application startup logic."
  []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:initialize-auth])

  ;; Initialize fulcro
  (app/set-root! graph/fulcro-app graph/UiRoot {:initialize-state? true})
  (inspect/app-started! graph/fulcro-app)
  (render))
