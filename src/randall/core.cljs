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
                      :fingering nil
                      :vamp nil
                      :vamp-2-strings nil
                      :twelve-keys nil}))


(defn keywords-str [kws]
  (map name kws))

(defn numbers-str [z]
  (map str z))

(defn keyword-pairs-str [pairs]
  (map (fn [[k q]]
         (str (name k) (name q)))
       pairs))

(defn make-table
  ([data]
   (make-table nil data))
  ([headers data]
   [:table {:class "table "}
    [:thead
     (into [:tr] (map (fn [h] [:th h]) headers))]
    (into [:tbody]
          (map (fn [row]
                 (into [:tr]
                       (map (fn [cell] [:td cell]) row)))
               data))]))

(defn make-view [title content-fn]
  (fn [data _owner]
    (reify
      om/IRender
      (render [_]
        (html
          [:div {:class "panel "}
           [:div {:class "panel-heading"} title]
           [:div {:class "panel-body"}
            (content-fn data)]])))))


(def twelve-keys-view
  (make-view "12 Keys"
             (fn [{:keys [twelve-keys]}]
               (make-table [(keywords-str twelve-keys)]))))

(def vamp-view
  (make-view "Goodrick Single String Vamp"
             (fn [{:keys [vamp]}]
               (make-table ["String" "Key" "Mode" "Tempo"]
                           [[(name (get vamp :string "")) (name (get vamp :key "")) (name (get vamp :mode "")) (:tempo vamp)]]))))

(defn tuple-str [[x y]]
  (if (and x y)
    (str (name x) " " (name y))
    " "))

(def vamp-2-strings-view
  (make-view "Goodrick Two String Vamp"
             (fn [{:keys [vamp-2-strings]}]
               (make-table ["Strings" "Key" "Mode" "Tempo"]
                           [[(tuple-str (get vamp-2-strings :strings []))
                             (name (get vamp-2-strings :key ""))
                             (name (get vamp-2-strings :mode ""))
                             (:tempo vamp-2-strings)]]))))

(def triads-view
  (make-view "Random Triads Voice Leading"
             (fn [{:keys [triads]}]
               (make-table [(keyword-pairs-str triads)]))))

(def fingering-view
  (make-view "Random Fingering"
             (fn [{:keys [fingering]} _owner]
               (make-table [[(numbers-str fingering)]]))))

(def fingerings-view
  (make-view "Random 6 String Fingerings"
             (fn [{:keys [fingerings]}]
               (make-table (map (comp vector numbers-str) fingerings)))))

(defn formula-str [f]
  (string/join " " (map name f)))

(defn zone-str [z]
  (string/join " " (map str z)))

(def improv-view
  (make-view "Wayne Krantz Improv Formula"
             (fn [{:keys [improv]}]
               (make-table ["Key" "Zone" "Tempo" "Formula"]
                           [[(name (get improv :key ""))
                             (-> improv :zone zone-str)
                             (-> improv :tempo)
                             (-> improv :formula formula-str)]]))))

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
                 (om/transact! app :vamp (fn [_] (r/vamp)))
                 (om/transact! app :vamp-2-strings (fn [_] (r/vamp-2-strings)))
                 (om/transact! app :twelve-keys (fn [_] (r/rand-twelve-keys)))
                 (recur (<! refresh)))))
    om/IRenderState
    (render-state [_ {:keys [refresh]}]
      (html
        [:div {:class "container-fluid"}
         [:div {:class "row"}
          [:div {:class "col-sm-3 col-md-2 sidebar"}
           [:div {:class "row"}
            [:div
             [:button {:type "button" :class "btn btn-link" :on-click #(put! refresh true)} "Refresh"]]]]
          [:div {:class "col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main"}
           [:div {:class "row"}
            (om/build improv-view app)]
           [:div {:class "row"}
            (om/build twelve-keys-view app)]
           [:div {:class "row"}
            (om/build vamp-view app)]
           [:div {:class "row"}
            (om/build vamp-2-strings-view app)]
           [:div {:class "row"}
            (om/build triads-view app)]
           [:div {:class "row"}
            (om/build fingering-view app)]
           [:div {:class "row"}
            (om/build fingerings-view app)]]]]))))

(om/root build-root app-state
         {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
  ;; :websocket-url "ws:localhost:3449/figwheel-ws" default
  :jsload-callback (fn [] (print "reloaded")))              ;; optional callback