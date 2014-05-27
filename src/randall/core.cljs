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

(defn keywords-str [kws]
  (map name kws))

(defn numbers-str [z]
  (map str z))

(defn keyword-pairs-str [pairs]
  (map (fn [[k q]]
         (str (name k) (name q)))
       pairs))

(defn tuple-str [[x y]]
  (if (and x y)
    (str (name x) " " (name y))
    " "))

(defn formula-str [f]
  (string/join " " (map name f)))

(defn zone-str [z]
  (string/join " " (map str z)))

(defn make-descriptions-list
  [headers data]
  (into [:dl {:class "dl-horizontal"}]
        (interleave
             (map (fn [x] [:dt x]) headers)
             (map (fn [x] [:dd x]) data))))

(defn make-view [content-fn]
  (fn [data _owner]
    (reify
      om/IRenderState
      (render-state [_ {:keys [refresh exercise]}]
        (html
          [:div {:class (str "row " (if-not (:enabled data) "hide"))
                 :style {:margin-top "20px"}}
           [:div {:class "panel panel-default"}
            [:div {:class "panel-heading"}
             (:title data)
             [:button {:class    "btn btn-warning btn-xs" :style {:margin-left "20px"}
                       :on-click #(put! refresh exercise)}
              "refresh"]]
            [:div {:class "panel-body"}
             (content-fn (:values data))]]])))))

(defn horizontal-list [items]
  (into [:ul {:class "list-inline"}]
        (for [each items]
          [:li {:class "lead"} each])))

(def twelve-keys-view
  (make-view (fn [values]
               (horizontal-list (keywords-str values)))))

(def vamp-view
  (make-view (fn [values]
               (make-descriptions-list
                 ["String" "Key" "Mode" "Tempo"]
                 [(name (get values :string ""))
                  (name (get values :key ""))
                  (name (get values :mode ""))
                  (:tempo values)]))))

(def vamp-2-strings-view
  (make-view (fn [values]
               (make-descriptions-list
                 ["Strings" "Key" "Mode" "Tempo"]
                 [(tuple-str (get values :strings []))
                  (name (get values :key ""))
                  (name (get values :mode ""))
                  (:tempo values)]))))

(def triads-view
  (make-view (fn [values]
               (horizontal-list (keyword-pairs-str values)))))

(def fingering-view
  (make-view (fn [values]
               (horizontal-list [(numbers-str values)]))))

(def improv-view
  (make-view (fn [values]
               (make-descriptions-list
                 ["Key" "Zone" "Tempo" "Formula"]
                 [(name (get values :key ""))
                  (-> values :zone zone-str)
                  (-> values :tempo)
                  (-> values :formula formula-str)]))))

(defn generate-values [app exercise]
  (om/transact! app (fn [app]
                      (reduce (fn [result [k ex]]
                                (assoc result k
                                              (assoc ex :values ((:generator ex))
                                                        :enabled (= k exercise))))
                              app app))))

(def exercise-order [:twelve-keys
                     :triads
                     :sevenths
                     :improv
                     :vamp
                     :vamp-2-strings
                     :fingering])

(def app-state (atom {:improv         {:enabled   false
                                       :title     "Krantz Formula"
                                       :values    nil
                                       :view      improv-view
                                       :generator r/improv}
                      :triads         {:enabled   false
                                       :title     "Random Triads"
                                       :values    nil
                                       :view      triads-view
                                       :generator #(r/rand-triads 10)}
                      :sevenths       {:enabled   false
                                       :title     "Random 7th Chords"
                                       :values    nil
                                       :view      triads-view
                                       :generator #(r/rand-sevenths 10)}
                      :fingering      {:enabled   false
                                       :title     "Fingering"
                                       :values    nil
                                       :view      fingering-view
                                       :generator r/rand-fingering}
                      :vamp           {:enabled   false
                                       :title     "Goodrick One String Vamp"
                                       :values    nil
                                       :view      vamp-view
                                       :generator r/vamp}
                      :vamp-2-strings {:enabled   false
                                       :title     "Goodrick Two String Vamp"
                                       :values    nil
                                       :view      vamp-2-strings-view
                                       :generator r/vamp-2-strings}
                      :twelve-keys    {:enabled   false
                                       :title     "12 Keys"
                                       :values    nil
                                       :view      twelve-keys-view
                                       :generator r/rand-twelve-keys}}))

(defn exercises-button [app _owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [refresh]}]
      (html
        [:div {:class "dropdown"}
         [:a {:class "btn btn-info dropdown-toggle" :data-toggle "dropdown" :href "#"}
          "Exercises "
          [:span {:class "caret"}]]
         (into [:ul {:class "dropdown-menu"}
                [:li [:a {:href "#" :on-click #(put! refresh (rand-nth (keys @app)))} "Random Exercise"]]]
               (for [ex exercise-order]
                 [:li [:a {:href "#" :on-click #(put! refresh ex)} (-> app ex :title)]]))]))))

(defn build-root [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:refresh (chan)})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [refresh]} (om/get-state owner)]
        (put! refresh (rand-nth (keys app)))
        (go-loop [exercise (<! refresh)]
                 (generate-values app exercise)
                 (recur (<! refresh)))))
    om/IRenderState
    (render-state [_ {:keys [refresh]}]
      (html
        [:div {:class "container-fluid"}
         [:div {:class "row"}
          (into [:div {:class "main"}
                 [:div {:class "row"}
                  [:div
                   (om/build exercises-button app {:init-state {:refresh refresh}})]]]
                (for [exercise exercise-order]
                  (let [ex (exercise app)]
                    (om/build (:view ex) ex {:init-state {:refresh refresh
                                                          :exercise exercise}}))))]]))))

(om/root build-root app-state
         {:target (. js/document (getElementById "app"))})

(fw/watch-and-reload
  ;; :websocket-url "ws:localhost:3449/figwheel-ws" default
  :jsload-callback (fn [] (print "reloaded")))              ;; optional callback