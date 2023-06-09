(ns tvt.a7.profedit.update
  (:require [clj-http.client :as client]
            [tvt.a7.profedit.profile :as prof]
            [clojure.java.io :as io]
            [seesaw.core :as ssc]
            [tvt.a7.profedit.config :refer [update-conf]]
            [j18n.core :as j18n])
  (:import [java.io BufferedReader InputStreamReader]
           [java.lang ProcessBuilder String]))


(def ^:private github-api-url
  (:github-api-url update-conf))


(def ^:private version-resource-path
  (:version-resource-path update-conf))


(defn- get-latest-tag []
  (let [response (client/get github-api-url {:as :json})
        latest-tag (first (:body response))]
    (:name latest-tag)))


(defn- get-current-version []
  (let [version-resource (io/resource version-resource-path)]
    (when version-resource
      (with-open [reader (BufferedReader.
                           (InputStreamReader.
                             (.openStream version-resource)))]
        (.readLine reader)))))


(defn- update-app []
  (let [java-cmd "./runtime/bin/java"
        jar-path "update.jar"
        ^"[Ljava.lang.String;" cmd-array (into-array String [java-cmd "-jar" jar-path])]
    (-> cmd-array
        (java.lang.ProcessBuilder.)
        (.directory (io/file "."))
        (.start))))


(defn- ask-to-update []
  (ssc/confirm (j18n/resource ::ask-if-should-update)
               :title (j18n/resource ::software-update)
               :type :info
               :option-type :yes-no))


(defn check-for-update []
  (future
   (try
     (when-let [current-version (get-current-version)]
       (let [latest-version (get-latest-tag)]
         (when (not= latest-version current-version)
           (ssc/invoke-later
            (when (ask-to-update)
              (println "Update available, starting the update process.")
              (update-app)
              (println "Exiting the application to allow the update.")
              (System/exit 0))))))
     (catch Exception e (prof/status-err! (.getMessage e)) nil))))
