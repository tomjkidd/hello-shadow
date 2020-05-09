(ns app.hello
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn click-counter [click-count]
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])

(defn print-db
  []
  [:div
   [:input {:type "button" :value "Print DB"
            :on-click #(rf/dispatch [:print-db])}]])

(defn display-token
  []
  (let [token @(rf/subscribe [:access-token])]
    [:input {:type "text" :value token}]))

(def click-count (r/atom 0))

(defn hello []
  [:<>
   [:p "Hello, hello-shadow is running!"]
   [:p "Here's an example of using a component with state:"]
   [click-counter click-count]
   [print-db]
   [display-token]])
