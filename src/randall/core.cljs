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

(def app-state (atom {:improv (r/improv)}))

(defn formula-str [f]
  (string/join " " (map name f)))

(defn zone-str [z]
  (string/join " " (map str z)))

(defn improv-view [{:keys [improv]} _owner]
  (reify
    om/IRender
    (render [_]
      (html
        [:div {:class "row"}
         [:div {:class "col-md-4 col-md-offset-3"}
          [:h3 "Wayne Krantz Improv Formula"]
          [:table {:class "table table-condensed"}
           [:tbody
            [:tr
             [:th "Key"]
             [:td (-> improv :key name)]]
            [:tr
             [:th "Zone"]
             [:td (-> improv :zone zone-str)]]
            [:tr
             [:th "Tempo"]
             [:td (-> improv :tempo)]]
            [:tr
             [:th "Formula"]
             [:td (-> improv :formula formula-str)]]]]]]))))

(defn build-root [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:refresh (chan)})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [refresh]} (om/get-state owner)]
        (go-loop [_ (<! refresh)]
                 (om/transact! app :improv (fn [_] (r/improv)))
                 (recur (<! refresh)))))
    om/IRenderState
    (render-state [_ {:keys [refresh]}]
      (html
        [:div {:class "container"}
         [:div (om/build improv-view app)]
         [:div {:class "row"}
          [:div {:class "col-md-4 col-md-offset-3"}
           [:button {:type "button" :class "btn btn-default" :on-click #(put! refresh true)} "New Formula"]]]]))))

(om/root build-root app-state
         {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
  ;; :websocket-url "ws:localhost:3449/figwheel-ws" default
  :jsload-callback (fn [] (print "reloaded")))              ;; optional callback