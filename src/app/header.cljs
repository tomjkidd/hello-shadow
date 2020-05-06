(ns app.header
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as string]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]
            ["@material-ui/core" :as mui]
            ["@material-ui/icons" :as mui-icons]
            ["@material-ui/core/styles" :refer [withStyles]]
            [goog.object :as go]))

mui/Switch
mui/FormControlLabel
mui/FormGroup

(defn custom-styles [^js/Mui.Theme theme]
  (clj->js
   {:root        {:flexGrow 1}
    :menu-button {:margin-right (.spacing theme 2)}
    :title       {:flexGrow 1}}))

(def with-custom-styles (withStyles custom-styles))

(defn header*
  "This is more a less a port of the [material-ui example][AppBar-example],
  adapted to fit using [reagent local state][reagent-local-state]

  [AppBar-example]:https://material-ui.com/components/app-bar/#app-bar-with-menu
  [reagent-local-state]:https://github.com/reagent-project/reagent-cookbook/blob/master/basics/component-level-state/README.md#component-level-state"
  []
  (let [local-state (atom {:anchor-el nil})]
    (fn [{:keys [classes]}]
      (let [{:keys [anchor-el]} @local-state
            handle-menu         (fn [event]
                                  (swap! local-state
                                         assoc
                                         :anchor-el
                                         (.-currentTarget event)))
            handle-close        (fn [event]
                                  (swap! local-state
                                         assoc
                                         :anchor-el
                                         nil))
            open?               (boolean anchor-el)]
        (js/console.warn classes)
        [:div
         {:class (.-root classes)}
         [:> mui/AppBar
          {:position "static"}
          [:> mui/Toolbar
           {}
           [:> mui/IconButton
            {:edge       "start"
             :color      "inherit"
             :aria-label "menu"
             :class (.-menuButton classes)}
            [:> mui-icons/Menu]]
           [:> mui/Typography
            {:class (.-title classes)
             :variant "h6"}
            "Header"]
           [:div
            [:> mui/IconButton
             {:aria-label    "account of current user"
              :aria-haspopup "true"
              :color         "inherit"
              :on-click      handle-menu}
             [:> mui-icons/AccountCircle]]
            [:> mui/Menu
             {:id              "menu-appbar"
              :anchorEl        anchor-el
              :anchorOrigin    (clj->js {:vertical   "top"
                                         :horizontal "right"})
              :keepMounted     true
              :transformOrigin (clj->js {:vertical   "top"
                                         :horizontal "right"})
              :open            open?
              :onClose         handle-close}
             [:> mui/MenuItem
              {:on-click #(do
                            (js/console.warn {:clicked :profile})
                            (handle-close %))}
              "Profile"]
             [:> mui/MenuItem
              {:on-click #(do
                            (js/console.warn {:clicked :account})
                            (handle-close %))}
              "Account"]
             [:> mui/MenuItem
              {:on-click #(do
                            (js/console.warn {:clicked :logout})
                            (handle-close %))}
              "Logout"]]]]]]))))

(defn header
  []
  [:> (with-custom-styles
        (reagent/reactify-component
         header*))])
