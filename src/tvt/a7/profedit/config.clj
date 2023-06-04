(ns tvt.a7.profedit.config
  (:import com.github.weisj.darklaf.LafManager)
  (:import [javax.swing UIManager]
           [javax.swing.plaf FontUIResource])
  (:import [com.github.weisj.darklaf.theme
            SolarizedLightTheme
            SolarizedDarkTheme
            OneDarkTheme
            IntelliJTheme
            HighContrastLightTheme
            HighContrastDarkTheme])
  (:require [clojure.spec.alpha :as s]
            [tvt.a7.profedit.fio :as fio]
            [tvt.a7.profedit.asi :as asi]
            [tvt.a7.profedit.profile :as prof]
            [seesaw.core :as sc]
            [tvt.a7.profedit.config :as conf]))


(s/def ::color-theme #{:sol-light :sol-dark :dark :light :hi-light :hi-dark})


(s/def ::language #{:english})


(s/def ::config (s/keys :req-un [::color-theme ::language]))


(def default-config {:color-theme :sol-light :language :english})


(def ^:private *config (atom default-config))


(defn get-color-theme []
  (get @*config :color-theme (:color-theme default-config)))


(defn save-config! [filename]
  (let [config @*config]
    (if (s/valid? ::config config)
      (fio/write-config filename config)
      (do
        (asi/pop-report! (prof/val-explain ::config config))
        (prof/status-err! ::bad-config-save-err)))))


(defn load-config! [filename]
  (if-let [new-config (fio/read-config filename)]
    (if (s/valid? ::config new-config)
      (reset! *config new-config)
      (do (prof/status-err! ::bad-config-file-err)
          (asi/pop-report! (prof/val-explain ::config new-config))))
    (do (reset! *config default-config)
        (save-config! filename))))


(defn set-theme! [theme-key]
  (if (s/valid? ::color-theme theme-key)
    (do
      (swap! *config assoc :color-theme theme-key)
      (condp = theme-key
        :sol-light (LafManager/setTheme (new SolarizedLightTheme))
        :sol-dark (LafManager/setTheme (new SolarizedDarkTheme))
        :dark (LafManager/setTheme (new OneDarkTheme))
        :light (LafManager/setTheme (new IntelliJTheme))
        :hi-light (LafManager/setTheme (new HighContrastLightTheme))
        :hi-dark (LafManager/setTheme (new HighContrastDarkTheme))
        (LafManager/setTheme (new OneDarkTheme)))
      (LafManager/install)
      (save-config! (fio/get-config-file-path)))
    (do (asi/pop-report! (prof/val-explain ::color-theme theme-key))
        (prof/status-err! ::bad-theme-selection-err))))


(def font-fat (FontUIResource. "Verdana" java.awt.Font/BOLD 26))

(def font-big (FontUIResource. "Verdana" java.awt.Font/PLAIN 24))

(def font-big-bold (FontUIResource. "Verdana" java.awt.Font/BOLD 24))

(def font-small (FontUIResource. "Verdana" java.awt.Font/PLAIN 16))


(defn set-ui-font [f]
  (let [keys (enumeration-seq (.keys (UIManager/getDefaults)))]
    (doseq [key keys]
      (when (instance? FontUIResource (UIManager/get key))
        (UIManager/put key f)))))


(defn reset-theme!
  "Save as set-theme but makes sure that fonts are preserved"
  [theme-key event-source]
  (let [rv  (set-theme! theme-key)]
    (sc/invoke-later
     (doseq [fat-label (sc/select (sc/to-root event-source) [:.fat])]
       (sc/config! fat-label :font font-fat)))
    rv))
