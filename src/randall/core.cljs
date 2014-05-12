(ns randall.core
  (:require
    [clojure.string :as string]
    [cljs.core.async :refer [put! chan <!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [figwheel.client :as fw :include-macros true]
    [randall.randomizer :as r]
    [sablono.core :as html :refer-macros [html]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(enable-console-print!)

(def app-state (atom {:improv nil
                      :triads nil
                      :fingerings nil
                      :fingering nil}))

(def panel-class "col-md-7 col-md-offset-2")

(defn numbers-str [z]
  (string/join " " (map str z)))

(defn triads-str [triads]
  (string/join " "
               (map (fn [[k q]]
                          (str (name k) (name q)))
                        triads)))

(defn make-view [title content-fn]
  (fn [data _owner]
    (reify
      om/IRender
      (render [_]
        (html
          [:div {:class panel-class}
           [:h3 title]
           (content-fn data)])))))

(def triads-view
  (make-view "Random Triads Voice Leading"
             (fn [{:keys [triads]}]
               [:p {:class "lead"} (triads-str triads)])))

(def fingering-view
  (make-view "Random Fingering"
             (fn [{:keys [fingering]} _owner]
               [:p {:class "lead"} (numbers-str fingering)])))

(defn fingerings-str [fs]
  (into [:ul {:class "list-unstyled"}]
          (mapv (fn [fingering] [:li {:class "lead"} (numbers-str fingering)])
                fs)))

(def fingerings-view
  (make-view "Random 6 String Fingerings"
             (fn [{:keys [fingerings]}]
               (fingerings-str fingerings))))

(defn formula-str [f]
  (string/join " " (map name f)))

(defn zone-str [z]
  (string/join " " (map str z)))

(def improv-view
  (make-view "Wayne Krantz Improv Formula"
             (fn [{:keys [improv]}]
               [:table {:class "table table-condensed"}
                [:tbody
                 [:tr
                  [:th "Key"]
                  [:td (name (get improv :key ""))]]
                 [:tr
                  [:th "Zone"]
                  [:td (-> improv :zone zone-str)]]
                 [:tr
                  [:th "Tempo"]
                  [:td (-> improv :tempo)]]
                 [:tr
                  [:th "Formula"]
                  [:td (-> improv :formula formula-str)]]]])))


(defn build-root [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:refresh (chan)})
    om/IWillMount
    (will-mount [_]

      (let [{:keys [refresh]} (om/get-state owner)]
        (put! refresh true)
        (go-loop [_ (<! refresh)]
                 (om/transact! app :improv (fn [_] (r/improv)))
                 (om/transact! app :triads (fn [_] (r/rand-triads 10)))
                 (om/transact! app :fingerings (fn [_] (:fingerings (r/rand-fingerings))))
                 (om/transact! app :fingering (fn [_] (r/rand-fingering)))
                 (recur (<! refresh)))))
    om/IRenderState
    (render-state [_ {:keys [refresh]}]
      (html
        [:div {:class "container"}
         [:div {:class "row"}
          [:div {:class panel-class}
           [:button {:type "button" :class "btn btn-link" :on-click #(put! refresh true)} "Refresh"]]]
         [:div {:class "row"}
          (om/build improv-view app)]
         [:div {:class "row"}
          (om/build triads-view app)]
         [:div {:class "row"}
          (om/build fingering-view app)]
         [:div {:class "row"}
          (om/build fingerings-view app)]]))))

(om/root build-root app-state
         {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
  ;; :websocket-url "ws:localhost:3449/figwheel-ws" default
  :jsload-callback (fn [] (print "reloaded")))              ;; optional callback