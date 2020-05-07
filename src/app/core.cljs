(ns app.core
  "This namespace contains your application and is the entrypoint for 'yarn start'."
  (:require [reagent.dom :as rdom]
            [app.hello :as hello]
            [app.login :as login]
            [app.header :as header]
            [re-frame.core :as rf]
            [app.state.subs];; to load subscriptions
            [app.state.events];; to load events
            ))

(defn app
  []
  (let [logged-in? @(rf/subscribe [:logged-in?])]
    (cond-> [:div]
      logged-in?       (into [[header/header]
                              [hello/hello]])
      (not logged-in?) (conj [login/not-logged-in]))))

(defn ^:dev/after-load render
  "Render the toplevel component for this app."
  []
  (rdom/render [app] (.getElementById js/document "app")))

(defn ^:export main
  "Run application startup logic."
  []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:initialize-auth])
  (render))
