(ns tvt.a7.profedit.frames
  (:require
   [tvt.a7.profedit.profile :as prof]
   [tvt.a7.profedit.distances :refer [make-dist-panel]]
   [tvt.a7.profedit.widgets :as w]
   [tvt.a7.profedit.actions :as a]
   [tvt.a7.profedit.ballistic :as ball]
   [tvt.a7.profedit.fio :as fio]
   [tvt.a7.profedit.config :as conf]
   [seesaw.core :as sc]
   [seesaw.forms :as sf]
   [j18n.core :as j18n]
   [seesaw.core :as ssc])
  (:gen-class))


(defn make-status-bar []
  (sc/vertical-panel
   :items
   [(sc/separator :orientation :horizontal)
    (w/status)]))


(defn pack-with-gap! [frame]
  (let [size (sc/config (sc/pack! frame) :size)
        height (. ^java.awt.Dimension size height)
        width (. ^java.awt.Dimension size width)]
    (sc/config! frame :size [(+ 0 width) :by (+ 0 height)])))


(defn make-menu-file [*state make-frame make-wizard-frame]
  (sc/menu
   :text ::frame-file-menu
   :icon (conf/key->icon :actions-group-menu)
   :items
   [(a/act-new! make-wizard-frame *state)
    (a/act-open! make-frame *state)
    (a/act-save! *state)
    (a/act-save-as! *state)
    (a/act-reload! make-frame *state)
    (a/act-import! make-frame *state)
    (a/act-export! *state)]))


(defn make-menu-themes [make-frame]
  (let [at! (fn [name key] (a/act-theme! make-frame name key))]
    (sc/menu
     :text ::frame-themes-menu
     :icon (conf/key->icon :actions-group-theme)
     :items
     [(at! ::action-theme-dark :dark)
      (at! ::action-theme-light :light)
      (at! ::action-theme-sol-dark :sol-dark)
      (at! ::action-theme-sol-light :sol-light)
      (at! ::action-theme-hi-dark :hi-dark)
      (at! ::action-theme-hi-light :hi-light)])))


(defn make-menu-languages [make-frame]
  (sc/menu
   :text ::frame-language-menu
   :icon (conf/key->icon :icon-languages)
   :items
   [(a/act-language-en! make-frame)
    (a/act-language-ua! make-frame)]))


(defn make-frame-wizard [*state content next-frame-cons]
  (let [frame-cons (partial make-frame-wizard *state content next-frame-cons)
        next-button (sc/button :text "Next"
                               :listen
                               [:action (fn [e]
                                          (sc/dispose! (sc/to-root e))
                                          (next-frame-cons))])
        frame (sc/frame
               :icon (conf/key->icon :icon-frame)
               :id :frame-main
               :on-close
               (if (System/getProperty "repl") :dispose :exit)
               :menubar
               (sc/menubar
                :items [(make-menu-themes frame-cons)
                        (make-menu-languages frame-cons)])
               :content (sc/border-panel
                         :vgap 30
                         :north next-button
                         :center content
                         :south (make-status-bar)))]
    (-> frame pack-with-gap! ssc/show!)))


(defn make-frame-main [*state wizard-cons content]
  (sc/frame
   :icon (conf/key->icon :icon-frame)
   :id :frame-main
   :on-close
   (if (System/getProperty "repl") :dispose :exit)
   :menubar
   (sc/menubar
    :items [(make-menu-file *state
                            (partial make-frame-main *state content)
                            wizard-cons)
            (make-menu-themes make-frame-main)
            (make-menu-languages make-frame-main)])
   :content content))