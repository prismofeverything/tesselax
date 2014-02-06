(ns tesselax.effects
  (:require [cljs.core.async :refer [<! >! timeout chan close! put! alts! sliding-buffer]]
            [domina :as dom]
            [domina.css :as css]
            [domina.events :as events]
            [tesselax.container :as container])
  (:require-macros [cljs.core.async.macros :refer [go alt!]]))

(enable-console-print!)

(def mouse-events (chan))

(defn event-chan
  [c id el type data]
  (let [writer (fn [event] 
                 (events/prevent-default event)
                 (events/stop-propagation event)
                 (put! c [id event data]))]
    (events/listen! el type writer)
    {:chan c
     :unsubscribe #(.removeEventListener el type writer)}))

(defn mouse-at
  [event]
  [(:clientX event) (:clientY event)])

(defn subtract-points
  [[a b] [c d]]
  [(- a c) (- b d)])

(defn make-listener
  [layout!]
  (event-chan mouse-events :down js/document :mousedown {})
  (event-chan mouse-events :up js/document :mouseup {})
  (event-chan mouse-events :move js/document :mousemove {})
  (go
   (loop [state {:state :ready}]
     (let [[id event data] (<! mouse-events)
           point (mouse-at event)]
       (recur
        (case (:state state)
          :ready 
          (if (= id :down)
            (let [target (:target event)
                  {:keys [x y]} (container/dom-position target)]
              (dom/remove-class! target "animate-position")
              (dom/add-class! target "front")
              {:state :dragging 
               :point point
               :target target
               :relative [(- (first point) x) (- (last point) y)]})
            state)

          :dragging 
          (condp = id
            :move 
            (let [[x y] (subtract-points point (:relative state))]
              (container/update-position! 
               (:target state)
               {:x x :y y})
              (assoc state 
                :point point))
            :up 
            (let [target (:target state)]
              (dom/remove-class! target "front")
              (dom/add-class! target "animate-position")
              (dom/add-class! target "fixed")
              (layout!)
              (dom/remove-class! target "fixed")
              {:state :ready}))
          
          state))))))
