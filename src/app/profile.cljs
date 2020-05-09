(ns app.profile
  (:require ["@material-ui/core" :as mui]))

(defn profile
  [{:user/keys [name email] :as props}]
  [:> mui/Card
   {}
   [:> mui/CardContent
    {}
    [:> mui/Typography
     {}
     "User Information"]
    [:> mui/Typography
     {}
     (when name
       (str "User name: " name))
     (when email
       (str "User email: " email))]]])
