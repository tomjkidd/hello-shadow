(ns app.profile
  (:require ["@material-ui/core" :as mui]))

(defn profile
  [{:ui-login/keys [user]}]
  (let [{:user/keys [name email]} user]
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
         (str "User email: " email))]]]))
